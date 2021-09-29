/*
 * Copyright (C) 2014 The Android Open Source Project
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

public class NegativeMarginDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new NegativeMarginDetector();
    }

    public void testLayoutWithoutRepositorySupport() throws Exception {
        assertEquals(""
                + "res/layout/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "    <TextView android:layout_marginTop=\"-1dp\"/> <!-- WARNING -->\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
                lintFiles("res/layout/negative_margins.xml"));
    }

    public void testIncrementalInLayout() throws Exception {
        assertEquals(""
                + "res/layout/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "    <TextView android:layout_marginTop=\"-1dp\"/> <!-- WARNING -->\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/negative_margins.xml:13: Warning: Margin values should not be negative (@dimen/negative is defined as -16dp in /TESTROOT/res/values/negative_margins.xml [NegativeMargin]\n"
                + "    <TextView android:layout_marginTop=\"@dimen/negative\"/> <!-- WARNING -->\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",
                lintProjectIncrementally(
                        "res/layout/negative_margins.xml",
                        "res/values/negative_margins.xml", "res/layout/negative_margins.xml"));
    }

    public void testValuesWithoutRepositorySupport() throws Exception {
        assertEquals(""
                + "res/values/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "        <item name=\"android:layout_marginBottom\">-5dp</item> <!-- WARNING -->\n"
                + "                                                 ^\n"
                + "0 errors, 1 warnings\n",
                lintFiles("res/values/negative_margins.xml"));
    }

    public void testIncrementalInValues() throws Exception {
        assertEquals(""
                + "res/values/negative_margins.xml:10: Warning: Margin values should not be negative (@dimen/negative is defined as -16dp in /TESTROOT/res/values/negative_margins.xml [NegativeMargin]\n"
                + "        <item name=\"android:layout_marginTop\">@dimen/negative</item> <!-- WARNING -->\n"
                + "                                              ^\n"
                + "res/values/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "        <item name=\"android:layout_marginBottom\">-5dp</item> <!-- WARNING -->\n"
                + "                                                 ^\n"
                + "0 errors, 2 warnings\n",
                lintProjectIncrementally(
                        "res/values/negative_margins.xml",
                        "res/values/negative_margins.xml", "res/layout/negative_margins.xml"));
    }

    public void testBatch() throws Exception {
        assertEquals(""
                + "res/layout/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "    <TextView android:layout_marginTop=\"-1dp\"/> <!-- WARNING -->\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values/negative_margins.xml:11: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "        <item name=\"android:layout_marginBottom\">-5dp</item> <!-- WARNING -->\n"
                + "                                                 ^\n"
                + "res/layout/negative_margins.xml:13: Warning: Margin values should not be negative [NegativeMargin]\n"
                + "    <TextView android:layout_marginTop=\"@dimen/negative\"/> <!-- WARNING -->\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 3 warnings\n",

            lintFiles("res/values/negative_margins.xml", "res/layout/negative_margins.xml"));
    }
}
