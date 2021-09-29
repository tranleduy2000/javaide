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
import com.android.tools.lint.detector.api.Issue;

@SuppressWarnings("javadoc")
public class ButtonDetectorTest extends AbstractCheckTest {
    private static Issue sTestIssue;

    @Override
    protected boolean isEnabled(Issue issue) {
        return super.isEnabled(issue) && sTestIssue == null || issue == sTestIssue;
    }

    @Override
    protected Detector getDetector() {
        return new ButtonDetector();
    }

    public void testButtonOrder() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout/buttonbar.xml:12: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:44: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:92: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:124: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:140: Warning: OK button should be on the right (was \"Ok | CANCEL\", should be \"CANCEL | Ok\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:156: Warning: OK button should be on the right (was \"OK | Abort\", should be \"Abort | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:177: Warning: Cancel button should be on the left (was \"Send | Cancel\", should be \"Cancel | Send\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrder2() throws Exception {
        // If the layout is in v14, it had better have the right order
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout-v14/buttonbar.xml:12: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:44: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:92: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:124: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:140: Warning: OK button should be on the right (was \"Ok | CANCEL\", should be \"CANCEL | Ok\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:156: Warning: OK button should be on the right (was \"OK | Abort\", should be \"Abort | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v14/buttonbar.xml:177: Warning: Cancel button should be on the left (was \"Send | Cancel\", should be \"Cancel | Send\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject(
                    "minsdk5targetsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml=>res/layout-v14/buttonbar.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrder3() throws Exception {
        // Similar to test 3, but also complain if the -v version is *higher* than 14
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout-v16/buttonbar.xml:12: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:44: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:92: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:124: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:140: Warning: OK button should be on the right (was \"Ok | CANCEL\", should be \"CANCEL | Ok\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:156: Warning: OK button should be on the right (was \"OK | Abort\", should be \"Abort | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-v16/buttonbar.xml:177: Warning: Cancel button should be on the left (was \"Send | Cancel\", should be \"Cancel | Send\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject(
                    "minsdk5targetsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml=>res/layout-v16/buttonbar.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrder4() throws Exception {
        // Targeting 14 but using a layout that also needs to work for older platforms:
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout/buttonbar.xml:12: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:44: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:92: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:124: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:140: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"Ok | CANCEL\", should be \"CANCEL | Ok\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:156: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: OK button should be on the right (was \"OK | Abort\", should be \"Abort | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:177: Warning: Layout uses the wrong button order for API >= 14: Create a layout-v14/buttonbar.xml file with opposite order: Cancel button should be on the left (was \"Send | Cancel\", should be \"Cancel | Send\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject(
                    "minsdk5targetsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrder5() throws Exception {
        // If the layout is in a non-ICS folder and has the wrong button order,
        // but there is a v14 version of the layout, don't complain about the non-v14 version
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "No warnings.",

            lintProject(
                    "minsdk5targetsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml",
                    "res/layout/layout1.xml=>res/layout-v14/buttonbar.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testSuppressed() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "No warnings.",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar_suppressed.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrderRelativeLayout() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "No warnings.",

            lintProject("res/layout/buttonbar2.xml", "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrderRelativeLayout2() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout/buttonbar3.xml:27: Warning: Cancel button should be on the left [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar3.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonOrderRelativeLayout3() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "No warnings.",

            lintProject("res/layout/buttonbar4.xml", "res/values/buttonbar-values.xml"));
    }


    public void testCase() throws Exception {
        sTestIssue = ButtonDetector.CASE;
        assertEquals(
            "res/values/buttonbar-values.xml:9: Warning: The standard Android way to capitalize Ok is \"OK\" (tip: use @android:string/ok instead) [ButtonCase]\n" +
            "    <string name=\"resume2\"> Ok </string>\n" +
            "                            ^\n" +
            "res/values/buttonbar-values.xml:10: Warning: The standard Android way to capitalize CANCEL is \"Cancel\" (tip: use @android:string/cancel instead) [ButtonCase]\n" +
            "    <string name=\"giveup2\">\"CANCEL\"</string>\n" +
            "                           ^\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject("res/layout/buttonbar.xml", "res/values/buttonbar-values.xml"));
    }

    public void testBack() throws Exception {
        sTestIssue = ButtonDetector.BACK_BUTTON;
        assertEquals(
            "res/layout/buttonbar.xml:183: Warning: Back buttons are not standard on Android; see design guide's navigation section [BackButton]\n" +
            "    <Button\n" +
            "    ^\n" +
            "0 errors, 1 warnings\n" +
            "",

            lintProject("res/layout/buttonbar.xml", "res/values/buttonbar-values.xml"));
    }

    public void testOldApp() throws Exception {
        // Target SDK < 14 - no warnings on button order
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
                "No warnings.",

        lintProject(
                "minsdk5targetsdk9.xml=>AndroidManifest.xml",
                "res/layout/buttonbar.xml",
                "res/values/buttonbar-values.xml"));
    }

    public void testEnglishLocales() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            "res/layout-en-rGB/buttonbar.xml:12: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:44: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:92: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:124: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:140: Warning: OK button should be on the right (was \"Ok | CANCEL\", should be \"CANCEL | Ok\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:156: Warning: OK button should be on the right (was \"OK | Abort\", should be \"Abort | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-en-rGB/buttonbar.xml:177: Warning: Cancel button should be on the left (was \"Send | Cancel\", should be \"Cancel | Send\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 7 warnings\n" +
            "",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml=>res/layout-en-rGB/buttonbar.xml",
                    "res/values/buttonbar-values.xml=>res/values-en-rGB/buttonbar-values.xml"));
    }

