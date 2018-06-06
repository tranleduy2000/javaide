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
public class DosLineEndingDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DosLineEndingDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/crcrlf.xml:4: Error: Incorrect line ending: found carriage return (\\r) without corresponding newline (\\n) [MangledCRLF]\n" +
            "    android:layout_height=\"match_parent\" >\r\n" +
            "^\n" +
            "1 errors, 0 warnings\n",
            lintProject("res/layout/crcrlf.xml"));
    }

    public void testIgnore() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject("res/layout/crcrlf_ignore.xml"));
    }

    public void testNegative() throws Exception {
        // Make sure we don't get warnings for a correct file
        assertEquals(
            "No warnings.",
            lintProject("res/layout/layout1.xml"));
    }
}
