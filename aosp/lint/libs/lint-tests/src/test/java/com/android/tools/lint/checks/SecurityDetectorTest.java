/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class SecurityDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new SecurityDetector();
    }

    public void testBroken() throws Exception {
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported service does not require permission [ExportedService]\n" +
            "        <service\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "exportservice1.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testBroken2() throws Exception {
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported service does not require permission [ExportedService]\n" +
            "        <service\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "exportservice2.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testBroken3() throws Exception {
        // Not defining exported, but have intent-filters
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported service does not require permission [ExportedService]\n" +
            "        <service\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "exportservice5.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testOk1() throws Exception {
        // Defines a permission on the <service> element
        assertEquals(
            "No warnings.",
            lintProject(
                    "exportservice3.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testOk2() throws Exception {
        // Defines a permission on the parent <application> element
        assertEquals(
            "No warnings.",
            lintProject(
                    "exportservice4.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testUri() throws Exception {
        assertEquals(
            "AndroidManifest.xml:25: Warning: Content provider shares everything; this is potentially dangerous. [GrantAllUris]\n" +
            "        <grant-uri-permission android:path=\"/\"/>\n" +
            "                              ~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:26: Warning: Content provider shares everything; this is potentially dangerous. [GrantAllUris]\n" +
            "        <grant-uri-permission android:pathPrefix=\"/\"/>\n" +
            "                              ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject(
                    "grantpermission.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    // exportprovider1.xml has two exported content providers with no permissions
    public void testContentProvider1() throws Exception {
        assertEquals(
            "AndroidManifest.xml:14: Warning: Exported content providers can provide access to potentially sensitive data [ExportedContentProvider]\n" +
            "        <provider\n" +
            "        ^\n" +
            "AndroidManifest.xml:20: Warning: Exported content providers can provide access to potentially sensitive data [ExportedContentProvider]\n" +
            "        <provider\n" +
            "        ^\n" +
            "0 errors, 2 warnings\n" +
            "",
             lintProject(
                    "exportprovider1.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    // exportprovider2.xml has no un-permissioned exported content providers
    public void testContentProvider2() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "exportprovider2.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testWorldWriteable() throws Exception {
        assertEquals(
            "src/test/pkg/WorldWriteableFile.java:26: Warning: Using MODE_WORLD_READABLE when creating files can be risky, review carefully [WorldReadableFiles]\n" +
            "            out = openFileOutput(mFile.getName(), MODE_WORLD_READABLE);\n" +
            "                                                  ~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WorldWriteableFile.java:31: Warning: Using MODE_WORLD_READABLE when creating files can be risky, review carefully [WorldReadableFiles]\n" +
            "            prefs = getSharedPreferences(mContext, MODE_WORLD_READABLE);\n" +
            "                                                   ~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WorldWriteableFile.java:25: Warning: Using MODE_WORLD_WRITEABLE when creating files can be risky, review carefully [WorldWriteableFiles]\n" +
            "            out = openFileOutput(mFile.getName(), MODE_WORLD_WRITEABLE);\n" +
            "                                                  ~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WorldWriteableFile.java:30: Warning: Using MODE_WORLD_WRITEABLE when creating files can be risky, review carefully [WorldWriteableFiles]\n" +
            "            prefs = getSharedPreferences(mContext, MODE_WORLD_WRITEABLE);\n" +
            "                                                   ~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n" +
            "",

            lintProject(
                // Java files must be renamed in source tree
                "src/test/pkg/WorldWriteableFile.java.txt=>src/test/pkg/WorldWriteableFile.java"));
    }

    public void testReceiver0() throws Exception {
        // Activities that do not have intent-filters do not need warnings
        assertEquals(
            "No warnings.",
            lintProject(
                "exportreceiver0.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testReceiver1() throws Exception {
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported receiver does not require permission [ExportedReceiver]\n" +
            "        <receiver\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                "exportreceiver1.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testReceiver2() throws Exception {
        // Defines a permission on the <activity> element
        assertEquals(
            "No warnings.",
            lintProject(
                "exportreceiver2.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testReceiver3() throws Exception {
        // Defines a permission on the parent <application> element
        assertEquals(
            "No warnings.",
            lintProject(
                "exportreceiver3.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testReceiver4() throws Exception {
        // Not defining exported, but have intent-filters
        assertEquals(
            "AndroidManifest.xml:12: Warning: Exported receiver does not require permission [ExportedReceiver]\n" +
            "        <receiver\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                "exportreceiver4.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testReceiver5() throws Exception {
      // Intent filter for standard Android action
      assertEquals(
          "No warnings.",
          lintProject(
              "exportreceiver5.xml=>AndroidManifest.xml",
              "res/values/strings.xml"));
    }

    public void testStandard() throws Exception {
        // Various regression tests for http://code.google.com/p/android/issues/detail?id=33976
        assertEquals(
            "No warnings.",
            lintProject("exportreceiver6.xml=>AndroidManifest.xml"));
    }

    public void testUsingInstallReferrerReceiver() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=73934
        assertEquals(
                "No warnings.",
                lintProject("exportreceiver7.xml=>AndroidManifest.xml"));
    }

    public void testGmsWearable() throws Exception {
        // As documented in
        //    https://developer.android.com/training/wearables/data-layer/events.html
        // you shouldn't need a permission here.
        assertEquals(
                "No warnings.",
                lintProject("exportreceiver8.xml=>AndroidManifest.xml"));

    }
}