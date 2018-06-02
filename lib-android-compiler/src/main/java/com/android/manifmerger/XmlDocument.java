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

import static com.android.manifmerger.ManifestModel.NodeTypes.USES_PERMISSION;
import static com.android.manifmerger.ManifestModel.NodeTypes.USES_SDK;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.xml.XmlFormatPreferences;
import com.android.ide.common.xml.XmlFormatStyle;
import com.android.ide.common.xml.XmlPrettyPrinter;
import com.android.utils.Pair;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a loaded xml document.
 *
 * Has pointers to the root {@link XmlElement} element and provides services to persist the document
 * to an external format. Also provides abilities to be merged with other
 * {@link com.android.manifmerger.XmlDocument} as well as access to the line numbers for all
 * document's xml elements and attributes.
 *
 */
public class XmlDocument {

    private static final int DEFAULT_SDK_VERSION = 1;

    private final Element mRootElement;
    // this is initialized lazily to avoid un-necessary early parsing.
    private final AtomicReference<XmlElement> mRootNode = new AtomicReference<XmlElement>(null);
    private final PositionXmlParser mPositionXmlParser;
    private final XmlLoader.SourceLocation mSourceLocation;
    private final KeyResolver<String> mSelectors;

    public XmlDocument(@NonNull PositionXmlParser positionXmlParser,
            @NonNull XmlLoader.SourceLocation sourceLocation,
            @NonNull KeyResolver<String> selectors,
            @NonNull Element element) {
        this.mPositionXmlParser = Preconditions.checkNotNull(positionXmlParser);
        this.mSourceLocation = Preconditions.checkNotNull(sourceLocation);
        this.mRootElement = Preconditions.checkNotNull(element);
        this.mSelectors = Preconditions.checkNotNull(selectors);
    }

    /**
     * Returns a pretty string representation of this document.
     */
    public String prettyPrint() {
        return XmlPrettyPrinter.prettyPrint(
                getXml(),
                XmlFormatPreferences.defaults(),
                XmlFormatStyle.get(getRootNode().getXml()),
                null, /* endOfLineSeparator */
                false /* endWithNewLine */);
    }

    /**
     * merge this higher priority document with a higher priority document.
     * @param lowerPriorityDocument the lower priority document to merge in.
     * @param mergingReportBuilder the merging report to record errors and actions.
     * @return a new merged {@link com.android.manifmerger.XmlDocument} or
     * {@link Optional#absent()} if there were errors during the merging activities.
     */
    public Optional<XmlDocument> merge(
            XmlDocument lowerPriorityDocument,
            MergingReport.Builder mergingReportBuilder) {

        mergingReportBuilder.getActionRecorder().recordDefaultNodeAction(getRootNode());

        getRootNode().mergeWithLowerPriorityNode(
                lowerPriorityDocument.getRootNode(), mergingReportBuilder);

        addImplicitElements(lowerPriorityDocument, mergingReportBuilder);

        // force re-parsing as new nodes may have appeared.
        return mergingReportBuilder.hasErrors()
                ? Optional.<XmlDocument>absent()
                : Optional.of(reparse());
    }

    /**
     * Forces a re-parsing of the document
     * @return a new {@link com.android.manifmerger.XmlDocument} with up to date information.
     */
    public XmlDocument reparse() {
        return new XmlDocument(mPositionXmlParser, mSourceLocation, mSelectors, mRootElement);
    }

    /**
     * Returns a {@link com.android.manifmerger.KeyResolver} capable of resolving all selectors
     * types
     */
    public KeyResolver<String> getSelectors() {
        return mSelectors;
    }

    /**
     * Compares this document to another {@link com.android.manifmerger.XmlDocument} ignoring all
     * attributes belonging to the {@link SdkConstants#TOOLS_URI} namespace.
     *
     * @param other the other document to compare against.
     * @return  a {@link String} describing the differences between the two XML elements or
     * {@link Optional#absent()} if they are equals.
     */
    public Optional<String> compareTo(XmlDocument other) {
        return getRootNode().compareTo(other.getRootNode());
    }

