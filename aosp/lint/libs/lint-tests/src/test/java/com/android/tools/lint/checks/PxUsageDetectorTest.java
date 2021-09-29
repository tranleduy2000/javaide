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
public class PxUsageDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new PxUsageDetector();
    }

    public void testPx() throws Exception {
        assertEquals(
            "res/layout/now_playing_after.xml:49: Warning: Avoid using \"mm\" as units (it does not work accurately on all devices); use \"dp\" instead [InOrMmUsage]\n" +
            "        android:layout_width=\"100mm\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/now_playing_after.xml:50: Warning: Avoid using \"in\" as units (it does not work accurately on all devices); use \"dp\" instead [InOrMmUsage]\n" +
            "        android:layout_height=\"120in\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/now_playing_after.xml:41: Warning: Avoid using \"px\" as units; use \"dp\" instead [PxUsage]\n" +
            "        android:layout_width=\"2px\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 3 warnings\n",
            lintFiles("res/layout/now_playing_after.xml"));
    }

    public void testSp() throws Exception {
        assertEquals(
            "res/layout/textsize.xml:11: Warning: Should use \"sp\" instead of \"dp\" for text sizes [SpUsage]\n" +
            "        android:textSize=\"14dp\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/textsize.xml:16: Warning: Should use \"sp\" instead of \"dp\" for text sizes [SpUsage]\n" +
            "        android:textSize=\"14dip\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/textsize.xml:33: Warning: Avoid using sizes smaller than 12sp: 11sp [SmallSp]\n" +
            "        android:textSize=\"11sp\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/textsize.xml:37: Warning: Avoid using sizes smaller than 12sp: 6sp [SmallSp]\n" +
            "        android:layout_height=\"6sp\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n",

            lintFiles("res/layout/textsize.xml"));
    }

    public void testStyles() throws Exception {
        assertEquals(
            "res/values/pxsp.xml:23: Warning: Avoid using \"mm\" as units (it does not work accurately on all devices); use \"dp\" instead [InOrMmUsage]\n" +
            "        <item name=\"android:textSize\">50mm</item>\n" +
            "                                      ^\n" +
            "res/values/pxsp.xml:25: Warning: Avoid using \"in\" as units (it does not work accurately on all devices); use \"dp\" instead [InOrMmUsage]\n" +
            "            50in\n" +
            "            ^\n" +
            "res/values/pxsp.xml:6: Warning: Should use \"sp\" instead of \"dp\" for text sizes [SpUsage]\n" +
            "        <item name=\"android:textSize\">50dp</item>\n" +
            "                                      ^\n" +
            "res/values/pxsp.xml:12: Warning: Should use \"sp\" instead of \"dp\" for text sizes [SpUsage]\n" +
            "        <item name=\"android:textSize\"> 50dip </item>\n" +
            "                                       ^\n" +
            "res/values/pxsp.xml:9: Warning: Avoid using \"px\" as units; use \"dp\" instead [PxUsage]\n" +
            "        <item name=\"android:textSize\">50px</item>\n" +
            "                                      ^\n" +
            "res/values/pxsp.xml:17: Warning: Avoid using \"px\" as units; use \"dp\" instead [PxUsage]\n" +
            "        <item name=\"android:paddingRight\"> 50px </item>\n" +
            "                                           ^\n" +
            "res/values/pxsp.xml:18: Warning: Avoid using \"px\" as units; use \"dp\" instead [PxUsage]\n" +
            "        <item name=\"android:paddingTop\">50px</item>\n" +
            "                                        ^\n" +
            "0 errors, 7 warnings\n",

            lintFiles("res/values/pxsp.xml"));
    }

    public void testIncrementalDimensions() throws Exception {
        assertEquals(""
                + "res/layout/textsize2.xml:9: Warning: Should use \"sp\" instead of \"dp\" for text sizes (@dimen/bottom_bar_portrait_button_font_size is defined as 16dp in /TESTROOT/res/values/dimens.xml [SpUsage]\n"
                + "        android:textSize=\"@dimen/bottom_bar_portrait_button_font_size\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
            lintProjectIncrementally(
                    "res/layout/textsize2.xml",
                    "res/values/dimens.xml", "res/layout/textsize2.xml"));
    }

    public void testBatchDimensions() throws Exception {
        assertEquals(""
                + "res/values/dimens.xml:2: Warning: This dimension is used as a text size: Should use \"sp\" instead of \"dp\" [SpUsage]\n"
                + "    <dimen name=\"bottom_bar_portrait_button_font_size\">16dp</dimen>\n"
                + "                                                       ^\n"
                + "    res/layout/textsize2.xml:9: Dimension used as a text size here\n"
                + "0 errors, 1 warnings\n",
            lintFiles("res/values/dimens.xml", "res/layout/textsize2.xml"));
    }
}
