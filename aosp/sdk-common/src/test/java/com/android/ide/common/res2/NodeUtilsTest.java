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

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class NodeUtilsTest extends TestCase {

    public void testBasicAttributes() throws Exception {
        Document document = createDocument();

        // create two nodes
        Node node1 = document.createElement("N1");
        Node node2 = document.createElement("N2");

        NodeUtils.addAttribute(document, node1, null, "foo", "bar");
        NodeUtils.addAttribute(document, node2, null, "foo", "bar");
        assertTrue(NodeUtils.compareAttributes(node1.getAttributes(), node2.getAttributes()));

        NodeUtils.addAttribute(document, node1, null, "foo2", "bar2");
        assertFalse(NodeUtils.compareAttributes(node1.getAttributes(), node2.getAttributes()));

        NodeUtils.addAttribute(document, node2, null, "foo2", "bar");
        assertFalse(NodeUtils.compareAttributes(node1.getAttributes(), node2.getAttributes()));
    }

    public void testNamespaceAttributes() throws Exception {
        Document document = createDocument();

        // create two nodes
        Node node1 = document.createElement("N1");
        Node node2 = document.createElement("N2");

        NodeUtils.addAttribute(document, node1, "http://some.uri/", "foo", "bar");
        NodeUtils.addAttribute(document, node2, "http://some.uri/", "foo", "bar");
        assertTrue(NodeUtils.compareAttributes(node1.getAttributes(), node2.getAttributes()));

        NodeUtils.addAttribute(document, node1, "http://some.uri/", "foo2", "bar2");
        NodeUtils.addAttribute(document, node2, "http://some.other.uri/", "foo2", "bar2");
        assertFalse(NodeUtils.compareAttributes(node1.getAttributes(), node2.getAttributes()));
    }

    public void testNodesWithChildrenNodes() throws Exception {
        Document document = createDocument();

        // create two nodes
        Node node1 = document.createElement("some-node");
        Node node2 = document.createElement("some-node");

        Node child1a = document.createElement("child1");
        Node child1b = document.createElement("child2");
        node1.appendChild(child1a);
        node1.appendChild(child1b);

        Node child2a = document.createElement("child1");
        Node child2b = document.createElement("child2");
        node2.appendChild(child2a);
        node2.appendChild(child2b);

        assertTrue(NodeUtils.compareElementNode(node1, node2, true));
    }

    public void testNodesWithChildrenNodesInWrongOrder() throws Exception {
        Document document = createDocument();

        // create two nodes
        Node node1 = document.createElement("some-node");
        Node node2 = document.createElement("some-node");

        Node child1a = document.createElement("child1");
        Node child1b = document.createElement("child2");
        node1.appendChild(child1a);
        node1.appendChild(child1b);

        Node child2a = document.createElement("child2");
        Node child2b = document.createElement("child1");
        node2.appendChild(child2a);
        node2.appendChild(child2b);

        assertFalse(NodeUtils.compareElementNode(node1, node2, true));
    }

    public void testNodesWithChildrenNodesInOtherOrder() throws Exception {
        Document document = createDocument();

        // create two nodes
        Node node1 = document.createElement("some-node");
        Node node2 = document.createElement("some-node");

        Node child1a = document.createElement("child1");
        Node child1b = document.createElement("child2");
        node1.appendChild(child1a);
        node1.appendChild(child1b);

        Node child2a = document.createElement("child2");
        Node child2b = document.createElement("child1");
        node2.appendChild(child2a);
        node2.appendChild(child2b);

        assertTrue(NodeUtils.compareElementNode(node1, node2, false));
    }

    public void testAdoptNode() throws Exception {
        Document document = createDocument();
        Node rootNode = document.createElement("root");
        document.appendChild(rootNode);

        // create a single s
        Node node = document.createElement("some-node");

        // add some children
        Node child1 = document.createElement("child1");
        Node child2 = document.createElement("child2");
        node.appendChild(child1).appendChild(child2);
        Node cdata = document.createCDATASection("some <random> text");
        child2.appendChild(cdata);

        // add some attributes
        NodeUtils.addAttribute(document, node, null, "foo", "bar");
        NodeUtils.addAttribute(document, node, "http://some.uri", "foo2", "bar2");
        NodeUtils.addAttribute(document, child1, "http://some.other.uri", "blah", "test");
        NodeUtils.addAttribute(document, child2, "http://another.uri", "blah", "test");

        // create the other document to receive the adopted node. It must have a root node.
        Document document2 = createDocument();
        rootNode = document2.createElement("root");
        document2.appendChild(rootNode);

        assertTrue(NodeUtils.compareElementNode(node, NodeUtils.adoptNode(document2, node), true));
    }

    public void testDuplicateNode() throws Exception {
        Document document = createDocument();
        Node rootNode = document.createElement("root");
        document.appendChild(rootNode);

        // create a single s
        Node node = document.createElement("some-node");

        // add some children
        Node child1 = document.createElement("child1");
        Node child2 = document.createElement("child2");
        node.appendChild(child1).appendChild(child2);
        Node cdata = document.createCDATASection("some <random> text");
        child2.appendChild(cdata);

        // add some attributes
        NodeUtils.addAttribute(document, node, null, "foo", "bar");
        NodeUtils.addAttribute(document, node, "http://some.uri", "foo2", "bar2");
        NodeUtils.addAttribute(document, child1, "http://some.other.uri", "blah", "test");
        NodeUtils.addAttribute(document, child2, "http://another.uri", "blah", "test");

        // create the other document to receive the adopted node. It must have a root node.
        Document document2 = createDocument();
        rootNode = document2.createElement("root");
        document2.appendChild(rootNode);

        assertTrue(NodeUtils.compareElementNode(
                node,
                NodeUtils.duplicateNode(document2, node),
                false));
    }

    private static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }
}
