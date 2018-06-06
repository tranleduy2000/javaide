/*
 * Copyright (C) 2014 The Android Open Source Project
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

@SuppressWarnings("SpellCheckingInspection")
public class AppCompatResourceDetectorTest extends AbstractCheckTest {
    public void testNotGradleProject() throws Exception {
        assertEquals("No warnings.",
                lintProject("res/menu/showAction1.xml"));
    }

    public void testNoAppCompat() throws Exception {
        assertEquals(""
            + "res/menu/showAction1.xml:6: Error: Should use android:showAsAction when not using the appcompat library [AppCompatResource]\n"
            + "        app:showAsAction=\"never\" />\n"
            + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n",
            lintProject(
                    "res/menu/showAction1.xml",
                    "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testCorrectAppCompat() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        "res/menu/showAction1.xml",
                        "bytecode/classes.jar=>libs/appcompat-v7-18.0.0.jar",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testWrongAppCompat() throws Exception {
        assertEquals(""
            + "res/menu/showAction2.xml:5: Error: Should use app:showAsAction with the appcompat library with xmlns:app=\"http://schemas.android.com/apk/res-auto\" [AppCompatResource]\n"
            + "        android:showAsAction=\"never\" />\n"
            + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 0 warnings\n",
        lintProject(
                "res/menu/showAction2.xml",
                "bytecode/classes.jar=>libs/appcompat-v7-18.0.0.jar",
                "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    public void testAppCompatV14() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        "res/menu/showAction2.xml=>res/menu-v14/showAction2.xml",
                        "bytecode/classes.jar=>libs/appcompat-v7-18.0.0.jar",
                        "multiproject/library.properties=>build.gradle")); // dummy; only name counts
    }

    @Override
    protected Detector getDetector() {
        return new AppCompatResourceDetector();
    }
}