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
public class OverdrawDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new OverdrawDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/main.xml:5: Warning: Possible overdraw: Root element paints background @drawable/ic_launcher with a theme that also paints a background (inferred theme is @style/MyTheme_First) [Overdraw]\n" +
            "    android:background=\"@drawable/ic_launcher\"\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/second.xml:5: Warning: Possible overdraw: Root element paints background @drawable/ic_launcher with a theme that also paints a background (inferred theme is @style/MyTheme) [Overdraw]\n" +
            "    android:background=\"@drawable/ic_launcher\"\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/sixth.xml:4: Warning: Possible overdraw: Root element paints background @drawable/custombg with a theme that also paints a background (inferred theme is @style/MyTheme) [Overdraw]\n" +
            "    android:background=\"@drawable/custombg\"\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/third.xml:5: Warning: Possible overdraw: Root element paints background @drawable/ic_launcher with a theme that also paints a background (inferred theme is @style/MyTheme_Third) [Overdraw]\n" +
            "    android:background=\"@drawable/ic_launcher\"\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n" +
            "",

            lintProject(
                "overdraw/.classpath=>.classpath",
                "overdraw/.project=>.project",
                "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                "overdraw/project.properties=>project.properties",
                "overdraw/res/drawable/custombg.xml=>res/drawable/custombg.xml",
                "overdraw/res/drawable/custombg2.xml=>res/drawable/custombg2.xml",
                "overdraw/res/drawable-hdpi/ic_launcher.png=>res/drawable-hdpi/ic_launcher.png",
                "overdraw/res/drawable-ldpi/ic_launcher.png=>res/drawable-ldpi/ic_launcher.png",
                "overdraw/res/drawable-mdpi/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                "overdraw/res/layout/sixth.xml=>res/layout/sixth.xml",
                "overdraw/res/layout/fifth.xml=>res/layout/fifth.xml",
                "overdraw/res/layout/fourth.xml=>res/layout/fourth.xml",
                "overdraw/res/layout/main.xml=>res/layout/main.xml",
                "overdraw/res/layout/second.xml=>res/layout/second.xml",
                "overdraw/res/layout/third.xml=>res/layout/third.xml",
                "overdraw/res/values/strings.xml=>res/values/strings.xml",
                "overdraw/res/values/styles.xml=>res/values/styles.xml",

                // Java files must be renamed in source tree
                "overdraw/gen/test/pkg/BuildConfig.java.txt=>gen/test/pkg/BuildConfig.java",
                "overdraw/gen/test/pkg/R.java.txt=>gen/test/pkg/R.java",
                "overdraw/src/test/pkg/FourthActivity.java.txt=>src/test/pkg/FourthActivity.java",
                "overdraw/src/test/pkg/OverdrawActivity.java.txt=>src/test/pkg/OverdrawActivity.java",
                "overdraw/src/test/pkg/SecondActivity.java.txt=>src/test/pkg/SecondActivity.java",
                "overdraw/src/test/pkg/ThirdActivity.java.txt=>src/test/pkg/ThirdActivity.java"
            ));
    }

    public void testSuppressed() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "overdraw/.classpath=>.classpath",
                "overdraw/.project=>.project",
                "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                "overdraw/project.properties=>project.properties",
                "overdraw/res/drawable/custombg.xml=>res/drawable/custombg.xml",
                "overdraw/res/drawable/custombg2.xml=>res/drawable/custombg2.xml",
                "overdraw/res/drawable-hdpi/ic_launcher.png=>res/drawable-hdpi/ic_launcher.png",
                "overdraw/res/drawable-ldpi/ic_launcher.png=>res/drawable-ldpi/ic_launcher.png",
                "overdraw/res/drawable-mdpi/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                "overdraw/res/layout/main_ignore.xml=>res/layout/main.xml",
                "overdraw/res/values/strings.xml=>res/values/strings.xml",
                "overdraw/res/values/styles.xml=>res/values/styles.xml",

                // Java files must be renamed in source tree
                "overdraw/gen/test/pkg/BuildConfig.java.txt=>gen/test/pkg/BuildConfig.java",
                "overdraw/gen/test/pkg/R.java.txt=>gen/test/pkg/R.java",
                "overdraw/src/test/pkg/FourthActivity.java.txt=>src/test/pkg/FourthActivity.java",
                "overdraw/src/test/pkg/OverdrawActivity.java.txt=>src/test/pkg/OverdrawActivity.java",
                "overdraw/src/test/pkg/SecondActivity.java.txt=>src/test/pkg/SecondActivity.java",
                "overdraw/src/test/pkg/ThirdActivity.java.txt=>src/test/pkg/ThirdActivity.java"
            ));
    }

    public void testContextAttribute() throws Exception {
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "overdraw/.classpath=>.classpath",
                        "overdraw/.project=>.project",
                        "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                        "overdraw/project.properties=>project.properties",
                        "overdraw/res/drawable-hdpi/ic_launcher.png=>res/drawable-hdpi/ic_launcher.png",
                        "overdraw/res/drawable-ldpi/ic_launcher.png=>res/drawable-ldpi/ic_launcher.png",
                        "overdraw/res/drawable-mdpi/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                        "overdraw/res/layout/fourth_context.xml=>res/layout/fourth.xml",
                        "overdraw/res/values/strings.xml=>res/values/strings.xml",
                        "overdraw/res/values/styles.xml=>res/values/styles.xml"
                ));
    }

    public void testContextAttribute2() throws Exception {
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "overdraw/.classpath=>.classpath",
                        "overdraw/.project=>.project",
                        "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                        "overdraw/project.properties=>project.properties",
                        "overdraw/res/drawable-hdpi/ic_launcher.png=>res/drawable-hdpi/ic_launcher.png",
                        "overdraw/res/drawable-ldpi/ic_launcher.png=>res/drawable-ldpi/ic_launcher.png",
                        "overdraw/res/drawable-mdpi/ic_launcher.png=>res/drawable-mdpi/ic_launcher.png",
                        "overdraw/res/layout/fourth_context2.xml=>res/layout/fourth.xml",
                        "overdraw/res/values/strings.xml=>res/values/strings.xml",
                        "overdraw/res/values/styles.xml=>res/values/styles.xml"
                ));
    }

    public void testNull() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=71197
        // @null as a background should not trigger a warning
        assertEquals(
                "No warnings.",

                lintProject(
                        "overdraw/.classpath=>.classpath",
                        "overdraw/.project=>.project",
                        "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                        "overdraw/project.properties=>project.properties",
                        "overdraw/res/layout/nullbackground.xml=>res/layout/null.xml",
                        "overdraw/res/values/strings.xml=>res/values/strings.xml",
                        "overdraw/res/values/styles.xml=>res/values/styles.xml"
                ));
    }

    public void testToolsBackground() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=80679
        // tools:background instead of android:background should not trigger a warning
        assertEquals(
                "No warnings.",

                lintProject(
                        "overdraw/.classpath=>.classpath",
                        "overdraw/.project=>.project",
                        "overdraw/AndroidManifest.xml=>AndroidManifest.xml",
                        "overdraw/project.properties=>project.properties",
                        "overdraw/res/layout/tools_background.xml=>res/layout/tools_background.xml",
                        "overdraw/res/values/strings.xml=>res/values/strings.xml",
                        "overdraw/res/values/styles.xml=>res/values/styles.xml"
                ));
    }}