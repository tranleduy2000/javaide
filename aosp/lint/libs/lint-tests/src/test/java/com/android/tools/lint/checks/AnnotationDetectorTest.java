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
import com.android.tools.lint.detector.api.Issue;

import java.util.List;

@SuppressWarnings("javadoc")
public class AnnotationDetectorTest extends AbstractCheckTest {
    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/WrongAnnotation.java:9: Error: The @SuppressLint annotation cannot be used on a local variable with the lint check 'NewApi': move out to the surrounding method [LocalSuppress]\n" +
            "    public static void foobar(View view, @SuppressLint(\"NewApi\") int foo) { // Invalid: class-file check\n" +
            "                                         ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WrongAnnotation.java:10: Error: The @SuppressLint annotation cannot be used on a local variable with the lint check 'NewApi': move out to the surrounding method [LocalSuppress]\n" +
            "        @SuppressLint(\"NewApi\") // Invalid\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WrongAnnotation.java:12: Error: The @SuppressLint annotation cannot be used on a local variable with the lint check 'NewApi': move out to the surrounding method [LocalSuppress]\n" +
            "        @SuppressLint({\"SdCardPath\", \"NewApi\"}) // Invalid: class-file based check on local variable\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WrongAnnotation.java:14: Error: The @SuppressLint annotation cannot be used on a local variable with the lint check 'NewApi': move out to the surrounding method [LocalSuppress]\n" +
            "        @android.annotation.SuppressLint({\"SdCardPath\", \"NewApi\"}) // Invalid (FQN)\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/WrongAnnotation.java:28: Error: The @SuppressLint annotation cannot be used on a local variable with the lint check 'NewApi': move out to the surrounding method [LocalSuppress]\n" +
            "        @SuppressLint(\"NewApi\")\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "5 errors, 0 warnings\n",

