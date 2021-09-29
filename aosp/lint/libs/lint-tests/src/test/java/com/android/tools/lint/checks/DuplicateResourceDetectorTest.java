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

import static com.android.tools.lint.detector.api.TextFormat.RAW;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Severity;

@SuppressWarnings("javadoc")
public class DuplicateResourceDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DuplicateResourceDetector();
    }

    public void test() throws Exception {
        assertEquals(
        "res/values/customattr2.xml:2: Error: ContentFrame has already been defined in this folder [DuplicateDefinition]\n" +
        "    <declare-styleable name=\"ContentFrame\">\n" +
        "                       ~~~~~~~~~~~~~~~~~~~\n" +
        "    res/values/customattr.xml:2: Previously defined here\n" +
        "res/values/strings2.xml:19: Error: wallpaper_instructions has already been defined in this folder [DuplicateDefinition]\n" +
        "    <string name=\"wallpaper_instructions\">Tap image to set landscape wallpaper</string>\n" +
        "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
        "    res/values/strings.xml:29: Previously defined here\n" +
        "2 errors, 0 warnings\n",

        lintProject(
                "res/values/strings.xml",
                "res/values-land/strings.xml=>res/values/strings2.xml",
                "res/values-cs/strings.xml",
                "res/values/customattr.xml",
                "res/values/customattr.xml=>res/values/customattr2.xml"));
    }

    public void testDotAliases() throws Exception {
        assertEquals(""
                + "res/values/duplicate-strings2.xml:5: Error: app_name has already been defined in this folder (app_name is equivalent to app.name) [DuplicateDefinition]\n"
                + "    <string name=\"app.name\">App Name 1</string>\n"
                + "            ~~~~~~~~~~~~~~~\n"
                + "    res/values/duplicate-strings2.xml:4: Previously defined here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/duplicate-strings2.xml"));
    }

    public void testSameFile() throws Exception {
        assertEquals(""
                + "res/values/duplicate-strings.xml:6: Error: app_name has already been defined in this folder [DuplicateDefinition]\n"
                + "    <string name=\"app_name\">App Name 1</string>\n"
                + "            ~~~~~~~~~~~~~~~\n"
                + "    res/values/duplicate-strings.xml:4: Previously defined here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/duplicate-strings.xml"));
    }

    public void testStyleItems() throws Exception {
        assertEquals(""
                + "res/values/duplicate-items.xml:7: Error: android:textColor has already been defined in this <style> [DuplicateDefinition]\n"
                + "        <item name=\"android:textColor\">#ff0000</item>\n"
                + "              ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/values/duplicate-items.xml:5: Previously defined here\n"
                + "res/values/duplicate-items.xml:13: Error: contentId has already been defined in this <declare-styleable> [DuplicateDefinition]\n"
                + "        <attr name=\"contentId\" format=\"integer\" />\n"
                + "              ~~~~~~~~~~~~~~~~\n"
                + "    res/values/duplicate-items.xml:11: Previously defined here\n"
                + "2 errors, 0 warnings\n",

                lintProject(
                        "res/values/duplicate-items.xml"));
    }

    public void testOk() throws Exception {
        assertEquals(
        "No warnings.",

        lintProject(
                "res/values/strings.xml",
                "res/values-cs/strings.xml",
                "res/values-de-rDE/strings.xml",
                "res/values-es/strings.xml",
                "res/values-es-rUS/strings.xml",
                "res/values-land/strings.xml",
                "res/values-cs/arrays.xml",
                "res/values-es/donottranslate.xml",
                "res/values-nl-rNL/strings.xml"));
    }

    public void testResourceAliases() throws Exception {
        assertEquals(""
                + "res/values/refs.xml:3: Error: Unexpected resource reference type; expected value of type @string/ [ReferenceType]\n"
                + "    <item name=\"invalid1\" type=\"string\">@layout/other</item>\n"
                + "                                        ^\n"
                + "res/values/refs.xml:5: Error: Unexpected resource reference type; expected value of type @drawable/ [ReferenceType]\n"
                + "          @layout/other\n"
                + "          ^\n"
                + "res/values/refs.xml:10: Error: Unexpected resource reference type; expected value of type @string/ [ReferenceType]\n"
                + "    <string name=\"invalid4\">@layout/indirect</string>\n"
                + "                            ^\n"
                + "res/values/refs.xml:15: Error: Unexpected resource reference type; expected value of type @color/ [ReferenceType]\n"
                + "    <item name=\"drawableAsColor\" type=\"color\">@drawable/my_drawable</item>\n"
                + "                                              ^\n"
                + "4 errors, 0 warnings\n",

            lintProject("res/values/refs.xml"));
    }

    public void testGetExpectedType() {
        assertEquals("string", DuplicateResourceDetector.getExpectedType(
                "Unexpected resource reference type; expected value of type `@string/`", RAW));
        assertEquals("string", DuplicateResourceDetector.getExpectedType(
                "Unexpected resource reference type; expected value of type @string/", TEXT));
    }

    public void testMipmapDrawable() throws Exception {
        // https://code.google.com/p/android/issues/detail?id=109892
        assertEquals("No warnings.",

                lintProject("res/values/refs2.xml"));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        if (issue == DuplicateResourceDetector.TYPE_MISMATCH) {
            assertNotNull(message, DuplicateResourceDetector.getExpectedType(message, TEXT));
        }
    }
}
