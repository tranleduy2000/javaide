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
import com.android.tools.lint.detector.api.Issue;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings("javadoc")
public class UnusedResourceDetectorTest extends AbstractCheckTest {
    private boolean mEnableIds = false;

    @Override
    protected Detector getDetector() {
        return new UnusedResourceDetector();
    }

    @Override
    protected boolean isEnabled(Issue issue) {
        if (issue == UnusedResourceDetector.ISSUE_IDS) {
            return mEnableIds;
        } else {
            return true;
        }
    }

    public void testUnused() throws Exception {
        mEnableIds = false;
        assertEquals(
           "res/layout/accessibility.xml: Warning: The resource R.layout.accessibility appears to be unused [UnusedResources]\n" +
           "res/layout/main.xml: Warning: The resource R.layout.main appears to be unused [UnusedResources]\n" +
           "res/layout/other.xml: Warning: The resource R.layout.other appears to be unused [UnusedResources]\n" +
           "res/values/strings2.xml:3: Warning: The resource R.string.hello appears to be unused [UnusedResources]\n" +
           "    <string name=\"hello\">Hello</string>\n" +
           "            ~~~~~~~~~~~~\n" +
           "0 errors, 4 warnings\n" +
           "",

            lintProject(
                "res/values/strings2.xml",
                "res/layout/layout1.xml=>res/layout/main.xml",
                "res/layout/layout1.xml=>res/layout/other.xml",

                // Rename .txt files to .java
                "src/my/pkg/Test.java.txt=>src/my/pkg/Test.java",
                "gen/my/pkg/R.java.txt=>gen/my/pkg/R.java",
                "AndroidManifest.xml",
                "res/layout/accessibility.xml"));
    }

    public void testUnusedIds() throws Exception {
        mEnableIds = true;

        assertEquals(
           "res/layout/accessibility.xml: Warning: The resource R.layout.accessibility appears to be unused [UnusedResources]\n" +
           "Warning: The resource R.layout.main appears to be unused [UnusedResources]\n" +
           "Warning: The resource R.layout.other appears to be unused [UnusedResources]\n" +
           "Warning: The resource R.string.hello appears to be unused [UnusedResources]\n" +
           "res/layout/accessibility.xml:2: Warning: The resource R.id.newlinear appears to be unused [UnusedIds]\n" +
           "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\" android:id=\"@+id/newlinear\" android:orientation=\"vertical\" android:layout_width=\"match_parent\" android:layout_height=\"match_parent\">\n" +
           "                                                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
           "res/layout/accessibility.xml:3: Warning: The resource R.id.button1 appears to be unused [UnusedIds]\n" +
           "    <Button android:text=\"Button\" android:id=\"@+id/button1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"></Button>\n" +
           "                                  ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
           "res/layout/accessibility.xml:4: Warning: The resource R.id.android_logo appears to be unused [UnusedIds]\n" +
           "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n" +
           "               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
           "res/layout/accessibility.xml:5: Warning: The resource R.id.android_logo2 appears to be unused [UnusedIds]\n" +
           "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n" +
           "                                                         ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
           "Warning: The resource R.id.imageView1 appears to be unused [UnusedIds]\n" +
           "Warning: The resource R.id.include1 appears to be unused [UnusedIds]\n" +
           "Warning: The resource R.id.linearLayout2 appears to be unused [UnusedIds]\n" +
           "0 errors, 11 warnings\n",

            lintProject(
                // Rename .txt files to .java
                "src/my/pkg/Test.java.txt=>src/my/pkg/Test.java",
                "gen/my/pkg/R.java.txt=>gen/my/pkg/R.java",
                "AndroidManifest.xml",
                "res/layout/accessibility.xml"));
    }

    public void testArrayReference() throws Exception {
        assertEquals(
           "res/values/arrayusage.xml:3: Warning: The resource R.array.my_array appears to be unused [UnusedResources]\n" +
           "<string-array name=\"my_array\">\n" +
           "              ~~~~~~~~~~~~~~~\n" +
           "0 errors, 1 warnings\n" +
           "",

            lintProject(
                "AndroidManifest.xml",
                "res/values/arrayusage.xml"));
    }

    public void testAttrs() throws Exception {
        assertEquals(
           "res/layout/customattrlayout.xml: Warning: The resource R.layout.customattrlayout appears to be unused [UnusedResources]\n" +
           "0 errors, 1 warnings\n" +
           "",

            lintProject(
                "res/values/customattr.xml",
                "res/layout/customattrlayout.xml",
                "unusedR.java.txt=>gen/my/pkg/R.java",
                "AndroidManifest.xml"));
    }

    public void testMultiProjectIgnoreLibraries() throws Exception {
        assertEquals(
           "No warnings.",

            lintProject(
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java",

                // Library project
                "multiproject/library-manifest.xml=>../LibraryProject/AndroidManifest.xml",
                "multiproject/library.properties=>../LibraryProject/project.properties",
                "multiproject/LibraryCode.java.txt=>../LibraryProject/src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>../LibraryProject/res/values/strings.xml"
            ));
    }