            lintProject(
                "src/test/pkg/WrongAnnotation.java.txt=>src/test/pkg/WrongAnnotation.java"
            ));
    }

    @SuppressWarnings("ClassNameDiffersFromFileName")
    public void testUniqueValues() throws Exception {
        assertEquals(""
                + "src/test/pkg/IntDefTest.java:9: Error: Constants STYLE_NO_INPUT and STYLE_NO_FRAME specify the same exact value (2); this is usually a cut & paste or merge error [UniqueConstants]\n"
                + "    @IntDef({STYLE_NORMAL, STYLE_NO_TITLE, STYLE_NO_FRAME, STYLE_NO_INPUT})\n"
                + "                                                           ~~~~~~~~~~~~~~\n"
                + "    src/test/pkg/IntDefTest.java:9: Previous same value\n"
                + "src/test/pkg/IntDefTest.java:28: Error: Constants FLAG3 and FLAG2 specify the same exact value (562949953421312); this is usually a cut & paste or merge error [UniqueConstants]\n"
                + "    @IntDef({FLAG2, FLAG3, FLAG1})\n"
                + "                    ~~~~~\n"
                + "    src/test/pkg/IntDefTest.java:28: Previous same value\n"
                + "2 errors, 0 warnings\n",

                lintProject(
                        java("src/test/pkg/IntDefTest.java", ""
                                + "package test.pkg;\n"
                                + "import android.support.annotation.IntDef;\n"
                                + "\n"
                                + "import java.lang.annotation.Retention;\n"
                                + "import java.lang.annotation.RetentionPolicy;\n"
                                + "\n"
                                + "@SuppressLint(\"UnusedDeclaration\")\n"
                                + "public class IntDefTest {\n"
                                + "    @IntDef({STYLE_NORMAL, STYLE_NO_TITLE, STYLE_NO_FRAME, STYLE_NO_INPUT})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface DialogStyle {}\n"
                                + "\n"
                                + "    public static final int STYLE_NORMAL = 0;\n"
                                + "    public static final int STYLE_NO_TITLE = 1;\n"
                                + "    public static final int STYLE_NO_FRAME = 2;\n"
                                + "    public static final int STYLE_NO_INPUT = 2;\n"
                                + "\n"
                                + "    @IntDef({STYLE_NORMAL, STYLE_NO_TITLE, STYLE_NO_FRAME, STYLE_NO_INPUT})\n"
                                + "    @SuppressWarnings(\"UniqueConstants\")\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface SuppressedDialogStyle {}\n"
                                + "\n"
                                + "\n"
                                + "    public static final long FLAG1 = 0x100000000000L;\n"
                                + "    public static final long FLAG2 = 0x0002000000000000L;\n"
                                + "    public static final long FLAG3 = 0x2000000000000L;\n"
                                + "\n"
                                + "    @IntDef({FLAG2, FLAG3, FLAG1})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags {}\n"
                                + "\n"

                                + ""
                                + "}"),
                        copy("src/android/support/annotation/IntDef.java.txt",
                                "src/android/support/annotation/IntDef.java")));
    }

    @SuppressWarnings("ClassNameDiffersFromFileName")
    public void testFlagStyle() throws Exception {
        assertEquals(""
                + "src/test/pkg/IntDefTest.java:13: Warning: Consider declaring this constant using 1 << 44 instead [ShiftFlags]\n"
                + "    public static final long FLAG5 = 0x100000000000L;\n"
                + "                                     ~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:14: Warning: Consider declaring this constant using 1 << 49 instead [ShiftFlags]\n"
                + "    public static final long FLAG6 = 0x0002000000000000L;\n"
                + "                                     ~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:15: Warning: Consider declaring this constant using 1 << 3 instead [ShiftFlags]\n"
                + "    public static final long FLAG7 = 8L;\n"
                + "                                     ~~\n"
                + "0 errors, 3 warnings\n",
                lintProject(
                        java("src/test/pkg/IntDefTest.java", ""
                                + "package test.pkg;\n"
                                + "import android.support.annotation.IntDef;\n"
                                + "\n"
                                + "import java.lang.annotation.Retention;\n"
                                + "import java.lang.annotation.RetentionPolicy;\n"
                                + "\n"
                                + "@SuppressWarnings(\"unused\")\n"
                                + "public class IntDefTest {\n"
                                + "    public static final long FLAG1 = 1;\n"
                                + "    public static final long FLAG2 = 2;\n"
                                + "    public static final long FLAG3 = 1 << 2;\n"
                                + "    public static final long FLAG4 = 1 << 3;\n"
                                + "    public static final long FLAG5 = 0x100000000000L;\n"
                                + "    public static final long FLAG6 = 0x0002000000000000L;\n"
                                + "    public static final long FLAG7 = 8L;\n"
                                + "    public static final long FLAG8 = 9L;\n"
                                + "    public static final long FLAG9 = 0;\n"
                                + "    public static final long FLAG10 = 1;\n"
                                + "    public static final long FLAG11 = -1;\n"
                                + "\n"
                                // Not a flag (missing flag=true)
                                + "    @IntDef({FLAG1, FLAG2, FLAG3})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags1 {}\n"
                                + "\n"
                                // OK: Too few values
                                + "    @IntDef(flag = true, value={FLAG1, FLAG2})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags2 {}\n"
                                + "\n"
                                // OK: Allow 0, 1, -1
                                + "    @IntDef(flag = true, value={FLAG9, FLAG10, FLAG11})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags3 {}\n"
                                + "\n"
                                // OK: Already using shifts
                                + "    @IntDef(flag = true, value={FLAG1, FLAG3, FLAG4})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags4 {}\n"
                                + "\n"
                                // Wrong: should be flagged
                                + "    @IntDef(flag = true, value={FLAG5, FLAG6, FLAG7, FLAG8})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    private @interface Flags5 {}\n"
                                + "}"),
                        copy("src/android/support/annotation/IntDef.java.txt",
                                "src/android/support/annotation/IntDef.java")));
    }

    @Override
    protected Detector getDetector() {
        return new AnnotationDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        List<Issue> issues = super.getIssues();

        // Need these issues on to be found by the registry as well to look up scope
        // in id references (these ids are referenced in the unit test java file below)
        issues.add(ApiDetector.UNSUPPORTED);
        issues.add(SdCardDetector.ISSUE);

        return issues;
    }
}