    public void testOtherLocales() throws Exception {
        sTestIssue = ButtonDetector.ORDER;
        assertEquals(
            // Hardcoded values only
            "res/layout-de/buttonbar.xml:12: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout-de/buttonbar.xml:44: Warning: OK button should be on the right (was \"OK | Cancel\", should be \"Cancel | OK\") [ButtonOrder]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 2 warnings\n" +
            "",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml=>res/layout-de/buttonbar.xml",
                    "res/values/buttonbar-values.xml=>res/values-de/buttonbar-values.xml"));
    }

    public void testOtherLocales2() throws Exception {
        sTestIssue = ButtonDetector.CASE;
        assertEquals(
                "No warnings.",

                lintProject("res/layout/buttonbar.xml=>res/layout-de/buttonbar.xml",
                        "res/values/buttonbar-values.xml=>res/values-de/buttonbar-values.xml"));
    }

    public void testButtonStyle() throws Exception {
        sTestIssue = ButtonDetector.STYLE;
        assertEquals(
            "res/layout/buttonbar.xml:12: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:17: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:28: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:33: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:44: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:49: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:60: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:65: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:76: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:81: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:92: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:97: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:108: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:113: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:124: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:129: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:140: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:145: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:156: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "res/layout/buttonbar.xml:161: Warning: Buttons in button bars should be borderless; use style=\"?android:attr/buttonBarButtonStyle\" (and ?android:attr/buttonBarStyle on the parent) [ButtonStyle]\n" +
            "        <Button\n" +
            "        ^\n" +
            "0 errors, 20 warnings\n",

            lintProject(
                    "apicheck/minsdk14.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml",
                    "res/layout/buttonbar2.xml",
                    "res/layout/buttonbar3.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testButtonStyleOldMinSdk() throws Exception {
        sTestIssue = ButtonDetector.STYLE;
        assertEquals(
            "No warnings.",

            lintProject(
                    "apicheck/minsdk4.xml=>AndroidManifest.xml",
                    "res/layout/buttonbar.xml",
                    "res/layout/buttonbar2.xml",
                    "res/layout/buttonbar3.xml",
                    "res/values/buttonbar-values.xml"));
    }

    public void testYesNo() throws Exception {
        sTestIssue = ButtonDetector.CASE;
        assertEquals(""
                + "res/layout/yesno.xml:10: Warning: @android:string/yes actually returns \"OK\", not \"Yes\"; use @android:string/ok instead or create a local string resource for Yes [ButtonCase]\n"
                + "        android:text=\"@android:string/yes\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/layout/yesno.xml:15: Warning: @android:string/no actually returns \"Cancel\", not \"No\"; use @android:string/cancel instead or create a local string resource for No [ButtonCase]\n"
                + "        android:text=\"@android:string/no\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "apicheck/minsdk4.xml=>AndroidManifest.xml",
                        "res/layout/yesno.xml"));
    }

    public void testIssue180417() throws Exception {
        // Regression test for
        //   https://code.google.com/p/android/issues/detail?id=180417
        sTestIssue = ButtonDetector.CASE;
        assertEquals(""
                        + "res/values/buttonbar-values.xml:4: Warning: The standard Android way to capitalize Ok is \"OK\" (tip: use @android:string/ok instead) [ButtonCase]\n"
                        + "    <string name=\"ok\">\"Ok\"</string>\n"
                        + "                      ^\n"
                        + "0 errors, 1 warnings\n",
                lintProject(
                        xml("res/values/buttonbar-values.xml", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<resources>\n"
                                + "\n"
                                + "    <string name=\"ok\">\"Ok\"</string>\n"
                                + "\n"
                                + "</resources>\n"),
                        xml("res/layout/buttonbar", ""
                                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                                + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                                + "    android:layout_width=\"match_parent\"\n"
                                + "    android:layout_height=\"match_parent\"\n"
                                + "    android:orientation=\"vertical\" >\n"
                                + "    <LinearLayout\n"
                                + "        android:layout_width=\"match_parent\"\n"
                                + "        android:layout_height=\"wrap_content\" >\n"
                                + "\n"
                                + "        <Button\n"
                                + "            android:layout_width=\"wrap_content\"\n"
                                + "            android:layout_height=\"wrap_content\"\n"
                                + "            android:text=\"@string/cancel\" />\n"
                                + "\n"
                                + "        <Button\n"
                                + "            android:layout_width=\"wrap_content\"\n"
                                + "            android:layout_height=\"wrap_content\"\n"
                                + "            android:text=\"@string/ok\" />\n"
                                + "    </LinearLayout>\n"
                                + "</LinearLayout>\n"))
                );
    }

    public void testIssue101279() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=101279
        sTestIssue = ButtonDetector.STYLE;
        assertEquals(
                "No warnings.",

                lintProject(
                        "apicheck/minsdk4.xml=>AndroidManifest.xml",
                        "res/layout/buttonbar5.xml"));
    }
}
