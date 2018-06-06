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
public class MissingIdDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new MissingIdDetector();
    }

    public void test() throws Exception {
        assertEquals(
        "res/layout/fragment.xml:7: Warning: This <fragment> tag should specify an id or a tag to preserve state across activity restarts [MissingId]\n" +
        "    <fragment\n" +
        "    ^\n" +
        "0 errors, 1 warnings\n",

        lintProject("res/layout/fragment.xml"));
    }
}
