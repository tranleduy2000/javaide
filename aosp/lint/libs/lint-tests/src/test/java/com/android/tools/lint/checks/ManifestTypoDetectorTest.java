/*
 * Copyright (C) 2013 The Android Open Source Project
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
public class ManifestTypoDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ManifestTypoDetector();
    }

    public void testOk() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "typo_not_found.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testTypoUsesSdk() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:7: Error: Misspelled tag <use-sdk>: Did you mean <uses-sdk> ? [ManifestTypo]\n"
                + "    <use-sdk android:minSdkVersion=\"14\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_sdk.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesSdk2() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:7: Error: Misspelled tag <user-sdk>: Did you mean <uses-sdk> ? [ManifestTypo]\n"
                + "    <user-sdk android:minSdkVersion=\"14\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_sdk2.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesPermission() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:9: Error: Misspelled tag <use-permission>: Did you mean <uses-permission> ? [ManifestTypo]\n"
                + "    <use-permission android:name=\"com.example.helloworld.permission\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_permission.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesPermission2() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:9: Error: Misspelled tag <user-permission>: Did you mean <uses-permission> ? [ManifestTypo]\n"
                + "    <user-permission android:name=\"com.example.helloworld.permission\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_permission2.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesFeature() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:11: Error: Misspelled tag <use-feature>: Did you mean <uses-feature> ? [ManifestTypo]\n"
                + "    <use-feature android:name=\"android.hardware.wifi\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_feature.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesFeature2() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:11: Error: Misspelled tag <user-feature>: Did you mean <uses-feature> ? [ManifestTypo]\n"
                + "    <user-feature android:name=\"android.hardware.wifi\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_feature2.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesLibrary() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:16: Error: Misspelled tag <use-library>: Did you mean <uses-library> ? [ManifestTypo]\n"
                + "        <use-library android:name=\"com.example.helloworld\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_library.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testTypoUsesLibrary2() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:16: Error: Misspelled tag <user-library>: Did you mean <uses-library> ? [ManifestTypo]\n"
                + "        <user-library android:name=\"com.example.helloworld\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                    "typo_uses_library2.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testOtherTypos() throws Exception {
        assertEquals(""
                + "AndroidManifest.xml:2: Error: Misspelled tag <mannifest>: Did you mean <manifest> ? [ManifestTypo]\n"
                + "<mannifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "^\n"
                + "AndroidManifest.xml:7: Error: Misspelled tag <uses-sd>: Did you mean <uses-sdk> ? [ManifestTypo]\n"
                + "    <uses-sd android:minSdkVersion=\"14\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:9: Error: Misspelled tag <spplication>: Did you mean <application> ? [ManifestTypo]\n"
                + "    <spplication\n"
                + "    ^\n"
                + "AndroidManifest.xml:12: Error: Misspelled tag <acctivity>: Did you mean <activity> ? [ManifestTypo]\n"
                + "        <acctivity\n"
                + "        ^\n"
                + "AndroidManifest.xml:15: Error: Misspelled tag <inten-filter>: Did you mean <intent-filter> ? [ManifestTypo]\n"
                + "            <inten-filter >\n"
                + "            ^\n"
                + "AndroidManifest.xml:16: Error: Misspelled tag <aktion>: Did you mean <action> ? [ManifestTypo]\n"
                + "                <aktion android:name=\"android.intent.action.MAIN\" />\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:18: Error: Misspelled tag <caaategory>: Did you mean <category> ? [ManifestTypo]\n"
                + "                <caaategory android:name=\"android.intent.category.LAUNCHER\" />\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "7 errors, 0 warnings\n",

                lintProject(
                        "typo_manifest.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }
}
