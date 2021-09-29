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

@SuppressWarnings("javadoc")
public class ResourceCycleDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new ResourceCycleDetector();
    }

    public void testStyles() throws Exception {
        assertEquals(""
                + "res/values/styles.xml:9: Error: Style DetailsPage_EditorialBuyButton should not extend itself [ResourceCycle]\n"
                + "<style name=\"DetailsPage_EditorialBuyButton\" parent=\"@style/DetailsPage_EditorialBuyButton\" />\n"
                + "                                             ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject("res/values/styles.xml"));
    }

    public void testStyleImpliedParent() throws Exception {
        assertEquals(""
                + "res/values/stylecycle.xml:3: Error: Potential cycle: PropertyToggle is the implied parent of PropertyToggle.Base and this defines the opposite [ResourceCycle]\n"
                + "  <style name=\"PropertyToggle\" parent=\"@style/PropertyToggle.Base\"></style>\n"
                + "                               ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

            lintProject("res/values/stylecycle.xml"));
    }

    public void testLayouts() throws Exception {
        assertEquals(""
                + "res/layout/layoutcycle1.xml:10: Error: Layout layoutcycle1 should not include itself [ResourceCycle]\n"
                + "        layout=\"@layout/layoutcycle1\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject("res/layout/layoutcycle1.xml"));
    }

    public void testColors() throws Exception {
        assertEquals(""
                + "res/values/colorcycle1.xml:2: Error: Color test should not reference itself [ResourceCycle]\n"
                + "    <color name=\"test\">@color/test</color>\n"
                + "                       ^\n"
                + "1 errors, 0 warnings\n",

                lintProject("res/values/colorcycle1.xml"));
    }

    public void testAaptCrash() throws Exception {
        assertEquals(""
                + "res/values/aaptcrash.xml:5: Error: This construct can potentially crash aapt during a build. Change @+id/titlebar to @id/titlebar and define the id explicitly using <item type=\"id\" name=\"titlebar\"/> instead. [AaptCrash]\n"
                + "        <item name=\"android:id\">@+id/titlebar</item>\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject("res/values/aaptcrash.xml"));
    }

    public void testDeepColorCycle1() throws Exception {
        assertEquals(""
                + "res/values/colorcycle2.xml:2: Error: Color Resource definition cycle: test1 => test2 => test3 => test1 [ResourceCycle]\n"
                + "    <color name=\"test1\">@color/test2</color>\n"
                + "                        ^\n"
                + "    res/values/colorcycle4.xml:2: Reference from @color/test3 to color/test1 here\n"
                + "    res/values/colorcycle3.xml:2: Reference from @color/test2 to color/test3 here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/colorcycle2.xml",
                        "res/values/colorcycle3.xml",
                        "res/values/colorcycle4.xml"
                ));
    }

    public void testDeepColorCycle2() throws Exception {
        assertEquals(""
                + "res/values/colorcycle5.xml:2: Error: Color Resource definition cycle: test1 => test2 => test1 [ResourceCycle]\n"
                + "    <color name=\"test1\">@color/test2</color>\n"
                + "                        ^\n"
                + "    res/values/colorcycle5.xml:3: Reference from @color/test2 to color/test1 here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/colorcycle5.xml"
                ));
    }

    public void testDeepStyleCycle1() throws Exception {
        assertEquals(""
                + "res/values/stylecycle1.xml:6: Error: Style Resource definition cycle: ButtonStyle => ButtonStyle.Base => ButtonStyle [ResourceCycle]\n"
                + "    <style name=\"ButtonStyle\" parent=\"ButtonStyle.Base\">\n"
                + "                              ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/values/stylecycle1.xml:3: Reference from @style/ButtonStyle.Base to style/ButtonStyle here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/stylecycle1.xml"
                ));
    }

    public void testDeepStyleCycle2() throws Exception {
        assertEquals(""
                + "res/values/stylecycle2.xml:3: Error: Style Resource definition cycle: mystyle1 => mystyle2 => mystyle3 => mystyle1 [ResourceCycle]\n"
                + "    <style name=\"mystyle1\" parent=\"@style/mystyle2\">\n"
                + "                           ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/values/stylecycle2.xml:9: Reference from @style/mystyle3 to style/mystyle1 here\n"
                + "    res/values/stylecycle2.xml:6: Reference from @style/mystyle2 to style/mystyle3 here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/stylecycle2.xml"
                ));
    }

    public void testDeepIncludeOk() throws Exception {
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "res/layout/layout1.xml",
                        "res/layout/layout2.xml",
                        "res/layout/layout3.xml",
                        "res/layout/layout4.xml"
                ));
    }

    public void testDeepIncludeCycle() throws Exception {
        assertEquals(""
                + "res/layout/layout1.xml:10: Error: Layout Resource definition cycle: layout1 => layout2 => layout4 => layout1 [ResourceCycle]\n"
                + "        layout=\"@layout/layout2\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/layout/layout4.xml:16: Reference from @layout/layout4 to layout/layout1 here\n"
                + "    res/layout/layout2.xml:16: Reference from @layout/layout2 to layout/layout4 here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/layout/layout1.xml",
                        "res/layout/layout2.xml",
                        "res/layout/layout3.xml",
                        "res/layout/layout4cycle.xml=>res/layout/layout4.xml"
                ));
    }

    public void testDeepAliasCycle() throws Exception {
        assertEquals(""
                + "res/values/aliases.xml:2: Error: Layout Resource definition cycle: layout10 => layout20 => layout30 => layout10 [ResourceCycle]\n"
                + "    <item name=\"layout10\" type=\"layout\">@layout/layout20</item>\n"
                + "                                        ^\n"
                + "    res/values/aliases.xml:4: Reference from @layout/layout30 to layout/layout10 here\n"
                + "    res/values/aliases.xml:3: Reference from @layout/layout20 to layout/layout30 here\n"
                + "res/values/colorcycle2.xml:2: Error: Color Resource definition cycle: test1 => test2 => test1 [ResourceCycle]\n"
                + "    <color name=\"test1\">@color/test2</color>\n"
                + "                        ^\n"
                + "    res/values/aliases.xml:5: Reference from @color/test2 to color/test1 here\n"
                + "res/layout/layout1.xml:10: Error: Layout Resource definition cycle: layout1 => layout2 => layout4 => layout1 [ResourceCycle]\n"
                + "        layout=\"@layout/layout2\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "    res/values/aliases.xml:6: Reference from @layout/layout4 to layout/layout1 here\n"
                + "    res/layout/layout2.xml:16: Reference from @layout/layout2 to layout/layout4 here\n"
                + "3 errors, 0 warnings\n",

                lintProject(
                        "res/values/aliases.xml",
                        "res/layout/layout1.xml",
                        "res/layout/layout2.xml",
                        "res/layout/layout3.xml",
                        "res/values/colorcycle2.xml"
                ));
    }

    public void testColorStateListCycle() throws Exception {
        assertEquals(""
                + "res/values/aliases2.xml:2: Error: Color Resource definition cycle: bright_foreground_dark => color1 => bright_foreground_dark [ResourceCycle]\n"
                + "    <item name=\"bright_foreground_dark\" type=\"color\">@color/color1</item>\n"
                + "                                                     ^\n"
                + "    res/color/color1.xml:3: Reference from @color/color1 to color/bright_foreground_dark here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/color/color1.xml",
                        "res/values/aliases2.xml"
                ));
    }

    public void testDrawableStateListCycle() throws Exception {
        assertEquals(""
                + "res/drawable/drawable1.xml:4: Error: Drawable Resource definition cycle: drawable1 => textfield_search_pressed => drawable2 => drawable1 [ResourceCycle]\n"
                + "    <item android:state_window_focused=\"false\" android:state_enabled=\"true\"\n"
                + "    ^\n"
                + "    res/values/aliases2.xml:4: Reference from @drawable/drawable2 to drawable/drawable1 here\n"
                + "    res/values/aliases2.xml:3: Reference from @drawable/textfield_search_pressed to drawable/drawable2 here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/drawable/drawable1.xml",
                        "res/values/aliases2.xml"
                ));
    }
}
