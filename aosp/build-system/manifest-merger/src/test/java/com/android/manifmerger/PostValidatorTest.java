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
import com.android.utils.ILogger;
import com.google.common.base.Joiner;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.PostValidator} class.
 */
public class PostValidatorTest extends TestCase {

    @Mock
    ILogger mILogger;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    public void testIncorrectRemove()
            throws ParserConfigurationException, SAXException, IOException {

        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "        <activity android:name=\"activityOne\" tools:remove=\"exported\"/>\n"
                + "\n"
                + "</manifest>";

        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "\n"
                + "        <activity android:name=\"activityOne\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemoveMain"), main);

        XmlDocument libraryDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemoveLib"), library);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        mainDocument.merge(libraryDocument, mergingReportBuilder);

        PostValidator.validate(mainDocument, mergingReportBuilder);
        for (MergingReport.Record record : mergingReportBuilder.build().getLoggingRecords()) {
            if (record.getSeverity() == MergingReport.Record.Severity.WARNING
                    && record.toString().contains("PostValidatorTest#testIncorrectRemoveMain:8")) {
                return;
            }
        }
        fail("No reference to faulty PostValidatorTest#testIncorrectRemoveMain:8 found in: \n" +
                Joiner.on("\n    ").join(mergingReportBuilder.build().getLoggingRecords()));
    }

    public void testIncorrectReplace()
            throws ParserConfigurationException, SAXException, IOException {

        String main = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "        <activity android:name=\"activityOne\" "
                + "             android:exported=\"false\""
                + "             tools:replace=\"exported\"/>\n"
                + "\n"
                + "</manifest>";

        String library = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "        <activity android:name=\"activityOne\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument mainDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectReplaceMain"), main);

        XmlDocument libraryDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectReplaceLib"), library);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        mainDocument.merge(libraryDocument, mergingReportBuilder);

        PostValidator.validate(mainDocument, mergingReportBuilder);
        for (MergingReport.Record record : mergingReportBuilder.build().getLoggingRecords()) {
            if (record.getSeverity() == MergingReport.Record.Severity.WARNING
                    && record.toString().contains("PostValidatorTest#testIncorrectReplaceMain:8")) {
                return;
            }
        }
        fail("No reference to faulty PostValidatorTest#testIncorrectRemoveMain:8 found in: \n" +
                Joiner.on("\n    ").join(mergingReportBuilder.build().getLoggingRecords()));
    }

    public void testApplicationInvalidOrder()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure application element is last.
        Node lastChild = xmlDocument.getRootNode().getXml().getLastChild();
        while(lastChild.getNodeType() != Node.ELEMENT_NODE) {
            lastChild = lastChild.getPreviousSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) lastChild);
        assertEquals(ManifestModel.NodeTypes.APPLICATION, xmlElement.getType());
    }

    public void testApplicationInvalidOrder_withComments()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <!-- with comments ! -->"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        System.out.println(xmlDocument.prettyPrint());
        // ensure application element is last.
        Node lastChild = xmlDocument.getRootNode().getXml().getLastChild();
        while(lastChild.getNodeType() != Node.ELEMENT_NODE) {
            lastChild = lastChild.getPreviousSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) lastChild);
        assertEquals(ManifestModel.NodeTypes.APPLICATION, xmlElement.getType());
        // check the comment was also moved.
        assertEquals(Node.COMMENT_NODE, lastChild.getPreviousSibling().getNodeType());
    }

    public void testApplicationValidOrder()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationValidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure application element is last.
        Node lastChild = xmlDocument.getRootNode().getXml().getLastChild();
        while(lastChild.getNodeType() != Node.ELEMENT_NODE) {
            lastChild = lastChild.getPreviousSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) lastChild);
        assertEquals(ManifestModel.NodeTypes.APPLICATION, xmlElement.getType());
    }

    public void testUsesSdkInvalidOrder()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUsesSdkInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure uses-sdk element is first.
        Node firstChild = xmlDocument.getRootNode().getXml().getFirstChild();
        while(firstChild.getNodeType() != Node.ELEMENT_NODE) {
            firstChild = firstChild.getNextSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) firstChild);
        assertEquals(ManifestModel.NodeTypes.USES_SDK, xmlElement.getType());
    }

    public void testUsesSdkInvalidOrder_withComments()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "    <!-- with comments ! -->"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUsesSdkInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        System.out.println(xmlDocument.prettyPrint());
        // ensure uses-sdk element is first.
        Node firstChild = xmlDocument.getRootNode().getXml().getFirstChild();
        while(firstChild.getNodeType() != Node.ELEMENT_NODE) {
            firstChild = firstChild.getNextSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) firstChild);
        assertEquals(ManifestModel.NodeTypes.USES_SDK, xmlElement.getType());
        // check the comment was also moved.
        assertEquals(Node.COMMENT_NODE, firstChild.getPreviousSibling().getNodeType());
    }

    public void testUsesSdkValidOrder()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "    <activity android:name=\"activityOne\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testUsesSdkValidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure uses-sdk element is first.
        Node firstChild = xmlDocument.getRootNode().getXml().getFirstChild();
        while(firstChild.getNodeType() != Node.ELEMENT_NODE) {
            firstChild = firstChild.getNextSibling();
        }
        OrphanXmlElement xmlElement = new OrphanXmlElement((Element) firstChild);
        assertEquals(ManifestModel.NodeTypes.USES_SDK, xmlElement.getType());
    }

    public void testAndroidNamespacePresence()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-sdk minSdkVersion=\"14\"/>"
                + "\n"
                + "    <application android:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure application element is last.
        String attribute = xmlDocument.getRootNode().getXml().getAttribute("xmlns:android");
        assertEquals(SdkConstants.ANDROID_URI, attribute);
    }

    public void testAndroidNamespacePresence_differentPrefix()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    xmlns:A=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <uses-sdk A:minSdkVersion=\"14\"/>"
                + "\n"
                + "    <application A:label=\"@string/lib_name\" />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure application element is last.
        String attribute = xmlDocument.getRootNode().getXml().getAttribute("xmlns:A");
        assertEquals(SdkConstants.ANDROID_URI, attribute);
    }

    public void testAndroidNamespaceAbsence()
            throws ParserConfigurationException, SAXException, IOException {

        String input = ""
                + "<manifest\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <application />\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testApplicationInvalidOrder"), input);

        MergingReport.Builder mergingReportBuilder = new MergingReport.Builder(mILogger);
        PostValidator.validate(xmlDocument, mergingReportBuilder);
        // ensure application element is last.
        String attribute = xmlDocument.getRootNode().getXml().getAttribute("xmlns:android");
        assertEquals(SdkConstants.ANDROID_URI, attribute);
    }
}
