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
public class TitleDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new TitleDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/menu/titles.xml:3: Error: Menu items should specify a title [MenuTitle]\n" +
            "    <item android:id=\"@+id/action_bar_progress_spinner\"\n" +
            "    ^\n" +
            "res/menu/titles.xml:12: Error: Menu items should specify a title [MenuTitle]\n" +
            "    <item android:id=\"@+id/menu_plus_one\"\n" +
            "    ^\n" +
            "2 errors, 0 warnings\n",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/menu/titles.xml"));
    }

    public void testOk() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject(
                    "apicheck/minsdk1.xml=>AndroidManifest.xml",
                    "res/menu/titles.xml"));
    }

    public void testOk2() throws Exception {
        assertEquals(
            "No warnings.",

            lintProject("res/menu-land/actions.xml"));
    }
}
