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
public class ObsoleteLayoutParamsDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ObsoleteLayoutParamsDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/wrongparams.xml:11: Warning: Invalid layout param in a FrameLayout: layout_weight [ObsoleteLayoutParam]\n" +
            "        android:layout_weight=\"1\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:23: Warning: Invalid layout param in a LinearLayout: layout_alignParentLeft [ObsoleteLayoutParam]\n" +
            "            android:layout_alignParentLeft=\"true\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:24: Warning: Invalid layout param in a LinearLayout: layout_alignParentTop [ObsoleteLayoutParam]\n" +
            "            android:layout_alignParentTop=\"true\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:33: Warning: Invalid layout param in a LinearLayout: layout_alignBottom [ObsoleteLayoutParam]\n" +
            "            android:layout_alignBottom=\"@+id/button1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:34: Warning: Invalid layout param in a LinearLayout: layout_toRightOf [ObsoleteLayoutParam]\n" +
            "            android:layout_toRightOf=\"@+id/button1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:42: Warning: Invalid layout param in a LinearLayout: layout_alignLeft [ObsoleteLayoutParam]\n" +
            "            android:layout_alignLeft=\"@+id/button1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams.xml:43: Warning: Invalid layout param in a LinearLayout: layout_below [ObsoleteLayoutParam]\n" +
            "            android:layout_below=\"@+id/button1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject("res/layout/wrongparams.xml"));
    }

    public void test2() throws Exception {
        // Test <merge> and custom view handling

        assertEquals(
            "No warnings.",

            lintProject("res/layout/wrongparams2.xml"));
    }

    public void test3() throws Exception {
        // Test includes across files (wrong layout param on root element)
        assertEquals(
            "res/layout/wrongparams3.xml:5: Warning: Invalid layout param 'layout_alignParentTop' (included from within a LinearLayout in layout/wrongparams4.xml) [ObsoleteLayoutParam]\n" +
            "    android:layout_alignParentTop=\"true\" >\n" +
            "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject("res/layout/wrongparams4.xml", "res/layout/wrongparams3.xml"));
    }

    public void test4() throws Exception {
        // Test includes with a <merge> (wrong layout param on child of root merge element)
        assertEquals(
            "res/layout/wrongparams5.xml:8: Warning: Invalid layout param 'layout_alignParentTop' (included from within a LinearLayout in layout/wrongparams6.xml) [ObsoleteLayoutParam]\n" +
            "        android:layout_alignParentTop=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams5.xml:15: Warning: Invalid layout param 'layout_alignParentLeft' (included from within a LinearLayout in layout/wrongparams6.xml) [ObsoleteLayoutParam]\n" +
            "        android:layout_alignParentLeft=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams6.xml:16: Warning: Invalid layout param in a LinearLayout: layout_alignStart [ObsoleteLayoutParam]\n" +
            "            android:layout_alignStart=\"@+id/include1\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/wrongparams6.xml:17: Warning: Invalid layout param in a LinearLayout: layout_toEndOf [ObsoleteLayoutParam]\n" +
            "            android:layout_toEndOf=\"@+id/include1\" />\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"  +
            "0 errors, 4 warnings\n",

            lintProject("res/layout/wrongparams5.xml", "res/layout/wrongparams6.xml"));
    }

    public void testIgnore() throws Exception {
        assertEquals(
             // Ignoring all but one of the warnings
            "res/layout/wrongparams.xml:12: Warning: Invalid layout param in a FrameLayout: layout_weight [ObsoleteLayoutParam]\n" +
            "        android:layout_weight=\"1\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject("res/layout/wrongparams_ignore.xml=>res/layout/wrongparams.xml"));
    }
}
