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

public class SignatureOrSystemDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new SignatureOrSystemDetector();
    }

    public void testNoWarningOnProtectionLevelsOtherThanSignatureOrSystem() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject(
                "protection_level_ok.xml=>AndroidManifest.xml")
        );
    }

    public void testWarningOnSignatureOrSystemProtectionLevel() throws Exception {
        assertEquals(
            "AndroidManifest.xml:13: Warning: protectionLevel should probably not be set to signatureOrSystem [SignatureOrSystemPermissions]\n"
                + "                android:protectionLevel=\"signatureOrSystem\"/>\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
            lintProject(
                "protection_level_fail.xml=>AndroidManifest.xml")
        );
    }
}
