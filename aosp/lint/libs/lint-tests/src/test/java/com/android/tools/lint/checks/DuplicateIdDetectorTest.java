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
public class DuplicateIdDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DuplicateIdDetector();
    }

    public void testDuplicate() throws Exception {
        assertEquals(
                "res/layout/duplicate.xml:5: Error: Duplicate id @+id/android_logo, already defined earlier in this layout [DuplicateIds]\n" +
                "    <ImageButton android:id=\"@+id/android_logo\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\" android:src=\"@drawable/android_button\" android:focusable=\"false\" android:clickable=\"false\" android:layout_weight=\"1.0\" />\n" +
                "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    res/layout/duplicate.xml:4: @+id/android_logo originally defined here\n" +
                "1 errors, 0 warnings\n" +
                "",
                lintFiles("res/layout/duplicate.xml"));
    }

    public void testDuplicateChains() throws Exception {
        assertEquals(
            "res/layout/layout1.xml:7: Warning: Duplicate id @+id/button1, defined or included multiple times in layout/layout1.xml: [layout/layout1.xml defines @+id/button1, layout/layout1.xml => layout/layout2.xml => layout/layout3.xml defines @+id/button1, layout/layout1.xml => layout/layout2.xml => layout/layout4.xml defines @+id/button1] [DuplicateIncludedIds]\n" +
            "    <include\n" +
            "    ^\n" +
            "    res/layout/layout1.xml:13: Defined here\n" +
            "    res/layout/layout3.xml:8: Defined here, included via layout/layout1.xml => layout/layout2.xml => layout/layout3.xml defines @+id/button1\n" +
            "    res/layout/layout4.xml:8: Defined here, included via layout/layout1.xml => layout/layout2.xml => layout/layout4.xml defines @+id/button1\n" +
            "res/layout/layout1.xml:7: Warning: Duplicate id @+id/button2, defined or included multiple times in layout/layout1.xml: [layout/layout1.xml defines @+id/button2, layout/layout1.xml => layout/layout2.xml => layout/layout4.xml defines @+id/button2] [DuplicateIncludedIds]\n" +
            "    <include\n" +
            "    ^\n" +
            "    res/layout/layout1.xml:19: Defined here\n" +
            "    res/layout/layout4.xml:14: Defined here, included via layout/layout1.xml => layout/layout2.xml => layout/layout4.xml defines @+id/button2\n" +
            "res/layout/layout2.xml:18: Warning: Duplicate id @+id/button1, defined or included multiple times in layout/layout2.xml: [layout/layout2.xml => layout/layout3.xml defines @+id/button1, layout/layout2.xml => layout/layout4.xml defines @+id/button1] [DuplicateIncludedIds]\n" +
            "    <include\n" +
            "    ^\n" +
            "    res/layout/layout3.xml:8: Defined here, included via layout/layout2.xml => layout/layout3.xml defines @+id/button1\n" +
            "    res/layout/layout4.xml:8: Defined here, included via layout/layout2.xml => layout/layout4.xml defines @+id/button1\n" +
            "0 errors, 3 warnings\n" +
            "",

            // layout1: defines @+id/button1, button2
            // layout3: defines @+id/button1
            // layout4: defines @+id/button1, button2
            // layout1 include layout2
            // layout2 includes layout3 and layout4

            // Therefore, layout3 and layout4 have no problems
            // In layout2, there's a duplicate definition of button1 (coming from 3 and 4)
            // In layout1, there's a duplicate definition of button1 (coming from layout1, 3 and 4)
            // In layout1, there'sa duplicate definition of button2 (coming from 1 and 4)

            lintProject("res/layout/layout1.xml", "res/layout/layout2.xml",
                        "res/layout/layout3.xml", "res/layout/layout4.xml"));
    }

    public void testSuppress() throws Exception {
        assertEquals(
            "res/layout/layout2.xml:18: Warning: Duplicate id @+id/button1, defined or included multiple times in layout/layout2.xml: [layout/layout2.xml => layout/layout3.xml defines @+id/button1, layout/layout2.xml => layout/layout4.xml defines @+id/button1] [DuplicateIncludedIds]\n" +
            "    <include\n" +
            "    ^\n" +
            "    res/layout/layout3.xml:8: Defined here, included via layout/layout2.xml => layout/layout3.xml defines @+id/button1\n" +
            "    res/layout/layout4.xml:8: Defined here, included via layout/layout2.xml => layout/layout4.xml defines @+id/button1\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject(
                    "res/layout/layout1_ignore.xml=>res/layout/layout1.xml",
                    "res/layout/layout2.xml",
                    "res/layout/layout3.xml",
                    "res/layout/layout4.xml"));
    }
}
