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

package com.android.tools.lint.checks;

import com.android.tools.lint.detector.api.Detector;

public class PreferenceActivityDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new PreferenceActivityDetector();
    }

    public void testWarningWhenImplicitlyExportingPreferenceActivity() throws Exception {
        assertEquals(
            "AndroidManifest.xml:28: Warning: PreferenceActivity should not be exported [ExportedPreferenceActivity]\n"
                + "        <activity\n"
                + "        ^\n"
                + "0 errors, 1 warnings\n",
            lintProject(
                "export_preference_activity_implicit.xml=>AndroidManifest.xml"
            ));
    }

    public void testWarningWhenExplicitlyExportingPreferenceActivity() throws Exception {
        assertEquals(
            "AndroidManifest.xml:28: Warning: PreferenceActivity should not be exported [ExportedPreferenceActivity]\n"
                    + "        <activity\n"
                    + "        ^\n"
                    + "0 errors, 1 warnings\n",
            lintProject(
                    "export_preference_activity_explicit.xml=>AndroidManifest.xml"
            ));
    }

    public void testNoWarningWhenExportingNonPreferenceActivity() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject(
                "AndroidManifest.xml=>AndroidManifest.xml"
            ));
    }

    public void testNoWarningWhenSuppressed() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "export_preference_activity_suppressed.xml=>AndroidManifest.xml"
                ));
    }

    public void testWarningWhenImplicitlyExportingPreferenceActivitySubclass() throws Exception {
        assertEquals(
            "AndroidManifest.xml:28: Warning: PreferenceActivity subclass test.pkg.PreferenceActivitySubclass should not be exported [ExportedPreferenceActivity]\n"
                + "        <activity\n"
                + "        ^\n"
                + "0 errors, 1 warnings\n",
            lintProject(
                "android/PreferenceActivity.java.txt=>src/android/app/PreferenceActivity.java",
                "src/test/pkg/PreferenceActivitySubclass.java.txt=>src/test/pkg/PreferenceActivitySubclass.java",
                "export_preference_activity_subclass_implicit.xml=>AndroidManifest.xml"
            ));
    }

    public void testWarningWhenExplicitlyExportingPreferenceActivitySubclass() throws Exception {
        assertEquals(
            "AndroidManifest.xml:28: Warning: PreferenceActivity subclass test.pkg.PreferenceActivitySubclass should not be exported [ExportedPreferenceActivity]\n"
                + "        <activity\n"
                + "        ^\n"
                + "0 errors, 1 warnings\n",
            lintProject(
                "android/PreferenceActivity.java.txt=>src/android/app/PreferenceActivity.java",
                "src/test/pkg/PreferenceActivitySubclass.java.txt=>src/test/pkg/PreferenceActivitySubclass.java",
                "export_preference_activity_subclass_explicit.xml=>AndroidManifest.xml"
            ));
    }

    public void testNoWarningWhenActivityNotExported() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject(
                "export_preference_activity_no_export.xml=>AndroidManifest.xml"
            ));
    }

    public void testWarningWhenTargetSDK19ButNoIsValidFragmentOverridden() throws Exception {
        assertEquals(
                "AndroidManifest.xml:30: Warning: PreferenceActivity subclass test.pkg.PreferenceActivitySubclass should not be exported [ExportedPreferenceActivity]\n"
                        + "        <activity\n"
                        + "        ^\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        "android/PreferenceActivity.java.txt=>src/android/app/PreferenceActivity.java",
                        "src/test/pkg/PreferenceActivitySubclass.java.txt=>src/test/pkg/PreferenceActivitySubclass.java",
                        "export_preference_activity_subclass_target_sdk_19.xml=>AndroidManifest.xml"
                ));
    }

    public void testNoWarningWhenTargetSDK19AndIsValidFragmentOverridden() throws Exception {
        assertEquals(
                "No warnings.",
                lintProject(
                        "android/PreferenceActivity.java.txt=>src/android/app/PreferenceActivity.java",
                        "src/test/pkg/PreferenceActivitySubclassOverridesIsValidFragment.java.txt=>src/test/pkg/PreferenceActivitySubclass.java",
                        "export_preference_activity_subclass_target_sdk_19.xml=>AndroidManifest.xml"
                ));
    }
}
