/*
 * Copyright (C) 2013 The Android Open Source Project
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
public class LayoutConsistencyDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new LayoutConsistencyDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "res/layout/layout1.xml:11: Warning: The id \"button1\" in layout \"layout1\" is missing from the following layout configurations: layout-xlarge (present in layout) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button1\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout1.xml:38: Warning: The id \"button4\" in layout \"layout1\" is missing from the following layout configurations: layout-xlarge (present in layout) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button4\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "wrongid/Foo.java.txt=>src/test/pkg/Foo.java",
                        "wrongid/layout1.xml=>res/layout/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-xlarge/layout1.xml"
                ));
    }

    public void testSuppress() throws Exception {
        // Same as unit test above, but button1 is suppressed with tools:ignore; button4 is not
        assertEquals(""
                + "res/layout/layout1.xml:56: Warning: The id \"button4\" in layout \"layout1\" is missing from the following layout configurations: layout-xlarge (present in layout) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button4\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "wrongid/Foo.java.txt=>src/test/pkg/Foo.java",
                        "wrongid/layout1_ignore.xml=>res/layout/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-xlarge/layout1.xml"
                ));
    }

    public void test2() throws Exception {
        assertEquals(""
                + "res/layout/layout1.xml:11: Warning: The id \"button1\" in layout \"layout1\" is missing from the following layout configurations: layout-xlarge (present in layout, layout-sw600dp, layout-sw600dp-land) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button1\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/layout-sw600dp/layout1.xml:11: Occurrence in layout-sw600dp\n"
                + "    res/layout-sw600dp-land/layout1.xml:11: Occurrence in layout-sw600dp-land\n"
                + "res/layout/layout1.xml:38: Warning: The id \"button4\" in layout \"layout1\" is missing from the following layout configurations: layout-xlarge (present in layout, layout-sw600dp, layout-sw600dp-land) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button4\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/layout-sw600dp/layout1.xml:38: Occurrence in layout-sw600dp\n"
                + "    res/layout-sw600dp-land/layout1.xml:38: Occurrence in layout-sw600dp-land\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "wrongid/Foo.java.txt=>src/test/pkg/Foo.java",
                        "wrongid/layout1.xml=>res/layout/layout1.xml",
                        "wrongid/layout1.xml=>res/layout-sw600dp/layout1.xml",
                        "wrongid/layout1.xml=>res/layout-sw600dp-land/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-xlarge/layout1.xml"
                ));
    }

    public void test3() throws Exception {
        assertEquals(""
                + "res/layout/layout1.xml:11: Warning: The id \"button1\" in layout \"layout1\" is only present in the following layout configurations: layout (missing from layout-sw600dp, layout-sw600dp-land, layout-xlarge) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button1\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout1.xml:38: Warning: The id \"button4\" in layout \"layout1\" is only present in the following layout configurations: layout (missing from layout-sw600dp, layout-sw600dp-land, layout-xlarge) [InconsistentLayout]\n"
                + "        android:id=\"@+id/button4\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "wrongid/Foo.java.txt=>src/test/pkg/Foo.java",
                        "wrongid/layout1.xml=>res/layout/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-sw600dp/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-sw600dp-land/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-xlarge/layout1.xml"
                ));
    }

    public void testNoJavaRefs() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                        "wrongid/layout1.xml=>res/layout/layout1.xml",
                        "wrongid/layout2.xml=>res/layout-xlarge/layout1.xml"
                ));
    }
}
