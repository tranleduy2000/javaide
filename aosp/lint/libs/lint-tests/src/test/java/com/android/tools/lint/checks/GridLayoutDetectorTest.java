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

import static com.android.tools.lint.checks.GridLayoutDetector.getNewValue;
import static com.android.tools.lint.checks.GridLayoutDetector.getOldValue;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;

@SuppressWarnings("javadoc")
public class GridLayoutDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new GridLayoutDetector();
    }

    public void testGridLayout1() throws Exception {
        assertEquals(
            "res/layout/gridlayout.xml:36: Error: Column attribute (3) exceeds declared grid column count (2) [GridLayout]\n" +
            "            android:layout_column=\"3\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 0 warnings\n" +
            "",
            lintFiles("res/layout/gridlayout.xml"));
    }

    public void testGridLayout2() throws Exception {
        assertEquals(""
                + "res/layout/layout.xml:9: Error: Wrong namespace; with v7 GridLayout you should use myns:orientation [GridLayout]\n"
                + "        android:orientation=\"horizontal\">\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout.xml:14: Error: Wrong namespace; with v7 GridLayout you should use myns:layout_row [GridLayout]\n"
                + "            android:layout_row=\"2\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",
                lintFiles("res/layout/gridlayout2.xml=>res/layout/layout.xml"));
    }

    public void testGridLayout3() throws Exception {
        assertEquals(""
                + "res/layout/layout.xml:12: Error: Wrong namespace; with v7 GridLayout you should use app:layout_row "
                + "(and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "            android:layout_row=\"2\" />\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintFiles("res/layout/gridlayout3.xml=>res/layout/layout.xml"));
    }

    public void testGridLayout4() throws Exception {
        assertEquals(""
                + "res/layout/layout.xml:6: Error: Wrong namespace; with v7 GridLayout you should use app:orientation (and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "        android:orientation=\"horizontal\">\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout.xml:12: Error: Wrong namespace; with v7 GridLayout you should use app:layout_columnWeight (and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "            android:layout_columnWeight=\"2\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout.xml:13: Error: Wrong namespace; with v7 GridLayout you should use app:layout_gravity (and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "            android:layout_gravity=\"fill\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout.xml:20: Error: Wrong namespace; with v7 GridLayout you should use app:layout_gravity (and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "            android:layout_gravity=\"fill\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/layout.xml:22: Error: Wrong namespace; with v7 GridLayout you should use app:layout_columnWeight (and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to your root element.) [GridLayout]\n"
                + "            android:layout_columnWeight=\"1\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "5 errors, 0 warnings\n",
                lintFiles("res/layout/gridlayout4.xml=>res/layout/layout.xml"));
    }

    public void testGetOldValue() {
        assertEquals("android:layout_row",
                getOldValue("Wrong namespace; with v7 GridLayout you should use app:layout_row",
                        TEXT));
        assertEquals("android:layout_row",
                getOldValue("Wrong namespace; with v7 GridLayout you should use app:layout_row " +
                                "(and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to " +
                                "your root element.)",
                        TEXT));
    }

    public void testGetNewValue() {
        assertNotNull("app:layout_row",
                getNewValue("Wrong namespace; with v7 GridLayout you should use app:layout_row",
                        TEXT));
        assertNotNull("app:layout_row",
                getNewValue("Wrong namespace; with v7 GridLayout you should use app:layout_row" +
                                "(and add xmlns:app=\"http://schemas.android.com/apk/res-auto\" to " +
                                "your root element.)",
                        TEXT));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        if (message.contains("with v7 GridLayout")) {
            assertNotNull(message, getOldValue(message, TEXT));
            assertNotNull(message, getNewValue(message, TEXT));
        }
    }
}
