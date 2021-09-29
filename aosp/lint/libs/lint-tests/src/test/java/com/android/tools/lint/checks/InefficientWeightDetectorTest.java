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
public class InefficientWeightDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new InefficientWeightDetector();
    }

    public void testWeights() throws Exception {
        assertEquals(
            "res/layout/inefficient_weight.xml:3: Error: Wrong orientation? No orientation specified, and the default is horizontal, yet this layout has multiple children where at least one has layout_width=\"match_parent\" [Orientation]\n" +
            "<LinearLayout\n" +
            "^\n" +
            "res/layout/inefficient_weight.xml:10: Warning: Use a layout_width of 0dp instead of match_parent for better performance [InefficientWeight]\n" +
            "     android:layout_width=\"match_parent\"\n" +
            "     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/inefficient_weight.xml:24: Warning: Use a layout_height of 0dp instead of wrap_content for better performance [InefficientWeight]\n" +
            "      android:layout_height=\"wrap_content\"\n" +
            "      ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 2 warnings\n",
            lintFiles("res/layout/inefficient_weight.xml"));
    }

    public void testWeights2() throws Exception {
        assertEquals(
            "res/layout/nested_weights.xml:23: Warning: Nested weights are bad for performance [NestedWeights]\n" +
            "            android:layout_weight=\"1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/nested_weights.xml"));
    }

    public void testWeights3() throws Exception {
        assertEquals(
            "res/layout/baseline_weights.xml:2: Warning: Set android:baselineAligned=\"false\" on this element for better performance [DisableBaselineAlignment]\n" +
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/baseline_weights.xml"));
    }

    public void testWeights4() throws Exception {
        assertEquals(
            "res/layout/activity_item_two_pane.xml:1: Warning: Set android:baselineAligned=\"false\" on this element for better performance [DisableBaselineAlignment]\n" +
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "0 errors, 1 warnings\n",
            lintFiles("res/layout/activity_item_two_pane.xml"));
    }

    public void testNoVerticalWeights3() throws Exception {
        // Orientation=vertical
        assertEquals(
            "No warnings.",
            lintFiles("res/layout/baseline_weights2.xml"));
    }

    public void testNoVerticalWeights4() throws Exception {
        // Orientation not specified ==> horizontal
        assertEquals(
            "res/layout/baseline_weights3.xml:2: Warning: Set android:baselineAligned=\"false\" on this element for better performance [DisableBaselineAlignment]\n" +
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/baseline_weights3.xml"));
    }

    public void testSuppressed() throws Exception {
        assertEquals(
            "No warnings.",

            lintFiles("res/layout/inefficient_weight2.xml"));
    }

    public void testNestedWeights() throws Exception {
        // Regression test for http://code.google.com/p/android/issues/detail?id=22889
        // (Comment 8)
        assertEquals(
                "No warnings.",

                lintFiles("res/layout/nested_weights2.xml"));
    }

    public void testWrong0Dp() throws Exception {
        assertEquals(
            "res/layout/wrong0dp.xml:19: Error: Suspicious size: this will make the view invisible, should be used with layout_weight [Suspicious0dp]\n" +
            "            android:layout_width=\"0dp\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrong0dp.xml:25: Error: Suspicious size: this will make the view invisible, should be used with layout_weight [Suspicious0dp]\n" +
            "            android:layout_height=\"0dp\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrong0dp.xml:34: Error: Suspicious size: this will make the view invisible, probably intended for layout_height [Suspicious0dp]\n" +
            "            android:layout_width=\"0dp\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrong0dp.xml:67: Error: Suspicious size: this will make the view invisible, probably intended for layout_width [Suspicious0dp]\n" +
            "            android:layout_height=\"0dp\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrong0dp.xml:90: Error: Suspicious size: this will make the view invisible, probably intended for layout_width [Suspicious0dp]\n" +
            "            android:layout_height=\"0dp\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "5 errors, 0 warnings\n",

            lintFiles("res/layout/wrong0dp.xml"));
    }

    public void testOrientation() throws Exception {
        assertEquals(""
                + "res/layout/orientation.xml:52: Error: No orientation specified, and the default is horizontal. This is a common source of bugs when children are added dynamically. [Orientation]\n"
                + "    <LinearLayout\n"
                + "    ^\n"
                + "1 errors, 0 warnings\n",

                lintFiles("res/layout/orientation.xml"));
    }

    public void testIncremental1() throws Exception {
        assertEquals(""
                + "res/layout/orientation2.xml:5: Error: No orientation specified, and the default is horizontal. This is a common source of bugs when children are added dynamically. [Orientation]\n"
                + "    <LinearLayout\n"
                + "    ^\n"
                + "1 errors, 0 warnings\n",

                lintProjectIncrementally("res/layout/orientation2.xml",
                        "res/layout/orientation2.xml"));
    }

    public void testIncremental2() throws Exception {
        assertEquals("No warnings.",

                lintProjectIncrementally("res/layout/orientation2.xml",
                        "res/layout/orientation2.xml",
                        "res/values/styles-inherited-orientation.xml"));
    }

    public void testIncremental3() throws Exception {
        assertEquals("No warnings.",
                lintProjectIncrementally("res/layout/orientation2.xml",
                        "res/layout/orientation2.xml",
                        "res/values/styles-orientation.xml"));
    }

    public void testIncremental4() throws Exception {
        assertEquals(""
            + "res/layout/inefficient_weight3.xml:9: Warning: Use a layout_height of 0dp instead of (undefined) for better performance [InefficientWeight]\n"
            + "    <Button\n"
            + "    ^\n"
            + "0 errors, 1 warnings\n",
            lintProjectIncrementally(
                    "res/layout/inefficient_weight3.xml",
                    "res/layout/inefficient_weight3.xml"));
    }

    public void testIncremental5() throws Exception {
        assertEquals("No warnings.",
            lintProjectIncrementally(
                    "res/layout/inefficient_weight3.xml",
                    "res/layout/inefficient_weight3.xml",
                    "res/values/styles-orientation.xml"));
    }

    public void testIncremental6() throws Exception {
        assertEquals(""
            + "res/layout/inefficient_weight3.xml:9: Warning: Use a layout_height of 0dp instead of wrap_content for better performance [InefficientWeight]\n"
            + "    <Button\n"
            + "    ^\n"
            + "0 errors, 1 warnings\n",
            lintProjectIncrementally(
                    "res/layout/inefficient_weight3.xml",
                    "res/layout/inefficient_weight3.xml",
                    "res/values/styles-inherited-orientation.xml"));
    }
}
