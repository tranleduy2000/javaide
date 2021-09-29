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

import static com.android.tools.lint.detector.api.TextFormat.RAW;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;

@SuppressWarnings("javadoc")
public class WrongCaseDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WrongCaseDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "res/layout/case.xml:18: Error: Invalid tag <Merge>; should be <merge> [WrongCase]\n"
                + "<Merge xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n"
                + "^\n"
                + "res/layout/case.xml:20: Error: Invalid tag <Fragment>; should be <fragment> [WrongCase]\n"
                + "    <Fragment android:name=\"foo.bar.Fragment\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/case.xml:21: Error: Invalid tag <Include>; should be <include> [WrongCase]\n"
                + "    <Include layout=\"@layout/foo\" />\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/case.xml:22: Error: Invalid tag <RequestFocus>; should be <requestFocus> [WrongCase]\n"
                + "    <RequestFocus />\n"
                + "    ~~~~~~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",

                lintProject("res/layout/case.xml"));
    }

    public void testGetOldValue() {
        assertEquals("Merge", WrongCaseDetector.getOldValue(
                "Invalid tag `<Merge>`; should be `<merge>`", RAW));
        assertEquals("Merge", WrongCaseDetector.getOldValue(
                "Invalid tag <Merge>; should be <merge>", TEXT));
    }

    public void testGetNewValue() {
        assertEquals("merge", WrongCaseDetector.getNewValue(
                "Invalid tag <Merge>; should be <merge>", TEXT));
        assertEquals("merge", WrongCaseDetector.getNewValue(
                "Invalid tag `<Merge>`; should be `<merge>`", RAW));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        assertNotNull(message, WrongCaseDetector.getOldValue(message, TEXT));
        assertNotNull(message, WrongCaseDetector.getNewValue(message, TEXT));
    }
}
