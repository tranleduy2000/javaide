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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.android.SdkConstants;
import com.android.utils.ILogger;
import com.android.xml.AndroidManifest;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link ElementsTrimmer} class
 */
public class ElementsTrimmerTest extends TestCase {

    @Mock
    ILogger mILogger;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testNoUseFeaturesDeclaration()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\" "
                + "         permissionGroup=\"permissionGroupOne\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNoUseFeaturesDeclaration"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);
        assertEquals(0, mergingReport.getActionRecorder().build().getNodeKeys().size());
    }


    public void testNothingToTrim()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x0002000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testNothingToTrim"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);
        assertEquals(0, mergingReport.getActionRecorder().build().getNodeKeys().size());
    }


    public void testMultipleAboveTwoResults()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature"
                + "             android:required=\"true\""
                + "             android:glEsVersion=\"0x00020000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00021000\"/>\n"
                + "    <uses-feature"
                + "             android:glEsVersion=\"0x00022000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMultipleAboveTwoResults"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);

        // check action recording.
        checkActionsRecording(mergingReport, 2);

        // check results.
        checkResults(xmlDocument, ImmutableList.of("0x00030000", "0x00022000"));
    }

    public void testSingleAboveTwoResults()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:glEsVersion=\"0x00020000\"/>\n"
                + "    <uses-feature android:glEsVersion=\"0x00021000\"/>\n"
                + "    <uses-feature android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testSingleAboveTwoResults"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);

        // check action recording.
        checkActionsRecording(mergingReport, 2);

        // check results.
        checkResults(xmlDocument, ImmutableList.of("0x00030000"));
    }

    public void testMultipleBelowTwoResults()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature"
                + "             android:required=\"true\""
                + "             android:glEsVersion=\"0x00010000\"/>\n"
                + "    <uses-feature"
                + "             android:glEsVersion=\"0x00011000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00012000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMultipleBelowTwoResults"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);

        // check action recording.
        checkActionsRecording(mergingReport, 1);

        // check results.
        checkResults(xmlDocument, ImmutableList.of("0x00011000", "0x00012000"));
    }

    public void testSingleBelowTwoResults()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:glEsVersion=\"0x00010000\"/>\n"
                + "    <uses-feature android:glEsVersion=\"0x00011000\"/>\n"
                + "    <uses-feature android:glEsVersion=\"0x00012000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testSingleBelowTwoResults"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);

        // check action recording.
        checkActionsRecording(mergingReport, 2);

        // check results.
        checkResults(xmlDocument, ImmutableList.of("0x00012000"));
    }

    public void testMultipleAboveAndBelowTwoResults()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature"
                + "             android:required=\"true\""
                + "             android:glEsVersion=\"0x00010000\"/>\n"
                + "    <uses-feature"
                + "             android:glEsVersion=\"0x00011000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00012000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"true\""
                + "             android:glEsVersion=\"0x00020000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00021000\"/>\n"
                + "    <uses-feature"
                + "             android:glEsVersion=\"0x00022000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMultipleAboveAndBelowTwoResults"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mILogger);
        ElementsTrimmer.trim(xmlDocument, mergingReport);
        assertFalse(mergingReport.hasErrors());
        Mockito.verifyZeroInteractions(mILogger);

        // check action recording.
        checkActionsRecording(mergingReport, 3);

        // check results.
        checkResults(xmlDocument,
                ImmutableList.of("0x00011000", "0x00012000", "0x00022000", "0x00030000"));
    }

    public void testUsesFeatureSplit_attributeDeleted()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:name=\"@string/lib_name\""
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00020000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUsesFeatureSplit"), input);

        ActionRecorder mockActionRecorder = Mockito.mock(ActionRecorder.class);
        MergingReport.Builder mockReport = Mockito.mock(MergingReport.Builder.class);
        when(mockReport.getActionRecorder()).thenReturn(mockActionRecorder);
        ElementsTrimmer.trim(xmlDocument, mockReport);

        // check that we have now 2 uses-feature with separated keys.
        NodeList elementsByTagName = xmlDocument.getRootNode().getXml()
                .getElementsByTagName("uses-feature");
        assertEquals(2, elementsByTagName.getLength());

        // verify the action was recorded.
        verify(mockActionRecorder).recordAttributeAction(any(XmlAttribute.class),
                eq(Actions.ActionType.REJECTED), (AttributeOperationType) eq(null));

        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            NamedNodeMap attributes = elementsByTagName.item(i).getAttributes();
            assertEquals(2, attributes.getLength());
            ensureOnlyOneKey(attributes, ManifestModel.NodeTypes.USES_FEATURE);
        }
    }

    public void testUsesFeatureSplit_elementDeleted()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature android:required=\"false\""
                + "             android:glEsVersion=\"0x00020000\"/>\n"
                + "    <uses-feature"
                + "             android:required=\"false\""
                + "             android:glEsVersion=\"0x00030000\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUsesFeatureSplit"), input);

        ActionRecorder mockActionRecorder = Mockito.mock(ActionRecorder.class);
        MergingReport.Builder mockReport = Mockito.mock(MergingReport.Builder.class);
        when(mockReport.getActionRecorder()).thenReturn(mockActionRecorder);
        ElementsTrimmer.trim(xmlDocument, mockReport);

        // check that we have now 2 uses-feature with separated keys.
        NodeList elementsByTagName = xmlDocument.getRootNode().getXml()
                .getElementsByTagName("uses-feature");
        assertEquals(1, elementsByTagName.getLength());

        // verify the action was recorded.
        verify(mockActionRecorder).recordNodeAction(any(XmlElement.class),
                eq(Actions.ActionType.REJECTED));

        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            NamedNodeMap attributes = elementsByTagName.item(i).getAttributes();
            assertEquals(2, attributes.getLength());
            ensureOnlyOneKey(attributes, ManifestModel.NodeTypes.USES_FEATURE);
        }
    }

    private void ensureOnlyOneKey(NamedNodeMap namedNodeMap, ManifestModel.NodeTypes nodeType) {
        String firstKey = null;
        ImmutableList<String> keyAttributesNames =
                nodeType.getNodeKeyResolver().getKeyAttributesNames();
        for (String keyAttributesName : keyAttributesNames) {
            if (namedNodeMap.getNamedItemNS(SdkConstants. ANDROID_URI, keyAttributesName) != null) {
                if (firstKey != null) {
                    fail("Found 2 keys : " + firstKey + " and " + keyAttributesName);
                }
                firstKey = keyAttributesName;
            }
        }
    }


    private static void checkActionsRecording(
            MergingReport.Builder mergingReport,
            int expectedActionsNumber) {

        Actions actions = mergingReport.build().getActions();
        assertEquals(expectedActionsNumber, actions.getNodeKeys().size());
        for (XmlNode.NodeKey nodeKey : actions.getNodeKeys()) {
            assertEquals(1, actions.getNodeRecords(nodeKey).size());
            assertEquals(Actions.ActionType.REJECTED,
                    actions.getNodeRecords(nodeKey).get(0).getActionType());
        }
    }

    private static void checkResults(XmlDocument xmlDocument, List<String> expectedVersions) {
        NodeList elementsByTagName = xmlDocument.getRootNode().getXml()
                .getElementsByTagName("uses-feature");
        assertEquals(expectedVersions.size(), elementsByTagName.getLength());
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element item = (Element) elementsByTagName.item(i);
            Attr glEsVersion = item.getAttributeNodeNS(SdkConstants.ANDROID_URI,
                    AndroidManifest.ATTRIBUTE_GLESVERSION);
            assertNotNull(glEsVersion);
            assertTrue(expectedVersions.contains(glEsVersion.getValue()));
        }
    }
}
