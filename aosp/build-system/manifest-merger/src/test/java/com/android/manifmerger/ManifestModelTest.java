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

import static com.android.manifmerger.XmlNode.NodeKey;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.android.xml.AndroidManifest;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.ManifestModel} class.
 */
public class ManifestModelTest extends TestCase {

    public void testNameResolution()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:name=\"camera\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNoUseFeaturesDeclaration"), input);

        XmlElement xmlElement = xmlDocument.getRootNode().getMergeableElements().get(0);
        assertEquals("uses-feature",xmlElement.getXml().getNodeName());
        assertEquals("camera", xmlElement.getKey());
    }

    public void testGlEsKeyResolution()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNoUseFeaturesDeclaration"), input);

        XmlElement xmlElement = xmlDocument.getRootNode().getMergeableElements().get(0);
        assertEquals("uses-feature",xmlElement.getXml().getNodeName());
        assertEquals("0x00030000", xmlElement.getKey());
    }


    public void testInvalidGlEsVersion()
            throws ParserConfigurationException, SAXException, IOException {

        AttributeModel.Hexadecimal32Bits validator =
                new AttributeModel.Hexadecimal32Bits();
        XmlAttribute xmlAttribute = Mockito.mock(XmlAttribute.class);
        MergingReport.Builder mergingReport = Mockito.mock(MergingReport.Builder.class);
        when(xmlAttribute.getId()).thenReturn(new NodeKey(AndroidManifest.ATTRIBUTE_GLESVERSION));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(xmlAttribute).addMessage(
                Mockito.any(MergingReport.Builder.class),
                eq(MergingReport.Record.Severity.ERROR),
                argumentCaptor.capture());
        when(xmlAttribute.printPosition()).thenReturn("unknown");
        assertFalse(validator.validates(mergingReport, xmlAttribute, "0xFFFFFFFFFFFF"));
        assertEquals("Attribute glEsVersion at unknown is not a valid hexadecimal "
                        + "32 bit value, found 0xFFFFFFFFFFFF",
                argumentCaptor.getValue());
    }

    public void testTooLowGlEsVersion()
            throws ParserConfigurationException, SAXException, IOException {

        AttributeModel.Hexadecimal32BitsWithMinimumValue validator =
                new AttributeModel.Hexadecimal32BitsWithMinimumValue(0x00010000);
        XmlAttribute xmlAttribute = Mockito.mock(XmlAttribute.class);
        MergingReport.Builder mergingReport = Mockito.mock(MergingReport.Builder.class);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(xmlAttribute).addMessage(
                Mockito.any(MergingReport.Builder.class),
                eq(MergingReport.Record.Severity.ERROR),
                argumentCaptor.capture());

        when(xmlAttribute.getId()).thenReturn(new NodeKey(AndroidManifest.ATTRIBUTE_GLESVERSION));
        when(xmlAttribute.printPosition()).thenReturn("unknown");
        assertFalse(validator.validates(mergingReport, xmlAttribute, "0xFFF"));
        assertEquals("Attribute glEsVersion at unknown is not a valid hexadecimal value, "
                        + "minimum is 0x00010000, maximum is 0x7FFFFFFF, found 0xFFF",
                argumentCaptor.getValue());
    }

    public void testOkGlEsVersion()
            throws ParserConfigurationException, SAXException, IOException {

        AttributeModel.Hexadecimal32BitsWithMinimumValue validator =
                new AttributeModel.Hexadecimal32BitsWithMinimumValue(0x00010000);
        XmlAttribute xmlAttribute = Mockito.mock(XmlAttribute.class);
        MergingReport.Builder mergingReport = Mockito.mock(MergingReport.Builder.class);

        when(xmlAttribute.getId()).thenReturn(new NodeKey(AndroidManifest.ATTRIBUTE_GLESVERSION));
        when(xmlAttribute.printPosition()).thenReturn("unknown");
        assertTrue(validator.validates(mergingReport, xmlAttribute, "0x00020001"));
        verifyNoMoreInteractions(xmlAttribute);
    }

    public void testTooBigGlEsVersion()
            throws ParserConfigurationException, SAXException, IOException {

        AttributeModel.Hexadecimal32BitsWithMinimumValue validator =
                new AttributeModel.Hexadecimal32BitsWithMinimumValue(0x00010000);
        XmlAttribute xmlAttribute = Mockito.mock(XmlAttribute.class);
        MergingReport.Builder mergingReport = Mockito.mock(MergingReport.Builder.class);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(xmlAttribute).addMessage(
                Mockito.any(MergingReport.Builder.class),
                eq(MergingReport.Record.Severity.ERROR),
                argumentCaptor.capture());

        when(xmlAttribute.getId()).thenReturn(new NodeKey(AndroidManifest.ATTRIBUTE_GLESVERSION));
        when(xmlAttribute.printPosition()).thenReturn("unknown");
        assertFalse(validator.validates(mergingReport, xmlAttribute, "0xFFFFFFFF"));
        assertEquals("Attribute glEsVersion at unknown is not a valid hexadecimal value,"
                        + " minimum is 0x00010000, maximum is 0x7FFFFFFF, found 0xFFFFFFFF",
                argumentCaptor.getValue());
    }

    public void testNoKeyResolution()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:required=\"false\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNoUseFeaturesDeclaration"), input);

        XmlElement xmlElement = xmlDocument.getRootNode().getMergeableElements().get(0);
        assertEquals("uses-feature",xmlElement.getXml().getNodeName());
        assertNull(xmlElement.getKey());
    }

    public void testTwoAttributesKeyResolution()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <compatible-screens>\n"
                + "         <screen/>\n"
                + "         <screen android:screenDensity=\"mdpi\"/>\n"
                + "         <screen android:screenSize=\"normal\"/>\n"
                + "         <screen android:screenSize=\"normal\" android:screenDensity=\"mdpi\"/>\n"
                + "    </compatible-screens>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNoUseFeaturesDeclaration"), input);

        XmlElement xmlElement = xmlDocument.getRootNode().getMergeableElements().get(0);
        ImmutableList<XmlElement> screenDefinitions = xmlElement.getMergeableElements();
        assertNull(screenDefinitions.get(0).getKey());
        assertEquals("mdpi", screenDefinitions.get(1).getKey());
        assertEquals("normal", screenDefinitions.get(2).getKey());
        assertEquals("normal+mdpi", screenDefinitions.get(3).getKey());
    }
}