    public void testMultiProject() throws Exception {
        File master = getProjectDir("MasterProject",
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals(
           // string1 is defined and used in the library project
           // string2 is defined in the library project and used in the master project
           // string3 is defined in the library project and not used anywhere
           "LibraryProject/res/values/strings.xml:7: Warning: The resource R.string.string3 appears to be unused [UnusedResources]\n" +
           "    <string name=\"string3\">String 3</string>\n" +
           "            ~~~~~~~~~~~~~~\n" +
           "0 errors, 1 warnings\n",

           checkLint(Arrays.asList(master, library)).replace("/TESTROOT/", ""));
    }

    public void testFqcnReference() throws Exception {
        assertEquals(
           "No warnings.",

            lintProject(
                "res/layout/layout1.xml=>res/layout/main.xml",
                "src/test/pkg/UnusedReference.java.txt=>src/test/pkg/UnusedReference.java",
                "AndroidManifest.xml"));
    }

    public void testIgnoreXmlDrawable() throws Exception {
        assertEquals(
           "No warnings.",

            lintProject(
                    "res/drawable/ic_menu_help.xml",
                    "gen/my/pkg/R2.java.txt=>gen/my/pkg/R.java"
            ));
    }

    public void testPlurals() throws Exception {
        assertEquals(
           "res/values/plurals.xml:3: Warning: The resource R.plurals.my_plural appears to be unused [UnusedResources]\n" +
           "    <plurals name=\"my_plural\">\n" +
           "             ~~~~~~~~~~~~~~~~\n" +
           "0 errors, 1 warnings\n" +
           "",

            lintProject(
                "res/values/strings4.xml",
                "res/values/plurals.xml",
                "AndroidManifest.xml"));
    }

    public void testNoMerging() throws Exception {
        // http://code.google.com/p/android/issues/detail?id=36952

        File master = getProjectDir("MasterProject",
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals(
           // The strings are all referenced in the library project's manifest file
           // which in this project is merged in
           "LibraryProject/res/values/strings.xml:7: Warning: The resource R.string.string3 appears to be unused [UnusedResources]\n" +
           "    <string name=\"string3\">String 3</string>\n" +
           "            ~~~~~~~~~~~~~~\n" +
           "0 errors, 1 warnings\n",

           checkLint(Arrays.asList(master, library)).replace("/TESTROOT/", ""));
    }

    public void testLibraryMerging() throws Exception {
        // http://code.google.com/p/android/issues/detail?id=36952
        File master = getProjectDir("MasterProject",
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main-merge.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals(
           // The strings are all referenced in the library project's manifest file
           // which in this project is merged in
           "No warnings.",

           checkLint(Arrays.asList(master, library)));
    }

    public void testCornerCase() throws Exception {
        // See http://code.google.com/p/projectlombok/issues/detail?id=415
        mEnableIds = true;
        assertEquals(
            "No warnings.",

             lintProject(
                 "src/test/pkg/Foo.java.txt=>src/test/pkg/Foo.java",
                 "AndroidManifest.xml"));
    }

    public void testAnalytics() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=42565
        mEnableIds = false;
        assertEquals(
                "No warnings.",

                lintProject(
                        "res/values/analytics.xml"
        ));
    }

    public void testIntegers() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=53995
        mEnableIds = true;
        assertEquals(
                "No warnings.",

                lintProject(
                        "res/values/integers.xml",
                        "res/anim/slide_in_out.xml"
                ));
    }

    public void testIntegerArrays() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=59761
        mEnableIds = false;
        assertEquals(
                "No warnings.",
                lintProject("res/values/integer_arrays.xml=>res/values/integer_arrays.xml"));
    }

    public void testUnitTestReferences() throws Exception {
        // Make sure that we pick up references in unit tests as well
        // Regression test for
        // https://code.google.com/p/android/issues/detail?id=79066
        mEnableIds = false;
        //noinspection ClassNameDiffersFromFileName
        assertEquals("No warnings.",

                lintProject(
                        copy("res/values/strings2.xml"),
                        copy("res/layout/layout1.xml", "res/layout/main.xml"),
                        copy("res/layout/layout1.xml", "res/layout/other.xml"),

                        copy("src/my/pkg/Test.java.txt", "src/my/pkg/Test.java"),
                        copy("gen/my/pkg/R.java.txt", "gen/my/pkg/R.java"),
                        copy("AndroidManifest.xml"),
                        copy("res/layout/accessibility.xml"),

                        // Add unit test source which references resources which would otherwise
                        // be marked as unused
                        java("test/my/pkg/MyTest.java", ""
                                + "package my.pkg;\n"
                                + "class MyTest {\n"
                                + "    public void test() {\n"
                                + "        System.out.println(R.layout.accessibility);\n"
                                + "        System.out.println(R.layout.main);\n"
                                + "        System.out.println(R.layout.other);\n"
                                + "        System.out.println(R.string.hello);\n"
                                + "    }\n"
                                + "}\n")
                        ));
    }
}
