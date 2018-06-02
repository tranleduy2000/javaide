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
import com.android.sdklib.mock.MockLog;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.ManifestMergerTest} class
 */
public class ManifestMerger2SmallTest extends TestCase {

    @Mock
    ActionRecorder mActionRecorder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testValidationFailure()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {

        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "        <activity android:name=\"activityOne\" "
                + "             tools:replace=\"exported\"/>\n"
                + "\n"
                + "</manifest>";

        File tmpFile = inputAsFile("ManifestMerger2Test_testValidationFailure", input);
        assertTrue(tmpFile.exists());

        try {
            MergingReport mergingReport = ManifestMerger2.newMerger(tmpFile, mockLog,
                    ManifestMerger2.MergeType.APPLICATION).merge();
            assertEquals(MergingReport.Result.ERROR, mergingReport.getResult());
            // check the log complains about the incorrect "tools:replace"
            assertStringPresenceInLogRecords(mergingReport, "tools:replace");
            assertFalse(mergingReport.getMergedDocument().isPresent());
        } finally {
            assertTrue(tmpFile.delete());
        }
    }

    public void testToolsAnnotationRemoval()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {

        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" "
                + "         tools:replace=\"label\"/>\n"
                + "\n"
                + "</manifest>";

        File tmpFile = inputAsFile("testToolsAnnotationRemoval", input);
        assertTrue(tmpFile.exists());

        try {
            MergingReport mergingReport = ManifestMerger2.newMerger(tmpFile, mockLog,
                    ManifestMerger2.MergeType.APPLICATION)
                    .withFeatures(ManifestMerger2.Invoker.Feature.REMOVE_TOOLS_DECLARATIONS)
                    .merge();
            assertEquals(MergingReport.Result.WARNING, mergingReport.getResult());
            // ensure tools annotation removal.
            XmlDocument mergedDocument = mergingReport.getMergedDocument().get();
            Optional<XmlElement> applicationNode = mergedDocument
                    .getByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
            assertTrue(applicationNode.isPresent());
            String replaceAttribute = applicationNode.get().getXml().getAttributeNS(
                    SdkConstants.TOOLS_URI, "replace");
            assertTrue(Strings.isNullOrEmpty(replaceAttribute));
            System.out.println(mergedDocument.prettyPrint());
            mergedDocument.prettyPrint();
        } finally {
            assertTrue(tmpFile.delete());
        }
    }

    public void testToolsAnnotationPresence()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {

        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" "
                + "         tools:replace=\"label\"/>\n"
                + "\n"
                + "</manifest>";

        File tmpFile = inputAsFile("testToolsAnnotationRemoval", input);
        assertTrue(tmpFile.exists());

        try {
            MergingReport mergingReport = ManifestMerger2.newMerger(tmpFile, mockLog,
                    ManifestMerger2.MergeType.LIBRARY)
                    .merge();
            assertEquals(MergingReport.Result.WARNING, mergingReport.getResult());
            // ensure tools annotation removal.
            XmlDocument mergedDocument = mergingReport.getMergedDocument().get();
            Optional<XmlElement> applicationNode = mergedDocument
                    .getByTypeAndKey(ManifestModel.NodeTypes.APPLICATION, null);
            assertTrue(applicationNode.isPresent());
            String replaceAttribute = applicationNode.get().getXml().getAttributeNS(
                    SdkConstants.TOOLS_URI, "replace");
            assertEquals("label", replaceAttribute);
            System.out.println(mergedDocument.prettyPrint());
        } finally {
            assertTrue(tmpFile.delete());
        }
    }


