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

import com.android.sdklib.mock.MockLog;
import com.google.common.base.Optional;
import com.google.common.base.Strings;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.ToolsInstructionsCleaner} class.
 */
public class ToolsInstructionsCleanerTest extends TestCase {

    public void testNodeRemoveOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\">\n"
                + "\n"
                + "        <activity android:name=\"activityOne\" tools:node=\"remove\"/>\n"
                + "    </application>"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeRemoveOperation"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        // ensure the activity did get deleted.
        assertFalse(activity.isPresent());
    }

    public void testNodeWithChildrenRemoveOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "        xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "        package=\"com.example.lib3\" >\n"
                + "\n"
                + "        <application>\n"
                + "             <activity android:name=\"com.example.lib3.activityOne\" >\n"
                + "                 <intent-filter tools:node=\"remove\" >\n"
                + "                     <action android:name=\"android.intent.action.VIEW\" />\n"
                + "                     <category android:name=\"android.intent.category.DEFAULT\" />\n"
                + "                     <category android:name=\"android.intent.category.BROWSABLE\" />\n"
                + "                 </intent-filter>\n"
                + "             </activity>\n"
                + "        </application>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeRemoveWithChildrenOperation"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        assertTrue(activity.isPresent());
        Optional<Element> intentFilter = getChildElementByName(application.get(), "intent-filter");

        // ensure the intent-filter did get deleted.
        assertFalse(intentFilter.isPresent());
    }

    public void testInvalidToolsRemoveOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\""
                + "    tools:node=\"remove\">\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeRemoveOperation"), main);

        assertNull(ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog));
    }

    public void testInvalidToolsRemoveAllOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\""
                + "    tools:node=\"removeAll\">\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeRemoveOperation"), main);

        assertNull(ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog));
    }

    public void testNodeReplaceOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\">\n"
                + "        <activity android:name=\"activityOne\" tools:node=\"replace\"/>\n"
                + "    </application>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeReplaceOperation"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        // ensure the activity did not get deleted.
        assertTrue(activity.isPresent());
    }

    public void testAttributeRemoveOperation()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\">\n"
                + "        <activity android:name=\"activityOne\" tools:remove=\"exported\"/>\n"
                + "    </application>\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testAttributeRemoveOperation"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        // ensure the activity did not get deleted.
        assertTrue(activity.isPresent());
        assertEquals(1, activity.get().getAttributes().getLength());
        assertNotNull(activity.get().getAttribute("android:name"));
    }

    public void testSelectorRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\">\n"
                + "        <activity android:name=\"activityOne\" "
                + "             tools:node=\"remove\" tools:selector=\"foo\"/>\n"
                + "    </application>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testSelectorRemoval"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        // ensure the activity did not get deleted since it has a selector
        assertTrue(activity.isPresent());
        assertTrue(Strings.isNullOrEmpty(activity.get().getAttribute("tools:selector")));
    }

    public void testOtherToolInstructionRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\">\n"
                + "        <activity android:name=\"activityOne\" tools:ignore=\"value\"/>\n"
                + "    </application>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNodeReplaceOperation"), main);

        Element rootElement = mainDocument.getRootNode().getXml();
        ToolsInstructionsCleaner.cleanToolsReferences(mainDocument, mockLog);

        Optional<Element> application = getChildElementByName(rootElement, "application");
        assertTrue(application.isPresent());

        Optional<Element> activity = getChildElementByName(application.get(), "activity");
        // ensure the activity did not get deleted.
        assertTrue(activity.isPresent());
        // ensure tools:ignore got deleted.
        assertNull(activity.get().getAttributeNodeNS("http://schemas.android.com/tools", "ignore"));
    }

    private static Optional<Element> getChildElementByName(Element parent, String name) {
        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE
                    && item.getNodeName().equals(name)) {
                return Optional.of((Element) item);
            }
        }
        return Optional.absent();
    }
}
