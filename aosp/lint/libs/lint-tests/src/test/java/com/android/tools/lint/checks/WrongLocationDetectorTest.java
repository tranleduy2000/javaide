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
public class WrongLocationDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new WrongLocationDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/layout/alias.xml:17: Error: This file should be placed in a values/ folder, not a layout/ folder [WrongFolder]\n" +
            "<resources>\n" +
            "^\n" +
            "1 errors, 0 warnings\n",

        lintProject("res/values/strings.xml=>res/layout/alias.xml"));
    }

    public void testOk() throws Exception {
        assertEquals("No warnings.",

        lintProject("res/values/strings.xml"));
    }
}