    public void testPackageOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\""
                + "    package=\"com.foo.old\" >\n"
                + "    <activity android:name=\"activityOne\"/>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testPackageOverride#xml"), xml);

        ManifestMerger2.SystemProperty.PACKAGE.addTo(mActionRecorder, refDocument, "com.bar.new");
        // verify the package value was overriden.
        assertEquals("com.bar.new", refDocument.getRootNode().getXml().getAttribute("package"));
    }

    public void testMissingPackageOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"/>\n"
                + "</manifest>";

        XmlDocument refDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMissingPackageOverride#xml"), xml);

        ManifestMerger2.SystemProperty.PACKAGE.addTo(mActionRecorder, refDocument, "com.bar.new");
        // verify the package value was added.
        assertEquals("com.bar.new", refDocument.getRootNode().getXml().getAttribute("package"));
    }

    public void testAddingSystemProperties()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"activityOne\"/>\n"
                + "</manifest>";

        XmlDocument document = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(),
                        "testAddingSystemProperties#xml"), xml);

        ManifestMerger2.SystemProperty.VERSION_CODE.addTo(mActionRecorder, document, "101");
        assertEquals("101",
                document.getXml().getDocumentElement().getAttribute("android:versionCode"));

        ManifestMerger2.SystemProperty.VERSION_NAME.addTo(mActionRecorder, document, "1.0.1");
        assertEquals("1.0.1",
                document.getXml().getDocumentElement().getAttribute("android:versionName"));

        ManifestMerger2.SystemProperty.MIN_SDK_VERSION.addTo(mActionRecorder, document, "10");
        Element usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("10", usesSdk.getAttribute("android:minSdkVersion"));

        ManifestMerger2.SystemProperty.TARGET_SDK_VERSION.addTo(mActionRecorder, document, "14");
        usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("14", usesSdk.getAttribute("android:targetSdkVersion"));

        ManifestMerger2.SystemProperty.MAX_SDK_VERSION.addTo(mActionRecorder, document, "16");
        usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("16", usesSdk.getAttribute("android:maxSdkVersion"));
    }

    public void testAddingSystemProperties_withDifferentPrefix()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest\n"
                + "    xmlns:t=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity t:name=\"activityOne\"/>\n"
                + "</manifest>";

        XmlDocument document = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(),
                        "testAddingSystemProperties#xml"), xml
        );

        ManifestMerger2.SystemProperty.VERSION_CODE.addTo(mActionRecorder, document, "101");
        // using the non namespace aware API to make sure the prefix is the expected one.
        assertEquals("101",
                document.getXml().getDocumentElement().getAttribute("t:versionCode"));
    }

    public void testOverridingSystemProperties()
            throws ParserConfigurationException, SAXException, IOException {
        String xml = ""
                + "<manifest versionCode=\"34\" versionName=\"3.4\"\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <uses-sdk minSdkVersion=\"9\" targetSdkVersion=\".9\"/>\n"
                + "    <activity android:name=\"activityOne\"/>\n"
                + "</manifest>";

        XmlDocument document = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(),
                        "testAddingSystemProperties#xml"), xml);
        // check initial state.
        assertEquals("34", document.getXml().getDocumentElement().getAttribute("versionCode"));
        assertEquals("3.4", document.getXml().getDocumentElement().getAttribute("versionName"));
        Element usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("9", usesSdk.getAttribute("minSdkVersion"));
        assertEquals(".9", usesSdk.getAttribute("targetSdkVersion"));


        ManifestMerger2.SystemProperty.VERSION_CODE.addTo(mActionRecorder, document, "101");
        assertEquals("101",
                document.getXml().getDocumentElement().getAttribute("android:versionCode"));

        ManifestMerger2.SystemProperty.VERSION_NAME.addTo(mActionRecorder, document, "1.0.1");
        assertEquals("1.0.1",
                document.getXml().getDocumentElement().getAttribute("android:versionName"));

        ManifestMerger2.SystemProperty.MIN_SDK_VERSION.addTo(mActionRecorder, document, "10");
        usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("10", usesSdk.getAttribute("android:minSdkVersion"));

        ManifestMerger2.SystemProperty.TARGET_SDK_VERSION.addTo(mActionRecorder, document, "14");
        usesSdk = (Element) document.getXml().getElementsByTagName("uses-sdk").item(0);
        assertNotNull(usesSdk);
        assertEquals("14", usesSdk.getAttribute("android:targetSdkVersion"));
    }

    public void testPlaceholderSubstitution()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {
        String xml = ""
                + "<manifest package=\"foo\" versionCode=\"34\" versionName=\"3.4\"\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\".activityOne\" android:label=\"${labelName}\"/>\n"
                + "</manifest>";

        Map<String, String> placeholders = ImmutableMap.of("labelName", "injectedLabelName");
        MockLog mockLog = new MockLog();
        File inputFile = inputAsFile("testPlaceholderSubstitution", xml);
        try {
            MergingReport mergingReport = ManifestMerger2
                    .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                    .setPlaceHolderValues(placeholders)
                    .merge();

            assertTrue(mergingReport.getResult().isSuccess());
            assertTrue(mergingReport.getMergedDocument().isPresent());
            XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
            Optional<XmlElement> activityOne = xmlDocument
                    .getByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY, "foo.activityOne");
            assertTrue(activityOne.isPresent());
            Optional<XmlAttribute> attribute = activityOne.get()
                    .getAttribute(XmlNode.fromXmlName("android:label"));
            assertTrue(attribute.isPresent());
            assertEquals("injectedLabelName", attribute.get().getValue());
        } finally {
            inputFile.delete();
        }
    }

    public void testApplicationIdSubstitution()
            throws ManifestMerger2.MergeFailureException, IOException {
        String xml = ""
                + "<manifest package=\"foo\" versionCode=\"34\" versionName=\"3.4\"\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"${applicationId}.activityOne\"/>\n"
                + "</manifest>";

        MockLog mockLog = new MockLog();
        File inputFile = inputAsFile("testPlaceholderSubstitution", xml);
        try {
            MergingReport mergingReport = ManifestMerger2
                    .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                    .setOverride(ManifestMerger2.SystemProperty.PACKAGE, "bar")
                    .merge();

            assertTrue(mergingReport.getResult().isSuccess());
            assertTrue(mergingReport.getMergedDocument().isPresent());
            XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
            assertEquals("bar", xmlDocument.getPackageName());
            Optional<XmlElement> activityOne = xmlDocument
                    .getByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY, "bar.activityOne");
            assertTrue(activityOne.isPresent());
        } finally {
            inputFile.delete();
        }
    }

    public void testNoApplicationIdValueProvided()
            throws IOException, ManifestMerger2.MergeFailureException {
        String xml = ""
                + "<manifest package=\"foo\" versionCode=\"34\" versionName=\"3.4\"\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity android:name=\"${applicationId}.activityOne\"/>\n"
                + "</manifest>";

        MockLog mockLog = new MockLog();
        File inputFile = inputAsFile("testPlaceholderSubstitution", xml);
        try {
            MergingReport mergingReport = ManifestMerger2
                    .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                    .merge();

            assertTrue(mergingReport.getResult().isSuccess());
            assertTrue(mergingReport.getMergedDocument().isPresent());
            XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
            assertEquals("foo", xmlDocument.getPackageName());
            Optional<XmlElement> activityOne = xmlDocument
                    .getByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY, "foo.activityOne");
            assertTrue(activityOne.isPresent());
        } finally {
            inputFile.delete();
        }
    }

    public void testNoFqcnsExtraction()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {
        String xml = ""
                + "<manifest\n"
                + "    package=\"com.foo.example\""
                + "    xmlns:t=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity t:name=\"activityOne\"/>\n"
                + "    <activity t:name=\"com.foo.bar.example.activityTwo\"/>\n"
                + "    <activity t:name=\"com.foo.example.activityThree\"/>\n"
                + "    <application t:name=\".applicationOne\" "
                + "         t:backupAgent=\"com.foo.example.myBackupAgent\"/>\n"
                + "</manifest>";

        File inputFile = inputAsFile("testFcqnsExtraction", xml);

        MockLog mockLog = new MockLog();
        MergingReport mergingReport = ManifestMerger2
                .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                .merge();

        assertTrue(mergingReport.getResult().isSuccess());
        XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
        assertEquals("com.foo.example.activityOne",
                xmlDocument.getXml().getElementsByTagName("activity").item(0).getAttributes()
                        .item(0).getNodeValue());
        assertEquals("com.foo.bar.example.activityTwo",
                xmlDocument.getXml().getElementsByTagName("activity").item(1).getAttributes()
                        .item(0).getNodeValue());
        assertEquals("com.foo.example.activityThree",
                xmlDocument.getXml().getElementsByTagName("activity").item(2).getAttributes()
                        .item(0).getNodeValue());
        assertEquals("com.foo.example.applicationOne",
                xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes()
                        .getNamedItemNS("http://schemas.android.com/apk/res/android", "name")
                        .getNodeValue());
        assertEquals("com.foo.example.myBackupAgent",
                xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes()
                        .getNamedItemNS("http://schemas.android.com/apk/res/android", "backupAgent")
                        .getNodeValue());
    }

    public void testFqcnsExtraction()
            throws ParserConfigurationException, SAXException, IOException,
            ManifestMerger2.MergeFailureException {
        String xml = ""
                + "<manifest\n"
                + "    package=\"com.foo.example\""
                + "    xmlns:t=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity t:name=\"activityOne\"/>\n"
                + "    <activity t:name=\"com.foo.bar.example.activityTwo\"/>\n"
                + "    <activity t:name=\"com.foo.example.activityThree\"/>\n"
                + "    <application t:name=\".applicationOne\" "
                + "         t:backupAgent=\"com.foo.example.myBackupAgent\"/>\n"
                + "</manifest>";

        File inputFile = inputAsFile("testFcqnsExtraction", xml);

        MockLog mockLog = new MockLog();
        MergingReport mergingReport = ManifestMerger2
                .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                .withFeatures(ManifestMerger2.Invoker.Feature.EXTRACT_FQCNS)
                .merge();

        assertTrue(mergingReport.getResult().isSuccess());
        XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
        assertEquals(".activityOne",
                xmlDocument.getXml().getElementsByTagName("activity").item(0).getAttributes()
                        .item(0).getNodeValue());
        assertEquals("com.foo.bar.example.activityTwo",
                xmlDocument.getXml().getElementsByTagName("activity").item(1).getAttributes()
                        .item(0).getNodeValue());
        assertEquals(".activityThree",
                xmlDocument.getXml().getElementsByTagName("activity").item(2).getAttributes()
                        .item(0).getNodeValue());
        assertEquals(".applicationOne",
                xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes()
                        .getNamedItemNS("http://schemas.android.com/apk/res/android", "name")
                        .getNodeValue());
        assertEquals(".myBackupAgent",
                xmlDocument.getXml().getElementsByTagName("application").item(0).getAttributes()
                        .getNamedItemNS("http://schemas.android.com/apk/res/android", "backupAgent")
                        .getNodeValue());
    }

    public void testNoPlaceholderReplacement()
            throws IOException, ManifestMerger2.MergeFailureException {
        String xml = ""
                + "<manifest\n"
                + "    package=\"${applicationId}\""
                + "    xmlns:t=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <activity t:name=\"activityOne\"/>\n"
                + "    <application t:name=\".applicationOne\" "
                + "         t:backupAgent=\"com.foo.example.myBackupAgent\"/>\n"
                + "</manifest>";

        File inputFile = inputAsFile("testNoPlaceHolderReplacement", xml);

        MockLog mockLog = new MockLog();
        MergingReport mergingReport = ManifestMerger2
                .newMerger(inputFile, mockLog, ManifestMerger2.MergeType.APPLICATION)
                .withFeatures(ManifestMerger2.Invoker.Feature.NO_PLACEHOLDER_REPLACEMENT)
                .merge();

        assertTrue(mergingReport.getResult().isSuccess());
        XmlDocument xmlDocument = mergingReport.getMergedDocument().get();
        assertEquals("${applicationId}",
                xmlDocument.getXml().getElementsByTagName("manifest")
                        .item(0).getAttributes().getNamedItem("package").getNodeValue());
    }

    /**
     * Utility method to save a {@link String} XML into a file.
     */
    private static File inputAsFile(String testName, String input) throws IOException {
        File tmpFile = File.createTempFile(testName, ".xml");
        FileWriter fw = null;
        try {
            fw = new FileWriter(tmpFile);
            fw.append(input);
        } finally {
            if (fw != null) fw.close();
        }
        return tmpFile;
    }

    private static void assertStringPresenceInLogRecords(MergingReport mergingReport, String s) {
        for (MergingReport.Record record : mergingReport.getLoggingRecords()) {
            if (record.toString().contains(s)) {
                return;
            }
        }
        // failed, dump the records
        for (MergingReport.Record record : mergingReport.getLoggingRecords()) {
            Logger.getAnonymousLogger().info(record.toString());
        }
        fail("could not find " + s + " in logging records");
    }
}
