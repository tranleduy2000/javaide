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
public class SdCardDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new SdCardDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/SdCardTest.java:13: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            " private static final String SDCARD_TEST_HTML = \"/sdcard/test.html\";\n" +
            "                                                ~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:14: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            " public static final String SDCARD_ROOT = \"/sdcard\";\n" +
            "                                          ~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:15: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            " public static final String PACKAGES_PATH = \"/sdcard/o/packages/\";\n" +
            "                                            ~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:16: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            " File deviceDir = new File(\"/sdcard/vr\");\n" +
            "                           ~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:20: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "   android.os.Debug.startMethodTracing(\"/sdcard/launcher\");\n" +
            "                                       ~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:22: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  if (new File(\"/sdcard\").exists()) {\n" +
            "               ~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:24: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  String FilePath = \"/sdcard/\" + new File(\"test\");\n" +
            "                    ~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:29: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  intent.setDataAndType(Uri.parse(\"file://sdcard/foo.json\"), \"application/bar-json\");\n" +
            "                                  ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:30: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  intent.putExtra(\"path-filter\", \"/sdcard(/.+)*\");\n" +
            "                                 ~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:31: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  intent.putExtra(\"start-dir\", \"/sdcard\");\n" +
            "                               ~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:32: Warning: Do not hardcode \"/data/\"; use Context.getFilesDir().getPath() instead [SdCardPath]\n" +
            "  String mypath = \"/data/data/foo\";\n" +
            "                  ~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:33: Warning: Do not hardcode \"/data/\"; use Context.getFilesDir().getPath() instead [SdCardPath]\n" +
            "  String base = \"/data/data/foo.bar/test-profiling\";\n" +
            "                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/SdCardTest.java:34: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  String s = \"file://sdcard/foo\";\n" +
            "             ~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 13 warnings\n",

            lintProject("src/test/pkg/SdCardTest.java.txt=>src/test/pkg/SdCardTest.java"));
    }

    public void testSuppress() throws Exception {
        assertEquals(
            // The only reference in the file not covered by an annotation
            "src/test/pkg/SuppressTest5.java:40: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "  String notAnnotated = \"/sdcard/mypath\";\n" +
            "                        ~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            // File with lots of /sdcard references, but with @SuppressLint warnings
            // on fields, methods, variable declarations etc
            lintProject("src/test/pkg/SuppressTest5.java.txt=>src/test/pkg/SuppressTest5.java"));
    }

    public void testUtf8Bom() throws Exception {
        assertEquals(
            "src/test/pkg/Utf8BomTest.java:4: Warning: Do not hardcode \"/sdcard/\"; use Environment.getExternalStorageDirectory().getPath() instead [SdCardPath]\n" +
            "    String s = \"/sdcard/mydir\";\n" +
            "               ~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject("src/test/pkg/Utf8BomTest.java.data=>src/test/pkg/Utf8BomTest.java"));
    }
}