    /**
     * Returns a {@link org.w3c.dom.Node} position automatically offsetting the line and number
     * columns by one (for PositionXmlParser, document starts at line 0, however for the common
     * understanding, document should start at line 1).
     */
    PositionXmlParser.Position getNodePosition(XmlNode node) {
        final PositionXmlParser.Position position =  mPositionXmlParser.getPosition(node.getXml());
        if (position == null) {
            return null;
        }
        return new PositionXmlParser.Position() {
            @Nullable
            @Override
            public PositionXmlParser.Position getEnd() {
                return position.getEnd();
            }

            @Override
            public void setEnd(@NonNull PositionXmlParser.Position end) {
                position.setEnd(end);
            }

            @Override
            public int getLine() {
                return position.getLine() + 1;
            }

            @Override
            public int getOffset() {
                return position.getOffset();
            }

            @Override
            public int getColumn() {
                return position.getColumn() +1;
            }
        };
    }

    public XmlLoader.SourceLocation getSourceLocation() {
        return mSourceLocation;
    }

    public synchronized XmlElement getRootNode() {
        if (mRootNode.get() == null) {
            this.mRootNode.set(new XmlElement(mRootElement, this));
        }
        return mRootNode.get();
    }

    public Optional<XmlElement> getByTypeAndKey(
            ManifestModel.NodeTypes type,
            @Nullable String keyValue) {

        return getRootNode().getNodeByTypeAndKey(type, keyValue);
    }

    public String getPackageName() {
        // TODO: allow injection through invocation parameters.
        return mRootElement.getAttribute("package");
    }

    public Document getXml() {
        return mRootElement.getOwnerDocument();
    }

    public int getMinSdkVersion() {
        Optional<XmlElement> usesSdk = getByTypeAndKey(
                ManifestModel.NodeTypes.USES_SDK, null);
        if (!usesSdk.isPresent()) {
            return DEFAULT_SDK_VERSION;
        }

        Optional<XmlAttribute> minSdkVersion = usesSdk.get()
                .getAttribute(XmlNode.fromXmlName("android:minSdkVersion"));
        return minSdkVersion.isPresent()
                ? Integer.parseInt(minSdkVersion.get().getValue())
                : DEFAULT_SDK_VERSION;
    }

    public int getTargetSdkVersion() {

        Optional<XmlElement> usesSdk = getByTypeAndKey(
                ManifestModel.NodeTypes.USES_SDK, null);
        if (!usesSdk.isPresent()) {
            return DEFAULT_SDK_VERSION;
        }

        Optional<XmlAttribute> targetSdkVersion = usesSdk.get()
                .getAttribute(XmlNode.fromXmlName("android:targetSdkVersion"));
        if (targetSdkVersion.isPresent()) {
            return Integer.parseInt(targetSdkVersion.get().getValue());
        }
        return getMinSdkVersion();
    }

    /**
     * Add all implicit elements from the passed lower priority document that are
     * required in the target SDK.
     */
    @SuppressWarnings("unchecked") // compiler confused about varargs and generics.
    public void addImplicitElements(XmlDocument lowerPriorityDocument,
            MergingReport.Builder mergingReport) {

        int thisTargetSdk = getTargetSdkVersion();
        int thisMinSdk = getMinSdkVersion();
        int libraryTargetSdk = lowerPriorityDocument.getTargetSdkVersion();
        int libraryMinSdk = lowerPriorityDocument.getMinSdkVersion();

        if (!checkUsesSdkMinVersion(thisMinSdk, libraryMinSdk)) {
            mergingReport.addMessage(getSourceLocation(), 0, 0, MergingReport.Record.Severity.ERROR,
                    String.format(
                            "uses-sdk:minSdkVersion %1$s cannot be smaller than version "
                                    + "%2$s declared in library %3$s",
                            thisMinSdk,
                            libraryMinSdk,
                            lowerPriorityDocument.getSourceLocation().print(true)
                    )
            );
            return;
        }

        // if the merged document target SDK is equal or smaller than the library's, nothing to do.
        if (thisTargetSdk <= libraryTargetSdk) {
            return;
        }

        // There is no need to add any implied permissions when targeting an old runtime.
        if (thisTargetSdk < 4) {
            return;
        }

        boolean hasWriteToExternalStoragePermission =
                getByTypeAndKey(USES_PERMISSION, permission("WRITE_EXTERNAL_STORAGE")).isPresent();

        if (libraryTargetSdk < 4) {
            Optional<Element> permission = addIfAbsent(mergingReport.getActionRecorder(),
                    USES_PERMISSION,
                    permission("WRITE_EXTERNAL_STORAGE"),
                    "targetSdkVersion < 4",
                    Pair.of("maxSdkVersion", "18") // permission became implied at 19.
            );
            hasWriteToExternalStoragePermission = permission.isPresent();

            addIfAbsent(mergingReport.getActionRecorder(),
                    USES_PERMISSION,
                    permission("READ_PHONE_STATE"),
                    "targetSdkVersion < 4");
        }
        // If the application has requested WRITE_EXTERNAL_STORAGE, we will
        // force them to always take READ_EXTERNAL_STORAGE as well.  We always
        // do this (regardless of target API version) because we can't have
        // an app with write permission but not read permission.
        if (hasWriteToExternalStoragePermission
                && !getByTypeAndKey(USES_PERMISSION, permission("READ_EXTERNAL_STORAGE"))
                        .isPresent()) {

            addIfAbsent(mergingReport.getActionRecorder(),
                    USES_PERMISSION,
                    permission("READ_EXTERNAL_STORAGE"),
                    "requested WRITE_EXTERNAL_STORAGE",
                    // NOTE TO @xav, where can we find the list of implied permissions at versions X
                    Pair.of("maxSdkVersion", "18") // permission became implied at 19, DID IT ???
            );
        }

        // Pre-JellyBean call log permission compatibility.
        if (libraryTargetSdk < 16) {
            if (getByTypeAndKey(USES_PERMISSION, permission("READ_CONTACTS")).isPresent()) {
                addIfAbsent(mergingReport.getActionRecorder(),
                        USES_PERMISSION, permission("READ_CALL_LOG"),
                        "targetSdkVersion < 16 and requested READ_CONTACTS");
            }
            if (getByTypeAndKey(USES_PERMISSION, permission("WRITE_CONTACTS")).isPresent()) {
                addIfAbsent(mergingReport.getActionRecorder(),
                        USES_PERMISSION, permission("WRITE_CALL_LOG"),
                        "targetSdkVersion < 16 and requested WRITE_CONTACTS");
            }
        }
    }

