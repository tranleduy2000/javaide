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

import java.io.File;

@SuppressWarnings("javadoc")
public class RequiredAttributeDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new RequiredAttributeDetector();
    }

    public void test() throws Exception {
        // Simple: Only consider missing attributes in the layout xml file
        // (though skip warnings on <merge> tags and under <GridLayout>
        assertEquals(
            "res/layout/size.xml:13: Error: The required layout_height attribute is missing [RequiredSize]\n" +
            "    <RadioButton\n" +
            "    ^\n" +
            "res/layout/size.xml:18: Error: The required layout_width attribute is missing [RequiredSize]\n" +
            "    <EditText\n" +
            "    ^\n" +
            "res/layout/size.xml:23: Error: The required layout_width and layout_height attributes are missing [RequiredSize]\n" +
            "    <EditText\n" +
            "    ^\n" +
            "3 errors, 0 warnings\n",

            lintProject("res/layout/size.xml"));
    }

    public void test2() throws Exception {
        // Consider styles (specifying sizes) and includes (providing sizes for the root tags)
        assertEquals(
            "res/layout/size2.xml:9: Error: The required layout_width and layout_height attributes are missing [RequiredSize]\n" +
            "    <Button\n" +
            "    ^\n" +
            "res/layout/size2.xml:18: Error: The required layout_height attribute is missing [RequiredSize]\n" +
            "    <Button\n" +
            "    ^\n" +
            "2 errors, 0 warnings\n",

            lintProject(
                    "res/layout/size2.xml",
                    "res/layout/sizeincluded.xml",
                    "res/values/sizestyles.xml"
                    ));
    }

    public void testInflaters() throws Exception {
        // Consider java inflation
        assertEquals(
            "res/layout/size5.xml:2: Error: The required layout_width and layout_height attributes are missing [RequiredSize]\n" +
            "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "^\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                    "src/test/pkg/InflaterTest.java.txt=>src/test/pkg/InflaterTest.java",
                    "res/layout/sizeincluded.xml=>res/layout/size1.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size2.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size3.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size4.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size5.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size6.xml",
                    "res/layout/sizeincluded.xml=>res/layout/size7.xml"
            ));
    }

    public void testRequestFocus() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=38700
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/layout/edit_type.xml"
            ));
    }

    public void testFrameworkStyles() throws Exception {
        // See http://code.google.com/p/android/issues/detail?id=38958
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/layout/listseparator.xml"
            ));
    }

    public void testThemeStyles() throws Exception {
        // Check that we don't complain about cases where the size is defined in a theme
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/layout/size.xml",
                    "res/values/themes.xml"
            ));
    }

    public void testThemeStyles2() throws Exception {
        // Check that we don't complain about cases where the size is defined in a theme
        assertEquals(
            "No warnings.",

            lintProject(
                    "res/layout/size.xml",
                    "res/values/themes2.xml"
            ));
    }

    public void testHasLayoutVariations() throws Exception {
        File projectDir = getProjectDir(null,
                copy("res/layout/size.xml"),
                copy("res/layout/size.xml", "res/layout-land/size.xml"),
                copy("res/layout/size.xml", "res/layout/size2.xml"));
        assertTrue(RequiredAttributeDetector.hasLayoutVariations(
                new File(projectDir, "res/layout/size.xml".replace('/', File.separatorChar))));
        assertTrue(RequiredAttributeDetector.hasLayoutVariations(
                new File(projectDir, "res/layout-land/size.xml".replace('/', File.separatorChar))));
        assertFalse(RequiredAttributeDetector.hasLayoutVariations(
                new File(projectDir, "res/layout/size2.xml".replace('/', File.separatorChar))));
    }
}
