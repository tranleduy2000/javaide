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

import static com.android.manifmerger.Actions.NodeRecord;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.android.SdkConstants;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link XmlElement}
 */
public class XmlElementTest extends TestCase {

    @Mock
    MergingReport.Builder mergingReport;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testToolsNodeInstructions()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:node=\"remove\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityTwo\" "
                + "         tools:node=\"removeAll\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityThree\" "
                + "         tools:node=\"mergeOnlyAttributes\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testToolsNodeInstructions()"), input);
        Optional<XmlElement> activity = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne");
        assertTrue(activity.isPresent());
        assertEquals(NodeOperationType.REMOVE,
                activity.get().getOperationType());
        activity = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityTwo");
        assertTrue(activity.isPresent());
        assertEquals(NodeOperationType.REMOVE_ALL,
                activity.get().getOperationType());
        activity = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityThree");
        assertTrue(activity.isPresent());
        assertEquals(NodeOperationType.MERGE_ONLY_ATTRIBUTES,
                activity.get().getOperationType());
    }

    public void testInvalidNodeInstruction()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:node=\"funkyValue\"/>\n"
                + "\n"
                + "</manifest>";

        try {
            XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                    TestUtils.sourceFile(getClass(), "testInvalidNodeInstruction()"), input);
            xmlDocument.getRootNode();
            fail("Exception not thrown");
        } catch (IllegalArgumentException expected) {
            // expected.
        }
    }

    public void testAttributeInstructions()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:remove=\"android:theme\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityTwo\" "
                + "         android:theme=\"@theme1\"\n"
                + "         tools:replace=\"android:theme\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityThree\" "
                + "         tools:strict=\"android:exported\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityFour\" "
                + "         android:theme=\"@theme1\"\n"
                + "         android:exported=\"true\"\n"
                + "         android:windowSoftInputMode=\"stateUnchanged\"\n"
                + "         tools:replace="
                + "\"android:theme, android:exported,android:windowSoftInputMode\"/>\n"
                + "\n"
                + "    <activity android:name=\"activityFive\" "
                + "         android:theme=\"@theme1\"\n"
                + "         android:exported=\"true\"\n"
                + "         android:windowSoftInputMode=\"stateUnchanged\"\n"
                + "         tools:remove=\"android:exported\"\n"
                + "         tools:replace=\"android:theme\"\n"
                + "         tools:strict=\"android:windowSoftInputMode\"/>\n"
                + "\n"
                + "</manifest>";

        // ActivityOne, remove operation.
        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testAttributeInstructions()"), input);
        Optional<XmlElement> activityOptional = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activity = activityOptional.get();
        assertEquals(1, activity.getAttributeOperations().size());
        AttributeOperationType attributeOperationType =
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme"));
        assertEquals(AttributeOperationType.REMOVE, attributeOperationType);

        // ActivityTwo, replace operation.
        activityOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityTwo");
        assertTrue(activityOptional.isPresent());
        activity = activityOptional.get();
        assertEquals(1, activity.getAttributeOperations().size());
        attributeOperationType = activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme"));
        assertEquals(AttributeOperationType.REPLACE, attributeOperationType);

        // ActivityThree, strict operation.
        activityOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityThree");
        assertTrue(activityOptional.isPresent());
        activity = activityOptional.get();
        assertEquals(1, activity.getAttributeOperations().size());
        attributeOperationType = activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme"));
        assertEquals(AttributeOperationType.STRICT, attributeOperationType);

        // ActivityFour, multiple target fields.
        activityOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityFour");
        assertTrue(activityOptional.isPresent());
        activity = activityOptional.get();
        assertEquals(3, activity.getAttributeOperations().size());
        assertEquals(AttributeOperationType.REPLACE,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme")));
        assertEquals(AttributeOperationType.REPLACE,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme")));
        assertEquals(AttributeOperationType.REPLACE,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme")));

        // ActivityFive, multiple operations.
        activityOptional = xmlDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityFive");
        assertTrue(activityOptional.isPresent());
        activity = activityOptional.get();
        assertEquals(3, activity.getAttributeOperations().size());

        assertEquals(AttributeOperationType.REMOVE,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:exported")));

        assertEquals(AttributeOperationType.REPLACE,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme")));

        assertEquals(AttributeOperationType.STRICT,
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:windowSoftInputMode")));
    }

    public void testNoNamespaceAwareAttributeInstructions()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:remove=\"theme\"/>\n"
                + "\n"
                + "</manifest>";

        // ActivityOne, remove operation.
        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testAttributeInstructions()"), input);
        Optional<XmlElement> activityOptional = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activity = activityOptional.get();
        assertEquals(1, activity.getAttributeOperations().size());
        AttributeOperationType attributeOperationType =
                activity.getAttributeOperationType(XmlNode.fromXmlName("android:theme"));
        assertEquals(AttributeOperationType.REMOVE, attributeOperationType);
    }

    public void testUnusualNamespacePrefixAttributeInstructions()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:z=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity z:name=\"activityOne\" tools:remove=\"theme\"/>\n"
                + "\n"
                + "</manifest>";

        // ActivityOne, remove operation.
        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testAttributeInstructions()"), input);
        Optional<XmlElement> activityOptional = xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activity = activityOptional.get();

        assertEquals(1, activity.getAttributeOperations().size());
        AttributeOperationType attributeOperationType =
                activity.getAttributeOperationType(XmlNode.fromXmlName("z:theme"));
        assertEquals(AttributeOperationType.REMOVE, attributeOperationType);
    }

    public void testInvalidAttributeInstruction()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:bad-name=\"android:theme\"/>\n"
                + "\n"
                + "</manifest>";

        try {
            XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                    TestUtils.sourceFile(getClass(), "testDiff6()"), input);
            xmlDocument.getRootNode();
            fail("Exception not thrown");
        } catch (RuntimeException expected) {
            // expected.
        }
    }

    public void testOtherToolsInstruction()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         tools:ignore=\"android:theme\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testOtherToolsInstruction"), input);
        xmlDocument.getRootNode();
    }


    public void testDiff1()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/apk/res/android/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";
        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff1()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff1()"), other);

        assertFalse(refDocument.getRootNode().getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                "com.example.lib3.activityOne").get()
                .compareTo(otherDocument.getRootNode().getNodeByTypeAndKey(
                        ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne")
                        .get()).isPresent());
    }

    public void testDiff2()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/apk/res/android/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"mcc\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff2()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff2()"), other);

        assertTrue(refDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne").get()
                .compareTo(
                        otherDocument.getRootNode().getNodeByTypeAndKey(
                                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne")
                                .get()
                ).isPresent());
    }

    public void testDiff3()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/apk/res/android/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\" android:exported=\"true\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff3()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff3()"), other);

        assertTrue(refDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne").get()
                .compareTo(
                        otherDocument.getRootNode().getNodeByTypeAndKey(
                                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne")
                                .get()
                ).isPresent());
    }

    public void testDiff4()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\" android:exported=\"false\"/>\n"
                + "\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff4()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff4()"), other);
        assertTrue(refDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne").get()
                .compareTo(
                        otherDocument.getRootNode().getNodeByTypeAndKey(
                                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne")
                                .get()
                ).isPresent());
    }

    public void testDiff5()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\">\n"
                + "\n"
                + "    </activity>\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff5()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff5()"), other);

        assertFalse(refDocument.compareTo(otherDocument).isPresent());
    }

    public void testDiff6()
            throws Exception {

        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\">\n"
                + "\n"
                + "       <intent-filter android:label=\"@string/foo\"/>\n"
                + "\n"
                + "    </activity>\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff6()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff6()"), other);
        assertTrue(
                refDocument.getRootNode().getNodeByTypeAndKey(
                        ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne").get()
                        .compareTo(
                                otherDocument.getRootNode().getNodeByTypeAndKey(
                                        ManifestModel.NodeTypes.ACTIVITY,
                                        "com.example.lib3.activityOne")
                                        .get()
                        ).isPresent()
        );
    }

    /**
     * test merging of same element types with no collision.
     */
    public void testMerge_NoCollision() throws ParserConfigurationException, SAXException, IOException {
        String reference = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\">\n"
                + "       <intent-filter android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String other = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityTwo\" "
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMerge()"), reference);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMerge()"), other);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        XmlDocument resultDocument = result.get();

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        Optional<XmlElement> activityTwo = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityTwo");
        assertTrue(activityTwo.isPresent());
    }

    /**
     * test merging of same element with no attribute collision.
     */
    public void testAttributeMerging()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\">\n"
                + "       <intent-filter android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        XmlDocument resultDocument = result.get();

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        // verify that both attributes are in the resulting merged element.
        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:configChanges")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
    }

    /**
     * test merging of same element with no attribute collision.
     */
    public void testElementRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" tools:node=\"remove\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:configChanges=\"locale\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        // run the instruction cleaner to get rid of all unwanted attributes, nodes.
        XmlDocument resultDocument = ToolsInstructionsCleaner.cleanToolsReferences(
                result.get(), mergingReportBuilder.getLogger());

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertFalse(activityOne.isPresent());
    }

    /**
     * test merging of same element with no attribute collision.
     */
    public void testElementReplacement()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" tools:node=\"replace\""
                + "         android:exported=\"true\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\">\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        XmlDocument resultDocument = result.get();

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        Logger.getAnonymousLogger().info(resultDocument.prettyPrint());


        assertFalse(refDocument.getRootNode().getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                "com.example.lib3.activityOne").get().compareTo(activityOne.get()).isPresent());
    }

    /**
     * test merging of same element type with STRICT enforcing and no difference between the high
     * and low priority elements.
     */
    public void testStrictElement_noDifference()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" tools:node=\"strict\""
                + "         android:exported=\"true\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        XmlDocument resultDocument = result.get();

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        Logger.getAnonymousLogger().info(resultDocument.prettyPrint());


        assertFalse(refDocument.getRootNode().getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                "com.example.lib3.activityOne").get().compareTo(activityOne.get()).isPresent());
    }

    /**
     * test merging of same element type with STRICT enforcing and no difference between the high
     * and low priority elements.
     */
    public void testStrictElement_withDifference()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" tools:node=\"strict\""
                + "         android:exported=\"true\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        refDocument.merge(otherDocument, mergingReportBuilder);
        assertEquals(MergingReport.Result.ERROR, mergingReportBuilder.build().getResult());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        for (MergingReport.Record record : loggingRecords) {
            Logger.getAnonymousLogger().info(record.toString());
        }
        assertEquals(1, loggingRecords.size());

    }

    /**
     * test attributes merging of elements with no conflict between attributes.
     */
    public void testMerging_noConflict()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <meta-data android:name=\"zoo\" android:value=\"@string/kangaroo\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activityOne = activityOptional.get();
        assertEquals(3, activityOne.getAttributes().size());
        assertEquals(1, activityOne.getMergeableElements().size());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        for (MergingReport.Record record : loggingRecords) {
            Logger.getAnonymousLogger().info(record.toString());
        }
    }

    /**
     * test attributes merging of elements with no conflict between attributes.
     */
    public void testMerging_withConflict()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:exported=\"false\">\n"
                + "       <action android:label=\"@string/foo\"/>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        refDocument.merge(otherDocument, mergingReportBuilder);
        assertEquals(MergingReport.Result.ERROR, mergingReportBuilder.build().getResult());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        for (MergingReport.Record record : loggingRecords) {
            Logger.getAnonymousLogger().info(record.toString());
        }
    }

    /**
     * test attributes merging of elements with no conflict between children.
     */
    public void testMerging_childrenDoNotConflict()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "       <meta-data android:name=\"fish\" android:value=\"@string/cod\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <meta-data android:name=\"bird\" android:value=\"@string/eagle\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activityOne = activityOptional.get();
        assertEquals(3, activityOne.getAttributes().size());
        // both metat-data should be present.
        assertEquals(2, activityOne.getMergeableElements().size());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        assertTrue(loggingRecords.isEmpty());
    }

    /**
     * test attributes merging of elements with some conflicts between children.
     */
    public void testMerging_childrenConflict()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "       <meta-data android:name=\"bird\" android:value=\"@string/hawk\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <meta-data android:name=\"bird\" android:value=\"@string/eagle\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertFalse(result.isPresent());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        assertEquals(1, loggingRecords.size());
        // check the error message complains about the right attribute.
        assertTrue(loggingRecords.get(0).toString().contains("meta-data#bird@value"));
    }

    /**
     * test attributes merging of elements with some conflicts between children.
     */
    public void testMerging_differentChildrenTypes()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\">\n"
                + "       <meta-data android:name=\"bird\" android:value=\"@string/hawk\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "       android:screenOrientation=\"landscape\">\n"
                + "       <intent-filter>\n"
                + "         <action android:name=\"android.appwidget.action.APPWIDGET_CONFIGURE\"/>"
                + "       </intent-filter>\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());
        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOptional.isPresent());
        XmlElement activityOne = activityOptional.get();
        assertEquals(3, activityOne.getAttributes().size());
        // both metat-data and intent-filter should be present.
        assertEquals(2, activityOne.getMergeableElements().size());
        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        assertTrue(loggingRecords.isEmpty());
    }

    /**
     * test attributes merging of elements with some conflicts between children.
     */
    public void testHigherPriorityDefaultValue_NoOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"dangerous\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.PERMISSION, "permissionOne");
        assertTrue(activityOptional.get()
                .getAttribute(XmlNode.fromXmlName("android:protectionLevel")).isPresent());
    }

    /**
     * test attributes merging of elements with some conflicts between children.
     */
    public void testLowerPriorityDefaultValue_NoOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\"\n"
                + "             android:protectionLevel=\"dangerous\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\">"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.PERMISSION, "permissionOne");
        assertTrue(activityOptional.get()
                .getAttribute(XmlNode.fromXmlName("android:protectionLevel")).isPresent());
    }

    /**
     * test attributes merging of elements with some conflicts between children.
     */
    public void testHigherPriorityDefaultValue_SameOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        Optional<XmlElement> activityOptional = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.PERMISSION, "permissionOne");
        assertTrue(activityOptional.get()
                .getAttribute(XmlNode.fromXmlName("android:protectionLevel")).isPresent());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemoveAll_severalTargets()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission tools:node=\"removeAll\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionTwo\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionThree\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        assertEquals(1, mergeableElements.size());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemoveAll_oneTarget()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission tools:node=\"removeAll\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        assertEquals(1, mergeableElements.size());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemoveAll_noTarget()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission tools:node=\"removeAll\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        assertEquals(1, mergeableElements.size());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testMergeOnlyAttributes()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" "
                + "         android:exported=\"true\""
                + "         tools:node=\"mergeOnlyAttributes\">\n"
                + "       <meta-data android:name=\"bird\" android:value=\"@string/hawk\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         android:screenOrientation=\"landscape\">\n"
                + "       <meta-data android:name=\"dog\" android:value=\"@string/dog\" />\n"
                + "    </activity>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

        Optional<XmlElement> activityOne = result.get().getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        assertEquals(1, activityOne.get().getMergeableElements().size());
        assertEquals(4, activityOne.get().getAttributes().size());

        // check that we kept the right child from the higher priority node.
        XmlNode.NodeName nodeName = XmlNode.fromXmlName(
                SdkConstants.ANDROID_NS_NAME_PREFIX + SdkConstants.ATTR_NAME);
        assertEquals("bird", activityOne.get().getMergeableElements().get(0)
                .getAttribute(nodeName).get().getValue());

        // check the records.
        Actions actionRecorder = mergingReportBuilder.getActionRecorder().build();
        assertEquals(3, actionRecorder.getNodeKeys().size());
        ImmutableList<NodeRecord> nodeRecords = actionRecorder.getNodeRecords(
                new XmlNode.NodeKey("activity#com.example.lib3.activityOne"));
        for (NodeRecord nodeRecord : nodeRecords) {
            if ("meta-data#dog".equals(nodeRecord.getTargetId().toString())) {
                assertEquals(Actions.ActionType.REJECTED,
                        nodeRecord.getActionType());
                return;
            }
        }
        fail("Did not find the meta-data rejection record");
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemove_withSelector()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "          tools:node=\"remove\""
                + "          tools:selector=\"com.example.lib1\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityOne = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib1\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityTwo = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib2\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionTwo\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        KeyResolver<String> keyResolver = (KeyResolver<String>) Mockito.mock(KeyResolver.class);
        when(keyResolver.resolve(any(String.class))).thenReturn("valid");
        XmlDocument refDocument = TestUtils.xmlDocumentFromString(keyResolver,
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument firstLibrary = TestUtils.xmlDocumentFromString(keyResolver,
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);
        XmlDocument secondLibrary = TestUtils.xmlDocumentFromString(keyResolver,
                TestUtils.sourceFile(getClass(), "lowerPriorityTwo"), lowerPriorityTwo);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());
        result = result.get().merge(secondLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        assertEquals(2, mergeableElements.size());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemoveAll_withSelector()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission"
                + "          tools:node=\"removeAll\"\n"
                + "          tools:selector=\"com.example.lib1\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityOne = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib1\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionTwo\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityTwo = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib2\">\n"
                + "\n"
                + "    <permission android:name=\"permissionThree\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionFour\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument firstLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);
        XmlDocument secondLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityTwo"), lowerPriorityTwo);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());
        result = result.get().merge(secondLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        assertEquals(3, mergeableElements.size());
        XmlNode.NodeName nodeName = XmlNode.fromXmlName("android:name");
        assertEquals("permissionThree",
                mergeableElements.get(1).getAttribute(nodeName).get().getValue());
        assertEquals("permissionFour",
                mergeableElements.get(2).getAttribute(nodeName).get().getValue());
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testInvalidSelector()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "          tools:node=\"remove\""
                + "          tools:selector=\"com.example.libXYZ\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityOne = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib1\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument firstLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertFalse(result.isPresent());

        ImmutableList<MergingReport.Record> loggingRecords = mergingReportBuilder.build()
                .getLoggingRecords();
        assertEquals(1, loggingRecords.size());
        assertEquals(MergingReport.Record.Severity.ERROR, loggingRecords.get(0).getSeverity());
        assertTrue(loggingRecords.get(0).getMessage().contains("tools:selector"));
    }

    /**
     * test tools:node="removeAll" with several target elements to be removed.
     */
    public void testRemoveAndRemoveAll_withReplace()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission"
                + "          android:name=\"permissionOne\"\n"
                + "          tools:node=\"remove\"\n"
                + "          tools:selector=\"com.example.lib1\">\n"
                + "    </permission>\n"
                + "    <permission"
                + "          tools:node=\"removeAll\"\n"
                + "          tools:selector=\"com.example.lib3\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionThree\""
                + "             android:protectionLevel=\"signature\""
                + "             tools:node=\"replace\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityOne = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib1\">\n"
                + "\n"
                + "    <permission android:name=\"permissionOne\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionTwo\""
                + "             android:protectionLevel=\"signature\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        String lowerPriorityTwo = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib2\">\n"
                + "\n"
                + "    <permission android:name=\"permissionThree\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "    <permission android:name=\"permissionFour\""
                + "             android:protectionLevel=\"normal\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument firstLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);
        XmlDocument secondLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityTwo"), lowerPriorityTwo);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());
        result = result.get().merge(secondLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();
        // I should have permissionTwo, permissionThree (replaced), permissionFour
        // + remove and removeAll not cleaned
        assertEquals(5, mergeableElements.size());
        XmlNode.NodeName nodeName = XmlNode.fromXmlName("android:name");
        for (int i = 0; i < 5; i++) {
            XmlElement xmlElement = mergeableElements.get(i);
            Optional<XmlAttribute> optionalName = xmlElement.getAttribute(nodeName);
            if (!optionalName.isPresent()) {
                continue;
            }
            String elementName = optionalName.get().getValue();
            if (elementName.equals("permissionThree")) {
                assertEquals("signature", xmlElement.getAttribute(
                        XmlNode.fromXmlName("android:protectionLevel")).get().getValue());
            } else {
                if (!elementName.equals("permissionOne") && !elementName.equals("permissionTwo")
                        && !elementName.equals("permissionFour")) {
                    fail("Unexepected permission " + elementName);
                }
            }

        }
    }

    public void testCompatibleScreens()
            throws ParserConfigurationException, SAXException, IOException {

        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <compatible-screens>\n"
                + "        <!-- all small size screens -->\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"ldpi\" />\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"mdpi\" />\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"xhdpi\" />\n"
                + "        <!-- all normal size screens -->\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"ldpi\" />\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"hdpi\" />\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"xhdpi\" />\n"
                + "    </compatible-screens>"
                + "\n"
                + "</manifest>";

        String lowerPriorityOne = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib1\">\n"
                + "\n"
                + "    <compatible-screens>\n"
                + "        <!-- all small size screens -->\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"ldpi\" />\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"mdpi\" />\n"
                + "        <screen android:screenSize=\"small\" android:screenDensity=\"hdpi\" />\n"
                + "        <!-- all normal size screens -->\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"mdpi\" />\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"hdpi\" />\n"
                + "        <screen android:screenSize=\"normal\" android:screenDensity=\"xhdpi\" />\n"
                + "    </compatible-screens>"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);

        XmlDocument firstLibrary = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriorityOne"), lowerPriorityOne);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(firstLibrary, mergingReportBuilder);
        assertTrue(result.isPresent());

        ImmutableList<XmlElement> mergeableElements = result.get().getRootNode()
                .getMergeableElements();

        assertEquals(1, mergeableElements.size());
        ImmutableList<XmlElement> mergedScreens = mergeableElements.get(0)
                .getMergeableElements();

        // we should have merged screens with no duplicated elements.
        assertEquals(8, mergedScreens.size());
    }
}
