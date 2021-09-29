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
public class AlwaysShowActionDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new AlwaysShowActionDetector();
    }

    public void testXmlMenus() throws Exception {
        assertEquals(
                "res/menu-land/actions.xml:6: Warning: Prefer \"ifRoom\" instead of \"always\" [AlwaysShowAction]\n" +
                "        android:showAsAction=\"always|collapseActionView\"\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    res/menu-land/actions.xml:13: <No location-specific message\n" +
                "    res/menu-land/actions.xml:18: <No location-specific message\n" +
                "    res/menu-land/actions.xml:54: <No location-specific message\n" +
                "0 errors, 1 warnings\n" +
                "",

                lintProject("res/menu-land/actions.xml"));
    }

    public void testXmlMenusWithFlags() throws Exception {
        assertEquals(
                "res/menu-land/actions2.xml:6: Warning: Prefer \"ifRoom\" instead of \"always\" [AlwaysShowAction]\n" +
                "        android:showAsAction=\"always|collapseActionView\"\n" +
                "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "    res/menu-land/actions2.xml:13: <No location-specific message\n" +
                "    res/menu-land/actions2.xml:18: <No location-specific message\n" +
                "    res/menu-land/actions2.xml:54: <No location-specific message\n" +
                "0 errors, 1 warnings\n" +
                "",

                lintProject("res/menu-land/actions2.xml"));
    }

    public void testJavaFail() throws Exception {
        assertEquals(
                "src/test/pkg/ActionTest1.java:7: Warning: Prefer \"SHOW_AS_ACTION_IF_ROOM\" instead of \"SHOW_AS_ACTION_ALWAYS\" [AlwaysShowAction]\n" +
                "        System.out.println(MenuItem.SHOW_AS_ACTION_ALWAYS);\n" +
                "                           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "0 errors, 1 warnings\n" +
                "",

                // Only references to ALWAYS
                lintProject("src/test/pkg/ActionTest1.java.txt=>src/test/pkg/ActionTest1.java"));
    }
    public void testJavaPass() throws Exception {
        assertEquals(
                "No warnings.",

                // Both references to ALWAYS and IF_ROOM
                lintProject(
                        "src/test/pkg/ActionTest1.java.txt=>src/test/pkg/ActionTest1.java",
                        "src/test/pkg/ActionTest2.java.txt=>src/test/pkg/ActionTest2.java"));
    }

    public void testSuppress() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject(
                    "res/menu-land/actions2_ignore.xml",
                    "src/test/pkg/ActionTest1_ignore.java.txt=>src/test/pkg/ActionTest1.java"));
    }
}
