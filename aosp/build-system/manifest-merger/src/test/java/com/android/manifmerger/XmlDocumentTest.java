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

import static com.android.manifmerger.MergingReport.Record.Severity.ERROR;

import com.android.SdkConstants;
import com.android.sdklib.SdkVersionInfo;
import com.android.sdklib.mock.MockLog;
import com.android.utils.ILogger;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for {@link com.android.manifmerger.XmlDocument}
 */
public class XmlDocumentTest extends TestCase {

    @Mock ILogger mLogger;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testMergeableElementsIdentification()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMergeableElementsIdentification()"), input);
        ImmutableList<XmlElement> mergeableElements = xmlDocument.getRootNode().getMergeableElements();
        assertEquals(2, mergeableElements.size());
    }

    public void testNamespaceEnabledElements()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <android:application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <android:activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMergeableElementsIdentification()"), input);
        ImmutableList<XmlElement> mergeableElements = xmlDocument.getRootNode().getMergeableElements();
        assertEquals(2, mergeableElements.size());
        assertEquals(ManifestModel.NodeTypes.APPLICATION, mergeableElements.get(0).getType());
        assertEquals(ManifestModel.NodeTypes.ACTIVITY, mergeableElements.get(1).getType());
    }

    public void testMultipleNamespaceEnabledElements()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<android:manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <android:application android:label=\"@string/lib_name\" \n"
                + "         tools:node=\"replace\" />\n"
                + "    <acme:custom-tag android:label=\"@string/lib_name\" />\n"
                + "    <acme:application acme:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</android:manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMergeableElementsIdentification()"), input);
        ImmutableList<XmlElement> mergeableElements = xmlDocument.getRootNode().getMergeableElements();
        assertEquals(3, mergeableElements.size());
        assertEquals(ManifestModel.NodeTypes.APPLICATION, mergeableElements.get(0).getType());
        assertEquals(ManifestModel.NodeTypes.CUSTOM, mergeableElements.get(1).getType());
        assertEquals(ManifestModel.NodeTypes.CUSTOM, mergeableElements.get(2).getType());

    }

    public void testGetXmlNodeByTypeAndKey()
            throws ParserConfigurationException, SAXException, IOException {
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testGetXmlNodeByTypeAndKey()"), input);
        assertTrue(xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "com.example.lib3.activityOne").isPresent());
        assertFalse(xmlDocument.getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.ACTIVITY, "noName").isPresent());
    }

    public void testSimpleMerge()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testSimpleMerge()"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "testSimpleMerge()"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        Logger.getAnonymousLogger().info(mergedDocument.get().prettyPrint());
        assertTrue(mergedDocument.get().getRootNode().getNodeByTypeAndKey(
                ManifestModel.NodeTypes.APPLICATION, null).isPresent());
        Optional<XmlElement> activityOne = mergedDocument.get()
                .getRootNode().getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());
    }

    public void testDiff1()
            throws Exception {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff1()"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "testDiff1()"), library);
        assertTrue(mainDocument.compareTo(libraryDocument).isPresent());
    }

    public void testDiff2()
            throws Exception {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff2()"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "testDiff2()"), library);
        assertFalse(mainDocument.compareTo(libraryDocument).isPresent());
    }

    public void testDiff3()
            throws Exception {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <!-- some comment that should be ignored -->\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <!-- some comment that should also be ignored -->\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testDiff3()"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "testDiff3()"), library);
        assertFalse(mainDocument.compareTo(libraryDocument).isPresent());
    }

    public void testWriting() throws ParserConfigurationException, SAXException, IOException {
        String input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<manifest xmlns:x=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:y=\"http://schemas.android.com/apk/res/android/tools\"\n"
                + "    package=\"com.example.lib3\" >\n"
                + "\n"
                + "    <application\n"
                + "        x:label=\"@string/lib_name\"\n"
                + "        y:node=\"replace\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testWriting()"), input);
        assertEquals(input, xmlDocument.prettyPrint());
    }

    public void testCustomElements()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <fantasy android:name=\"fantasyOne\" \n"
                + "         no-ns-attribute=\"no-ns\" >\n"
                + "    </fantasy>\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <fantasy android:name=\"fantasyTwo\" \n"
                + "         no-ns-attribute=\"no-ns\" >\n"
                + "    </fantasy>\n"
                + "    <acme:another acme:name=\"anotherOne\" \n"
                + "         acme:ns-attribute=\"ns-value\" >\n"
                + "        <some-child acme:child-attr=\"foo\" /> \n"
                + "    </acme:another>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlElement rootNode = mergedDocument.get().getRootNode();
        assertTrue(rootNode.getNodeByTypeAndKey(
                ManifestModel.NodeTypes.APPLICATION, null).isPresent());
        Optional<XmlElement> activityOne = rootNode
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        boolean foundFantasyOne = false;
        boolean foundFantasyTwo = false;
        boolean foundAnother = false;
        NodeList childNodes = rootNode.getXml().getChildNodes();
        for (int i =0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeName().equals("fantasy")) {
                String name = ((Element) item).getAttributeNS(SdkConstants.ANDROID_URI, "name");
                if (name.equals("fantasyOne"))
                    foundFantasyOne = true;
                if (name.equals("fantasyTwo"))
                    foundFantasyTwo = true;
            }
            if (item.getNodeName().equals("acme:another")) {
                foundAnother = true;
            }
        }
        assertTrue(foundAnother);
        assertTrue(foundFantasyOne);
        assertTrue(foundFantasyTwo);

        Element validated = validate(mergedDocument.get().prettyPrint());

        // make sure acme: namespace is defined.
        Node namedItem = validated.getAttributes().getNamedItem("xmlns:acme");
        assertEquals(namedItem.getNodeValue(), "http://acme.org/schemas");
    }

    public void testMultipleCustomElements()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    xmlns:acme2=\"http://acme2.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <acme:another acme:name=\"anotherOne\" \n"
                + "         acme:ns-attribute=\"ns-value\" >\n"
                + "        <acme:some-child acme:child-attr=\"foo\" /> \n"
                + "    </acme:another>\n"
                + "    <acme:another2 acme:name=\"anotherOne\" \n"
                + "         acme:ns-attribute=\"ns-value\" >\n"
                + "        <acme:some-child acme:child-attr=\"foo\" /> \n"
                + "    </acme:another2>\n"
                + "    <acme2:another acme2:name=\"anotherOne\" \n"
                + "         acme2:ns-attribute=\"ns-value\" >\n"
                + "        <acme2:some-child acme2:child-attr=\"foo\" /> \n"
                + "    </acme2:another>\n"
                + "    <acme2:another2 acme2:name=\"anotherOne\" \n"
                + "         acme2:ns-attribute=\"ns-value\" >\n"
                + "        <acme2:some-child acme2:child-attr=\"foo\" /> \n"
                + "    </acme2:another2>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlElement rootNode = mergedDocument.get().getRootNode();
        assertTrue(rootNode.getNodeByTypeAndKey(
                ManifestModel.NodeTypes.APPLICATION, null).isPresent());
        Optional<XmlElement> activityOne = rootNode
                .getNodeByTypeAndKey(ManifestModel.NodeTypes.ACTIVITY,
                        "com.example.lib3.activityOne");
        assertTrue(activityOne.isPresent());

        Element validated = validate(mergedDocument.get().prettyPrint());

        // make sure acme: and acme2: namespaces are defined.
        Node namedItem = validated.getAttributes().getNamedItem("xmlns:acme");
        assertEquals(namedItem.getNodeValue(), "http://acme.org/schemas");
        namedItem = validated.getAttributes().getNamedItem("xmlns:acme2");
        assertEquals(namedItem.getNodeValue(), "http://acme2.org/schemas");

        // check we have all the children we expected.
        int elementsNumber = 0;
        for (int i = 0; i < validated.getChildNodes().getLength(); i++) {
            Node item = validated.getChildNodes().item(i);
            if (item instanceof Element) {
                elementsNumber++;
            }
        }
        assertEquals(6, elementsNumber);
    }

    public void testIllegalLibraryVersionMerge()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"4\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
        MergingReport mergingReport = mergingReportBuilder.build();
        assertEquals(1, mergingReport.getLoggingRecords().size());
        assertTrue(mergingReport.getLoggingRecords().get(0).getSeverity() == ERROR);
        assertTrue(mergingReport.getLoggingRecords().get(0).toString().contains(
                "uses-sdk:minSdkVersion 4"));
    }

    public void testNoMergingNecessary()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testNoUsesSdkPresence()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testGlEsVersionFromFlavor()
            throws ParserConfigurationException, SAXException, IOException {
        String flavor = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-feature\n"
                + "        android:name=\"android.hardware.camera\"\n"
                + "        android:glEsVersion=\"0x00020000\"\n"
                + "        android:required=\"true\" />"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument flavorDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "flavor"), flavor);
        XmlDocument mainDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                flavorDocument.merge(mainDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        System.out.println(mergedDocument.get().prettyPrint());
        XmlDocument xmlDocument = mergedDocument.get();
        Optional<XmlElement> usesFeature = xmlDocument
                .getByTypeAndKey(ManifestModel.NodeTypes.USES_FEATURE,
                        "android.hardware.camera");
        assertTrue(usesFeature.isPresent());
        Optional<XmlAttribute> glEsVersion = usesFeature.get()
                .getAttribute(XmlNode.fromXmlName("android:glEsVersion"));
        assertTrue(glEsVersion.isPresent());
        assertEquals("0x00020000", glEsVersion.get().getValue());
    }


    public void testNoUsesSdkPresenceInMain()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
    }

    public void testNoUsesSdkPresenceInLibrary()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testLibraryVersion3Merge()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());

        // check records.
        Actions actions = mergingReportBuilder.getActionRecorder().build();
        XmlElement xmlElement = xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").get();
        ImmutableList<Actions.NodeRecord> nodeRecords = actions
                .getNodeRecords(XmlNode.NodeKey.fromXml(xmlElement.getXml()));
        assertEquals(1, nodeRecords.size());
        assertEquals(nodeRecords.iterator().next().mReason,
                "com.example.lib3 has a targetSdkVersion < 4");
    }

    public void testLibraryVersion10Merge()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"10\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testLibraryVersion3MergeWithContacts()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"

                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"3\"/>\n"
                + "    <uses-permission android:name=\"android.permission.READ_CONTACTS\"/>\n"
                + "    <uses-permission android:name=\"android.permission.WRITE_CONTACTS\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());

        // check records.
        Actions actions = mergingReportBuilder.getActionRecorder().build();
        XmlElement xmlElement = xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").get();
        ImmutableList<Actions.NodeRecord> nodeRecords = actions
                .getNodeRecords(XmlNode.NodeKey.fromXml(xmlElement.getXml()));
        assertEquals(1, nodeRecords.size());
        assertEquals(nodeRecords.iterator().next().mReason,
                "com.example.lib3 has a targetSdkVersion < 4");

        xmlElement = xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").get();
        nodeRecords = actions.getNodeRecords(XmlNode.NodeKey.fromXml(xmlElement.getXml()));
        assertEquals(1, nodeRecords.size());
        assertEquals(nodeRecords.iterator().next().mReason,
                "com.example.lib3 has targetSdkVersion < 16 and requested READ_CONTACTS");
    }

    public void testLibraryVersion3MergeWithoutContacts()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "    <uses-permission android:name=\"android.permission.READ_CONTACTS\"/>\n"
                + "    <uses-permission android:name=\"android.permission.WRITE_CONTACTS\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testLibraryVersion10MergeWithContacts()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"10\"/>\n"
                + "    <uses-permission android:name=\"android.permission.READ_CONTACTS\"/>\n"
                + "    <uses-permission android:name=\"android.permission.WRITE_CONTACTS\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }

    public void testLibraryAtVersion10MergeWithContactsInMain()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "    <uses-permission android:name=\"android.permission.READ_CONTACTS\"/>\n"
                + "    <uses-permission android:name=\"android.permission.WRITE_CONTACTS\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"10\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        // no permissions should be added since the library did not add the permissions itself.
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());
    }


    public void testUsesSdkAbsenceInOverlay()
            throws ParserConfigurationException, SAXException, IOException {
        String overlay = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-permission android:name=\"android.permission.READ_CONTACTS\"/>\n"
                + "    <uses-permission android:name=\"android.permission.WRITE_CONTACTS\"/>\n"
                + "\n"
                + "</manifest>";
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-sdk android:minSdkVersion=\"15\"/>\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "overlay"),
                overlay,
                XmlDocument.Type.OVERLAY,
                Optional.of("com.example.lib3"));
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        MockLog mockLog = new MockLog();
        mergingReportBuilder.build().log(mockLog);
        System.out.println(mockLog.toString());
        assertTrue(mergedDocument.isPresent());
    }


    /**
     * test illegal importation of a preview library (using the minSdk attribute) in a released
     * application.
     */
    public void testLibraryAtPreviewInOldApp_usingMinSdk()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
        MergingReport mergingReport = mergingReportBuilder.build();
        ImmutableList<MergingReport.Record> loggingRecords = mergingReport.getLoggingRecords();
        assertTrue(mergingReport.getResult().isError());
        assertEquals(1, loggingRecords.size());
        assertTrue(loggingRecords.get(0).getMessage().contains("XYZ"));
    }

    /**
     * test illegal importation of a preview library (using the minSdk attribute) in a released
     * application.
     */
    public void testLibraryAtPreviewInNewerApp_usingMinSdk()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\""
                + (SdkVersionInfo.HIGHEST_KNOWN_API + 2) // fantasy version in the far future.
                + "\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
        MergingReport mergingReport = mergingReportBuilder.build();
        ImmutableList<MergingReport.Record> loggingRecords = mergingReport.getLoggingRecords();
        assertTrue(mergingReport.getResult().isError());
        assertEquals(1, loggingRecords.size());
        assertTrue(loggingRecords.get(0).getMessage().contains("XYZ"));
    }


    /**
     * test illegal importation of a preview library (using the targetSdk attribute) in a released
     * application.
     */
    public void testLibraryAtPreviewInOldApp_usingTargetSdk()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
        MergingReport mergingReport = mergingReportBuilder.build();
        ImmutableList<MergingReport.Record> loggingRecords = mergingReport.getLoggingRecords();
        assertTrue(mergingReport.getResult().isError());
        assertEquals(1, loggingRecords.size());
        assertTrue(loggingRecords.get(0).getMessage().contains("XYZ"));
    }

    /**
     * test legal importation of a released library (using the minSdk attribute) into a preview
     * application.
     */
    public void testLibraryAtReleaseAgainstAppInPreview_usingMinSdk()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"19\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        // make sure the resulting min version is "XYZ".
        Optional<XmlElement> usesSdk = mergedDocument.get()
                .getByTypeAndKey(ManifestModel.NodeTypes.USES_SDK, null);
        Optional<XmlAttribute> attribute = usesSdk.get()
                .getAttribute(XmlNode.fromXmlName("android:minSdkVersion"));
        assertTrue(attribute.isPresent());
        assertEquals("XYZ", attribute.get().getValue());
    }

    /**
     * test legal importation of a released library (using the minSdk attribute) into a preview
     * application.
     */
    public void testLibraryAtReleaseAgainstAppInPreview_usingTargetSdk()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"14\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        // make sure the resulting target version is "XYZ".
        Optional<XmlElement> usesSdk = mergedDocument.get()
                .getByTypeAndKey(ManifestModel.NodeTypes.USES_SDK, null);
        Optional<XmlAttribute> attribute = usesSdk.get()
                .getAttribute(XmlNode.fromXmlName("android:targetSdkVersion"));
        assertTrue(attribute.isPresent());
        assertEquals("XYZ", attribute.get().getValue());
    }

    /**
     * test illegal importation of a more recent released library (using the minSdk attribute) into
     * an older preview application.
     */
    public void testLibraryMoreRecentThanCodeName()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:minSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:minSdkVersion=\""
                + (SdkVersionInfo.HIGHEST_KNOWN_API + 2) // fantasy version in the far future.
                + "\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertFalse(mergedDocument.isPresent());
    }

    /**
     * Test that implicit elements are added correctly when importing an old library into a preview
     * application.
     */
    public void testLibraryVersion3MergeInPreviewApp()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "    <uses-sdk android:targetSdkVersion=\"XYZ\"/>\n"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_EXTERNAL_STORAGE").isPresent());
        assertTrue(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_PHONE_STATE").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.READ_CALL_LOG").isPresent());
        assertFalse(xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_CALL_LOG").isPresent());

        // check records.
        Actions actions = mergingReportBuilder.getActionRecorder().build();
        XmlElement xmlElement = xmlDocument.getByTypeAndKey(ManifestModel.NodeTypes.USES_PERMISSION,
                "android.permission.WRITE_EXTERNAL_STORAGE").get();
        ImmutableList<Actions.NodeRecord> nodeRecords = actions
                .getNodeRecords(XmlNode.NodeKey.fromXml(xmlElement.getXml()));
        assertEquals(1, nodeRecords.size());
        assertEquals(nodeRecords.iterator().next().mReason,
                "com.example.lib3 has a targetSdkVersion < 4");
    }

    /**
     * Test that multiple intent-filters with the same key and no override are not merged or
     * discarded.
     */
    public void testMultipleIntentFilter_sameKey_noLibraryDeclaration()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"myspecialdeeplinkscheme\"/>\n"
                + "                 <data android:host=\"home\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\" />\n"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);
        assertEquals(2, allIntentFilters.size());
        assertEquals(allIntentFilters.get(0).getId(), allIntentFilters.get(1).getId());
    }

    /**
     * Test that multiple intent-filters with the same key and no override are not merged or
     * discarded.
     */
    public void testMultipleIntentFilter_sameKey_noOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"myspecialdeeplinkscheme\"/>\n"
                + "                 <data android:host=\"home\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.SEARCH\" />\n"
                + "             </intent-filter>\n"
                + "         </activity>"
                + "     </application>"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);
        assertEquals(3, allIntentFilters.size());
    }

    /**
     * Test that multiple intent-filters with the same key and no override are not merged or
     * discarded.
     */
    public void testMultipleIntentFilter_sameKey_sameOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"myspecialdeeplinkscheme\"/>\n"
                + "                 <data android:host=\"home\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.SEARCH\" />\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
            + "                 </intent-filter>\n"
                + "         </activity>"
                + "    </application>"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);

        // the second intent-filter of the library should not have been merged in.
        assertEquals(3, allIntentFilters.size());
    }

    /**
     * Test that multiple intent-filters with the same key and no override are not merged or
     * discarded.
     */
    public void testMultipleIntentFilter_sameKey_differentOverride()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"myspecialdeeplinkscheme\"/>\n"
                + "                 <data android:host=\"home\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.SEARCH\" />\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.bar.com\"/>\n"
                + "                 </intent-filter>\n"
                + "         </activity>"
                + "    </application>"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);
        // all intent-filters should have been merged.
        assertEquals(4, allIntentFilters.size());
    }

    /**
     * Test that multiple intent-filters with the same key and no override are not merged or
     * discarded.
     */
    public void testMultipleIntentFilter_sameKey_removal()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter tools:node=\"remove\">\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.SEARCH\" />\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.bar.com\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>"
                + "    </application>"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);
        // the cleaner is not removed the "remove" node so we should have 2.
        assertEquals(2, allIntentFilters.size());
    }

    public void testMultipleIntentFilter_sameKey_removalAll()
            throws ParserConfigurationException, SAXException, IOException {
        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter tools:node=\"removeAll\"/>\n"
                + "         </activity>\n"
                + "     </application>"
                + "\n"
                + "</manifest>";
        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:acme=\"http://acme.org/schemas\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application>\n"
                + "         <activity android:name=\"activityOne\">\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.SEARCH\" />\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.bar.com\"/>\n"
                + "             </intent-filter>\n"
                + "             <intent-filter>\n"
                + "                 <action android:name=\"android.intent.action.VIEW\"/>\n"
                + "                 <category android:name=\"android.intent.category.DEFAULT\"/>\n"
                + "                 <category android:name=\"android.intent.category.BROWSABLE\"/>\n"
                + "                 <data android:scheme=\"https\"/>\n"
                + "                 <data android:host=\"www.foo.com\"/>\n"
                + "             </intent-filter>\n"
                + "         </activity>"
                + "    </application>"
                + "    <uses-sdk android:targetSdkVersion=\"3\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "main"), main);
        XmlDocument libraryDocument = TestUtils.xmlLibraryFromString(
                TestUtils.sourceFile(getClass(), "library"), library);
        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mLogger);
        Optional<XmlDocument> mergedDocument =
                mainDocument.merge(libraryDocument, mergingReportBuilder);

        assertTrue(mergedDocument.isPresent());
        XmlDocument xmlDocument = mergedDocument.get();
        List<XmlElement> allIntentFilters = getAllElementsOfType(xmlDocument,
                ManifestModel.NodeTypes.INTENT_FILTER);
        // since the cleaner has not run, there is one intent-filter with the removeAll annotation.
        assertEquals(1, allIntentFilters.size());
    }

    private static List<XmlElement> getAllElementsOfType(
            XmlDocument xmlDocument,
            ManifestModel.NodeTypes nodeType) {
        ImmutableList.Builder<XmlElement> listBuilder = ImmutableList.builder();
        getAllElementsOfType(xmlDocument.getRootNode(), nodeType, listBuilder);
        return listBuilder.build();
    }

    private static void getAllElementsOfType(XmlElement element,
            ManifestModel.NodeTypes nodeType,
            ImmutableList.Builder<XmlElement> allElementsBuilder) {

        for (XmlElement xmlElement : element.getMergeableElements()) {
            if (xmlElement.isA(nodeType)) {
                allElementsBuilder.add(xmlElement);
            } else {
                getAllElementsOfType(xmlElement, nodeType, allElementsBuilder);
            }
        }
    }

    private static Element validate(String xml)
            throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        dBuilder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {

            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                fail(e.getMessage());
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                fail(e.getMessage());
            }
        });
        Document validated = dBuilder.parse(
                new InputSource(new StringReader(xml)));
        return (Element) validated.getChildNodes().item(0);
    }
}