    /**
     * Returns true if the minSdkVersion of the application and the library are compatible, false
     * otherwise.
     */
    private boolean checkUsesSdkMinVersion(int thisMinSdk, int libraryMinSdk) {

        // the merged document minSdk cannot be lower than a library
        if (thisMinSdk < libraryMinSdk) {

            // check if this higher priority document has any tools instructions for the node
            // or the attribute.
            Optional<XmlElement> xmlElementOptional = getByTypeAndKey(USES_SDK, null);
            if (!xmlElementOptional.isPresent()) {
                return false;
            }
            XmlElement xmlElement = xmlElementOptional.get();

            if (!xmlElement.getOperationType().isOverriding()) {
                // last chance, check the attribute.
                if (xmlElement.getAttributeOperationType(XmlNode.fromXmlName(
                        "android:minSdkVersion")) == AttributeOperationType.STRICT) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds a new element of type nodeType with a specific keyValue if the element is absent in this
     * document. Will also add attributes expressed through key value pairs.
     *
     * @param actionRecorder to records creation actions.
     * @param nodeType the node type to crete
     * @param keyValue the optional key for the element.
     * @param attributes the optional array of key value pairs for extra element attribute.
     * @return the Xml element whether it was created or existed or {@link Optional#absent()} if
     * it does not exist in this document.
     */
    private Optional<Element> addIfAbsent(
            @NonNull ActionRecorder actionRecorder,
            @NonNull ManifestModel.NodeTypes nodeType,
            @Nullable String keyValue,
            @Nullable String reason,
            @Nullable Pair<String, String>... attributes) {

        Optional<XmlElement> xmlElementOptional = getByTypeAndKey(nodeType, keyValue);
        if (xmlElementOptional.isPresent()) {
            return Optional.absent();
        }
        Element elementNS = getXml()
                .createElementNS(SdkConstants.ANDROID_URI, "android:" + nodeType.toXmlName());


        ImmutableList<String> keyAttributesNames = nodeType.getNodeKeyResolver()
                .getKeyAttributesNames();
        if (keyAttributesNames.size() == 1) {
            elementNS.setAttributeNS(
                    SdkConstants.ANDROID_URI, "android:" + keyAttributesNames.get(0), keyValue);
        }
        if (attributes != null) {
            for (Pair<String, String> attribute : attributes) {
                elementNS.setAttributeNS(
                        SdkConstants.ANDROID_URI, "android:" + attribute.getFirst(),
                        attribute.getSecond());
            }
        }

        // record creation.
        XmlElement xmlElement = new XmlElement(elementNS, this);
        actionRecorder.recordImpliedNodeAction(xmlElement, reason);

        getRootNode().getXml().appendChild(elementNS);
        return Optional.of(elementNS);
    }

    private static String permission(String permissionName) {
        return "android.permission." + permissionName;
    }
}
