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
public class StateListDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new StateListDetector();
    }

    public void testStates() throws Exception {
        assertEquals(
            "res/drawable/states.xml:3: Warning: This item is unreachable because a previous item (item #1) is a more general match than this one [StateListReachable]\n" +
            "    <item android:state_pressed=\"true\"\n" +
            "    ^\n" +
            "    res/drawable/states.xml:2: Earlier item which masks item\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject("res/drawable/states.xml"));
    }

    public void testCustomStates() throws Exception {
        assertEquals(
            "No warnings.",
            lintProject("res/drawable/states2.xml"));
    }

    public void testStates3() throws Exception {
        assertEquals(
            "res/drawable/states3.xml:24: Warning: This item is unreachable because a previous item (item #1) is a more general match than this one [StateListReachable]\n" +
            "    <item android:state_checked=\"false\" android:state_window_focused=\"false\"\n" +
            "    ^\n" +
            "    res/drawable/states3.xml:18: Earlier item which masks item\n" +
            "0 errors, 1 warnings\n" +
            "",
            lintProject("res/drawable/states3.xml"));
    }
}
