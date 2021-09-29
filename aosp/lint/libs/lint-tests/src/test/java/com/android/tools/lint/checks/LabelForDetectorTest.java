/*
 * Copyright (C) 2012 The Android Open Source Project
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
public class LabelForDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new LabelForDetector();
    }

    public void test() throws Exception {
        assertEquals(
        "res/layout/labelfor.xml:54: Warning: No label views point to this text field with an android:labelFor=\"@+id/@+id/editText2\" attribute [LabelFor]\n" +
        "    <EditText\n" +
        "    ^\n" +
        "res/layout/labelfor.xml:61: Warning: No label views point to this text field with an android:labelFor=\"@+id/@+id/autoCompleteTextView2\" attribute [LabelFor]\n" +
        "    <AutoCompleteTextView\n" +
        "    ^\n" +
        "res/layout/labelfor.xml:68: Warning: No label views point to this text field with an android:labelFor=\"@+id/@+id/multiAutoCompleteTextView2\" attribute [LabelFor]\n" +
        "    <MultiAutoCompleteTextView\n" +
        "    ^\n" +
        "0 errors, 3 warnings\n",

        lintProject(
                "apicheck/minsdk17.xml=>AndroidManifest.xml",
                "res/layout/labelfor.xml"
        ));
    }

    public void testSuppressed() throws Exception {
        assertEquals(
        "No warnings.",

        lintProject(
            "apicheck/minsdk17.xml=>AndroidManifest.xml",
            "res/layout/labelfor_ignore.xml"
        ));
    }


    public void testOk() throws Exception {
        assertEquals(
        "No warnings.",

        lintProject(
                "apicheck/minsdk17.xml=>AndroidManifest.xml",
                "res/layout/accessibility.xml"
            ));
    }

    public void testNotApplicable() throws Exception {
        assertEquals(
        "No warnings.",

        lintProject(
                "apicheck/minsdk14.xml=>AndroidManifest.xml",
                "res/layout/labelfor.xml"
        ));
    }
}

