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

import static org.mockito.Mockito.when;

import com.android.SdkConstants;
import com.android.utils.StdLogger;
import com.google.common.base.Optional;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for {@link com.android.manifmerger.XmlAttribute} class
 */
public class XmlAttributeTest extends TestCase {

    @Mock
    XmlDocument mXmlDocument;

    @Mock
    XmlElement mXmlElement;

    @Mock
    Attr mAttr;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        when(mAttr.getNamespaceURI()).thenReturn(SdkConstants.ANDROID_URI);
        when(mAttr.getPrefix()).thenReturn("android");
        when(mAttr.getLocalName()).thenReturn("name");

        when(mXmlElement.getType()).thenReturn(ManifestModel.NodeTypes.ACTIVITY);
        when(mXmlElement.getDocument()).thenReturn(mXmlDocument);
        when(mXmlDocument.getPackageNameForAttributeExpansion()).thenReturn("com.foo.bar");
    }

    public void testPackageSubstitution_noDot() {

        when(mAttr.getValue()).thenReturn("ActivityOne");
        // this will reset the package.
        assertNotNull(new XmlAttribute(mXmlElement, mAttr,
                AttributeModel.newModel("ActivityOne").setIsPackageDependent().build()));
        Mockito.verify(mAttr).setValue("com.foo.bar.ActivityOne");
    }

    public void testPackageSubstitution_withDot() {

        when(mAttr.getValue()).thenReturn(".ActivityOne");
        // this will reset the package.
        assertNotNull(new XmlAttribute(mXmlElement, mAttr,
                AttributeModel.newModel("ActivityOne").setIsPackageDependent().build()));
        Mockito.verify(mAttr).setValue("com.foo.bar.ActivityOne");
    }

    public void testNoPackageSubstitution() {

        when(mAttr.getValue()).thenReturn("com.foo.foo2.ActivityOne");
        // this will NOT reset the package.
        assertNotNull(new XmlAttribute(mXmlElement, mAttr,
                AttributeModel.newModel("ActivityOne").setIsPackageDependent().build()));
        Mockito.verify(mAttr).getValue();
        Mockito.verifyNoMoreInteractions(mAttr);
    }

    public void testAttributeRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:remove=\"theme\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:theme=\"@oldtheme\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlLibraryFromString(
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

        // verify that only android:name and tools:remove remains in the result.
        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "remove")).isPresent());

        Actions actions = mergingReportBuilder.getActionRecorder().build();
        // check the recorded actions.
        List<Actions.AttributeRecord> attributeRecords =
                actions.getAttributeRecords(activityOne.get().getId(),
                        XmlNode.fromXmlName("android:theme"));
        assertNotNull(attributeRecords);
        assertEquals(1, attributeRecords.size());
        Actions.AttributeRecord attributeRecord = attributeRecords.get(0);
        assertEquals(Actions.ActionType.REJECTED, attributeRecord.getActionType());
        assertEquals(AttributeOperationType.REMOVE, attributeRecord.getOperationType());
        assertEquals(6, attributeRecord.getActionLocation().getPosition().getStartLine());
    }

    public void testNamespaceAwareAttributeRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:remove=\"android:theme\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:theme=\"@oldtheme\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlLibraryFromString(
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

        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "remove")).isPresent());
    }

    public void testMultipleAttributesRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:remove=\"theme, exported\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:exported=\"true\"\n"
                + "         android:theme=\"@oldtheme\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlLibraryFromString(
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

        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "remove")).isPresent());
    }

    public void testDeepAttributeRemoval()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:remove=\"theme, exported\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:screenOrientation=\"landscape\"\n"
                + "         android:theme=\"@oldtheme\"/>\n"
                + "\n"
                + "</manifest>";

        String evenLowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                + "         android:theme=\"@oldtheme\"\n"
                + "         android:exported=\"true\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument highPriority = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "highPriority"), higherPriority);
        XmlDocument lowPriority = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowPriority"), lowerPriority);
        XmlDocument lowestPriority = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowestPriority"), evenLowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = highPriority.merge(lowPriority, mergingReportBuilder);
        assertTrue(result.isPresent());
        XmlDocument resultDocument = result.get();
        result = resultDocument.merge(lowestPriority, mergingReportBuilder);
        assertTrue(result.isPresent());
        resultDocument = result.get();

        Optional<XmlElement> activityOne = resultDocument.getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(3, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:screenOrientation")).isPresent());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "remove")).isPresent());

        Actions actions = mergingReportBuilder.getActionRecorder().build();
        // check the recorded actions.
        List<Actions.AttributeRecord> attributeRecords =
                actions.getAttributeRecords(activityOne.get().getId(),
                        XmlNode.fromXmlName("android:theme"));
        assertNotNull(attributeRecords);

        // theme was removed twice...
        assertEquals(2, attributeRecords.size());
        Actions.AttributeRecord attributeRecord = attributeRecords.get(0);
        assertEquals(Actions.ActionType.REJECTED, attributeRecord.getActionType());
        assertEquals(AttributeOperationType.REMOVE, attributeRecord.getOperationType());
        assertEquals("XmlAttributeTest#lowPriority",
                attributeRecord.getActionLocation().getFile().print(true));
        assertEquals(7, attributeRecord.getActionLocation().getPosition().getStartLine());

        attributeRecord = attributeRecords.get(1);
        assertEquals(Actions.ActionType.REJECTED, attributeRecord.getActionType());
        assertEquals(AttributeOperationType.REMOVE, attributeRecord.getOperationType());
        assertEquals("XmlAttributeTest#lowestPriority",
                attributeRecord.getActionLocation().getFile().print(true));
        assertEquals(6, attributeRecord.getActionLocation().getPosition().getStartLine());
    }

    public void testDefaultValueIllegalOverriding()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                // implicit required=true attribute present.
                + "    <uses-library android:name=\"libraryOne\"/>\n"
                + "    <permission android:name=\"permissionOne\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-library android:name=\"libraryOne\" android:required=\"false\"/>\n"
                + "    <permission android:name=\"permissionOne\" "
                + "          android:protectionLevel=\"dangerous\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "higherPriority"), higherPriority);
        XmlDocument otherDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "lowerPriority"), lowerPriority);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(
                new StdLogger(StdLogger.Level.VERBOSE));
        Optional<XmlDocument> result = refDocument.merge(otherDocument, mergingReportBuilder);
        assertTrue(result.isPresent());

    }

    public void testToolsAttributeMerging()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:remove=\"theme,exported\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                +"          tools:remove=\"bar\"/>\n"
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

        Optional<XmlElement> activityOne = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        // verify that only android:name and tools:remove remains in the result.
        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        Optional<XmlAttribute> toolsRemove = activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "remove"));
        assertTrue(toolsRemove.isPresent());
        assertEquals("theme,exported,bar", toolsRemove.get().getValue());

    }

    public void testToolsNodeAttributeNotMerging()
            throws ParserConfigurationException, SAXException, IOException {
        String higherPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"\n"
                + "         tools:node=\"replace\"/>\n"
                + "\n"
                + "</manifest>";

        String lowerPriority = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" \n"
                +"          tools:node=\"remove\"/>\n"
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

        Optional<XmlElement> activityOne = result.get().getRootNode()
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        // verify that only android:name and tools:remove remains in the result.
        List<XmlAttribute> attributes = activityOne.get().getAttributes();
        assertEquals(2, attributes.size());
        assertTrue(activityOne.get().getAttribute(
                XmlNode.fromXmlName("android:name")).isPresent());
        Optional<XmlAttribute> toolsRemove = activityOne.get().getAttribute(
                XmlNode.fromNSName(SdkConstants.TOOLS_URI, "tools", "node"));
        assertTrue(toolsRemove.isPresent());
        assertEquals("replace", toolsRemove.get().getValue());

    }
}
