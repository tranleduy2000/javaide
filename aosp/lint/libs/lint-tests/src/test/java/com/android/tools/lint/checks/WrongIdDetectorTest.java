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
public class WrongIdDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WrongIdDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/layout1.xml:14: Error: The id \"button5\" is not defined anywhere. Did you mean one of {button1, button2, button3, button4} ? [UnknownId]\n" +
            "        android:layout_alignBottom=\"@+id/button5\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:17: Error: The id \"my_id3\" is not defined anywhere. Did you mean my_id2 ? [UnknownId]\n" +
            "        android:layout_alignRight=\"@+id/my_id3\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:18: Error: The id \"my_id1\" is defined but not assigned to any views. Did you mean my_id2 ? [UnknownId]\n" +
            "        android:layout_alignTop=\"@+id/my_id1\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:15: Warning: The id \"my_id2\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignLeft=\"@+id/my_id2\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "3 errors, 1 warnings\n" +
            "",

            lintProject(
                    "wrongid/layout1.xml=>res/layout/layout1.xml",
                    "wrongid/layout2.xml=>res/layout/layout2.xml",
                    "wrongid/ids.xml=>res/values/ids.xml"
        ));
    }

    public void testSingleFile() throws Exception {
        assertEquals(
            "res/layout/layout1.xml:14: Warning: The id \"button5\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignBottom=\"@+id/button5\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:15: Warning: The id \"my_id2\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignLeft=\"@+id/my_id2\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:17: Warning: The id \"my_id3\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignRight=\"@+id/my_id3\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:18: Warning: The id \"my_id1\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignTop=\"@+id/my_id1\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n" +
            "",

            lintFiles("wrongid/layout1.xml=>res/layout/layout1.xml"));
    }

    public void testSuppressed() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                    "wrongid/ignorelayout1.xml=>res/layout/layout1.xml",
                    "wrongid/layout2.xml=>res/layout/layout2.xml",
                    "wrongid/ids.xml=>res/values/ids.xml"
        ));
    }

    public void testSuppressedSingleFile() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("wrongid/ignorelayout1.xml=>res/layout/layout1.xml"));
    }

    public void testNewIdPrefix() throws Exception {
        assertEquals(
                "No warnings.",

                lintFiles("res/layout/default_item_badges.xml",
                          "res/layout/detailed_item.xml"));
    }

    public void testSiblings() throws Exception {
        assertEquals(""
                + "res/layout/siblings.xml:55: Error: @id/button5 is not a sibling in the same RelativeLayout [NotSibling]\n"
                + "        android:layout_alignTop=\"@id/button5\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/siblings.xml:56: Error: @id/button6 is not a sibling in the same RelativeLayout [NotSibling]\n"
                + "        android:layout_toRightOf=\"@id/button6\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/siblings.xml:63: Error: @+id/button5 is not a sibling in the same RelativeLayout [NotSibling]\n"
                + "        android:layout_alignTop=\"@+id/button5\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/siblings.xml:64: Error: @+id/button6 is not a sibling in the same RelativeLayout [NotSibling]\n"
                + "        android:layout_toRightOf=\"@+id/button6\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",

                lintFiles("res/layout/siblings.xml"));
    }

    public void testInvalidIds1() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=56029
        assertEquals(""
                + "res/layout/invalid_ids.xml:23: Error: ID definitions must be of the form @+id/name; try using @+id/menu_Reload [InvalidId]\n"
                + "        android:id=\"@+menu/Reload\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/invalid_ids.xml:31: Error: ID definitions must be of the form @+id/name; try using @+id/_id_foo [InvalidId]\n"
                + "        android:id=\"@+/id_foo\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/invalid_ids.xml:37: Error: ID definitions must be of the form @+id/name; try using @+id/myid_button5 [InvalidId]\n"
                + "            android:id=\"@+myid/button5\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/invalid_ids.xml:43: Error: ID definitions must be of the form @+id/name; try using @+id/string_whatevs [InvalidId]\n"
                + "            android:id=\"@+string/whatevs\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",

                lintFiles("res/layout/invalid_ids.xml"));
    }

    public void testInvalidIds2() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=65244
        assertEquals(""
                + "res/layout/invalid_ids2.xml:8: Error: ID definitions must be of the form @+id/name; try using @+id/btn_skip [InvalidId]\n"
                + "        android:id=\"@+id/btn/skip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/invalid_ids2.xml:16: Error: Invalid id: missing value [InvalidId]\n"
                + "        android:id=\"@+id/\"\n"
                + "        ~~~~~~~~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",

                lintFiles("res/layout/invalid_ids2.xml"));
    }

    public void testIncremental() throws Exception {
        assertEquals(
            "res/layout/layout1.xml:14: Error: The id \"button5\" is not defined anywhere. Did you mean one of {button1, button2, button3, button4} ? [UnknownId]\n" +
            "        android:layout_alignBottom=\"@+id/button5\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:17: Error: The id \"my_id3\" is not defined anywhere. Did you mean one of {my_id1, my_id2} ? [UnknownId]\n" +
            "        android:layout_alignRight=\"@+id/my_id3\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:18: Error: The id \"my_id1\" is defined but not assigned to any views. Did you mean one of {my_id2, my_id3} ? [UnknownId]\n" +
            "        android:layout_alignTop=\"@+id/my_id1\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/layout1.xml:15: Warning: The id \"my_id2\" is not referring to any views in this layout [UnknownIdInLayout]\n" +
            "        android:layout_alignLeft=\"@+id/my_id2\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "3 errors, 1 warnings\n",

            lintProjectIncrementally(
                    "res/layout/layout1.xml",

                    "wrongid/layout1.xml=>res/layout/layout1.xml",
                    "wrongid/layout2.xml=>res/layout/layout2.xml",
                    "wrongid/ids.xml=>res/values/ids.xml"
            ));
    }

    public void testSelfReference() throws Exception {
        // Make sure we highlight direct references to self
        // Regression test for https://code.google.com/p/android/issues/detail?id=136103
        assertEquals(""
                + "res/layout/layout3.xml:9: Error: Cannot be relative to self: id=tv_portfolio_title, layout_below=tv_portfolio_title [NotSibling]\n"
                + "        android:layout_below=\"@+id/tv_portfolio_title\"/>\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintFiles("wrongid/layout3.xml=>res/layout/layout3.xml"));
    }
}
