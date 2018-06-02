/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.ide.common.res2;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;


/**
 * Utility class to handle Nodes.
 *
 * - convert Node from one XML {@link Document} to be used by another Document
 * - compare Nodes and attributes.
 */
class NodeUtils {

    /**
     * Makes a new document adopt a node from a different document, and correctly reassign namespace
     * and prefix
     * @param document the new document
     * @param node the node to adopt.
     * @return the adopted node.
     */
    static Node adoptNode(Document document, Node node) {
        Node newNode = document.adoptNode(node);

        updateNamespace(newNode, document);

        return newNode;
    }

    static Node duplicateNode(Document document, Node node) {
        Node newNode;
        if (node.getNamespaceURI() != null) {
            newNode = document.createElementNS(node.getNamespaceURI(), node.getLocalName());
        } else {
            newNode = document.createElement(node.getNodeName());
        }

        // copy the attributes
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0 ; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);

            Attr newAttr;
            if (attr.getNamespaceURI() != null) {
                newAttr = document.createAttributeNS(attr.getNamespaceURI(), attr.getLocalName());
                newNode.getAttributes().setNamedItemNS(newAttr);
            } else {
                newAttr = document.createAttribute(attr.getName());
                newNode.getAttributes().setNamedItem(newAttr);
            }

            newAttr.setValue(attr.getValue());
        }

        // then duplicate the sub-nodes.
        NodeList children = node.getChildNodes();
        for (int i = 0 ; i < children.getLength() ; i++) {
            Node child = children.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Node duplicatedChild = duplicateNode(document, child);
            newNode.appendChild(duplicatedChild);
        }

        return newNode;
    }

    static void addAttribute(Document document, Node node,
                             String namespaceUri, String attrName, String attrValue) {
        Attr attr;
        if (namespaceUri != null) {
            attr = document.createAttributeNS(namespaceUri, attrName);
        } else {
            attr = document.createAttribute(attrName);
        }

        attr.setValue(attrValue);

        if (namespaceUri != null) {
            node.getAttributes().setNamedItemNS(attr);
        } else {
            node.getAttributes().setNamedItem(attr);
        }
    }

    /**
     * Updates the namespace of a given node (and its children) to work in a given document
     * @param node the node to update
     * @param document the new document
     */
    private static void updateNamespace(Node node, Document document) {

        // first process this node
        processSingleNodeNamespace(node, document);

        // then its attributes
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0, n = attributes.getLength(); i < n; i++) {
                Node attribute = attributes.item(i);
                if (!processSingleNodeNamespace(attribute, document)) {
                    String nsUri = attribute.getNamespaceURI();
                    if (nsUri != null) {
                        attributes.removeNamedItemNS(nsUri, attribute.getLocalName());
                    } else {
                        attributes.removeNamedItem(attribute.getLocalName());
                    }
                }
            }
        }

        // then do it for the children nodes.
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0, n = children.getLength(); i < n; i++) {
                Node child = children.item(i);
                if (child != null) {
                    updateNamespace(child, document);
                }
            }
        }
    }

    /**
     * Update the namespace of a given node to work with a given document.
     *
     * @param node the node to update
     * @param document the new document
     *
     * @return false if the attribute is to be dropped
     */
    private static boolean processSingleNodeNamespace(Node node, Document document) {
        if ("xmlns".equals(node.getLocalName())) {
            return false;
        }

        String ns = node.getNamespaceURI();
        if (ns != null) {
            NamedNodeMap docAttributes = document.getAttributes();

            String prefix = getPrefixForNs(docAttributes, ns);
            if (prefix == null) {
                prefix = getUniqueNsAttribute(docAttributes);
                Attr nsAttr = document.createAttribute(prefix);
                nsAttr.setValue(ns);
                document.getChildNodes().item(0).getAttributes().setNamedItem(nsAttr);
            }

            // set the prefix on the node, by removing the xmlns: start
            prefix = prefix.substring(6);
            node.setPrefix(prefix);
        }

        return true;
    }

    /**
     * Looks for an existing prefix for a a given namespace.
     * The prefix must start with "xmlns:". The whole prefix is returned.
     * @param attributes the list of attributes to look through
     * @param ns the namespace to find.
     * @return the found prefix or null if none is found.
     */
    private static String getPrefixForNs(NamedNodeMap attributes, String ns) {
        if (attributes != null) {
            for (int i = 0, n = attributes.getLength(); i < n; i++) {
                Attr attribute = (Attr) attributes.item(i);
                if (ns.equals(attribute.getValue()) && ns.startsWith(SdkConstants.XMLNS_PREFIX)) {
                    return attribute.getName();
                }
            }
        }

        return null;
    }

    private static String getUniqueNsAttribute(NamedNodeMap attributes) {
        if (attributes == null) {
            return "xmlns:ns1";
        }

        int i = 2;
        while (true) {
            String name = String.format("xmlns:ns%d", i++);
            if (attributes.getNamedItem(name) == null) {
                return name;
            }
        }
    }

    static boolean compareElementNode(@NonNull Node node1, @NonNull Node node2, boolean strict) {
        if (!node1.getNodeName().equals(node2.getNodeName())) {
            return false;
        }

        NamedNodeMap attr1 = node1.getAttributes();
        NamedNodeMap attr2 = node2.getAttributes();

        if (!compareAttributes(attr1, attr2)) {
            return false;
        }

        if (strict) {
            return compareChildren(node1.getChildNodes(), node2.getChildNodes());
        }

        return compareContent(node1.getChildNodes(), node2.getChildNodes());
    }

    private static boolean compareChildren(
            @NonNull NodeList children1,
            @NonNull NodeList children2) {
        // because this represents a resource values, we're going to be very strict about this
        // comparison.
        if (children1.getLength() != children2.getLength()) {
            return false;
        }

        for (int i = 0, n = children1.getLength(); i < n; i++) {
            Node child1 = children1.item(i);
            Node child2 = children2.item(i);

            short nodeType = child1.getNodeType();
            if (nodeType != child2.getNodeType()) {
                return false;
            }

            switch (nodeType) {
                case Node.ELEMENT_NODE:
                    if (!compareElementNode(child1, child2, true)) {
                        return false;
                    }
                    break;
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                case Node.COMMENT_NODE:
                    if (!child1.getNodeValue().equals(child2.getNodeValue())) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    private static boolean compareContent(
            @NonNull NodeList children1,
            @NonNull NodeList children2) {
        // only compares the content (ie not the text node).

        // accumulate both true children list.
        List<Node> childList = getElementChildren(children1);
        List<Node> childList2 = getElementChildren(children2);

        if (childList.size() != childList2.size()) {
            return false;
        }

        // no attempt to match nodes one to one.
        for (Node child : childList) {
            boolean found = false;
            for (Node child2 : childList2) {
                if (compareElementNode(child, child2, false)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    @NonNull
    private static List<Node> getElementChildren(@NonNull NodeList children) {
        List<Node> results = Lists.newArrayListWithExpectedSize(children.getLength());

        final int len = children.getLength();
        for (int i = 0; i < len; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                results.add(child);
            }
        }

        return results;
    }

    @VisibleForTesting
    static boolean compareAttributes(
            @NonNull NamedNodeMap attrMap1,
            @NonNull NamedNodeMap attrMap2) {
        if (attrMap1.getLength() != attrMap2.getLength()) {
            return false;
        }

        for (int i = 0, n = attrMap1.getLength(); i < n; i++) {
            Attr attr1 = (Attr) attrMap1.item(i);

            String ns1 = attr1.getNamespaceURI();

            Attr attr2;
            if (ns1 != null) {
                attr2 = (Attr) attrMap2.getNamedItemNS(ns1, attr1.getLocalName());
            }  else {
                attr2 = (Attr) attrMap2.getNamedItem(attr1.getName());
            }

            if (attr2 == null || !attr2.getValue().equals(attr1.getValue())) {
                return false;
            }
        }

        return true;
    }

    @Nullable
    static String getAttribute(@NonNull Node node, @NonNull String attrName) {
        Attr attr = (Attr) node.getAttributes().getNamedItem(attrName);
        if (attr != null) {
            return attr.getValue();
        }
        return null;
    }
}
