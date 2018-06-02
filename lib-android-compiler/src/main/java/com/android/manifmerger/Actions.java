/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.manifmerger;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.annotations.concurrency.Immutable;
import com.android.ide.common.xml.XmlPrettyPrinter;
import com.android.utils.ILogger;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.LineReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Contains all actions taken during a merging invocation.
 */
@Immutable
public class Actions {

    // TODO: i18n
    @VisibleForTesting
    static final String HEADER = "-- Merging decision tree log ---\n";

    // defines all the records for the merging tool activity, indexed by element name+key.
    // iterator should be ordered by the key insertion order.
    private final ImmutableMap<XmlNode.NodeKey, DecisionTreeRecord> mRecords;

    public Actions(ImmutableMap<XmlNode.NodeKey, DecisionTreeRecord> records) {
        mRecords = records;
    }

    /**
     * Returns a {@link ImmutableList} of {@link NodeRecord}s for the
     * passed xml {@link Element}
     * @return the node records for that element or an empty list if none exist.
     */
    @NonNull
    public ImmutableList<NodeRecord> getNodeRecords(Element element) {
        XmlNode.NodeKey nodeKey = XmlNode.NodeKey.fromXml(element);
        return mRecords.containsKey(nodeKey)
                ? mRecords.get(nodeKey).getNodeRecords()
                : ImmutableList.<NodeRecord>of();

    }

    /**
     * Returns a {@link ImmutableSet} of all the element's keys that have
     * at least one {@link NodeRecord}.
     */
    @NonNull
    public ImmutableSet<XmlNode.NodeKey> getNodeKeys() {
        return mRecords.keySet();
    }

    /**
     * Returns an {@link ImmutableList} of {@link NodeRecord} for the element identified with the
     * passed key.
     */
    @NonNull
    public ImmutableList<NodeRecord> getNodeRecords(XmlNode.NodeKey key) {
        return mRecords.containsKey(key)
                ? mRecords.get(key).getNodeRecords()
                : ImmutableList.<NodeRecord>of();
    }

    /**
     * Returns a {@link ImmutableList} of all attributes names that have at least one record for
     * the element identified with the passed key.
     */
    @NonNull
    public ImmutableList<XmlNode.NodeName> getRecordedAttributeNames(XmlNode.NodeKey nodeKey) {
        DecisionTreeRecord decisionTreeRecord = mRecords.get(nodeKey);
        if (decisionTreeRecord == null) {
            return ImmutableList.of();
        }
        return decisionTreeRecord.getAttributesRecords().keySet().asList();
    }

    /**
     * Returns the {@link ImmutableList} of {@link AttributeRecord} for
     * the attribute identified by attributeName of the element identified by elementKey.
     */
    @NonNull
    public ImmutableList<AttributeRecord> getAttributeRecords(XmlNode.NodeKey elementKey,
            XmlNode.NodeName attributeName) {

        DecisionTreeRecord decisionTreeRecord = mRecords.get(elementKey);
        if (decisionTreeRecord == null) {
            return ImmutableList.of();
        }
        return decisionTreeRecord.getAttributeRecords(attributeName);
    }

