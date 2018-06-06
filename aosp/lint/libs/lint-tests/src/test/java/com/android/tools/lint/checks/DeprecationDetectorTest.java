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
public class DeprecationDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DeprecationDetector();
    }

    public void testApi1() throws Exception {
        assertEquals(
            "res/layout/deprecation.xml:2: Warning: AbsoluteLayout is deprecated [Deprecated]\n" +
            "<AbsoluteLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "res/layout/deprecation.xml:18: Warning: android:editable is deprecated: Use an <EditText> to make it editable [Deprecated]\n" +
            "        android:editable=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:26: Warning: android:editable is deprecated: <EditText> is already editable [Deprecated]\n" +
            "    <EditText android:editable=\"true\" />\n" +
            "              ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:27: Warning: android:editable is deprecated: Use inputType instead [Deprecated]\n" +
            "    <EditText android:editable=\"false\" />\n" +
            "              ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 4 warnings\n",

            lintProject(
                    "apicheck/minsdk1.xml=>AndroidManifest.xml",
                    "res/layout/deprecation.xml"));
    }

    public void testApi4() throws Exception {
        assertEquals(
            "res/layout/deprecation.xml:2: Warning: AbsoluteLayout is deprecated [Deprecated]\n" +
            "<AbsoluteLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "res/layout/deprecation.xml:16: Warning: android:autoText is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:autoText=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:17: Warning: android:capitalize is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:capitalize=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:18: Warning: android:editable is deprecated: Use an <EditText> to make it editable [Deprecated]\n" +
            "        android:editable=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:20: Warning: android:inputMethod is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:inputMethod=\"@+id/foo\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:21: Warning: android:numeric is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:numeric=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:22: Warning: android:password is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:password=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:23: Warning: android:phoneNumber is deprecated: Use inputType instead [Deprecated]\n" +
            "        android:phoneNumber=\"true\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:26: Warning: android:editable is deprecated: <EditText> is already editable [Deprecated]\n" +
            "    <EditText android:editable=\"true\" />\n" +
            "              ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/deprecation.xml:27: Warning: android:editable is deprecated: Use inputType instead [Deprecated]\n" +
            "    <EditText android:editable=\"false\" />\n" +
            "              ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 10 warnings\n",

            lintProject(
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/layout/deprecation.xml"));
    }
}
