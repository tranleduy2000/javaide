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

import com.android.SdkConstants;
import com.android.ide.common.blame.SourcePosition;
import com.android.ide.common.xml.XmlFormatPreferences;
import com.android.ide.common.xml.XmlFormatStyle;
import com.android.ide.common.xml.XmlPrettyPrinter;
import com.google.common.base.Optional;

import junit.framework.TestCase;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for {@link XmlLoader}
 */
public class XmlLoaderTest extends TestCase {

    public void testAndroidPrefix() throws IOException, SAXException, ParserConfigurationException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testToolsPrefix()"), input);
        Optional<XmlElement> applicationOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
        assertTrue(applicationOptional.isPresent());
        Node label = applicationOptional.get().getXml().getAttributes().item(0);
        assertEquals("label", label.getLocalName());
        assertEquals(SdkConstants.ANDROID_URI, label.getNamespaceURI());
        assertEquals("android:label", label.getNodeName());
    }

    public void testPrettyPrint() throws IOException, SAXException, ParserConfigurationException {

        String input = ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\" >\n"
                + "\n"
                + "    <!-- this is a comment -->\n"
                + "    <application android:label=\"@string/lib_name\" >\n"
                + "\n"
                + "        <!-- The activity name will be expanded to its full FQCN by default. -->\n"
                + "        <activity\n"
                + "            android:name=\"com.example.lib3.MainActivity\"\n"
                + "            android:label=\"@string/app_name\" >\n"
                + "            <intent-filter>\n\n"
                + "                <!-- some other comment -->\n"
                + "                <action android:name=\"android.intent.action.MAIN\" />\n"
                + "\n"
                + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
                + "            </intent-filter>\n"
                + "        </activity>\n"
                + "    </application>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPrettyPrint()"), input);
        Optional<XmlElement> applicationOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
        assertTrue(applicationOptional.isPresent());
        String prettyPrinted = XmlPrettyPrinter
                .prettyPrint(xmlDocument.getXml(), XmlFormatPreferences.defaults(),
                        XmlFormatStyle.get(xmlDocument.getRootNode().getXml()), null, false);
        assertEquals(input, prettyPrinted);
    }

    public void testToolsPrefix() throws IOException, SAXException, ParserConfigurationException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" tools:node=\"replace\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testToolsPrefix()"),input);
        Optional<XmlElement> applicationOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
        assertTrue(applicationOptional.isPresent());
        Element application = applicationOptional.get().getXml();
        assertEquals(2, application.getAttributes().getLength());
        Attr label = application.getAttributeNodeNS(SdkConstants.ANDROID_URI, "label");
        assertEquals("android:label", label.getNodeName());
        Attr tools = application.getAttributeNodeNS(SdkConstants.TOOLS_URI,
                NodeOperationType.NODE_LOCAL_NAME);
        assertEquals("replace", tools.getNodeValue());

        // check positions.
        SourcePosition applicationPosition = applicationOptional.get().getPosition();
        assertNotNull(applicationPosition);
        assertEquals(6, applicationPosition.getStartLine() + 1);
        assertEquals(5, applicationPosition.getStartColumn() + 1);

        XmlAttribute xmlAttribute =
                new XmlAttribute(applicationOptional.get(), tools, null /* AttributeModel */);
        SourcePosition toolsPosition = xmlAttribute.getPosition();
        assertNotNull(toolsPosition);
        assertEquals(6, toolsPosition.getStartLine() + 1);
        assertEquals(51, toolsPosition.getStartColumn() + 1);
    }

    public void testUnusualPrefixes()
            throws IOException, SAXException, ParserConfigurationException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:x=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:y=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application x:label=\"@string/lib_name\" y:node=\"replace\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUnusualPrefixes()"), input);
        Optional<XmlElement> applicationOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
        assertTrue(applicationOptional.isPresent());
        Element application = applicationOptional.get().getXml();
        assertEquals(2, application.getAttributes().getLength());
        Node label = application.getAttributeNodeNS(SdkConstants.ANDROID_URI, "label");
        assertEquals("x:label", label.getNodeName());
        Node tools = application.getAttributeNodeNS(SdkConstants.TOOLS_URI,
                NodeOperationType.NODE_LOCAL_NAME);
        assertEquals("y:node", tools.getNodeName());
        assertEquals("replace", tools.getNodeValue());
    }



}