    /**
     * Initial dump of the merging tool actions, need to be refined and spec'ed out properly.
     * @param logger logger to log to at INFO level.
     */
    void log(ILogger logger) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(HEADER);
        for (Map.Entry<XmlNode.NodeKey, DecisionTreeRecord> record : mRecords.entrySet()) {
            stringBuilder.append(record.getKey()).append("\n");
            for (NodeRecord nodeRecord : record.getValue().getNodeRecords()) {
                nodeRecord.print(stringBuilder);
                stringBuilder.append('\n');
            }
            for (Map.Entry<XmlNode.NodeName, List<AttributeRecord>> attributeRecords :
                    record.getValue().mAttributeRecords.entrySet()) {
                stringBuilder.append('\t').append(attributeRecords.getKey()).append('\n');
                for (AttributeRecord attributeRecord : attributeRecords.getValue()) {
                    stringBuilder.append("\t\t");
                    attributeRecord.print(stringBuilder);
                    stringBuilder.append('\n');
                }

            }
        }
        logger.info(stringBuilder.toString());
    }

    /**
     * Defines all possible actions taken from the merging tool for an xml element or attribute.
     */
    enum ActionType {
        /**
         * The element was added into the resulting merged manifest.
         */
        ADDED,
        /**
         * The element was injected from the merger invocation parameters.
         */
        INJECTED,
        /**
         * The element was merged with another element into the resulting merged manifest.
         */
        MERGED,
        /**
         * The element was rejected.
         */
        REJECTED,
        /**
         * The implied element was added was added when importing a library that expected the
         * element to be present by default while targeted SDK requires its declaration.
         */
        IMPLIED,
    }

    /**
     * Defines an abstract record contain common metadata for elements and attributes actions.
     */
    public abstract static class Record {

        @NonNull protected final ActionType mActionType;
        @NonNull protected final ActionLocation mActionLocation;
        @NonNull protected final XmlNode.NodeKey mTargetId;
        @Nullable protected final String mReason;

        private Record(@NonNull ActionType actionType,
                @NonNull ActionLocation actionLocation,
                @NonNull XmlNode.NodeKey targetId,
                @Nullable String reason) {
            mActionType = Preconditions.checkNotNull(actionType);
            mActionLocation = Preconditions.checkNotNull(actionLocation);
            mTargetId = Preconditions.checkNotNull(targetId);
            mReason = reason;
        }

        private Record(@NonNull Element xml) {
            mActionType = ActionType.valueOf(xml.getAttribute("action-type"));
            mActionLocation = new ActionLocation(getFirstChildElement(xml));
            mTargetId = new XmlNode.NodeKey(xml.getAttribute("target-id"));
            String reason = xml.getAttribute("reason");
            mReason = Strings.isNullOrEmpty(reason) ? null : reason;
        }

        public ActionType getActionType() {
            return mActionType;
        }

        public ActionLocation getActionLocation() {
            return mActionLocation;
        }

        public XmlNode.NodeKey getTargetId() {
            return mTargetId;
        }

        public void print(StringBuilder stringBuilder) {
            stringBuilder.append(mActionType)
                    .append(" from ")
                    .append(mActionLocation);
            if (mReason != null) {
                stringBuilder.append(" reason: ")
                        .append(mReason);
            }
        }

        public Element toXml(Document document) {
            Element record = document.createElement("record");
            record.setAttribute("action-type", mActionType.toString());
            record.setAttribute("target-id", mTargetId.toString());
            if (mReason != null) {
                record.setAttribute("reason", mReason);
            }
            addAttributes(record);
            Element location = document.createElement("location");
            record.appendChild(mActionLocation.toXml(location));
            record.appendChild(location);
            return record;
        }

        protected abstract void addAttributes(Element element);
    }

    /**
     * Defines a merging tool action for an xml element.
     */
    public static class NodeRecord extends Record {

        private final NodeOperationType mNodeOperationType;

        NodeRecord(@NonNull ActionType actionType,
                @NonNull ActionLocation actionLocation,
                @NonNull XmlNode.NodeKey targetId,
                @Nullable String reason,
                @NonNull NodeOperationType nodeOperationType) {
            super(actionType, actionLocation, targetId, reason);
            this.mNodeOperationType = Preconditions.checkNotNull(nodeOperationType);
        }

        NodeRecord(@NonNull Element xml) {
            super(xml);
            mNodeOperationType = NodeOperationType.valueOf(xml.getAttribute("opType"));
        }

        @Override
        protected void addAttributes(Element element) {
            element.setAttribute("opType", mNodeOperationType.toString());
        }

        @Override
        public String toString() {
            return "Id=" + mTargetId.toString() + " actionType=" + getActionType()
                    + " location=" + getActionLocation()
                    + " opType=" + mNodeOperationType;
        }
    }

    /**
     * Defines a merging tool action for an xml attribute
     */
    public static class AttributeRecord extends Record {

        // first in wins which should be fine, the first
        // operation type will be the highest priority one
        private final AttributeOperationType mOperationType;

        AttributeRecord(
                @NonNull ActionType actionType,
                @NonNull ActionLocation actionLocation,
                @NonNull XmlNode.NodeKey targetId,
                @Nullable String reason,
                @Nullable AttributeOperationType operationType) {
            super(actionType, actionLocation, targetId, reason);
            this.mOperationType = operationType;
        }

        AttributeRecord(@NonNull Element xml) {
            super(xml);
            mOperationType = AttributeOperationType.valueOf(xml.getAttribute("opType"));
        }

        @Nullable
        public AttributeOperationType getOperationType() {
            return mOperationType;
        }

        @Override
        protected void addAttributes(Element element) {
            if (mOperationType != null) {
                element.setAttribute("opType", mOperationType.toString());
            }
        }

        @Override
        public String toString() {
            return "Id=" + mTargetId + " actionType=" + getActionType()
                    + " location=" + getActionLocation()
                    + " opType=" + getOperationType();
        }
    }

    /**
     * Defines an action location which is composed of a pointer to the source location (e.g. a
     * file) and a position within that source location.
     */
    public static final class ActionLocation {
        private final XmlLoader.SourceLocation mSourceLocation;
        private final PositionXmlParser.Position mPosition;

        public ActionLocation(@NonNull XmlLoader.SourceLocation sourceLocation,
                @NonNull PositionXmlParser.Position position) {
            mSourceLocation = Preconditions.checkNotNull(sourceLocation);
            mPosition = Preconditions.checkNotNull(position);
        }

        ActionLocation(Element xml) {
            final Element location = getFirstChildElement(xml);
            mSourceLocation = XmlLoader.locationFromXml(location);
            mPosition = PositionImpl.fromXml(getNextSiblingElement(location));
        }

        public PositionXmlParser.Position getPosition() {
            return mPosition;
        }

        public XmlLoader.SourceLocation getSourceLocation() {
            return mSourceLocation;
        }

        @Override
        public String toString() {
            return mSourceLocation.print(true)
                    + ":" + mPosition.getLine() + ":" + mPosition.getColumn();
        }

        public Node toXml(Element location) {
            location.appendChild(mSourceLocation.toXml(location.getOwnerDocument()));
            location.appendChild(PositionImpl.toXml(mPosition, location.getOwnerDocument()));
            return location;
        }
    }

    public String persist()
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
        Element rootElement = document.createElement("manifest-merger-mappings");
        document.appendChild(rootElement);

        for (Map.Entry<XmlNode.NodeKey, DecisionTreeRecord> decisionTreeRecordEntry :
                mRecords.entrySet()) {

            Element elementActions = document.createElement("element-actions");
            elementActions.setAttribute("id", decisionTreeRecordEntry.getKey().toString());
            decisionTreeRecordEntry.getValue().toXml(elementActions);
            rootElement.appendChild(elementActions);
        }

        return XmlPrettyPrinter.prettyPrint(document, false);
    }

    @Nullable
    public static Actions load(InputStream inputStream)
            throws IOException, SAXException, ParserConfigurationException {

        return load(new PositionXmlParser().parse(inputStream));
    }

    @Nullable
    public static Actions load(String xml)
            throws IOException, SAXException, ParserConfigurationException {

        return load(new PositionXmlParser().parse(xml));
    }

    @Nullable
    private static Actions load(Document document) throws IOException {
        if (document == null) return null;

        Element rootElement = document.getDocumentElement();
        if (!rootElement.getNodeName().equals("manifest-merger-mappings")) {
            throw new IOException("File is not a manifest-merger-mappings");
        }
        ImmutableMap.Builder<XmlNode.NodeKey, DecisionTreeRecord> records = ImmutableMap.builder();
        NodeList elementActions = rootElement.getChildNodes();
        for (int i = 0; i < elementActions.getLength(); i++) {
            if (elementActions.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
            Element elementAction = (Element) elementActions.item(i);
            XmlNode.NodeKey key = new XmlNode.NodeKey(elementAction.getAttribute("id"));
            DecisionTreeRecord decisionTreeRecord = new DecisionTreeRecord(elementAction);
            records.put(key, decisionTreeRecord);
        }
        return new Actions(records.build());
    }

    private static Element getFirstChildElement(Element element) {
        Node child = element.getFirstChild();
        while(child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }
        return (Element) child;
    }

    private static Element getNextSiblingElement(Element element) {
        Node sibling = element.getNextSibling();
        while(sibling != null && sibling.getNodeType() != Node.ELEMENT_NODE) {
            sibling = sibling.getNextSibling();
        }
        return (Element) sibling;
    }

    public ImmutableMultimap<Integer, Record> getResultingSourceMapping(XmlDocument xmlDocument)
            throws ParserConfigurationException, SAXException, IOException {

        XmlLoader.SourceLocation inMemory = XmlLoader.UNKNOWN;

        XmlDocument loadedWithLineNumbers = XmlLoader.load(
                xmlDocument.getSelectors(), inMemory, xmlDocument.prettyPrint());
        ImmutableMultimap.Builder<Integer, Record> mappingBuilder = ImmutableMultimap.builder();
        for (XmlElement xmlElement : loadedWithLineNumbers.getRootNode().getMergeableElements()) {
            parse(xmlElement, mappingBuilder);
        }
        return mappingBuilder.build();
    }

    private void parse(XmlElement element,
            ImmutableMultimap.Builder<Integer, Record> mappings) {
        DecisionTreeRecord decisionTreeRecord = mRecords.get(element.getId());
        if (decisionTreeRecord != null) {
            NodeRecord nodeRecord = findNodeRecord(decisionTreeRecord);
            if (nodeRecord != null) {
                mappings.put(element.getPosition().getLine(), nodeRecord);
            }
            for (XmlAttribute xmlAttribute : element.getAttributes()) {
                AttributeRecord attributeRecord = findAttributeRecord(decisionTreeRecord,
                        xmlAttribute);
                if (attributeRecord != null) {
                    mappings.put(xmlAttribute.getPosition().getLine(), attributeRecord);
                }
            }
        }
        for (XmlElement xmlElement : element.getMergeableElements()) {
            parse(xmlElement, mappings);
        }
    }

    public String blame(XmlDocument xmlDocument)
            throws IOException, SAXException, ParserConfigurationException {

        ImmutableMultimap<Integer, Record> resultingSourceMapping =
                getResultingSourceMapping(xmlDocument);
        LineReader lineReader = new LineReader(
                new StringReader(xmlDocument.prettyPrint()));

        StringBuilder actualMappings = new StringBuilder();
        String line;
        int count = 1;
        while ((line = lineReader.readLine()) != null) {
            actualMappings.append(count).append(line).append("\n");
            if (resultingSourceMapping.containsKey(count)) {
                for (Record record : resultingSourceMapping.get(count)) {
                    actualMappings.append(count).append("-->")
                            .append(record.getActionLocation().toString())
                            .append("\n");
                }
            }
            count++;
        }
        return actualMappings.toString();
    }

    @Nullable
    private static NodeRecord findNodeRecord(DecisionTreeRecord decisionTreeRecord) {
        for (NodeRecord nodeRecord : decisionTreeRecord.getNodeRecords()) {
            if (nodeRecord.getActionType() == ActionType.ADDED) {
                return nodeRecord;
            }
        }
        return null;
    }

    @Nullable
    private static AttributeRecord findAttributeRecord(
            DecisionTreeRecord decisionTreeRecord,
            XmlAttribute xmlAttribute) {
        for (AttributeRecord attributeRecord : decisionTreeRecord
                .getAttributeRecords(xmlAttribute.getName())) {
            if (attributeRecord.getActionType() == ActionType.ADDED) {
                return attributeRecord;
            }
        }
        return null;
    }

    /**
     * Internal structure on how {@link com.android.manifmerger.Actions.Record}s are kept for an
     * xml element.
     *
     * Each xml element should have an associated DecisionTreeRecord which keeps a list of
     * {@link com.android.manifmerger.Actions.NodeRecord} for all the node actions related
     * to this xml element.
     *
     * It will also contain a map indexed by attribute name on all the attribute actions related
     * to that particular attribute within the xml element.
     *
     */
    static class DecisionTreeRecord {
        // all other occurrences of the nodes decisions, in order of decisions.
        private final List<NodeRecord> mNodeRecords = new ArrayList<NodeRecord>();

        // all attributes decisions indexed by attribute name.
        final Map<XmlNode.NodeName, List<AttributeRecord>> mAttributeRecords =
                new HashMap<XmlNode.NodeName, List<AttributeRecord>>();

        ImmutableList<NodeRecord> getNodeRecords() {
            return ImmutableList.copyOf(mNodeRecords);
        }

        ImmutableMap<XmlNode.NodeName, List<AttributeRecord>> getAttributesRecords() {
            return ImmutableMap.copyOf(mAttributeRecords);
        }

        DecisionTreeRecord() {
        }

        DecisionTreeRecord(Element elementAction) {
            Preconditions.checkArgument(elementAction.getNodeName().equals("element-actions"));
            NodeList childNodes = elementAction.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeName().equals("node-records")) {
                    NodeList nodeRecords = child.getChildNodes();
                    for (int j = 0; j < nodeRecords.getLength(); j++) {
                        if (nodeRecords.item(j).getNodeType() != Node.ELEMENT_NODE) continue;
                        NodeRecord nodeRecord = new NodeRecord((Element) nodeRecords.item(j));
                        mNodeRecords.add(nodeRecord);
                    }
                } else if (child.getNodeName().equals("attribute-records")) {
                    // id, record*
                    Element id = getFirstChildElement((Element) child);
                    XmlNode.NodeName nodeName = Strings.isNullOrEmpty(id.getAttribute("name"))
                            ? XmlNode.fromNSName(
                                    id.getAttribute("namespace-uri"),
                                    id.getAttribute("prefix"),
                                    id.getAttribute("local-name"))
                            : XmlNode.fromXmlName(id.getAttribute("name"));
                    Element record = id;
                    ImmutableList.Builder<AttributeRecord> attributeRecords =
                            ImmutableList.builder();
                    while ((record = getNextSiblingElement(record)) != null) {
                        AttributeRecord attributeRecord = new AttributeRecord(record);
                        attributeRecords.add(attributeRecord);
                    }
                    mAttributeRecords.put(nodeName, attributeRecords.build());
                }
            }
        }

        void addNodeRecord(NodeRecord nodeRecord) {
            mNodeRecords.add(nodeRecord);
        }

        ImmutableList<AttributeRecord> getAttributeRecords(XmlNode.NodeName attributeName) {
            List<AttributeRecord> attributeRecords = mAttributeRecords.get(attributeName);
            return attributeRecords == null
                    ? ImmutableList.<AttributeRecord>of()
                    : ImmutableList.copyOf(attributeRecords);
        }

        public void toXml(Element elementAction) {
            Document document = elementAction.getOwnerDocument();
            Element nodeRecords = document.createElement("node-records");
            elementAction.appendChild(nodeRecords);
            for (NodeRecord nodeRecord : mNodeRecords) {
                Element xmlNode = nodeRecord.toXml(document);
                nodeRecords.appendChild(xmlNode);
            }
            for (Map.Entry<XmlNode.NodeName, List<AttributeRecord>> nodeNameListEntry :
                    mAttributeRecords.entrySet()) {
                Element attributeRecords = document.createElement("attribute-records");
                elementAction.appendChild(attributeRecords);
                Element id = document.createElement("id");
                nodeNameListEntry.getKey().persistTo(id);
                attributeRecords.appendChild(id);

                for (AttributeRecord attributeRecord : nodeNameListEntry.getValue()) {
                    Element xmlAttributeRecord = attributeRecord.toXml(document);
                    attributeRecords.appendChild(xmlAttributeRecord);
                }
            }
        }
    }
}
