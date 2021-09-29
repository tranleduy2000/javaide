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

import static com.android.SdkConstants.ANDROID_MANIFEST_XML;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.BuildType;
import com.android.builder.model.BuildTypeContainer;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.ProductFlavorContainer;
import com.android.builder.model.SourceProvider;
import com.android.builder.model.SourceProviderContainer;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("javadoc")
public class ManifestDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ManifestDetector();
    }

    private Set<Issue> mEnabled = new HashSet<Issue>();

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }

    public void testOrderOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ORDER);
        assertEquals(
                "No warnings.",
                lintProject(
                        "AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testBrokenOrder() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ORDER);
        assertEquals(
            "AndroidManifest.xml:16: Warning: <uses-sdk> tag appears after <application> tag [ManifestOrder]\n" +
            "   <uses-sdk android:minSdkVersion=\"Froyo\" />\n" +
            "   ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject(
                    "broken-manifest.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testMissingUsesSdk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.USES_SDK);
        assertEquals(
            "AndroidManifest.xml: Warning: Manifest should specify a minimum API level with <uses-sdk android:minSdkVersion=\"?\" />; if it really supports all versions of Android set it to 1. [UsesMinSdkAttributes]\n" +
            "0 errors, 1 warnings\n",
            lintProject(
                    "missingusessdk.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testMissingUsesSdkInGradle() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.SET_VERSION);
        assertEquals(""
                + "No warnings.",
                lintProject("missingusessdk.xml=>AndroidManifest.xml",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testMissingMinSdk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.USES_SDK);
        assertEquals(
            "AndroidManifest.xml:7: Warning: <uses-sdk> tag should specify a minimum API level with android:minSdkVersion=\"?\" [UsesMinSdkAttributes]\n" +
            "    <uses-sdk android:targetSdkVersion=\"10\" />\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject(
                    "missingmin.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testMissingTargetSdk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.USES_SDK);
        assertEquals(
            "AndroidManifest.xml:7: Warning: <uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=\"?\" [UsesMinSdkAttributes]\n" +
            "    <uses-sdk android:minSdkVersion=\"10\" />\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n",
            lintProject(
                    "missingtarget.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testOldTargetSdk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.TARGET_NEWER);
        assertEquals(
            "AndroidManifest.xml:7: Warning: Not targeting the latest versions of Android; compatibility modes apply. Consider testing and updating this version. Consult the android.os.Build.VERSION_CODES javadoc for details. [OldTargetApi]\n" +
            "    <uses-sdk android:minSdkVersion=\"10\" android:targetSdkVersion=\"14\" />\n" +
            "                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n",
            lintProject(
                    "oldtarget.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testMultipleSdk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.MULTIPLE_USES_SDK);
        assertEquals(
            "AndroidManifest.xml:8: Error: There should only be a single <uses-sdk> element in the manifest: merge these together [MultipleUsesSdk]\n" +
            "    <uses-sdk android:targetSdkVersion=\"14\" />\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "    AndroidManifest.xml:7: Also appears here\n" +
            "    AndroidManifest.xml:9: Also appears here\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                    "multiplesdk.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testWrongLocation() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.WRONG_PARENT);
        assertEquals(
            "AndroidManifest.xml:8: Error: The <uses-sdk> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <uses-sdk android:minSdkVersion=\"Froyo\" />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:9: Error: The <uses-permission> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <uses-permission />\n" +
            "       ~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:10: Error: The <permission> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <permission />\n" +
            "       ~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:11: Error: The <permission-tree> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <permission-tree />\n" +
            "       ~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:12: Error: The <permission-group> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <permission-group />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:14: Error: The <uses-sdk> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <uses-sdk />\n" +
            "       ~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:15: Error: The <uses-configuration> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <uses-configuration />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:16: Error: The <uses-feature> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <uses-feature />\n" +
            "       ~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:17: Error: The <supports-screens> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <supports-screens />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:18: Error: The <compatible-screens> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <compatible-screens />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:19: Error: The <supports-gl-texture> element must be a direct child of the <manifest> root element [WrongManifestParent]\n" +
            "       <supports-gl-texture />\n" +
            "       ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:24: Error: The <uses-library> element must be a direct child of the <application> element [WrongManifestParent]\n" +
            "   <uses-library />\n" +
            "   ~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:25: Error: The <activity> element must be a direct child of the <application> element [WrongManifestParent]\n" +
            "   <activity android:name=\".HelloWorld\"\n" +
            "   ^\n" +
            "13 errors, 0 warnings\n" +
            "",

            lintProject("broken-manifest2.xml=>AndroidManifest.xml"));
    }

    public void testDuplicateActivity() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DUPLICATE_ACTIVITY);
        assertEquals(
            "AndroidManifest.xml:16: Error: Duplicate registration for activity com.example.helloworld.HelloWorld [DuplicateActivity]\n" +
            "       <activity android:name=\"com.example.helloworld.HelloWorld\"\n" +
            "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n" +
            "",

            lintProject(
                    "duplicate-manifest.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testDuplicateActivityAcrossSourceSets() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DUPLICATE_ACTIVITY);
        File master = getProjectDir("MasterProject",
                // Master project
                "AndroidManifest.xml=>AndroidManifest.xml",
                "multiproject/main-merge.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "AndroidManifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals("No warnings.",
                checkLint(Arrays.asList(master, library)));
    }

    public void testIgnoreDuplicateActivity() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DUPLICATE_ACTIVITY);
        assertEquals(
            "No warnings.",

            lintProject(
                    "duplicate-manifest-ignore.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testAllowBackup() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(
                "AndroidManifest.xml:9: Warning: Should explicitly set android:allowBackup to " +
                "true or false (it's true by default, and that can have some security " +
                "implications for the application's data) [AllowBackup]\n" +
                "    <application\n" +
                "    ^\n" +
                "0 errors, 1 warnings\n",
                lintProject(
                        "AndroidManifest.xml",
                        "apicheck/minsdk14.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testAllowBackupOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(
                "No warnings.",
                lintProject(
                        "allowbackup.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testAllowBackupOk2() throws Exception {
        // Requires build api >= 4
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(
                "No warnings.",
                lintProject(
                        "apicheck/minsdk1.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testAllowBackupOk3() throws Exception {
        // Not flagged in library projects
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(
                "No warnings.",
                lintProject(
                        "AndroidManifest.xml",
                        "multiproject/library.properties=>project.properties",
                        "res/values/strings.xml"));
    }

    public void testAllowIgnore() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(
                "No warnings.",
                lintProject(
                        "allowbackup_ignore.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testDuplicatePermissions() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.UNIQUE_PERMISSION);
        assertEquals(
                "AndroidManifest.xml:12: Error: Permission name SEND_SMS is not unique (appears in both foo.permission.SEND_SMS and bar.permission.SEND_SMS) [UniquePermission]\n" +
                "    <permission android:name=\"bar.permission.SEND_SMS\"\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    AndroidManifest.xml:9: Previous permission here\n" +
                "1 errors, 0 warnings\n",

                lintProject(
                        "duplicate_permissions1.xml=>AndroidManifest.xml",
                        "res/values/strings.xml"));
    }

    public void testDuplicatePermissionsMultiProject() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.UNIQUE_PERMISSION);

        File master = getProjectDir("MasterProject",
                // Master project
                "duplicate_permissions2.xml=>AndroidManifest.xml",
                "multiproject/main-merge.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "duplicate_permissions3.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals(
                "LibraryProject/AndroidManifest.xml:9: Error: Permission name SEND_SMS is not unique (appears in both foo.permission.SEND_SMS and bar.permission.SEND_SMS) [UniquePermission]\n" +
                "    <permission android:name=\"bar.permission.SEND_SMS\"\n" +
                "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "1 errors, 0 warnings\n",

           checkLint(Arrays.asList(master, library)));
    }

    public void testMissingVersion() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.SET_VERSION);
        assertEquals(""
            + "AndroidManifest.xml:2: Warning: Should set android:versionCode to specify the application version [MissingVersion]\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "^\n"
            + "AndroidManifest.xml:2: Warning: Should set android:versionName to specify the application version [MissingVersion]\n"
            + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "^\n"
            + "0 errors, 2 warnings\n",
            lintProject("no_version.xml=>AndroidManifest.xml"));
    }

    public void testVersionNotMissingInGradleProjects() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.SET_VERSION);
        assertEquals(""
            + "No warnings.",
            lintProject("no_version.xml=>AndroidManifest.xml",
                    "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testIllegalReference() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ILLEGAL_REFERENCE);
        assertEquals(""
            + "AndroidManifest.xml:4: Warning: The android:versionCode cannot be a resource url, it must be a literal integer [IllegalResourceRef]\n"
            + "    android:versionCode=\"@dimen/versionCode\"\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "AndroidManifest.xml:7: Warning: The android:minSdkVersion cannot be a resource url, it must be a literal integer (or string if a preview codename) [IllegalResourceRef]\n"
            + "    <uses-sdk android:minSdkVersion=\"@dimen/minSdkVersion\" android:targetSdkVersion=\"@dimen/targetSdkVersion\" />\n"
            + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "AndroidManifest.xml:7: Warning: The android:targetSdkVersion cannot be a resource url, it must be a literal integer (or string if a preview codename) [IllegalResourceRef]\n"
            + "    <uses-sdk android:minSdkVersion=\"@dimen/minSdkVersion\" android:targetSdkVersion=\"@dimen/targetSdkVersion\" />\n"
            + "                                                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "0 errors, 3 warnings\n",

            lintProject("illegal_version.xml=>AndroidManifest.xml"));
    }

    public void testDuplicateUsesFeature() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DUPLICATE_USES_FEATURE);
        assertEquals(
            "AndroidManifest.xml:11: Warning: Duplicate declaration of uses-feature android.hardware.camera [DuplicateUsesFeature]\n" +
            "    <uses-feature android:name=\"android.hardware.camera\"/>\n" +
            "                  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n",
            lintProject(
                    "duplicate_uses_feature.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testDuplicateUsesFeatureOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DUPLICATE_USES_FEATURE);
        assertEquals(
            "No warnings.",
            lintProject(
                    "duplicate_uses_feature_ok.xml=>AndroidManifest.xml",
                    "res/values/strings.xml"));
    }

    public void testMissingApplicationIcon() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.APPLICATION_ICON);
        assertEquals(
            "AndroidManifest.xml:9: Warning: Should explicitly set android:icon, there is no default [MissingApplicationIcon]\n" +
            "    <application\n" +
            "    ^\n" +
            "0 errors, 1 warnings\n",
            lintProject(
                "missing_application_icon.xml=>AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testMissingApplicationIconInLibrary() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.APPLICATION_ICON);
        assertEquals(
            "No warnings.",
            lintProject(
                "missing_application_icon.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "res/values/strings.xml"));
    }

    public void testMissingApplicationIconOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.APPLICATION_ICON);
        assertEquals(
            "No warnings.",
            lintProject(
                "AndroidManifest.xml",
                "res/values/strings.xml"));
    }

    public void testDeviceAdmin() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.DEVICE_ADMIN);
        assertEquals(""
                + "AndroidManifest.xml:31: Warning: You must have an intent filter for action android.app.action.DEVICE_ADMIN_ENABLED [DeviceAdmin]\n"
                + "            <meta-data android:name=\"android.app.device_admin\"\n"
                + "                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:44: Warning: You must have an intent filter for action android.app.action.DEVICE_ADMIN_ENABLED [DeviceAdmin]\n"
                + "            <meta-data android:name=\"android.app.device_admin\"\n"
                + "                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:56: Warning: You must have an intent filter for action android.app.action.DEVICE_ADMIN_ENABLED [DeviceAdmin]\n"
                + "            <meta-data android:name=\"android.app.device_admin\"\n"
                + "                       ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 3 warnings\n",
                lintProject("deviceadmin.xml=>AndroidManifest.xml"));
    }

    public void testMockLocations() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.MOCK_LOCATION);
        assertEquals(""
                + "AndroidManifest.xml:9: Error: Mock locations should only be requested in a test or debug-specific manifest file (typically src/debug/AndroidManifest.xml) [MockLocation]\n"
                + "    <uses-permission android:name=\"android.permission.ACCESS_MOCK_LOCATION\" /> \n"
                + "                     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        "mock_location.xml=>AndroidManifest.xml",
                        "mock_location.xml=>debug/AndroidManifest.xml",
                        "mock_location.xml=>test/AndroidManifest.xml",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
        // TODO: When we have an instantiatable gradle model, test with real model and verify
        // that a manifest file in a debug build type does not get flagged.
    }

    public void testMockLocationsOk() throws Exception {
        // Not a Gradle project
        mEnabled = Collections.singleton(ManifestDetector.MOCK_LOCATION);
        assertEquals(""
                + "No warnings.",
                lintProject(
                        "mock_location.xml=>AndroidManifest.xml"));
    }

    public void testGradleOverrides() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.GRADLE_OVERRIDES);
        assertEquals(""
                + "AndroidManifest.xml:4: Warning: This versionCode value (1) is not used; it is always overridden by the value specified in the Gradle build script (2) [GradleOverrides]\n"
                + "    android:versionCode=\"1\"\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:5: Warning: This versionName value (1.0) is not used; it is always overridden by the value specified in the Gradle build script (MyName) [GradleOverrides]\n"
                + "    android:versionName=\"1.0\" >\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:7: Warning: This minSdkVersion value (14) is not used; it is always overridden by the value specified in the Gradle build script (5) [GradleOverrides]\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"17\" />\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "AndroidManifest.xml:7: Warning: This targetSdkVersion value (17) is not used; it is always overridden by the value specified in the Gradle build script (16) [GradleOverrides]\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\"17\" />\n"
                + "                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 4 warnings\n",
                lintProject(
                        "gradle_override.xml=>AndroidManifest.xml",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testGradleOverridesOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.GRADLE_OVERRIDES);
        // (See custom logic in #createClient which returns -1/null for the merged flavor
        // from this test, and not from testGradleOverrides)
        assertEquals(""
                + "No warnings.",
                lintProject(
                        "gradle_override.xml=>AndroidManifest.xml",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testManifestPackagePlaceholder() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.GRADLE_OVERRIDES);
        assertEquals(""
                + "AndroidManifest.xml:3: Warning: Cannot use placeholder for the package in the manifest; set applicationId in build.gradle instead [GradleOverrides]\n"
                + "    package=\"${packageName}\" >\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "gradle_override_placeholder.xml=>AndroidManifest.xml",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testMipMap() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.MIPMAP);
        assertEquals("No warnings.",

                lintProject(
                        "mipmap.xml=>AndroidManifest.xml"));
    }

    public void testMipMapWithDensityFiltering() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.MIPMAP);
        assertEquals(""
                        + "AndroidManifest.xml:9: Warning: Should use @mipmap instead of @drawable for launcher icons [MipmapIcons]\n"
                        + "        android:icon=\"@drawable/ic_launcher\"\n"
                        + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "AndroidManifest.xml:14: Warning: Should use @mipmap instead of @drawable for launcher icons [MipmapIcons]\n"
                        + "            android:icon=\"@drawable/activity1\"\n"
                        + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 2 warnings\n",

                lintProject(
                        "mipmap.xml=>AndroidManifest.xml"));
    }

    public void testFullBackupContentBoolean() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals("No warnings.",

                lintProjectIncrementally(
                        "AndroidManifest.xml",
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "\n"
                                + "    <application\n"
                                + "        android:allowBackup=\"true\"\n"
                                + "        android:fullBackupContent=\"true\"\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n")));
    }

    public void testFullBackupContentMissing() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(""
                + "AndroidManifest.xml:7: Warning: Missing <full-backup-content> resource [AllowBackup]\n"
                + "        android:fullBackupContent=\"@xml/backup\"\n"
                + "                                   ~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProjectIncrementally(
                        "AndroidManifest.xml",
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "\n"
                                + "    <application\n"
                                + "        android:allowBackup=\"true\"\n"
                                + "        android:fullBackupContent=\"@xml/backup\"\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                        + "\n"
                        + "</manifest>\n")));
    }

    public void testFullBackupContentOk() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals("No warnings.",

                lintProjectIncrementally(
                        "AndroidManifest.xml",
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "\n"
                                + "    <application\n"
                                + "        android:allowBackup=\"true\"\n"
                                + "        android:fullBackupContent=\"@xml/backup\"\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n"),
                        xml("res/xml/backup.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<full-backup-content>\n"
                                + "     <include domain=\"file\" path=\"dd\"/>\n"
                                + "     <exclude domain=\"file\" path=\"dd/fo3o.txt\"/>\n"
                                + "     <exclude domain=\"file\" path=\"dd/ss/foo.txt\"/>\n"
                                + "</full-backup-content>")));
    }

    public void testHasBackupSpecifiedInTarget23() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals("No warnings.",

                lintProject(
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "    <uses-sdk android:targetSdkVersion=\"23\" />"
                                + "\n"
                                + "    <application\n"
                                + "        android:fullBackupContent=\"no\"\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n")));
    }

    public void testMissingBackupInTarget23() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(""
                + "AndroidManifest.xml:5: Warning: Should explicitly set android:fullBackupContent to true or false to opt-in to or out of full app data back-up and restore, or alternatively to an @xml resource which specifies which files to backup [AllowBackup]\n"
                + "    <application\n"
                + "    ^\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "    <uses-sdk android:targetSdkVersion=\"23\" />"
                                + "\n"
                                + "    <application\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n")));
    }

    public void testMissingBackupWithoutGcmPreTarget23() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals("No warnings.",

                lintProject(
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "    <uses-sdk android:targetSdkVersion=\"21\" />"
                                + "\n"
                                + "    <application\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n")));
    }

    public void testMissingBackupWithGcmPreTarget23() throws Exception {
        mEnabled = Collections.singleton(ManifestDetector.ALLOW_BACKUP);
        assertEquals(""
                + "AndroidManifest.xml:5: Warning: Should explicitly set android:fullBackupContent to avoid backing up the GCM device specific regId. [AllowBackup]\n"
                + "    <application\n"
                + "    ^\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        xml("AndroidManifest.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    package=\"com.example.helloworld\" >\n"
                                + "    <uses-sdk android:targetSdkVersion=\"21\" />"
                                + "\n"
                                + "    <application\n"
                                + "        android:label=\"@string/app_name\"\n"
                                + "        android:theme=\"@style/AppTheme\" >"
                                + "        <receiver\n"
                                + "            android:name=\".GcmBroadcastReceiver\"\n"
                                + "            android:permission=\"com.google.android.c2dm.permission.SEND\" >\n"
                                + "            <intent-filter>\n"
                                + "                <action android:name=\"com.google.android.c2dm.intent.RECEIVE\" />\n"
                                + "                <category android:name=\"com.example.gcm\" />\n"
                                + "            </intent-filter>\n"
                                + "        </receiver>\n"
                                + "    </application>\n"
                                + "\n"
                                + "</manifest>\n")));
    }

    // Custom project which locates all manifest files in the project rather than just
    // being hardcoded to the root level

    @Override
    protected TestLintClient createClient() {
        if ("testMipMapWithDensityFiltering".equals(getName())) {
            // Set up a mock project model for the resource configuration test(s)
            // where we provide a subset of densities to be included
            return new TestLintClient() {
                @NonNull
                @Override
                protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                    return new Project(this, dir, referenceDir) {
                        @Override
                        public boolean isGradleProject() {
                            return true;
                        }

                        @Nullable
                        @Override
                        public AndroidProject getGradleProjectModel() {
                            /*
                            Simulate variant freeBetaDebug in this setup:
                                defaultConfig {
                                    ...
                                    resConfigs "cs"
                                }
                                flavorDimensions  "pricing", "releaseType"
                                productFlavors {
                                    beta {
                                        flavorDimension "releaseType"
                                        resConfig "en", "de"
                                        resConfigs "nodpi", "hdpi"
                                    }
                                    normal { flavorDimension "releaseType" }
                                    free { flavorDimension "pricing" }
                                    paid { flavorDimension "pricing" }
                                }
                             */
                            ProductFlavor flavorFree = mock(ProductFlavor.class);
                            when(flavorFree.getName()).thenReturn("free");
                            when(flavorFree.getResourceConfigurations())
                                    .thenReturn(Collections.<String>emptyList());

                            ProductFlavor flavorNormal = mock(ProductFlavor.class);
                            when(flavorNormal.getName()).thenReturn("normal");
                            when(flavorNormal.getResourceConfigurations())
                                    .thenReturn(Collections.<String>emptyList());

                            ProductFlavor flavorPaid = mock(ProductFlavor.class);
                            when(flavorPaid.getName()).thenReturn("paid");
                            when(flavorPaid.getResourceConfigurations())
                                    .thenReturn(Collections.<String>emptyList());

                            ProductFlavor flavorBeta = mock(ProductFlavor.class);
                            when(flavorBeta.getName()).thenReturn("beta");
                            List<String> resConfigs = Arrays.asList("hdpi", "en", "de", "nodpi");
                            when(flavorBeta.getResourceConfigurations()).thenReturn(resConfigs);

                            ProductFlavor defaultFlavor = mock(ProductFlavor.class);
                            when(defaultFlavor.getName()).thenReturn("main");
                            when(defaultFlavor.getResourceConfigurations()).thenReturn(
                                    Collections.singleton("cs"));

                            ProductFlavorContainer containerBeta =
                                    mock(ProductFlavorContainer.class);
                            when(containerBeta.getProductFlavor()).thenReturn(flavorBeta);

                            ProductFlavorContainer containerFree =
                                    mock(ProductFlavorContainer.class);
                            when(containerFree.getProductFlavor()).thenReturn(flavorFree);

                            ProductFlavorContainer containerPaid =
                                    mock(ProductFlavorContainer.class);
                            when(containerPaid.getProductFlavor()).thenReturn(flavorPaid);

                            ProductFlavorContainer containerNormal =
                                    mock(ProductFlavorContainer.class);
                            when(containerNormal.getProductFlavor()).thenReturn(flavorNormal);

                            ProductFlavorContainer defaultContainer =
                                    mock(ProductFlavorContainer.class);
                            when(defaultContainer.getProductFlavor()).thenReturn(defaultFlavor);

                            List<ProductFlavorContainer> containers = Arrays.asList(
                                    containerPaid, containerFree, containerNormal, containerBeta
                            );

                            AndroidProject project = mock(AndroidProject.class);
                            when(project.getProductFlavors()).thenReturn(containers);
                            when(project.getDefaultConfig()).thenReturn(defaultContainer);
                            return project;
                        }

                        @Nullable
                        @Override
                        public Variant getCurrentVariant() {
                            List<String> productFlavorNames = Arrays.asList("free", "beta");
                            Variant mock = mock(Variant.class);
                            when(mock.getProductFlavors()).thenReturn(productFlavorNames);
                            return mock;
                        }
                    };
                }
            };
        }
        if (mEnabled.contains(ManifestDetector.MOCK_LOCATION)) {
            return new TestLintClient() {
                @NonNull
                @Override
                protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                    return new Project(this, dir, referenceDir) {
                        @NonNull
                        @Override
                        public List<File> getManifestFiles() {
                            if (mManifestFiles == null) {
                                mManifestFiles = Lists.newArrayList();
                                addManifestFiles(mDir);
                            }

                            return mManifestFiles;
                        }

                        private void addManifestFiles(File dir) {
                            if (dir.getName().equals(ANDROID_MANIFEST_XML)) {
                                mManifestFiles.add(dir);
                            } else if (dir.isDirectory()) {
                                File[] files = dir.listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        addManifestFiles(file);
                                    }
                                }
                            }
                        }

                        @NonNull SourceProvider createSourceProvider(File manifest) {
                            SourceProvider provider = mock(SourceProvider.class);
                            when(provider.getManifestFile()).thenReturn(manifest);
                            return provider;
                        }

                        @Nullable
                        @Override
                        public AndroidProject getGradleProjectModel() {
                            if (!isGradleProject()) {
                                return null;
                            }

                            File main = new File(mDir, ANDROID_MANIFEST_XML);
                            File debug = new File(mDir, "debug" + File.separator + ANDROID_MANIFEST_XML);
                            File test = new File(mDir, "test" + File.separator + ANDROID_MANIFEST_XML);

                            SourceProvider defaultSourceProvider = createSourceProvider(main);
                            SourceProvider debugSourceProvider = createSourceProvider(debug);
                            SourceProvider testSourceProvider = createSourceProvider(test);

                            ProductFlavorContainer defaultConfig = mock(ProductFlavorContainer.class);
                            when(defaultConfig.getSourceProvider()).thenReturn(defaultSourceProvider);

                            BuildType buildType = mock(BuildType.class);
                            when(buildType.isDebuggable()).thenReturn(true);

                            BuildTypeContainer buildTypeContainer = mock(BuildTypeContainer.class);
                            when(buildTypeContainer.getBuildType()).thenReturn(buildType);
                            when(buildTypeContainer.getSourceProvider()).thenReturn(debugSourceProvider);
                            List<BuildTypeContainer> buildTypes = Lists.newArrayList(buildTypeContainer);

                            SourceProviderContainer extraProvider = mock(SourceProviderContainer.class);
                            when(extraProvider.getArtifactName()).thenReturn(AndroidProject.ARTIFACT_ANDROID_TEST);
                            when(extraProvider.getSourceProvider()).thenReturn(testSourceProvider);
                            List<SourceProviderContainer> extraProviders = Lists.newArrayList(extraProvider);

                            ProductFlavorContainer productFlavorContainer = mock(ProductFlavorContainer.class);
                            when(productFlavorContainer.getExtraSourceProviders()).thenReturn(extraProviders);
                            List<ProductFlavorContainer> productFlavors = Lists.newArrayList(productFlavorContainer);

                            AndroidProject project = mock(AndroidProject.class);
                            when(project.getDefaultConfig()).thenReturn(defaultConfig);
                            when(project.getBuildTypes()).thenReturn(buildTypes);
                            when(project.getProductFlavors()).thenReturn(productFlavors);
                            return project;
                        }
                    };
                }
            };
        } else if (mEnabled.contains(ManifestDetector.GRADLE_OVERRIDES)) {
            return new TestLintClient() {
                @NonNull
                @Override
                protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                    return new Project(this, dir, referenceDir) {
                        @Override
                        public boolean isGradleProject() {
                            return true;
                        }

                        @Nullable
                        @Override
                        public Variant getCurrentVariant() {
                            ProductFlavor flavor = mock(ProductFlavor.class);
                            if (getName().equals("ManifestDetectorTest_testGradleOverridesOk") ||
                                    getName().equals(
                                        "ManifestDetectorTest_testManifestPackagePlaceholder")) {
                                when(flavor.getMinSdkVersion()).thenReturn(null);
                                when(flavor.getTargetSdkVersion()).thenReturn(null);
                                when(flavor.getVersionCode()).thenReturn(null);
                                when(flavor.getVersionName()).thenReturn(null);
                            } else {
                                assertEquals(getName(), "ManifestDetectorTest_testGradleOverrides");

                                ApiVersion apiMock = mock(ApiVersion.class);
                                when(apiMock.getApiLevel()).thenReturn(5);
                                when(apiMock.getApiString()).thenReturn("5");
                                when(flavor.getMinSdkVersion()).thenReturn(apiMock);

                                apiMock = mock(ApiVersion.class);
                                when(apiMock.getApiLevel()).thenReturn(16);
                                when(apiMock.getApiString()).thenReturn("16");
                                when(flavor.getTargetSdkVersion()).thenReturn(apiMock);

                                when(flavor.getVersionCode()).thenReturn(2);
                                when(flavor.getVersionName()).thenReturn("MyName");
                            }

                            Variant mock = mock(Variant.class);
                            when(mock.getMergedFlavor()).thenReturn(flavor);
                            return mock;
                        }
                    };
                }
            };

        }
        return super.createClient();
    }
}
