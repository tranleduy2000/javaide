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
public class FieldGetterDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new FieldGetterDetector();
    }

    public void test() throws Exception {
        assertEquals(
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
            "0 errors, 5 warnings\n" +
            "",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.class.data=>bin/classes/test/bytecode/GetterTest.class"
                ));
    }

    public void testPostFroyo() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "apicheck/minsdk10.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.class.data=>bin/classes/test/bytecode/GetterTest.class"
                ));
    }

    public void testLibraries() throws Exception {
        // This tests the infrastructure: it makes sure that we *don't* run this
        // check in jars that are on the jar library dependency path (testJar() checks
        // that it *does* work for local jar classes)
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/classpath-lib=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.jar.data=>libs/library.jar"
                ));
    }

    public void testJar() throws Exception {
        assertEquals(
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
            "0 errors, 5 warnings\n" +
            "",

            lintProject(
                "bytecode/classpath-jar=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/GetterTest.java.txt=>src/test/bytecode/GetterTest.java",
                "bytecode/GetterTest.jar.data=>bin/classes.jar"
                ));
    }

    public void testTruncatedData() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                    "bytecode/classpath-jar=>.classpath",
                    "bytecode/GetterTest.jar.data=>bin/test/pkg/bogus.class"
                    ));
    }

    public void testCornerCases() throws Exception {
        assertEquals(
            "src/test/pkg/TestFieldGetter.java:21: Warning: Calling getter method getPath() on self is slower than field access (path) [FieldGetter]\n" +
            "        getPath(); // Should be flagged\n" +
            "        ~~~~~~~\n" +
            "0 errors, 1 warnings\n",

            lintProject(
                "bytecode/classpath-jar=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "bytecode/TestFieldGetter.java.txt=>src/test/pkg/TestFieldGetter.java",
                "bytecode/TestFieldGetter.class.data=>bin/classes/test/pkg/TestFieldGetter.class"
                ));
    }
}
