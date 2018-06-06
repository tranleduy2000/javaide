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

import static com.android.SdkConstants.ATTR_DRAWABLE_RIGHT;
import static com.android.SdkConstants.ATTR_LAYOUT_ALIGN_END;
import static com.android.SdkConstants.ATTR_LAYOUT_ALIGN_PARENT_LEFT;
import static com.android.SdkConstants.ATTR_LAYOUT_ALIGN_PARENT_START;
import static com.android.SdkConstants.ATTR_LAYOUT_ALIGN_RIGHT;
import static com.android.SdkConstants.ATTR_LAYOUT_MARGIN_END;
import static com.android.SdkConstants.ATTR_NAME;
import static com.android.tools.lint.checks.RtlDetector.ATTRIBUTES;
import static com.android.tools.lint.checks.RtlDetector.convertNewToOld;
import static com.android.tools.lint.checks.RtlDetector.convertOldToNew;
import static com.android.tools.lint.checks.RtlDetector.convertToOppositeDirection;
import static com.android.tools.lint.checks.RtlDetector.isRtlAttributeName;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("javadoc")
public class RtlDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new RtlDetector();
    }

    private Set<Issue> mEnabled = new HashSet<Issue>();
    private static final Set<Issue> ALL = new HashSet<Issue>();
    static {
        ALL.add(RtlDetector.USE_START);
        ALL.add(RtlDetector.ENABLED);
        ALL.add(RtlDetector.COMPAT);
        ALL.add(RtlDetector.SYMMETRY);
    }

    public void testIsRtlAttributeName() {
        assertTrue(isRtlAttributeName(ATTR_LAYOUT_ALIGN_PARENT_START));
        assertTrue(isRtlAttributeName(ATTR_LAYOUT_MARGIN_END));
        assertTrue(isRtlAttributeName(ATTR_LAYOUT_ALIGN_END));
        assertFalse(isRtlAttributeName(ATTR_LAYOUT_ALIGN_PARENT_LEFT));
        assertFalse(isRtlAttributeName(ATTR_DRAWABLE_RIGHT));
        assertFalse(isRtlAttributeName(ATTR_LAYOUT_ALIGN_RIGHT));
        assertFalse(isRtlAttributeName(ATTR_NAME));
    }

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }

    public void testTarget14WithRtl() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "minsdk5targetsdk14.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testTarget17WithRtl() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "res/layout/rtl.xml:14: Warning: Use \"start\" instead of \"left\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"left\"\n"
                + "                                ~~~~\n"
                + "res/layout/rtl.xml:22: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"right\"\n"
                + "                                ~~~~~\n"
                + "res/layout/rtl.xml:30: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:gravity=\"right\"\n"
                + "                         ~~~~~\n"
                + "AndroidManifest.xml: Warning: The project references RTL attributes, but does not explicitly enable or disable RTL support with android:supportsRtl in the manifest [RtlEnabled]\n"
                + "0 errors, 4 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testTarget14() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "minsdk5targetsdk14.xml=>AndroidManifest.xml"
                ));
    }

    public void testOlderCompilationTarget() throws Exception {
        mEnabled = ALL;
        assertEquals(
                "No warnings.",

                lintProject(
                        "rtl/project-api14.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testUseStart() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.USE_START);
        assertEquals(""
                + "res/layout/rtl.xml:14: Warning: Use \"start\" instead of \"left\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"left\"\n"
                + "                                ~~~~\n"
                + "res/layout/rtl.xml:22: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"right\"\n"
                + "                                ~~~~~\n"
                + "res/layout/rtl.xml:30: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:gravity=\"right\"\n"
                + "                         ~~~~~\n"
                + "0 errors, 3 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testTarget17Rtl() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.USE_START);
        assertEquals(""
                + "res/layout/rtl.xml:14: Warning: Use \"start\" instead of \"left\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"left\"\n"
                + "                                ~~~~\n"
                + "res/layout/rtl.xml:22: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:layout_gravity=\"right\"\n"
                + "                                ~~~~~\n"
                + "res/layout/rtl.xml:30: Warning: Use \"end\" instead of \"right\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        android:gravity=\"right\"\n"
                + "                         ~~~~~\n"
                + "0 errors, 3 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/min17rtl.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testRelativeLayoutInOld() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.USE_START);
        assertEquals(""
                + "res/layout/relative.xml:10: Warning: Consider adding android:layout_alignParentStart=\"true\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignParentLeft=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:13: Warning: Consider adding android:layout_marginStart=\"40dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_marginLeft=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:24: Warning: Consider adding android:layout_marginStart=\"40dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_marginLeft=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:26: Warning: Consider adding android:layout_toEndOf=\"@id/loading_progress\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_toRightOf=\"@id/loading_progress\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:29: Warning: Consider adding android:paddingEnd=\"120dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:paddingRight=\"120dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:37: Warning: Consider adding android:layout_alignParentStart=\"true\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignParentLeft=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:38: Warning: Consider adding android:layout_alignEnd=\"@id/text\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignRight=\"@id/text\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:47: Warning: Consider adding android:layout_alignStart=\"@id/cancel\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignLeft=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:48: Warning: Consider adding android:layout_alignEnd=\"@id/cancel\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignRight=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 9 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/relative.xml=>res/layout/relative.xml"
                ));
    }

    public void testRelativeLayoutInNew() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.USE_START);
        assertEquals(""
                + "res/layout/relative.xml:10: Warning: Consider replacing android:layout_alignParentLeft with android:layout_alignParentStart=\"true\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignParentLeft=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:13: Warning: Consider replacing android:layout_marginLeft with android:layout_marginStart=\"40dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_marginLeft=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:24: Warning: Consider replacing android:layout_marginLeft with android:layout_marginStart=\"40dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_marginLeft=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:26: Warning: Consider replacing android:layout_toRightOf with android:layout_toEndOf=\"@id/loading_progress\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_toRightOf=\"@id/loading_progress\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:29: Warning: Consider replacing android:paddingRight with android:paddingEnd=\"120dip\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:paddingRight=\"120dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:37: Warning: Consider replacing android:layout_alignParentLeft with android:layout_alignParentStart=\"true\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignParentLeft=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:38: Warning: Consider replacing android:layout_alignRight with android:layout_alignEnd=\"@id/text\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignRight=\"@id/text\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:47: Warning: Consider replacing android:layout_alignLeft with android:layout_alignStart=\"@id/cancel\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignLeft=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:48: Warning: Consider replacing android:layout_alignRight with android:layout_alignEnd=\"@id/cancel\" to better support right-to-left layouts [RtlHardcoded]\n"
                + "        android:layout_alignRight=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 9 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/min17rtl.xml=>AndroidManifest.xml",
                        "rtl/relative.xml=>res/layout/relative.xml"
                ));
    }

    public void testRelativeLayoutCompat() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.COMPAT);
        assertEquals(""
                + "res/layout/relative.xml:10: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_alignParentLeft=\"true\" [RtlCompat]\n"
                + "        android:layout_alignParentStart=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:13: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_marginLeft=\"40dip\" [RtlCompat]\n"
                + "        android:layout_marginStart=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:24: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_marginLeft=\"40dip\" [RtlCompat]\n"
                + "        android:layout_marginStart=\"40dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:26: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_toRightOf=\"@id/loading_progress\" [RtlCompat]\n"
                + "        android:layout_toEndOf=\"@id/loading_progress\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:29: Error: To support older versions than API 17 (project specifies 5) you should also add android:paddingRight=\"120dip\" [RtlCompat]\n"
                + "        android:paddingEnd=\"120dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:37: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_alignParentLeft=\"true\" [RtlCompat]\n"
                + "        android:layout_alignParentStart=\"true\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:38: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_alignRight=\"@id/text\" [RtlCompat]\n"
                + "        android:layout_alignEnd=\"@id/text\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:47: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_alignLeft=\"@id/cancel\" [RtlCompat]\n"
                + "        android:layout_alignStart=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/relative.xml:48: Error: To support older versions than API 17 (project specifies 5) you should also add android:layout_alignRight=\"@id/cancel\" [RtlCompat]\n"
                + "        android:layout_alignEnd=\"@id/cancel\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "9 errors, 0 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/relativeCompat.xml=>res/layout/relative.xml"
                ));
    }


    public void testRelativeCompatOk() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/relativeOk.xml=>res/layout/relative.xml"
                ));
    }

    public void testTarget17NoRtl() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/min17nortl.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testJava() throws Exception {
        mEnabled = ALL;
        assertEquals(""
                + "src/test/pkg/GravityTest.java:24: Warning: Use \"Gravity.START\" instead of \"Gravity.LEFT\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        t1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);\n"
                + "                              ~~~~\n"
                + "src/test/pkg/GravityTest.java:30: Warning: Use \"Gravity.START\" instead of \"Gravity.LEFT\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
                + "        t1.setGravity(LEFT | RIGHT); // static imports\n"
                + "                      ~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/GravityTest.java.txt=>src/test/pkg/GravityTest.java"
                ));
    }

    public void testOk1() throws Exception {
        mEnabled = ALL;
        // targetSdkVersion < 17
        assertEquals(
                "No warnings.",

                lintProject("rtl/rtl.xml=>res/layout/rtl.xml"));
    }

    public void testOk2() throws Exception {
        mEnabled = ALL;
        // build target < 14
        assertEquals(
                "No warnings.",

                lintProject(
                        "overdraw/project.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/rtl.xml=>res/layout/rtl.xml"
                ));
    }

    public void testNullLocalName() throws Exception {
        // Regression test for attribute with null local name
        mEnabled = ALL;
        assertEquals(
                "No warnings.",

                lintProject(
                        "overdraw/project.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/rtl_noprefix.xml=>res/layout/rtl.xml"
                ));
    }

    public void testSymmetry() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.SYMMETRY);
        assertEquals(""
                + "res/layout/relative.xml:29: Warning: When you define paddingRight you should probably also define paddingLeft for right-to-left symmetry [RtlSymmetry]\n"
                + "        android:paddingRight=\"120dip\"\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/relative.xml=>res/layout/relative.xml"
                ));
    }

    public void testCompatAttributeValueConversion() throws Exception {
        // Ensure that when the RTL value contains a direction, we produce the
        // compatibility version of it for the compatibility attribute, e.g. if the
        // attribute for paddingEnd is ?listPreferredItemPaddingEnd, when we suggest
        // also setting paddingRight we suggest ?listPreferredItemPaddingRight
        mEnabled = Collections.singleton(RtlDetector.COMPAT);
        assertEquals(""
                + "res/layout/symmetry.xml:8: Error: To support older versions than API 17 (project specifies 5) you should also add android:paddingRight=\"?android:listPreferredItemPaddingRight\" [RtlCompat]\n"
                + "        android:paddingEnd=\"?android:listPreferredItemPaddingEnd\"\n"
                + "        ~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/symmetry.xml=>res/layout/symmetry.xml"
                ));
    }

    public void testTextAlignment() throws Exception {
        mEnabled = Collections.singleton(RtlDetector.COMPAT);
        assertEquals(""
                + "res/layout/spinner.xml:49: Error: Inconsistent alignment specification between textAlignment and gravity attributes: was end, expected start [RtlCompat]\n"
                + "            android:textAlignment=\"textStart\"/> <!-- ERROR -->\n"
                + "                                   ~~~~~~~~~\n"
                + "    res/layout/spinner.xml:46: Incompatible direction here\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "rtl/project-api17.properties=>project.properties",
                        "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                        "rtl/spinner.xml=>res/layout/spinner.xml"
                ));
    }

    public void testConvertBetweenAttributes() {
        assertEquals("alignParentStart", convertOldToNew("alignParentLeft"));
        assertEquals("alignParentEnd", convertOldToNew("alignParentRight"));
        assertEquals("paddingEnd", convertOldToNew("paddingRight"));
        assertEquals("paddingStart", convertOldToNew("paddingLeft"));

        assertEquals("alignParentLeft", convertNewToOld("alignParentStart"));
        assertEquals("alignParentRight", convertNewToOld("alignParentEnd"));
        assertEquals("paddingRight", convertNewToOld("paddingEnd"));
        assertEquals("paddingLeft", convertNewToOld("paddingStart"));

        for (int i = 0, n = ATTRIBUTES.length; i < n; i += 2) {
            String oldAttribute = ATTRIBUTES[i];
            String newAttribute = ATTRIBUTES[i + 1];
            assertEquals(newAttribute, convertOldToNew(oldAttribute));
            assertEquals(oldAttribute, convertNewToOld(newAttribute));
        }
    }

    public void testConvertToOppositeDirection() {
        assertEquals("alignParentRight", convertToOppositeDirection("alignParentLeft"));
        assertEquals("alignParentLeft", convertToOppositeDirection("alignParentRight"));
        assertEquals("alignParentStart", convertToOppositeDirection("alignParentEnd"));
        assertEquals("alignParentEnd", convertToOppositeDirection("alignParentStart"));
        assertEquals("paddingLeft", convertToOppositeDirection("paddingRight"));
        assertEquals("paddingRight", convertToOppositeDirection("paddingLeft"));
        assertEquals("paddingStart", convertToOppositeDirection("paddingEnd"));
        assertEquals("paddingEnd", convertToOppositeDirection("paddingStart"));
    }

    public void testEnumConstants() throws Exception {
        // Regression test for
        //   https://code.google.com/p/android/issues/detail?id=75480
        // Also checks that static imports work correctly
        mEnabled = ALL;
        assertEquals(""
            + "src/test/pkg/GravityTest2.java:19: Warning: Use \"Gravity.START\" instead of \"Gravity.LEFT\" to ensure correct behavior in right-to-left locales [RtlHardcoded]\n"
            + "        if (gravity == LEFT) { // ERROR\n"
            + "                       ~~~~\n"
            + "0 errors, 1 warnings\n",

            lintProject(
                    "rtl/project-api17.properties=>project.properties",
                    "rtl/minsdk5targetsdk17.xml=>AndroidManifest.xml",
                    "rtl/GravityTest2.java.txt=>src/test/pkg/GravityTest2.java"
            ));
    }
}
