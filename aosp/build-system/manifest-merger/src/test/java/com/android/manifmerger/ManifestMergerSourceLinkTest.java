/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.manifmerger;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.manifmerger.IMergerLog.FileAndLine;
import com.android.sdklib.mock.MockLog;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import java.io.File;

public class ManifestMergerSourceLinkTest extends TestCase {
    public void testSourceLinks() throws Exception {
        MockLog log = new MockLog();
        IMergerLog mergerLog = MergerLog.wrapSdkLog(log);
        ManifestMerger merger = new ManifestMerger(mergerLog, new ICallback() {
            @Override
            public int queryCodenameApiLevel(@NonNull String codename) {
                if ("ApiCodename1".equals(codename)) {
                    return 1;
                } else if ("ApiCodename10".equals(codename)) {
                    return 10;
                }
                return ICallback.UNKNOWN_CODENAME;
            }
        });
        merger.setInsertSourceMarkers(true);

        Document mainDoc = MergerXmlUtils.parseDocument(""
            + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
            + "    package=\"com.example.app1\"\n"
            + "    android:versionCode=\"100\"\n"
            + "    android:versionName=\"1.0.0\">\n"
            + "\n"
            + "    <uses-sdk android:minSdkVersion=\"3\" android:targetSdkVersion=\"11\"/>\n"
            + "\n"
            + "    <application\n"
            + "            android:name=\"TheApp\"\n"
            + "            android:backupAgent=\".MyBackupAgent\" >\n"
            + "        <activity android:name=\".MainActivity\" />\n"
            + "        <receiver android:name=\"AppReceiver\" />\n"
            + "        <activity android:name=\"com.example.lib2.LibActivity\" />\n"
            + "\n"
            + "        <!-- This key is defined in the main application. -->\n"
            + "        <meta-data\n"
            + "            android:name=\"name.for.yet.another.api.key\"\n"
            + "            android:value=\"your_yet_another_api_key\"/>\n"
            + "\n"
            + "        <!-- Merged elements will be appended here at the end. -->\n"
            + "    </application>\n"
            + "\n"
            + "</manifest>",
            mergerLog, new FileAndLine("main", 1));
        assertNotNull(mainDoc);
        Document library1 = MergerXmlUtils.parseDocument(""
            + "<manifest\n"
            + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    package=\"com.example.app1\">\n"
            + "\n"
            + "    <application android:name=\"TheApp\" >\n"
            + "        <activity android:name=\".Library1\" />\n"
            + "\n"
            + "        <!-- The library maps API key gets merged in the main application. -->\n"
            + "        <meta-data\n"
            + "            android:name=\"name.for.maps.api.key\"\n"
            + "            android:value=\"your_maps_api_key\"/>\n"
            + "\n"
            + "        <!-- The library backup key gets merged in the main application. -->\n"
            + "        <meta-data\n"
            + "            android:name=\"name.for.backup.api.key\"\n"
            + "            android:value=\"your_backup_api_key\" />\n"
            + "    </application>\n"
            + "\n"
            + "</manifest>",
            mergerLog, new FileAndLine("library1", 1));
        assertNotNull(library1);
        Document library2 = MergerXmlUtils.parseDocument(""
            + "<manifest\n"
            + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    package=\"com.example.lib3\">\n"
            + "\n"
            + "    <!-- This comment is ignored. -->\n"
            + "\n"
            + "    <application android:label=\"@string/lib_name\" >\n"
            + "\n"
            + "        <!-- The first comment just before the element\n"
            + "             is carried over as-is.\n"
            + "        -->\n"
            + "        <!-- Formatting is preserved. -->\n"
            + "        <!-- All consecutive comments are taken together. -->\n"
            + "\n"
            + "        <activity-alias\n"
            + "            android:name=\"com.example.alias.MyActivity\"\n"
            + "            android:targetActivity=\"com.example.MainActivity\"\n"
            + "            android:label=\"@string/alias_name\"\n"
            + "            android:icon=\"@drawable/alias_icon\"\n"
            + "            >\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\" />\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
            + "            </intent-filter>\n"
            + "        </activity-alias>\n"
            + "\n"
            + "        <!-- This is a dup of the 2nd activity in lib2 -->\n"
            + "        <activity\n"
            + "            android:name=\"com.example.LibActivity2\"\n"
            + "            android:label=\"@string/lib_activity_name\"\n"
            + "            android:icon=\"@drawable/lib_activity_icon\"\n"
            + "            android:theme=\"@style/Lib.Theme\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\" />\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
            + "            </intent-filter>\n"
            + "        </activity>\n"
            + "\n"
            + "    </application>\n"
            + "\n"
            + "</manifest>",
            mergerLog, new FileAndLine("library2", 1));
        assertNotNull(library2);

        Document library3 = MergerXmlUtils.parseDocument(""
            + "<manifest\n"
            + "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    package=\"com.example.lib3\">\n"
            + "\n"
            + "    <application android:label=\"@string/lib_name\" >\n"
            + "        <activity\n"
            + "            android:name=\"com.example.LibActivity3\"\n"
            + "            android:label=\"@string/lib_activity_name3\"\n"
            + "            android:icon=\"@drawable/lib_activity_icon3\"\n"
            + "            android:theme=\"@style/Lib.Theme\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\" />\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\" />\n"
            + "            </intent-filter>\n"
            + "        </activity>\n"
            + "\n"
            + "    </application>\n"
            + "\n"
            + "</manifest>",
            mergerLog, new FileAndLine("library3", 1));
        assertNotNull(library3);

        MergerXmlUtils.setSource(mainDoc, new File("/path/to/main/doc"));
        MergerXmlUtils.setSource(library1, new File("/path/to/library1"));
        MergerXmlUtils.setSource(library2, new File("/path/to/library2"));
        MergerXmlUtils.setSource(library3, new File("/path/to/library3"));

        boolean ok = merger.process(mainDoc, library1, library2, library3);
        assertTrue(ok);
        String actual = MergerXmlUtils.printXmlString(mainDoc, mergerLog);
        assertEquals("Encountered unexpected errors/warnings", "", log.toString());
        String expected = ""
            + "<!-- From: file:/path/to/main/doc -->\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionCode=\"100\" android:versionName=\"1.0.0\" package=\"com.example.app1\">\n"
            + "\n"
            + "    <uses-sdk android:minSdkVersion=\"3\" android:targetSdkVersion=\"11\"/>\n"
            + "\n"
            + "    <application android:backupAgent=\"com.example.app1.MyBackupAgent\" android:name=\"com.example.app1.TheApp\">\n"
            + "        <activity android:name=\"com.example.app1.MainActivity\"/>\n"
            + "        <receiver android:name=\"com.example.app1.AppReceiver\"/>\n"
            + "        <activity android:name=\"com.example.lib2.LibActivity\"/>\n"
            + "\n"
            + "        <!-- This key is defined in the main application. -->\n"
            + "        <meta-data android:name=\"name.for.yet.another.api.key\" android:value=\"your_yet_another_api_key\"/>\n"
            + "\n"
            + "        <!-- Merged elements will be appended here at the end. -->\n"
            + "        <!-- From: file:/path/to/library1 -->\n"
            + "        <activity android:name=\"com.example.app1.Library1\"/>\n"
            + "\n"
            + "        <!-- The library maps API key gets merged in the main application. -->\n"
            + "        <meta-data android:name=\"name.for.maps.api.key\" android:value=\"your_maps_api_key\"/>\n"
            + "\n"
            + "        <!-- The library backup key gets merged in the main application. -->\n"
            + "        <meta-data android:name=\"name.for.backup.api.key\" android:value=\"your_backup_api_key\"/>\n"
            + "\n"
            + "        <!-- From: file:/path/to/library2 -->\n"
            + "        <!-- This is a dup of the 2nd activity in lib2 -->\n"
            + "        <activity android:icon=\"@drawable/lib_activity_icon\" android:label=\"@string/lib_activity_name\" android:name=\"com.example.LibActivity2\" android:theme=\"@style/Lib.Theme\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\"/>\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n"
            + "            </intent-filter>\n"
            + "        </activity>\n"
            + "\n"
            + "        <!-- The first comment just before the element\n"
            + "             is carried over as-is.\n"
            + "        -->\n"
            + "        <!-- Formatting is preserved. -->\n"
            + "        <!-- All consecutive comments are taken together. -->\n"
            + "\n"
            + "        <activity-alias android:icon=\"@drawable/alias_icon\" android:label=\"@string/alias_name\" android:name=\"com.example.alias.MyActivity\" android:targetActivity=\"com.example.MainActivity\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\"/>\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n"
            + "            </intent-filter>\n"
            + "        </activity-alias>\n"
            + "        <!-- From: file:/path/to/library3 -->\n"
            + "        <activity android:icon=\"@drawable/lib_activity_icon3\" android:label=\"@string/lib_activity_name3\" android:name=\"com.example.LibActivity3\" android:theme=\"@style/Lib.Theme\">\n"
            + "            <intent-filter>\n"
            + "                <action android:name=\"android.intent.action.MAIN\"/>\n"
            + "                <category android:name=\"android.intent.category.LAUNCHER\"/>\n"
            + "            </intent-filter>\n"
            + "        </activity>\n"
            + "        <!-- From: file:/path/to/main/doc -->\n"
            + "        \n"
            + "    </application>\n"
            + "\n"
            + "</manifest>\n";

        if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
            // Adjust mock paths & EOLs for windows
            actual = actual.replace("\r\n", "\n");
            expected = expected.replace("file:/path/to/", "file:/C:/path/to/");
        }

        try {
            assertEquals(expected, actual);

        } catch (Exception originalFailure) {
            // DOM implementations vary slightly whether they'll insert a newline for comment
            // inserted outside document
            // JDK 7 doesn't, JDK 6 does
            int index = expected.indexOf('\n');
            assertTrue(index != -1);
            expected = expected.substring(0, index) + expected.substring(index + 1);
            try {
                assertEquals(expected, actual);
            } catch (Throwable ignore) {
                // If the second test fails too, throw the *original* exception,
                // before we tried to tweak the EOL.
                throw originalFailure;
            }
        }
    }
}
