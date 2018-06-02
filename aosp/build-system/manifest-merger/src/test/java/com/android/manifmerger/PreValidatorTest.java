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

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Tests for the {@link com.android.manifmerger.PreValidator} class.
 */
public class PreValidatorTest extends TestCase {

    public void testCorrectInstructions()
            throws ParserConfigurationException, SAXException, IOException {

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
                + "             android:exported=\"false\""
                + "             tools:replace=\"exported\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemove"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.SUCCESS, validated);
        assertTrue(mockLog.toString().isEmpty());
    }

    public void testIncorrectReplace()
            throws ParserConfigurationException, SAXException, IOException {

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

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemove"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.ERROR, validated);
        // assert the error message complains about the bad instruction usage.
        assertStringPresenceInLogRecords(mergingReport, "tools:replace");
    }

    public void testIncorrectRemove()
            throws ParserConfigurationException, SAXException, IOException {

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
                + "             android:exported=\"true\""
                + "             tools:remove=\"exported\"/>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemove"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.ERROR, validated);
        // assert the error message complains about the bad instruction usage.
        assertStringPresenceInLogRecords(mergingReport, "tools:remove");
    }

    public void testIncorrectRemoveAll()
            throws ParserConfigurationException, SAXException, IOException {

        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission "
                + "             android:label=\"@string/lib_name\""
                + "             android:name=\"permissionOne\""
                + "             tools:node=\"removeAll\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemove"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.ERROR, validated);
        // assert the error message complains about the bad instruction usage.
        assertStringPresenceInLogRecords(mergingReport, "tools:node=\"removeAll\"");
    }


    public void testIncorrectSelector()
            throws ParserConfigurationException, SAXException, IOException {

        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "    <permission "
                + "             android:label=\"@string/lib_name\""
                + "             android:name=\"permissionOne\""
                + "             tools:node=\"replace\" tools:selector=\"foo\">\n"
                + "    </permission>\n"
                + "\n"
                + "</manifest>";

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testIncorrectRemove"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.ERROR, validated);
        // assert the error message complains about the bad instruction usage.
        assertStringPresenceInLogRecords(mergingReport, "tools:selector=\"foo\"");
    }

    public void testNoKeyElement()
            throws ParserConfigurationException, SAXException, IOException {

        MockLog mockLog = new MockLog();
        String input = ""
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

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testScreenMerging"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.SUCCESS, validated);
    }

    public void testMultipleIntentFilterWithSameKeyValue()
            throws ParserConfigurationException, SAXException, IOException {
        MockLog mockLog = new MockLog();
        String input = ""
                + "<manifest\n"
                + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
                + "    package=\"com.example.lib3\">\n"
                + "\n"
                + "     <application>\n"
                + "         <activity android:name=\"com.foo.bar.DeepLinkRouterActivity\" android:theme=\"@android:style/Theme.NoDisplay\">\n"
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

        XmlDocument xmlDocument = TestUtils.xmlDocumentFromString(
                TestUtils.sourceFile(getClass(), "testMultipleIntentFilterWithSameKeyValue"), input);

        MergingReport.Builder mergingReport = new MergingReport.Builder(mockLog);
        MergingReport.Result validated = PreValidator.validate(mergingReport, xmlDocument);
        assertEquals(MergingReport.Result.SUCCESS, validated);
    }

    private static void assertStringPresenceInLogRecords(MergingReport.Builder mergingReport, String s) {
        for (MergingReport.Record record : mergingReport.build().getLoggingRecords()) {
            if (record.toString().contains(s)) {
                return;
            }
        }
        // failed, dump the records
        for (MergingReport.Record record : mergingReport.build().getLoggingRecords()) {
            Logger.getAnonymousLogger().info(record.toString());
        }
        fail("could not find " + s + " in logging records");
    }
}
