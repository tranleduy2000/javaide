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
public class ChildCountDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ChildCountDetector();
    }

    public void testChildCount() throws Exception {
        assertEquals(
            "res/layout/has_children.xml:3: Warning: A list/grid should have no children declared in XML [AdapterViewChildren]\n" +
            "<ListView\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/has_children.xml"));
    }

    public void testChildCount2() throws Exception {
        // A <requestFocus/> tag is okay.
        assertEquals(
                "No warnings.",
                lintFiles("res/layout/has_children2.xml"));
    }
}
