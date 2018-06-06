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
public class HardcodedValuesDetectorTest  extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new HardcodedValuesDetector();
    }

    public void testStrings() throws Exception {
        assertEquals(
            "res/layout/accessibility.xml:3: Warning: [I18N] Hardcoded string \"Button\", should use @string resource [HardcodedText]\n" +
            "    <Button android:text=\"Button\" android:id=\"@+id/button1\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"></Button>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/layout/accessibility.xml:6: Warning: [I18N] Hardcoded string \"Button\", should use @string resource [HardcodedText]\n" +
            "    <Button android:text=\"Button\" android:id=\"@+id/button2\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"></Button>\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 2 warnings\n",

            lintFiles("res/layout/accessibility.xml"));
    }

    public void testMenus() throws Exception {
        assertEquals(
            "res/menu/menu.xml:7: Warning: [I18N] Hardcoded string \"My title 1\", should use @string resource [HardcodedText]\n" +
            "        android:title=\"My title 1\">\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "res/menu/menu.xml:13: Warning: [I18N] Hardcoded string \"My title 2\", should use @string resource [HardcodedText]\n" +
            "        android:title=\"My title 2\">\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 2 warnings\n",

            lintFiles("res/menu/menu.xml"));
    }

    public void testMenusOk() throws Exception {
        assertEquals(
            "No warnings.",
            lintFiles("res/menu/titles.xml"));
    }

    public void testSuppress() throws Exception {
        // All but one errors in the file contain ignore attributes - direct, inherited
        // and lists
        assertEquals(
            "res/layout/ignores.xml:61: Warning: [I18N] Hardcoded string \"Hardcoded\", should use @string resource [HardcodedText]\n" +
            "        android:text=\"Hardcoded\"\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintFiles("res/layout/ignores.xml"));
    }

    public void testSuppressViaComment() throws Exception {
        assertEquals(""
                + "res/layout/ignores2.xml:51: Warning: [I18N] Hardcoded string \"Hardcoded\", should use @string resource [HardcodedText]\n"
                + "        android:text=\"Hardcoded\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintFiles("res/layout/ignores2.xml"));
    }
}
