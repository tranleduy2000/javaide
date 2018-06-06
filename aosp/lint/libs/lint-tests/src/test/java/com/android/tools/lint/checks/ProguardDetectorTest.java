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
public class ProguardDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ProguardDetector();
    }

    public void testProguard() throws Exception {
        assertEquals(
            "proguard.cfg:21: Error: Obsolete ProGuard file; use -keepclasseswithmembers instead of -keepclasseswithmembernames [Proguard]\n" +
            "-keepclasseswithmembernames class * {\n" +
            "^\n" +
            "1 errors, 0 warnings\n" +
            "",
            lintFiles("proguard.cfg"));
    }

    public void testProguardNewPath() throws Exception {
        assertEquals(
            "proguard-project.txt:21: Error: Obsolete ProGuard file; use -keepclasseswithmembers instead of -keepclasseswithmembernames [Proguard]\n" +
            "-keepclasseswithmembernames class * {\n" +
            "^\n" +
            "1 errors, 0 warnings\n" +
            "",
            lintFiles("proguard.cfg=>proguard-project.txt"));
    }

    public void testProguardRandomName() throws Exception {
        assertEquals(
            "myfile.txt:21: Error: Obsolete ProGuard file; use -keepclasseswithmembers instead of -keepclasseswithmembernames [Proguard]\n" +
            "-keepclasseswithmembernames class * {\n" +
            "^\n" +
            "myfile.txt:8: Warning: Local ProGuard configuration contains general Android configuration: Inherit these settings instead? Modify project.properties to define proguard.config=${sdk.dir}/tools/proguard/proguard-android.txt:myfile.txt and then keep only project-specific configuration here [ProguardSplit]\n" +
            "-keep public class * extends android.app.Activity\n" +
            "^\n" +
            "1 errors, 1 warnings\n" +
            "",
            lintProject(
                    "proguard.cfg=>myfile.txt",
                    "proguard.properties=>project.properties"));
    }

    public void testSilent() throws Exception {
        assertEquals(
                "No warnings.",

                lintFiles(
                        "proguard.pro=>proguard.cfg",
                        "project.properties1=>project.properties"));
    }

    public void testSilent2() throws Exception {
        assertEquals(
                "No warnings.",

                lintFiles(
                        "proguard.pro=>proguard.cfg",
                        "project.properties3=>project.properties"));
    }

    public void testSplit() throws Exception {
        assertEquals(
            "proguard.cfg:14: Warning: Local ProGuard configuration contains general Android configuration: Inherit these settings instead? Modify project.properties to define proguard.config=${sdk.dir}/tools/proguard/proguard-android.txt:proguard.cfg and then keep only project-specific configuration here [ProguardSplit]\n" +
            "-keep public class * extends android.app.Activity\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintFiles(
                    "proguard.pro=>proguard.cfg",
                    "project.properties2=>project.properties"));
    }
}
