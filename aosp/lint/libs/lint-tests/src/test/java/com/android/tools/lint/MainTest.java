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

package com.android.tools.lint;

import static com.android.tools.lint.LintCliFlags.ERRNO_EXISTS;
import static com.android.tools.lint.LintCliFlags.ERRNO_INVALID_ARGS;
import static com.android.tools.lint.LintCliFlags.ERRNO_SUCCESS;

import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.AccessibilityDetector;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.security.Permission;
import java.util.List;

@SuppressWarnings("javadoc")
public class MainTest extends AbstractCheckTest {
    protected String checkLint(String[] args, List<File> files) throws Exception {
        PrintStream previousOut = System.out;
        try {
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));

            Main.main(args);

            return output.toString();
        } finally {
            System.setOut(previousOut);
        }
    }

    private void checkDriver(String expectedOutput, String expectedError, int expectedExitCode,
            String[] args)
            throws Exception {
        PrintStream previousOut = System.out;
        PrintStream previousErr = System.err;
        try {
            // Trap System.exit calls:
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm)
                {
                        // allow anything.
                }
                @Override
                public void checkPermission(Permission perm, Object context)
                {
                        // allow anything.
                }
                @Override
                public void checkExit(int status) {
                    throw new ExitException(status);
                }
            });

            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.setOut(new PrintStream(output));
            final ByteArrayOutputStream error = new ByteArrayOutputStream();
            System.setErr(new PrintStream(error));

            int exitCode = 0xCAFEBABE; // not set
            try {
                Main.main(args);
            } catch (ExitException e) {
                // Allow
                exitCode = e.getStatus();
            }

            assertEquals(expectedError, cleanup(error.toString()));
            assertEquals(expectedOutput, cleanup(output.toString()));
            assertEquals(expectedExitCode, exitCode);
        } finally {
            // Re-enable system exit for unit test
            System.setSecurityManager(null);

            System.setOut(previousOut);
            System.setErr(previousErr);
        }
    }

    public void testArguments() throws Exception {
        checkDriver(
        // Expected output
        "\n" +
        "Scanning MainTest_testArguments: .\n" +
        "res/layout/accessibility.xml:4: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n" +
        "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n" +
        "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
        "res/layout/accessibility.xml:5: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n" +
        "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n" +
        "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
        "0 errors, 2 warnings\n",

        // Expected error
        "",

        // Expected exit code
        ERRNO_SUCCESS,

        // Args
        new String[] {
                "--check",
                "ContentDescription",
                "--disable",
                "LintError",
                getProjectDir(null, "res/layout/accessibility.xml").getPath()

        });
    }

    public void testShowDescription() throws Exception {
        checkDriver(
        // Expected output
        "NewApi\n" +
        "------\n" +
        "Summary: Calling new methods on older versions\n" +
        "\n" +
        "Priority: 6 / 10\n" +
        "Severity: Error\n" +
        "Category: Correctness\n" +
        "\n" +
        "This check scans through all the Android API calls in the application and\n" +
        "warns about any calls that are not available on all versions targeted by this\n" +
        "application (according to its minimum SDK attribute in the manifest).\n" +
        "\n" +
        "If you really want to use this API and don't need to support older devices\n" +
        "just set the minSdkVersion in your build.gradle or AndroidManifest.xml files.\n" +
        "\n" +
        "If your code is deliberately accessing newer APIs, and you have ensured (e.g.\n" +
        "with conditional execution) that this code will only ever be called on a\n" +
        "supported platform, then you can annotate your class or method with the\n" +
        "@TargetApi annotation specifying the local minimum SDK to apply, such as\n" +
        "@TargetApi(11), such that this check considers 11 rather than your manifest\n" +
        "file's minimum SDK as the required API level.\n" +
        "\n" +
        "If you are deliberately setting android: attributes in style definitions, make\n" +
        "sure you place this in a values-vNN folder in order to avoid running into\n" +
        "runtime conflicts on certain devices where manufacturers have added custom\n" +
        "attributes whose ids conflict with the new ones on later platforms.\n" +
        "\n" +
        "Similarly, you can use tools:targetApi=\"11\" in an XML file to indicate that\n" +
        "the element will only be inflated in an adequate context.\n" +
        "\n" +
        "\n",

        // Expected error
        "",

        // Expected exit code
        ERRNO_SUCCESS,

        // Args
        new String[] {
                "--show",
                "NewApi"
        });
    }

    public void testShowDescriptionWithUrl() throws Exception {
        checkDriver(""
                // Expected output
                + "SdCardPath\n"
                + "----------\n"
                + "Summary: Hardcoded reference to /sdcard\n"
                + "\n"
                + "Priority: 6 / 10\n"
                + "Severity: Warning\n"
                + "Category: Correctness\n"
                + "\n"
                + "Your code should not reference the /sdcard path directly; instead use\n"
                + "Environment.getExternalStorageDirectory().getPath().\n"
                + "\n"
                + "Similarly, do not reference the /data/data/ path directly; it can vary in\n"
                + "multi-user scenarios. Instead, use Context.getFilesDir().getPath().\n"
                + "\n"
                + "More information: \n"
                + "http://developer.android.com/guide/topics/data/data-storage.html#filesExternal\n"
                + "\n",

                // Expected error
                "",

                // Expected exit code
                ERRNO_SUCCESS,

                // Args
                new String[] {
                        "--show",
                        "SdCardPath"
                });
    }

    public void testNonexistentLibrary() throws Exception {
        checkDriver(
        "",
        "Library foo.jar does not exist.\n",

        // Expected exit code
        ERRNO_INVALID_ARGS,

        // Args
        new String[] {
                "--libraries",
                "foo.jar",
                "prj"

        });
    }

    public void testMultipleProjects() throws Exception {
        File project = getProjectDir(null, "bytecode/classes.jar=>libs/classes.jar");
        checkDriver(
        "",
        "The --sources, --classpath, --libraries and --resources arguments can only be used with a single project\n",

        // Expected exit code
        ERRNO_INVALID_ARGS,

        // Args
        new String[] {
                "--libraries",
                new File(project, "libs/classes.jar").getPath(),
                "--disable",
                "LintError",
                project.getPath(),
                project.getPath()

        });
    }

    public void testCustomResourceDirs() throws Exception {
        File project = getProjectDir(null,
                "res/layout/accessibility.xml=>myres1/layout/accessibility1.xml",
                "res/layout/accessibility.xml=>myres2/layout/accessibility1.xml"
        );

        checkDriver(
                "\n"
                + "Scanning MainTest_testCustomResourceDirs: ..\n"
                + "myres1/layout/accessibility1.xml:4: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres2/layout/accessibility1.xml:4: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres1/layout/accessibility1.xml:5: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres2/layout/accessibility1.xml:5: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 4 warnings\n", // Expected output
                "",

                // Expected exit code
                ERRNO_SUCCESS,

                // Args
                new String[] {
                        "--check",
                        "ContentDescription",
                        "--disable",
                        "LintError",
                        "--resources",
                        new File(project, "myres1").getPath(),
                        "--resources",
                        new File(project, "myres2").getPath(),
                        project.getPath(),
                });
    }

    public void testPathList() throws Exception {
        File project = getProjectDir(null,
                "res/layout/accessibility.xml=>myres1/layout/accessibility1.xml",
                "res/layout/accessibility.xml=>myres2/layout/accessibility1.xml"
        );

        checkDriver(
                "\n"
                + "Scanning MainTest_testPathList: ..\n"
                + "myres1/layout/accessibility1.xml:4: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres2/layout/accessibility1.xml:4: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageView android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres1/layout/accessibility1.xml:5: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "myres2/layout/accessibility1.xml:5: Warning: [Accessibility] Missing contentDescription attribute on image [ContentDescription]\n"
                + "    <ImageButton android:importantForAccessibility=\"yes\" android:id=\"@+id/android_logo2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 4 warnings\n", // Expected output
                "",

                // Expected exit code
                ERRNO_SUCCESS,

                // Args
                new String[] {
                        "--check",
                        "ContentDescription",
                        "--disable",
                        "LintError",
                        "--resources",
                        // Combine two paths with a single separator here
                        new File(project, "myres1").getPath()
                            + ':' + new File(project, "myres2").getPath(),
                        project.getPath(),
                });
    }

    public void testClassPath() throws Exception {
        File project = getProjectDir(null,
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.jar.data=>bin/classes.jar"
        );
        checkDriver(
        "\n" +
        "Scanning MainTest_testClassPath: \n" +
        "src/test/bytecode/GetterTest.java:47: Warning: Calling getter method getFoo1() on self is slower than field access (mFoo1) [FieldGetter]\n" +
        "  getFoo1();\n" +
        "  ~~~~~~~\n" +
        "src/test/bytecode/GetterTest.java:48: Warning: Calling getter method getFoo2() on self is slower than field access (mFoo2) [FieldGetter]\n" +
        "  getFoo2();\n" +
        "  ~~~~~~~\n" +
        "src/test/bytecode/GetterTest.java:52: Warning: Calling getter method isBar1() on self is slower than field access (mBar1) [FieldGetter]\n" +
        "  isBar1();\n" +
        "  ~~~~~~\n" +
        "src/test/bytecode/GetterTest.java:54: Warning: Calling getter method getFoo1() on self is slower than field access (mFoo1) [FieldGetter]\n" +
        "  this.getFoo1();\n" +
        "       ~~~~~~~\n" +
        "src/test/bytecode/GetterTest.java:55: Warning: Calling getter method getFoo2() on self is slower than field access (mFoo2) [FieldGetter]\n" +
        "  this.getFoo2();\n" +
        "       ~~~~~~~\n" +
        "0 errors, 5 warnings\n",
        "",

        // Expected exit code
        ERRNO_SUCCESS,

        // Args
        new String[] {
                "--check",
                "FieldGetter",
                "--classpath",
                new File(project, "bin/classes.jar").getPath(),
                "--disable",
                "LintError",
                project.getPath()
        });
    }

    public void testLibraries() throws Exception {
        File project = getProjectDir(null,
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.jar.data=>bin/classes.jar"
        );
        checkDriver(
        "\n" +
        "Scanning MainTest_testLibraries: \n" +
        "No issues found.\n",
        "",

        // Expected exit code
        ERRNO_SUCCESS,

        // Args
        new String[] {
                "--check",
                "FieldGetter",
                "--libraries",
                new File(project, "bin/classes.jar").getPath(),
                "--disable",
                "LintError",
                project.getPath()
        });
    }

    @Override
    protected Detector getDetector() {
        // Sample issue to check by the main driver
        return new AccessibilityDetector();
    }

    private static class ExitException extends SecurityException {
        private static final long serialVersionUID = 1L;

        private final int mStatus;

        public ExitException(int status) {
            super("Unit test");
            mStatus = status;
        }

        public int getStatus() {
            return mStatus;
        }
    }

    public void test_getCleanPath() throws Exception {
        assertEquals("foo", LintCliClient.getCleanPath(new File("foo")));
        String sep = File.separator;
        assertEquals("foo" + sep + "bar",
                LintCliClient.getCleanPath(new File("foo" + sep + "bar")));
        assertEquals(sep,
                LintCliClient.getCleanPath(new File(sep)));
        assertEquals("foo" + sep + "bar",
                LintCliClient.getCleanPath(new File("foo" + sep + "." + sep + "bar")));
        assertEquals("bar",
                LintCliClient.getCleanPath(new File("foo" + sep + ".." + sep + "bar")));
        assertEquals("",
                LintCliClient.getCleanPath(new File("foo" + sep + "..")));
        assertEquals("foo",
                LintCliClient.getCleanPath(new File("foo" + sep + "bar" + sep + "..")));
        assertEquals("foo" + sep + ".foo" + sep + "bar",
                LintCliClient.getCleanPath(new File("foo" + sep + ".foo" + sep + "bar")));
        assertEquals("foo" + sep + "bar",
                LintCliClient.getCleanPath(new File("foo" + sep + "bar" + sep + ".")));
        assertEquals("foo" + sep + "...",
                LintCliClient.getCleanPath(new File("foo" + sep + "...")));
        assertEquals(".." + sep + "foo",
                LintCliClient.getCleanPath(new File(".." + sep + "foo")));
        assertEquals(sep + "foo",
                LintCliClient.getCleanPath(new File(sep + "foo")));
        assertEquals(sep,
                LintCliClient.getCleanPath(new File(sep + "foo" + sep + "..")));
        assertEquals(sep + "foo",
                LintCliClient.getCleanPath(new File(sep + "foo" + sep + "bar " + sep + "..")));
        assertEquals(sep + "c:",
                LintCliClient.getCleanPath(new File(sep + "c:")));
        assertEquals(sep + "c:" + sep + "foo",
                LintCliClient.getCleanPath(new File(sep + "c:" + sep + "foo")));
    }

    public void testGradle() throws Exception {
        File project = getProjectDir(null,
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>build.gradle", // dummy; only name counts
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class"
        );
        checkDriver(""
                + "\n"
                + "MainTest_testGradle: Error: \"MainTest_testGradle\" is a Gradle project. To correctly analyze Gradle projects, you should run \"gradlew :lint\" instead. [LintError]\n"
                + "1 errors, 0 warnings\n",

                "",

                // Expected exit code
                ERRNO_SUCCESS,

                // Args
                new String[] {
                        "--check",
                        "HardcodedText",
                        project.getPath()
                });
    }

    public void testValidateOutput() throws Exception {
        File project = getProjectDir(null,
                "res/layout/accessibility.xml=>myres1/layout/accessibility1.xml"
        );

        File outputDir = new File(project, "build");
        outputDir.mkdirs();
        assertTrue(outputDir.setWritable(true));

        checkDriver(
                "\n"
                        + "Scanning MainTest_testValidateOutput: .\n"
                        + "Scanning MainTest_testValidateOutput (Phase 2): \n", // Expected output

                "",

                // Expected exit code
                ERRNO_SUCCESS,

                // Args
                new String[]{
                        "--text",
                        new File(outputDir, "foo2.text").getPath(),
                        project.getPath(),
                });

        //noinspection ResultOfMethodCallIgnored
        boolean disabledWrite = outputDir.setWritable(false);
        assertTrue(disabledWrite);

        checkDriver(
                "", // Expected output

                "Cannot write XML output file /TESTROOT/build/foo.xml\n", // Expected error

                // Expected exit code
                ERRNO_EXISTS,

                // Args
                new String[] {
                        "--xml",
                        new File(outputDir, "foo.xml").getPath(),
                        project.getPath(),
                });

        checkDriver(
                "", // Expected output

                "Cannot write HTML output file /TESTROOT/build/foo.html\n", // Expected error

                // Expected exit code
                ERRNO_EXISTS,

                // Args
                new String[] {
                        "--html",
                        new File(outputDir, "foo.html").getPath(),
                        project.getPath(),
                });

        checkDriver(
                "", // Expected output

                "Cannot write text output file /TESTROOT/build/foo.text\n", // Expected error

                // Expected exit code
                ERRNO_EXISTS,

                // Args
                new String[] {
                        "--text",
                        new File(outputDir, "foo.text").getPath(),
                        project.getPath(),
                });
    }

    @Override
    protected boolean isEnabled(Issue issue) {
        return true;
    }
}
