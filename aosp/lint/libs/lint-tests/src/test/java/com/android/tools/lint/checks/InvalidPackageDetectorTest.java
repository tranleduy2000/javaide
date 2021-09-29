/*
 * Copyright (C) 2012 The Android Open Source Project
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
public class InvalidPackageDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new InvalidPackageDetector();
    }

    public void testUnsupportedJavaLibraryCode() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=39109
        assertEquals(
            "libs/unsupported.jar: Error: Invalid package reference in library; not included in Android: java.awt. Referenced from test.pkg.LibraryClass. [InvalidPackage]\n" +
            "libs/unsupported.jar: Error: Invalid package reference in library; not included in Android: javax.swing. Referenced from test.pkg.LibraryClass. [InvalidPackage]\n" +
            "2 errors, 0 warnings\n",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "apicheck/layout.xml=>res/layout/layout.xml",
                    "apicheck/themes.xml=>res/values/themes.xml",
                    "apicheck/themes.xml=>res/color/colors.xml",
                    "apicheck/unsupported.jar.data=>libs/unsupported.jar"
                ));
    }

    public void testOk() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk2.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "bytecode/GetterTest.jar.data=>libs/GetterTest.jar",
                "bytecode/classes.jar=>libs/classes.jar"
            ));
    }

    public void testLibraryInJavax() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "apicheck/layout.xml=>res/layout/layout.xml",
                "apicheck/themes.xml=>res/values/themes.xml",
                "apicheck/themes.xml=>res/color/colors.xml",
                "bytecode/javax.jar.data=>libs/javax.jar"
            ));
    }

    public void testAnnotationProcessors1() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=64014
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "apicheck/layout.xml=>res/layout/layout.xml",
                "apicheck/themes.xml=>res/values/themes.xml",
                "apicheck/themes.xml=>res/color/colors.xml",
                "bytecode/butterknife-2.0.1.jar.data=>libs/butterknife-2.0.1.jar"
            ));
    }

    public void testAnnotationProcessors2() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=64014
        assertEquals(
            "No warnings.",

            lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "apicheck/layout.xml=>res/layout/layout.xml",
                "apicheck/themes.xml=>res/values/themes.xml",
                "apicheck/themes.xml=>res/color/colors.xml",
                "bytecode/dagger-compiler-1.2.1-subset.jar.data=>libs/dagger-compiler-1.2.1.jar"
            ));
    }
}
