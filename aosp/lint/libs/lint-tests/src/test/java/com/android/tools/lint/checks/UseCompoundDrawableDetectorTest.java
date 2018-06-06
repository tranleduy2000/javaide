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
public class UseCompoundDrawableDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new UseCompoundDrawableDetector();
    }

    public void testCompound() throws Exception {
        assertEquals(
            "res/layout/compound.xml:3: Warning: This tag and its children can be replaced by one <TextView/> and a compound drawable [UseCompoundDrawables]\n" +
            "<LinearLayout\n" +
            "^\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintFiles("res/layout/compound.xml"));
    }

    public void testCompound2() throws Exception {
        // Ignore layouts that set a custom background
        assertEquals(
                "No warnings.",
                lintFiles("res/layout/compound2.xml"));
    }

    public void testCompound3() throws Exception {
        // Ignore layouts that set an image scale type
        assertEquals(
                "No warnings.",
                lintFiles("res/layout/compound3.xml"));
    }
}
