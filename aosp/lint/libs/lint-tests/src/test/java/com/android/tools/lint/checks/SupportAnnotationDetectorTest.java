/*
 * Copyright (C) 2015 The Android Open Source Project
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

import static com.android.tools.lint.checks.SupportAnnotationDetector.PERMISSION_ANNOTATION;

import com.android.tools.lint.ExternalAnnotationRepository;
import com.android.tools.lint.ExternalAnnotationRepositoryTest;
import com.android.tools.lint.client.api.JavaParser.ResolvedAnnotation;
import com.android.tools.lint.client.api.JavaParser.ResolvedMethod;
import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("ClassNameDiffersFromFileName") // For embedded unit tests
public class SupportAnnotationDetectorTest extends AbstractCheckTest {
    private static final boolean SDK_ANNOTATIONS_AVAILABLE =
            new SupportAnnotationDetectorTest().createClient().findResource(
            ExternalAnnotationRepository.SDK_ANNOTATIONS_PATH) != null;

    @Override
    protected Detector getDetector() {
        return new SupportAnnotationDetector();
    }

    public void testRange() throws Exception {
        assertEquals(""
                + "src/test/pkg/RangeTest.java:32: Error: Expected length 5 (was 4) [Range]\n"
                + "        printExact(\"1234\"); // ERROR\n"
                + "                   ~~~~~~\n"
                + "src/test/pkg/RangeTest.java:34: Error: Expected length 5 (was 6) [Range]\n"
                + "        printExact(\"123456\"); // ERROR\n"
                + "                   ~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:36: Error: Expected length ≥ 5 (was 4) [Range]\n"
                + "        printMin(\"1234\"); // ERROR\n"
                + "                 ~~~~~~\n"
                + "src/test/pkg/RangeTest.java:43: Error: Expected length ≤ 8 (was 9) [Range]\n"
                + "        printMax(\"123456789\"); // ERROR\n"
                + "                 ~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:45: Error: Expected length ≥ 4 (was 3) [Range]\n"
                + "        printRange(\"123\"); // ERROR\n"
                + "                   ~~~~~\n"
                + "src/test/pkg/RangeTest.java:49: Error: Expected length ≤ 6 (was 7) [Range]\n"
                + "        printRange(\"1234567\"); // ERROR\n"
                + "                   ~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:53: Error: Expected size 5 (was 4) [Range]\n"
                + "        printExact(new int[]{1, 2, 3, 4}); // ERROR\n"
                + "                   ~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:55: Error: Expected size 5 (was 6) [Range]\n"
                + "        printExact(new int[]{1, 2, 3, 4, 5, 6}); // ERROR\n"
                + "                   ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:57: Error: Expected size ≥ 5 (was 4) [Range]\n"
                + "        printMin(new int[]{1, 2, 3, 4}); // ERROR\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:65: Error: Expected size ≤ 8 (was 9) [Range]\n"
                + "        printMax(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}); // ERROR\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:67: Error: Expected size ≥ 4 (was 3) [Range]\n"
                + "        printRange(new int[] {1,2,3}); // ERROR\n"
                + "                   ~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:71: Error: Expected size ≤ 6 (was 7) [Range]\n"
                + "        printRange(new int[] {1,2,3,4,5,6,7}); // ERROR\n"
                + "                   ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:74: Error: Expected size to be a multiple of 3 (was 4 and should be either 3 or 6) [Range]\n"
                + "        printMultiple(new int[] {1,2,3,4}); // ERROR\n"
                + "                      ~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:75: Error: Expected size to be a multiple of 3 (was 5 and should be either 3 or 6) [Range]\n"
                + "        printMultiple(new int[] {1,2,3,4,5}); // ERROR\n"
                + "                      ~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:77: Error: Expected size to be a multiple of 3 (was 7 and should be either 6 or 9) [Range]\n"
                + "        printMultiple(new int[] {1,2,3,4,5,6,7}); // ERROR\n"
                + "                      ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:80: Error: Expected size ≥ 4 (was 3) [Range]\n"
                + "        printMinMultiple(new int[]{1, 2, 3}); // ERROR\n"
                + "                         ~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RangeTest.java:84: Error: Value must be ≥ 4 (was 3) [Range]\n"
                + "        printAtLeast(3); // ERROR\n"
                + "                     ~\n"
                + "src/test/pkg/RangeTest.java:91: Error: Value must be ≤ 7 (was 8) [Range]\n"
                + "        printAtMost(8); // ERROR\n"
                + "                    ~\n"
                + "src/test/pkg/RangeTest.java:93: Error: Value must be ≥ 4 (was 3) [Range]\n"
                + "        printBetween(3); // ERROR\n"
                + "                     ~\n"
                + "src/test/pkg/RangeTest.java:98: Error: Value must be ≤ 7 (was 8) [Range]\n"
                + "        printBetween(8); // ERROR\n"
                + "                     ~\n"
                + "src/test/pkg/RangeTest.java:102: Error: Value must be ≥ 2.5 (was 2.49) [Range]\n"
                + "        printAtLeastInclusive(2.49f); // ERROR\n"
                + "                              ~~~~~\n"
                + "src/test/pkg/RangeTest.java:106: Error: Value must be > 2.5 (was 2.49) [Range]\n"
                + "        printAtLeastExclusive(2.49f); // ERROR\n"
                + "                              ~~~~~\n"
                + "src/test/pkg/RangeTest.java:107: Error: Value must be > 2.5 (was 2.5) [Range]\n"
                + "        printAtLeastExclusive(2.5f); // ERROR\n"
                + "                              ~~~~\n"
                + "src/test/pkg/RangeTest.java:113: Error: Value must be ≤ 7.0 (was 7.1) [Range]\n"
                + "        printAtMostInclusive(7.1f); // ERROR\n"
                + "                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:117: Error: Value must be < 7.0 (was 7.0) [Range]\n"
                + "        printAtMostExclusive(7.0f); // ERROR\n"
                + "                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:118: Error: Value must be < 7.0 (was 7.1) [Range]\n"
                + "        printAtMostExclusive(7.1f); // ERROR\n"
                + "                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:120: Error: Value must be ≥ 2.5 (was 2.4) [Range]\n"
                + "        printBetweenFromInclusiveToInclusive(2.4f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:124: Error: Value must be ≤ 5.0 (was 5.1) [Range]\n"
                + "        printBetweenFromInclusiveToInclusive(5.1f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:126: Error: Value must be > 2.5 (was 2.4) [Range]\n"
                + "        printBetweenFromExclusiveToInclusive(2.4f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:127: Error: Value must be > 2.5 (was 2.5) [Range]\n"
                + "        printBetweenFromExclusiveToInclusive(2.5f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:129: Error: Value must be ≤ 5.0 (was 5.1) [Range]\n"
                + "        printBetweenFromExclusiveToInclusive(5.1f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:131: Error: Value must be ≥ 2.5 (was 2.4) [Range]\n"
                + "        printBetweenFromInclusiveToExclusive(2.4f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:135: Error: Value must be < 5.0 (was 5.0) [Range]\n"
                + "        printBetweenFromInclusiveToExclusive(5.0f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:137: Error: Value must be > 2.5 (was 2.4) [Range]\n"
                + "        printBetweenFromExclusiveToExclusive(2.4f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:138: Error: Value must be > 2.5 (was 2.5) [Range]\n"
                + "        printBetweenFromExclusiveToExclusive(2.5f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:141: Error: Value must be < 5.0 (was 5.0) [Range]\n"
                + "        printBetweenFromExclusiveToExclusive(5.0f); // ERROR\n"
                + "                                             ~~~~\n"
                + "src/test/pkg/RangeTest.java:145: Error: Value must be ≥ 4 (was -7) [Range]\n"
                + "        printBetween(-7); // ERROR\n"
                + "                     ~~\n"
                + "src/test/pkg/RangeTest.java:146: Error: Value must be > 2.5 (was -10.0) [Range]\n"
                + "        printAtLeastExclusive(-10.0f); // ERROR\n"
                + "                              ~~~~~~\n"
                + "src/test/pkg/RangeTest.java:156: Error: Value must be ≥ -1 (was -2) [Range]\n"
                + "        printIndirect(-2); // ERROR\n"
                + "                      ~~\n"
                + "src/test/pkg/RangeTest.java:157: Error: Value must be ≤ 42 (was 43) [Range]\n"
                + "        printIndirect(43); // ERROR\n"
                + "                      ~~\n"
                + "src/test/pkg/RangeTest.java:158: Error: Expected length 5 (was 7) [Range]\n"
                + "        printIndirectSize(\"1234567\"); // ERROR\n"
                + "                          ~~~~~~~~~\n"
                + "41 errors, 0 warnings\n",

                lintProject("src/test/pkg/RangeTest.java.txt=>src/test/pkg/RangeTest.java",
                        "src/android/support/annotation/Size.java.txt=>src/android/support/annotation/Size.java",
                        "src/android/support/annotation/IntRange.java.txt=>src/android/support/annotation/IntRange.java",
                        "src/android/support/annotation/FloatRange.java.txt=>src/android/support/annotation/FloatRange.java"
                ));
    }

    public void testTypeDef() throws Exception {
        assertEquals(""
                + "src/test/pkg/IntDefTest.java:31: Error: Must be one of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setStyle(0, 0); // ERROR\n"
                + "                 ~\n"
                + "src/test/pkg/IntDefTest.java:32: Error: Must be one of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setStyle(-1, 0); // ERROR\n"
                + "                 ~~\n"
                + "src/test/pkg/IntDefTest.java:33: Error: Must be one of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setStyle(UNRELATED, 0); // ERROR\n"
                + "                 ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:34: Error: Must be one of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setStyle(IntDefTest.UNRELATED, 0); // ERROR\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:35: Error: Flag not allowed here [WrongConstant]\n"
                + "        setStyle(IntDefTest.STYLE_NORMAL|STYLE_NO_FRAME, 0); // ERROR: Not a flag\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:36: Error: Flag not allowed here [WrongConstant]\n"
                + "        setStyle(~STYLE_NO_FRAME, 0); // ERROR: Not a flag\n"
                + "                 ~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:55: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", UNRELATED); // ERROR\n"
                + "                     ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:56: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", UNRELATED|STYLE_NO_TITLE); // ERROR\n"
                + "                     ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:57: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", STYLE_NORMAL|STYLE_NO_TITLE|UNRELATED); // ERROR\n"
                + "                                                 ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:58: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", 1); // ERROR\n"
                + "                     ~\n"
                + "src/test/pkg/IntDefTest.java:59: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", arg < 0 ? STYLE_NORMAL : UNRELATED); // ERROR\n"
                + "                                              ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:60: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", arg < 0 ? UNRELATED : STYLE_NORMAL); // ERROR\n"
                + "                               ~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:79: Error: Must be one of: IntDefTest.TYPE_1, IntDefTest.TYPE_2 [WrongConstant]\n"
                + "        setTitle(\"\", UNRELATED_TYPE); // ERROR\n"
                + "                     ~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:80: Error: Must be one of: IntDefTest.TYPE_1, IntDefTest.TYPE_2 [WrongConstant]\n"
                + "        setTitle(\"\", \"type2\"); // ERROR\n"
                + "                     ~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:87: Error: Must be one of: IntDefTest.TYPE_1, IntDefTest.TYPE_2 [WrongConstant]\n"
                + "        setTitle(\"\", type); // ERROR\n"
                + "                     ~~~~\n"
                + "src/test/pkg/IntDefTest.java:92: Error: Must be one or more of: IntDefTest.STYLE_NORMAL, IntDefTest.STYLE_NO_TITLE, IntDefTest.STYLE_NO_FRAME, IntDefTest.STYLE_NO_INPUT [WrongConstant]\n"
                + "        setFlags(\"\", flag); // ERROR\n"
                + "                     ~~~~\n"
                + (SDK_ANNOTATIONS_AVAILABLE ?
                "src/test/pkg/IntDefTest.java:99: Error: Must be one of: View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL, View.LAYOUT_DIRECTION_INHERIT, View.LAYOUT_DIRECTION_LOCALE [WrongConstant]\n"
                + "        view.setLayoutDirection(View.TEXT_DIRECTION_LTR); // ERROR\n"
                + "                                ~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:100: Error: Must be one of: View.LAYOUT_DIRECTION_LTR, View.LAYOUT_DIRECTION_RTL, View.LAYOUT_DIRECTION_INHERIT, View.LAYOUT_DIRECTION_LOCALE [WrongConstant]\n"
                + "        view.setLayoutDirection(0); // ERROR\n"
                + "                                ~\n"
                + "src/test/pkg/IntDefTest.java:101: Error: Flag not allowed here [WrongConstant]\n"
                + "        view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR|View.LAYOUT_DIRECTION_RTL); // ERROR\n"
                + "                                ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/IntDefTest.java:102: Error: Must be one of: Context.POWER_SERVICE, Context.WINDOW_SERVICE, Context.LAYOUT_INFLATER_SERVICE, Context.ACCOUNT_SERVICE, Context.ACTIVITY_SERVICE, Context.ALARM_SERVICE, Context.NOTIFICATION_SERVICE, Context.ACCESSIBILITY_SERVICE, Context.CAPTIONING_SERVICE, Context.KEYGUARD_SERVICE, Context.LOCATION_SERVICE, Context.SEARCH_SERVICE, Context.SENSOR_SERVICE, Context.STORAGE_SERVICE, Context.WALLPAPER_SERVICE, Context.VIBRATOR_SERVICE, Context.CONNECTIVITY_SERVICE, Context.NETWORK_STATS_SERVICE, Context.WIFI_SERVICE, Context.WIFI_P2P_SERVICE, Context.NSD_SERVICE, Context.AUDIO_SERVICE, Context.FINGERPRINT_SERVICE, Context.MEDIA_ROUTER_SERVICE, Context.TELEPHONY_SERVICE, Context.TELEPHONY_SUBSCRIPTION_SERVICE, Context.CARRIER_CONFIG_SERVICE, Context.TELECOM_SERVICE, Context.CLIPBOARD_SERVICE, Context.INPUT_METHOD_SERVICE, Context.TEXT_SERVICES_MANAGER_SERVICE, Context.APPWIDGET_SERVICE, Context.DROPBOX_SERVICE, Context.DEVICE_POLICY_SERVICE, Context.UI_MODE_SERVICE, Context.DOWNLOAD_SERVICE, Context.NFC_SERVICE, Context.BLUETOOTH_SERVICE, Context.USB_SERVICE, Context.LAUNCHER_APPS_SERVICE, Context.INPUT_SERVICE, Context.DISPLAY_SERVICE, Context.USER_SERVICE, Context.RESTRICTIONS_SERVICE, Context.APP_OPS_SERVICE, Context.CAMERA_SERVICE, Context.PRINT_SERVICE, Context.CONSUMER_IR_SERVICE, Context.TV_INPUT_SERVICE, Context.USAGE_STATS_SERVICE, Context.MEDIA_SESSION_SERVICE, Context.BATTERY_SERVICE, Context.JOB_SCHEDULER_SERVICE, Context.MEDIA_PROJECTION_SERVICE, Context.MIDI_SERVICE [WrongConstant]\n"
                + "        context.getSystemService(TYPE_1); // ERROR\n"
                + "                                 ~~~~~~\n"
                + "20 errors, 0 warnings\n" :
                "16 errors, 0 warnings\n"),

                lintProject("src/test/pkg/IntDefTest.java.txt=>src/test/pkg/IntDefTest.java",
                        "src/android/support/annotation/IntDef.java.txt=>src/android/support/annotation/IntDef.java",
                        "src/android/support/annotation/StringDef.java.txt=>src/android/support/annotation/StringDef.java"
                ));
    }

    public void testColorInt() throws Exception {
        // Needs updated annotations!
        assertEquals((SDK_ANNOTATIONS_AVAILABLE ? ""
                + "src/test/pkg/WrongColor.java:9: Error: Should pass resolved color instead of resource id here: getResources().getColor(R.color.blue) [ResourceAsColor]\n"
                + "        paint2.setColor(R.color.blue);\n"
                + "                        ~~~~~~~~~~~~\n"
                + "src/test/pkg/WrongColor.java:11: Error: Should pass resolved color instead of resource id here: getResources().getColor(R.color.red) [ResourceAsColor]\n"
                + "        textView.setTextColor(R.color.red);\n"
                + "                              ~~~~~~~~~~~\n"
                + "src/test/pkg/WrongColor.java:12: Error: Should pass resolved color instead of resource id here: getResources().getColor(android.R.color.black) [ResourceAsColor]\n"
                + "        textView.setTextColor(android.R.color.black);\n"
                + "                              ~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/WrongColor.java:13: Error: Should pass resolved color instead of resource id here: getResources().getColor(R.color.blue) [ResourceAsColor]\n"
                + "        textView.setTextColor(foo > 0 ? R.color.green : R.color.blue);\n"
                + "                                                        ~~~~~~~~~~~~\n"
                + "src/test/pkg/WrongColor.java:13: Error: Should pass resolved color instead of resource id here: getResources().getColor(R.color.green) [ResourceAsColor]\n"
                + "        textView.setTextColor(foo > 0 ? R.color.green : R.color.blue);\n"
                + "                                        ~~~~~~~~~~~~~\n" : "")
                + "src/test/pkg/WrongColor.java:21: Error: Should pass resolved color instead of resource id here: getResources().getColor(R.color.blue) [ResourceAsColor]\n"
                + "        foo2(R.color.blue);\n"
                + "             ~~~~~~~~~~~~\n"
                + "src/test/pkg/WrongColor.java:20: Error: Expected resource of type color [ResourceType]\n"
                + "        foo1(0xffff0000);\n"
                + "             ~~~~~~~~~~\n"
                + (SDK_ANNOTATIONS_AVAILABLE ? "7 errors, 0 warnings\n" : "2 errors, 0 warnings\n"),

                lintProject(
                        copy("src/test/pkg/WrongColor.java.txt", "src/test/pkg/WrongColor.java"),
                        copy("src/android/support/annotation/ColorInt.java.txt", "src/android/support/annotation/ColorInt.java"),
                        mColorResAnnotation
                ));
    }

    public void testColorInt2() throws Exception {
        assertEquals(""
                + "src/test/pkg/ColorTest.java:23: Error: Should pass resolved color instead of resource id here: getResources().getColor(actualColor) [ResourceAsColor]\n"
                + "        setColor2(actualColor); // ERROR\n"
                + "                  ~~~~~~~~~~~\n"
                + "src/test/pkg/ColorTest.java:24: Error: Should pass resolved color instead of resource id here: getResources().getColor(getColor2()) [ResourceAsColor]\n"
                + "        setColor2(getColor2()); // ERROR\n"
                + "                  ~~~~~~~~~~~\n"
                + "src/test/pkg/ColorTest.java:17: Error: Expected a color resource id (R.color.) but received an RGB integer [ResourceType]\n"
                + "        setColor1(actualColor); // ERROR\n"
                + "                  ~~~~~~~~~~~\n"
                + "src/test/pkg/ColorTest.java:18: Error: Expected a color resource id (R.color.) but received an RGB integer [ResourceType]\n"
                + "        setColor1(getColor1()); // ERROR\n"
                + "                  ~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",

                lintProject(
                        java("src/test/pkg/ColorTest.java", ""
                                + "package test.pkg;\n"
                                + "import android.content.Context;\n"
                                + "import android.content.res.Resources;\n"
                                + "import android.support.annotation.ColorInt;\n"
                                + "import android.support.annotation.ColorRes;\n"
                                + "\n"
                                + "public abstract class ColorTest {\n"
                                + "    @ColorInt\n"
                                + "    public abstract int getColor1();\n"
                                + "    public abstract void setColor1(@ColorRes int color);\n"
                                + "    @ColorRes\n"
                                + "    public abstract int getColor2();\n"
                                + "    public abstract void setColor2(@ColorInt int color);\n"
                                + "\n"
                                + "    public void test1(Context context) {\n"
                                + "        int actualColor = getColor1();\n"
                                + "        setColor1(actualColor); // ERROR\n"
                                + "        setColor1(getColor1()); // ERROR\n"
                                + "        setColor1(getColor2()); // OK\n"
                                + "    }\n"
                                + "    public void test2(Context context) {\n"
                                + "        int actualColor = getColor2();\n"
                                + "        setColor2(actualColor); // ERROR\n"
                                + "        setColor2(getColor2()); // ERROR\n"
                                + "        setColor2(getColor1()); // OK\n"
                                + "    }\n"
                                + "}\n"),
                        mColorResAnnotation,
                        mColorIntAnnotation
                ));
    }

    public void testColorInt3() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=176321

        if (!SDK_ANNOTATIONS_AVAILABLE) {
            return;
        }
        assertEquals(""
                        + "src/test/pkg/ColorTest.java:11: Error: Expected a color resource id (R.color.) but received an RGB integer [ResourceType]\n"
                        + "        setColor(actualColor);\n"
                        + "                 ~~~~~~~~~~~\n"
                        + "1 errors, 0 warnings\n",

                lintProject(
                        java("src/test/pkg/ColorTest.java", ""
                                + "package test.pkg;\n"
                                + "import android.content.Context;\n"
                                + "import android.content.res.Resources;\n"
                                + "import android.support.annotation.ColorRes;\n"
                                + "\n"
                                + "public abstract class ColorTest {\n"
                                + "    public abstract void setColor(@ColorRes int color);\n"
                                + "\n"
                                + "    public void test(Context context, @ColorRes int id) {\n"
                                + "        int actualColor = context.getResources().getColor(id, null);\n"
                                + "        setColor(actualColor);\n"
                                + "    }\n"
                                + "}\n"),
                        mColorResAnnotation
                ));
    }
    public void testResourceType() throws Exception {
        assertEquals((SDK_ANNOTATIONS_AVAILABLE ? ""
                + "src/p1/p2/Flow.java:13: Error: Expected resource of type drawable [ResourceType]\n"
                + "        resources.getDrawable(10); // ERROR\n"
                + "                              ~~\n"
                + "src/p1/p2/Flow.java:18: Error: Expected resource of type drawable [ResourceType]\n"
                + "        resources.getDrawable(R.string.my_string); // ERROR\n"
                + "                              ~~~~~~~~~~~~~~~~~~\n" : "")
                + "src/p1/p2/Flow.java:22: Error: Expected resource of type drawable [ResourceType]\n"
                + "        myMethod(R.string.my_string, null); // ERROR\n"
                + "                 ~~~~~~~~~~~~~~~~~~\n"
                + "src/p1/p2/Flow.java:26: Error: Expected resource of type drawable [ResourceType]\n"
                + "        resources.getDrawable(R.string.my_string); // ERROR\n"
                + "                              ~~~~~~~~~~~~~~~~~~\n"
                + "src/p1/p2/Flow.java:32: Error: Expected resource identifier (R.type.name) [ResourceType]\n"
                + "        myAnyResMethod(50); // ERROR\n"
                + "                       ~~\n"
                + (SDK_ANNOTATIONS_AVAILABLE ? "src/p1/p2/Flow.java:60: Error: Expected resource of type drawable [ResourceType]\n"
                + "        resources.getDrawable(MimeTypes.getAnnotatedString()); // Error\n"
                + "                              ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" : "")
                + "src/p1/p2/Flow.java:68: Error: Expected resource of type drawable [ResourceType]\n"
                + "        myMethod(z, null); // ERROR\n"
                + "                 ~\n"
                + (SDK_ANNOTATIONS_AVAILABLE ? "7 errors, 0 warnings\n" : "4 errors, 0 warnings\n"),

                lintProject(
                        copy("src/p1/p2/Flow.java.txt", "src/p1/p2/Flow.java"),
                        copy("src/android/support/annotation/DrawableRes.java.txt", "src/android/support/annotation/DrawableRes.java"),
                        mStringResAnnotation,
                        mStyleResAnnotation,
                        mAnyResAnnotation
                ));
    }

    public void testTypes2() throws Exception {
        assertEquals(""
                + "src/test/pkg/ActivityType.java:5: Error: Expected resource of type drawable [ResourceType]\n"
                + "    SKI(1),\n"
                + "        ~\n"
                + "src/test/pkg/ActivityType.java:6: Error: Expected resource of type drawable [ResourceType]\n"
                + "    SNOWBOARD(2);\n"
                + "              ~\n"
                + "2 errors, 0 warnings\n",

                lintProject(
                        java("src/test/pkg/ActivityType.java", ""
                                + "import android.support.annotation.DrawableRes;\n"
                                + "\n"
                                + "enum ActivityType {\n"
                                + "\n"
                                + "    SKI(1),\n"
                                + "    SNOWBOARD(2);\n"
                                + "\n"
                                + "    private final int mIconResId;\n"
                                + "\n"
                                + "    ActivityType(@DrawableRes int iconResId) {\n"
                                + "        mIconResId = iconResId;\n"
                                + "    }\n"
                                + "}"),
                        copy("src/android/support/annotation/DrawableRes.java.txt",
                                "src/android/support/annotation/DrawableRes.java")));
    }

    @SuppressWarnings({"MethodMayBeStatic", "ResultOfObjectAllocationIgnored"})
    public void testConstructor() throws Exception {
        assertEquals(""
                + "src/test/pkg/ConstructorTest.java:14: Error: Expected resource of type drawable [ResourceType]\n"
                + "        new ConstructorTest(1, 3);\n"
                + "                            ~\n"
                + "src/test/pkg/ConstructorTest.java:14: Error: Value must be ≥ 5 (was 3) [Range]\n"
                + "        new ConstructorTest(1, 3);\n"
                + "                               ~\n"
                + "src/test/pkg/ConstructorTest.java:19: Error: Method test.pkg.ConstructorTest must be called from the UI thread, currently inferred thread is worker thread [WrongThread]\n"
                + "        new ConstructorTest(res, range);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "3 errors, 0 warnings\n",

                lintProject(
                        java("src/test/pkg/ConstructorTest.java", ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import android.support.annotation.DrawableRes;\n"
                                + "import android.support.annotation.IntRange;\n"
                                + "import android.support.annotation.UiThread;\n"
                                + "import android.support.annotation.WorkerThread;\n"
                                + "\n"
                                + "public class ConstructorTest {\n"
                                + "    @UiThread\n"
                                + "    ConstructorTest(@DrawableRes int iconResId, @IntRange(from = 5) int start) {\n"
                                + "    }\n"
                                + "\n"
                                + "    public void testParameters() {\n"
                                + "        new ConstructorTest(1, 3);\n"
                                + "    }\n"
                                + "\n"
                                + "    @WorkerThread\n"
                                + "    public void testMethod(int res, int range) {\n"
                                + "        new ConstructorTest(res, range);\n"
                                + "    }\n"
                                + "}\n"),
                        mWorkerThreadPermission,
                        mUiThreadPermission,
                        copy("src/android/support/annotation/DrawableRes.java.txt",
                                "src/android/support/annotation/DrawableRes.java"),
                        copy("src/android/support/annotation/IntRange.java.txt",
                                "src/android/support/annotation/IntRange.java")
                ));
    }

    public void testColorAsDrawable() throws Exception {
        assertEquals(
                "No warnings.",

                lintProject("src/p1/p2/ColorAsDrawable.java.txt=>src/p1/p2/ColorAsDrawable.java"));
    }

    public void testCheckResult() throws Exception {
        if (!SDK_ANNOTATIONS_AVAILABLE) {
            // Currently only tests @CheckResult on SDK annotations
            return;
        }
        assertEquals(""
                + "src/test/pkg/CheckPermissions.java:22: Warning: The result of extractAlpha is not used [CheckResult]\n"
                + "        bitmap.extractAlpha(); // WARNING\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/CheckPermissions.java:10: Warning: The result of checkCallingOrSelfPermission is not used; did you mean to call #enforceCallingOrSelfPermission(String,String)? [UseCheckPermission]\n"
                + "        context.checkCallingOrSelfPermission(Manifest.permission.INTERNET); // WRONG\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/CheckPermissions.java:11: Warning: The result of checkPermission is not used; did you mean to call #enforcePermission(String,int,int,String)? [UseCheckPermission]\n"
                + "        context.checkPermission(Manifest.permission.INTERNET, 1, 1);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 3 warnings\n",

                lintProject("src/test/pkg/CheckPermissions.java.txt=>src/test/pkg/CheckPermissions.java"));
    }

    private final TestFile mPermissionTest = java("src/test/pkg/PermissionTest.java", ""
                + "package test.pkg;\n"
                + "\n"
                + "import android.location.LocationManager;\n"
                + "\n"
                + "public class PermissionTest {\n"
                + "    public static void test(LocationManager locationManager, String provider) {\n"
                + "        LocationManager.Location location = locationManager.myMethod(provider);\n"
                + "    }\n"
                + "}\n");

    private final TestFile mLocationManagerStub = java("src/android/location/LocationManager.java", ""
                + "package android.location;\n"
                + "\n"
                + "import android.support.annotation.RequiresPermission;\n"
                + "\n"
                + "import static android.Manifest.permission.ACCESS_COARSE_LOCATION;\n"
                + "import static android.Manifest.permission.ACCESS_FINE_LOCATION;\n"
                + "\n"
                + "@SuppressWarnings(\"UnusedDeclaration\")\n"
                + "public abstract class LocationManager {\n"
                + "    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})\n"
                + "    public abstract Location myMethod(String provider);\n"
                + "    public static class Location {\n"
                + "    }\n"
                + "}\n");

        private final TestFile mComplexLocationManagerStub = java("src/android/location/LocationManager.java", ""
                + "package android.location;\n"
                + "\n"
                + "import android.support.annotation.RequiresPermission;\n"
                + "\n"
                + "import static android.Manifest.permission.ACCESS_COARSE_LOCATION;\n"
                + "import static android.Manifest.permission.ACCESS_FINE_LOCATION;\n"
                + "import static android.Manifest.permission.BLUETOOTH;\n"
                + "import static android.Manifest.permission.READ_SMS;\n"
                + "\n"
                + "@SuppressWarnings(\"UnusedDeclaration\")\n"
                + "public abstract class LocationManager {\n"
                + "    @RequiresPermission(\"(\" + ACCESS_FINE_LOCATION + \"|| \" + ACCESS_COARSE_LOCATION + \") && (\" + BLUETOOTH + \" ^ \" + READ_SMS + \")\")\n"
                + "    public abstract Location myMethod(String provider);\n"
                + "    public static class Location {\n"
                + "    }\n"
                + "}\n");

        private final TestFile mRequirePermissionAnnotation = java("src/android/support/annotation/RequiresPermission.java", ""
                + "/*\n"
                + " * Copyright (C) 2015 The Android Open Source Project\n"
                + " *\n"
                + " * Licensed under the Apache License, Version 2.0 (the \"License\");\n"
                + " * you may not use this file except in compliance with the License.\n"
                + " * You may obtain a copy of the License at\n"
                + " *\n"
                + " *      http://www.apache.org/licenses/LICENSE-2.0\n"
                + " *\n"
                + " * Unless required by applicable law or agreed to in writing, software\n"
                + " * distributed under the License is distributed on an \"AS IS\" BASIS,\n"
                + " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
                + " * See the License for the specific language governing permissions and\n"
                + " * limitations under the License.\n"
                + " */\n"
                + "package android.support.annotation;\n"
                + "\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.Target;\n"
                + "\n"
                + "import static java.lang.annotation.ElementType.*;\n"
                + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
                + "@Retention(CLASS)\n"
                + "@Target({METHOD,CONSTRUCTOR,FIELD,PARAMETER,ANNOTATION_TYPE})\n"
                + "public @interface RequiresPermission {\n"
                + "    String value() default \"\";\n"
                + "    String[] allOf() default {};\n"
                + "    String[] anyOf() default {};\n"
                + "    boolean conditional() default false;\n"
                + "    String notes() default \"\";\n"
                + "    @Target({FIELD,METHOD,PARAMETER})\n"
                + "    @interface Read {\n"
                + "        RequiresPermission value();\n"
                + "    }\n"
                + "    @Target({FIELD,METHOD,PARAMETER})\n"
                + "    @interface Write {\n"
                + "        RequiresPermission value();\n"
                + "    }\n"
                + "}");

    private final TestFile mUiThreadPermission = java("src/android/support/annotation/UiThread.java", ""
            + "package android.support.annotation;\n"
            + "\n"
            + "import java.lang.annotation.Retention;\n"
            + "import java.lang.annotation.Target;\n"
            + "\n"
            + "import static java.lang.annotation.ElementType.CONSTRUCTOR;\n"
            + "import static java.lang.annotation.ElementType.METHOD;\n"
            + "import static java.lang.annotation.ElementType.TYPE;\n"
            + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
            + "\n"
            + "@Retention(CLASS)\n"
            + "@Target({METHOD,CONSTRUCTOR,TYPE})\n"
            + "public @interface UiThread {\n"
            + "}\n");

    private final TestFile mMainThreadPermission = java("src/android/support/annotation/MainThread.java", ""
            + "package android.support.annotation;\n"
            + "\n"
            + "import java.lang.annotation.Retention;\n"
            + "import java.lang.annotation.Target;\n"
            + "\n"
            + "import static java.lang.annotation.ElementType.CONSTRUCTOR;\n"
            + "import static java.lang.annotation.ElementType.METHOD;\n"
            + "import static java.lang.annotation.ElementType.TYPE;\n"
            + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
            + "\n"
            + "@Retention(CLASS)\n"
            + "@Target({METHOD,CONSTRUCTOR,TYPE})\n"
            + "public @interface MainThread {\n"
            + "}\n");

    private final TestFile mWorkerThreadPermission = java("src/android/support/annotation/WorkerThread.java", ""
            + "package android.support.annotation;\n"
            + "\n"
            + "import java.lang.annotation.Retention;\n"
            + "import java.lang.annotation.Target;\n"
            + "\n"
            + "import static java.lang.annotation.ElementType.CONSTRUCTOR;\n"
            + "import static java.lang.annotation.ElementType.METHOD;\n"
            + "import static java.lang.annotation.ElementType.TYPE;\n"
            + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
            + "\n"
            + "@Retention(CLASS)\n"
            + "@Target({METHOD,CONSTRUCTOR,TYPE})\n"
            + "public @interface WorkerThread {\n"
            + "}\n");

    private TestFile createResAnnotation(String prefix) {
        return java("src/android/support/annotation/" + prefix + "Res.java", ""
                + "package android.support.annotation;\n"
                + "\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.Target;\n"
                + "\n"
                + "import static java.lang.annotation.ElementType.*;\n"
                + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
                + "\n"
                + "@Retention(CLASS)\n"
                + "@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})\n"
                + "public @interface " + prefix + "Res {\n"
                + "}\n");
    }

    private final TestFile mColorResAnnotation = createResAnnotation("Color");
    private final TestFile mStringResAnnotation = createResAnnotation("String");
    private final TestFile mStyleResAnnotation = createResAnnotation("Style");
    private final TestFile mAnyResAnnotation = createResAnnotation("Any");

    private final TestFile mColorIntAnnotation = java("src/android/support/annotation/ColorInt.java", ""
            + "package android.support.annotation;\n"
            + "\n"
            + "import java.lang.annotation.Retention;\n"
            + "import java.lang.annotation.Target;\n"
            + "\n"
            + "import static java.lang.annotation.ElementType.*;\n"
            + "import static java.lang.annotation.RetentionPolicy.CLASS;\n"
            + "\n"
            + "@Retention(CLASS)\n"
            + "@Target({METHOD, PARAMETER, FIELD, LOCAL_VARIABLE})\n"
            + "public @interface ColorInt {\n"
            + "}\n");

    private TestFile getManifestWithPermissions(int targetSdk, String... permissions) {
        return getManifestWithPermissions(1, targetSdk, permissions);
    }

    private TestFile getManifestWithPermissions(int minSdk, int targetSdk, String... permissions) {
        StringBuilder permissionBlock = new StringBuilder();
        for (String permission : permissions) {
            permissionBlock.append("    <uses-permission android:name=\"").append(permission)
                    .append("\" />\n");
        }
        return xml("AndroidManifest.xml", ""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    package=\"foo.bar2\"\n"
                + "    android:versionCode=\"1\"\n"
                + "    android:versionName=\"1.0\" >\n"
                + "\n"
                + "    <uses-sdk android:minSdkVersion=\"14\" android:targetSdkVersion=\""
                + targetSdk + "\" />\n"
                + "\n"
                + permissionBlock.toString()
                + "\n"
                + "    <application\n"
                + "        android:icon=\"@drawable/ic_launcher\"\n"
                + "        android:label=\"@string/app_name\" >\n"
                + "    </application>\n"
                + "\n"
                + "</manifest>");
    }

    private TestFile mRevokeTest = java("src/test/pkg/RevokeTest.java", ""
            + "package test.pkg;\n"
            + "\n"
            + "import android.content.Context;\n"
            + "import android.content.pm.PackageManager;\n"
            + "import android.location.LocationManager;\n"
            + "\n"
            + "import java.security.AccessControlException;\n"
            + "\n"
            + "public class RevokeTest {\n"
            + "    public static void test1(LocationManager locationManager, String provider) {\n"
            + "        try {\n"
            + "            // Ok: Security exception caught in one of the branches\n"
            + "            locationManager.myMethod(provider); // OK\n"
            + "        } catch (IllegalArgumentException ignored) {\n"
            + "        } catch (SecurityException ignored) {\n"
            + "        }\n"
            + "\n"
            + "        try {\n"
            + "            // Ok: Security exception super class caught in one of the branches\n"
            + "            locationManager.myMethod(provider); // OK\n"
            + "        } catch (RuntimeException e) { // includes Security Exception\n"
            + "        }\n"
            + "\n"
            + "        try {\n"
            + "            // Ok: Caught in outer statement\n"
            + "            try {\n"
            + "                locationManager.myMethod(provider); // OK\n"
            + "            } catch (IllegalArgumentException e) {\n"
            + "                // inner\n"
            + "            }\n"
            + "        } catch (SecurityException ignored) {\n"
            + "        }\n"
            + "\n"
            + "        try {\n"
            + "            // Ok: Security exception super class caught in one of the branches\n"
            + "            locationManager.myMethod(provider); // OK\n"
            + "        } catch (Exception e) { // includes Security Exception\n"
            + "        }\n"
            + "\n"
            + "        // NOT OK: Catching security exception subclass (except for dedicated ones?)\n"
            + "\n"
            + "        try {\n"
            + "            // Error: catching security exception, but not all of them\n"
            + "            locationManager.myMethod(provider); // ERROR\n"
            + "        } catch (AccessControlException e) { // security exception but specific one\n"
            + "        }\n"
            + "    }\n"
            + "\n"
            + "    public static void test2(LocationManager locationManager, String provider) {\n"
            + "        locationManager.myMethod(provider); // ERROR: not caught\n"
            + "    }\n"
            + "\n"
            + "    public static void test3(LocationManager locationManager, String provider)\n"
            + "            throws IllegalArgumentException {\n"
            + "        locationManager.myMethod(provider); // ERROR: not caught by right type\n"
            + "    }\n"
            + "\n"
            + "    public static void test4(LocationManager locationManager, String provider)\n"
            + "            throws AccessControlException {  // Security exception but specific one\n"
            + "        locationManager.myMethod(provider); // ERROR\n"
            + "    }\n"
            + "\n"
            + "    public static void test5(LocationManager locationManager, String provider)\n"
            + "            throws SecurityException {\n"
            + "        locationManager.myMethod(provider); // OK\n"
            + "    }\n"
            + "\n"
            + "    public static void test6(LocationManager locationManager, String provider)\n"
            + "            throws Exception { // includes Security Exception\n"
            + "        locationManager.myMethod(provider); // OK\n"
            + "    }\n"
            + "\n"
            + "    public static void test7(LocationManager locationManager, String provider, Context context)\n"
            + "            throws IllegalArgumentException {\n"
            + "        if (context.getPackageManager().checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {\n"
            + "            return;\n"
            + "        }\n"
            + "        locationManager.myMethod(provider); // OK: permission checked\n"
            + "    }\n"
            + "\n"
            + "}\n");

    public void testMissingPermissions() throws Exception {
        assertEquals(""
                + "src/test/pkg/PermissionTest.java:7: Error: Missing permissions required by LocationManager.myMethod: android.permission.ACCESS_FINE_LOCATION or android.permission.ACCESS_COARSE_LOCATION [MissingPermission]\n"
                + "        LocationManager.Location location = locationManager.myMethod(provider);\n"
                + "                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(14),
                        mPermissionTest,
                        mLocationManagerStub,
                        mRequirePermissionAnnotation));
    }

    public void testHasPermission() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        getManifestWithPermissions(14, "android.permission.ACCESS_FINE_LOCATION"),
                        mPermissionTest,
                        mLocationManagerStub,
                        mRequirePermissionAnnotation));
    }

    public void testRevokePermissions() throws Exception {
        assertEquals(""
                + "src/test/pkg/RevokeTest.java:44: Error: Call requires permission which may be rejected by user: code should explicitly check to see if permission is available (with checkPermission) or handle a potential SecurityException [MissingPermission]\n"
                + "            locationManager.myMethod(provider); // ERROR\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RevokeTest.java:50: Error: Call requires permission which may be rejected by user: code should explicitly check to see if permission is available (with checkPermission) or handle a potential SecurityException [MissingPermission]\n"
                + "        locationManager.myMethod(provider); // ERROR: not caught\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RevokeTest.java:55: Error: Call requires permission which may be rejected by user: code should explicitly check to see if permission is available (with checkPermission) or handle a potential SecurityException [MissingPermission]\n"
                + "        locationManager.myMethod(provider); // ERROR: not caught by right type\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/RevokeTest.java:60: Error: Call requires permission which may be rejected by user: code should explicitly check to see if permission is available (with checkPermission) or handle a potential SecurityException [MissingPermission]\n"
                + "        locationManager.myMethod(provider); // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "4 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(23, "android.permission.ACCESS_FINE_LOCATION"),
                        mLocationManagerStub,
                        mRequirePermissionAnnotation,
                        mRevokeTest
                ));
    }

    public void testImpliedPermissions() throws Exception {
        // Regression test for
        //   https://code.google.com/p/android/issues/detail?id=177381
        assertEquals(""
                + "src/test/pkg/PermissionTest2.java:11: Error: Missing permissions required by PermissionTest2.method1: my.permission.PERM2 [MissingPermission]\n"
                + "        method1(); // ERROR\n"
                + "        ~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(14, 14, "android.permission.ACCESS_FINE_LOCATION"),
                        java("src/test/pkg/PermissionTest2.java", ""
                                + "package test.pkg;\n"
                                + "import android.support.annotation.RequiresPermission;\n"
                                + "\n"
                                + "public class PermissionTest2 {\n"
                                + "    @RequiresPermission(allOf = {\"my.permission.PERM1\",\"my.permission.PERM2\"})\n"
                                + "    public void method1() {\n"
                                + "    }\n"
                                + "\n"
                                + "    @RequiresPermission(\"my.permission.PERM1\")\n"
                                + "    public void method2() {\n"
                                + "        method1(); // ERROR\n"
                                + "    }\n"
                                + "\n"
                                + "    @RequiresPermission(allOf = {\"my.permission.PERM1\",\"my.permission.PERM2\"})\n"
                                + "    public void method3() {\n"
                                + "        // The above @RequiresPermission implies that we are holding these\n"
                                + "        // permissions here, so the call to method1() should not be flagged as\n"
                                + "        // missing a permission!\n"
                                + "        method1(); // OK\n"
                                + "    }\n"
                                + "}\n"),
                        mRequirePermissionAnnotation
                ));
    }

    public void testRevokePermissionsPre23() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        getManifestWithPermissions(14, "android.permission.ACCESS_FINE_LOCATION"),
                        mLocationManagerStub,
                        mRequirePermissionAnnotation,
                        mRevokeTest
                ));
    }

    public void testComplexPermission1() throws Exception {
        assertEquals(""
                + "src/test/pkg/PermissionTest.java:7: Error: Missing permissions required by LocationManager.myMethod: android.permission.BLUETOOTH xor android.permission.READ_SMS [MissingPermission]\n"
                + "        LocationManager.Location location = locationManager.myMethod(provider);\n"
                + "                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(14,
                                "android.permission.ACCESS_FINE_LOCATION"),
                        mPermissionTest,
                        mComplexLocationManagerStub,
                        mRequirePermissionAnnotation));
    }

    public void testComplexPermission2() throws Exception {
        assertEquals("No warnings.",
                lintProject(
                        getManifestWithPermissions(14,
                                "android.permission.ACCESS_FINE_LOCATION",
                                "android.permission.BLUETOOTH"),
                        mPermissionTest,
                        mComplexLocationManagerStub,
                        mRequirePermissionAnnotation));
    }

    public void testComplexPermission3() throws Exception {
        assertEquals(""
                + "src/test/pkg/PermissionTest.java:7: Error: Missing permissions required by LocationManager.myMethod: android.permission.BLUETOOTH xor android.permission.READ_SMS [MissingPermission]\n"
                + "        LocationManager.Location location = locationManager.myMethod(provider);\n"
                + "                                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(14,
                                "android.permission.ACCESS_FINE_LOCATION",
                                "android.permission.BLUETOOTH",
                                "android.permission.READ_SMS"),
                        mPermissionTest,
                        mComplexLocationManagerStub,
                        mRequirePermissionAnnotation));
    }

    public void testPermissionAnnotation() throws Exception {
        assertEquals(""
                + "src/test/pkg/LocationManager.java:24: Error: Missing permissions required by LocationManager.getLastKnownLocation: android.permission.ACCESS_FINE_LOCATION or android.permission.ACCESS_COARSE_LOCATION [MissingPermission]\n"
                + "        Location location = manager.getLastKnownLocation(\"provider\");\n"
                + "                            ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",
                lintProject(
                        java("src/test/pkg/LocationManager.java", ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import android.support.annotation.RequiresPermission;\n"
                                + "\n"
                                + "import java.lang.annotation.Retention;\n"
                                + "import java.lang.annotation.RetentionPolicy;\n"
                                + "\n"
                                + "import static android.Manifest.permission.ACCESS_COARSE_LOCATION;\n"
                                + "import static android.Manifest.permission.ACCESS_FINE_LOCATION;\n"
                                + "\n"
                                + "@SuppressWarnings(\"UnusedDeclaration\")\n"
                                + "public abstract class LocationManager {\n"
                                + "    @RequiresPermission(anyOf = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION})\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    @interface AnyLocationPermission {\n"
                                + "    }\n"
                                + "\n"
                                + "    @AnyLocationPermission\n"
                                + "    public abstract Location getLastKnownLocation(String provider);\n"
                                + "    public static class Location {\n"
                                + "    }\n"
                                + "    \n"
                                + "    public static void test(LocationManager manager) {\n"
                                + "        Location location = manager.getLastKnownLocation(\"provider\");\n"
                                + "    }\n"
                                + "}\n"),
                        mRequirePermissionAnnotation));
    }

    public void testThreading() throws Exception {
        assertEquals(""
                + "src/test/pkg/ThreadTest.java:15: Error: Method onPreExecute must be called from the main thread, currently inferred thread is worker thread [WrongThread]\n"
                + "                onPreExecute(); // ERROR\n"
                + "                ~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ThreadTest.java:16: Error: Method paint must be called from the UI thread, currently inferred thread is worker thread [WrongThread]\n"
                + "                view.paint(); // ERROR\n"
                + "                ~~~~~~~~~~~~\n"
                + "src/test/pkg/ThreadTest.java:22: Error: Method publishProgress must be called from the worker thread, currently inferred thread is main thread [WrongThread]\n"
                + "                publishProgress(); // ERROR\n"
                + "                ~~~~~~~~~~~~~~~~~\n"
                + "3 errors, 0 warnings\n",

            lintProject(
                java("src/test/pkg/ThreadTest.java", ""
                        + "package test.pkg;\n"
                        + "\n"
                        + "import android.support.annotation.MainThread;\n"
                        + "import android.support.annotation.UiThread;\n"
                        + "import android.support.annotation.WorkerThread;\n"
                        + "\n"
                        + "public class ThreadTest {\n"
                        + "    public static AsyncTask testTask() {\n"
                        + "\n"
                        + "        return new AsyncTask() {\n"
                        + "            final CustomView view = new CustomView();\n"
                        + "\n"
                        + "            @Override\n"
                        + "            protected void doInBackground(Object... params) {\n"
                        + "                onPreExecute(); // ERROR\n"
                        + "                view.paint(); // ERROR\n"
                        + "                publishProgress(); // OK\n"
                        + "            }\n"
                        + "\n"
                        + "            @Override\n"
                        + "            protected void onPreExecute() {\n"
                        + "                publishProgress(); // ERROR\n"
                        + "                onProgressUpdate(); // OK\n"
                        + "            }\n"
                        + "        };\n"
                        + "    }\n"
                        + "\n"
                        + "    @UiThread\n"
                        + "    public static class View {\n"
                        + "        public void paint() {\n"
                        + "        }\n"
                        + "    }\n"
                        + "\n"
                        + "    public static class CustomView extends View {\n"
                        + "        @Override public void paint() {\n"
                        + "        }\n"
                        + "    }\n"
                        + "\n"
                        + "    public abstract static class AsyncTask {\n"
                        + "        @WorkerThread\n"
                        + "        protected abstract void doInBackground(Object... params);\n"
                        + "\n"
                        + "        @MainThread\n"
                        + "        protected void onPreExecute() {\n"
                        + "        }\n"
                        + "\n"
                        + "        @MainThread\n"
                        + "        protected void onProgressUpdate(Object... values) {\n"
                        + "        }\n"
                        + "\n"
                        + "        @WorkerThread\n"
                        + "        protected final void publishProgress(Object... values) {\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"),
                        mUiThreadPermission,
                        mMainThreadPermission,
                        mWorkerThreadPermission));
    }

    public void testIntentPermission() throws Exception {
        if (SDK_ANNOTATIONS_AVAILABLE) {
            TestLintClient client = createClient();
            ExternalAnnotationRepository repository = ExternalAnnotationRepository.get(client);
            ResolvedMethod method = ExternalAnnotationRepositoryTest.createMethod(
                    "android.content.Context", "void", "startActivity",
                    "android.content.Intent");
            ResolvedAnnotation a = repository.getAnnotation(method, 0, PERMISSION_ANNOTATION);
            if (a == null) {
                // Running tests from outside the IDE (where it can't find the
                // bundled up to date annotations in tools/adt/idea/android/annotations)
                // and we have the annotations.zip file available in platform-tools,
                // but its contents are old (it's from Android M Preview 1, not including
                // the new intent-annotation data); skip this test for now.
                return;
            }
        }

        assertEquals(!SDK_ANNOTATIONS_AVAILABLE ? "" // Most of the intent/content provider checks are based on framework annotations
                + "src/test/pkg/ActionTest.java:86: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        myStartActivity(\"\", null, new Intent(ACTION_CALL));\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:87: Error: Missing permissions required to read ActionTest.BOOKMARKS_URI: com.android.browser.permission.READ_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        myReadResolverMethod(\"\", BOOKMARKS_URI);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:88: Error: Missing permissions required to read ActionTest.BOOKMARKS_URI: com.android.browser.permission.READ_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        myWriteResolverMethod(BOOKMARKS_URI);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "3 errors, 0 warnings\n" : ""

                + "src/test/pkg/ActionTest.java:36: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivity(intent);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:42: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivity(intent);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:43: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivity(intent, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:44: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivityForResult(intent, 0);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:45: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivityFromChild(activity, intent, 0);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:46: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivityIfNeeded(intent, 0);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:47: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startActivityFromFragment(null, intent, 0);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:48: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        activity.startNextMatchingActivity(intent);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:54: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        context.sendBroadcast(intent);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:55: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        context.sendBroadcast(intent, \"\");\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:56: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        context.sendBroadcastAsUser(intent, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:57: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        context.sendStickyBroadcast(intent);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:62: Error: Missing permissions required to read ActionTest.BOOKMARKS_URI: com.android.browser.permission.READ_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        resolver.query(BOOKMARKS_URI, null, null, null, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:65: Error: Missing permissions required to write ActionTest.BOOKMARKS_URI: com.android.browser.permission.WRITE_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        resolver.insert(BOOKMARKS_URI, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:66: Error: Missing permissions required to write ActionTest.BOOKMARKS_URI: com.android.browser.permission.WRITE_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        resolver.delete(BOOKMARKS_URI, null, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:67: Error: Missing permissions required to write ActionTest.BOOKMARKS_URI: com.android.browser.permission.WRITE_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        resolver.update(BOOKMARKS_URI, null, null, null);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:86: Error: Missing permissions required by intent ActionTest.ACTION_CALL: android.permission.CALL_PHONE [MissingPermission]\n"
                + "        myStartActivity(\"\", null, new Intent(ACTION_CALL));\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:87: Error: Missing permissions required to read ActionTest.BOOKMARKS_URI: com.android.browser.permission.READ_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        myReadResolverMethod(\"\", BOOKMARKS_URI);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ActionTest.java:88: Error: Missing permissions required to read ActionTest.BOOKMARKS_URI: com.android.browser.permission.READ_HISTORY_BOOKMARKS [MissingPermission]\n"
                + "        myWriteResolverMethod(BOOKMARKS_URI);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "19 errors, 0 warnings\n",

                lintProject(
                        getManifestWithPermissions(14, 23),
                        java("src/test/pkg/ActionTest.java", ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import android.Manifest;\n"
                                + "import android.app.Activity;\n"
                                + "import android.content.ContentResolver;\n"
                                + "import android.content.Context;\n"
                                + "import android.content.Intent;\n"
                                + "import android.database.Cursor;\n"
                                + "import android.net.Uri;\n"
                                + "import android.support.annotation.RequiresPermission;\n"
                                + "\n"
                                //+ "import static android.Manifest.permission.READ_HISTORY_BOOKMARKS;\n"
                                //+ "import static android.Manifest.permission.WRITE_HISTORY_BOOKMARKS;\n"
                                + "\n"
                                + "@SuppressWarnings({\"deprecation\", \"unused\"})\n"
                                + "public class ActionTest {\n"
                                + "     public static final String READ_HISTORY_BOOKMARKS=\"com.android.browser.permission.READ_HISTORY_BOOKMARKS\";\n"
                                + "     public static final String WRITE_HISTORY_BOOKMARKS=\"com.android.browser.permission.WRITE_HISTORY_BOOKMARKS\";\n"
                                + "    @RequiresPermission(Manifest.permission.CALL_PHONE)\n"
                                + "    public static final String ACTION_CALL = \"android.intent.action.CALL\";\n"
                                + "\n"
                                + "    @RequiresPermission.Read(@RequiresPermission(READ_HISTORY_BOOKMARKS))\n"
                                + "    @RequiresPermission.Write(@RequiresPermission(WRITE_HISTORY_BOOKMARKS))\n"
                                + "    public static final Uri BOOKMARKS_URI = Uri.parse(\"content://browser/bookmarks\");\n"
                                + "\n"
                                + "    public static final Uri COMBINED_URI = Uri.withAppendedPath(BOOKMARKS_URI, \"bookmarks\");\n"
                                + "    \n"
                                + "    public static void activities1(Activity activity) {\n"
                                + "        Intent intent = new Intent(Intent.ACTION_CALL);\n"
                                + "        intent.setData(Uri.parse(\"tel:1234567890\"));\n"
                                + "        // This one will only be flagged if we have framework metadata on Intent.ACTION_CALL\n"
                                + "        activity.startActivity(intent);\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void activities2(Activity activity) {\n"
                                + "        Intent intent = new Intent(ACTION_CALL);\n"
                                + "        intent.setData(Uri.parse(\"tel:1234567890\"));\n"
                                + "        activity.startActivity(intent);\n"
                                + "    }\n"
                                + "    public static void activities3(Activity activity) {\n"
                                + "        Intent intent;\n"
                                + "        intent = new Intent(ACTION_CALL);\n"
                                + "        intent.setData(Uri.parse(\"tel:1234567890\"));\n"
                                + "        activity.startActivity(intent);\n"
                                + "        activity.startActivity(intent, null);\n"
                                + "        activity.startActivityForResult(intent, 0);\n"
                                + "        activity.startActivityFromChild(activity, intent, 0);\n"
                                + "        activity.startActivityIfNeeded(intent, 0);\n"
                                + "        activity.startActivityFromFragment(null, intent, 0);\n"
                                + "        activity.startNextMatchingActivity(intent);\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void broadcasts(Context context) {\n"
                                + "        Intent intent;\n"
                                + "        intent = new Intent(ACTION_CALL);\n"
                                + "        context.sendBroadcast(intent);\n"
                                + "        context.sendBroadcast(intent, \"\");\n"
                                + "        context.sendBroadcastAsUser(intent, null);\n"
                                + "        context.sendStickyBroadcast(intent);\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void contentResolvers(Context context, ContentResolver resolver) {\n"
                                + "        // read\n"
                                + "        resolver.query(BOOKMARKS_URI, null, null, null, null);\n"
                                + "\n"
                                + "        // write\n"
                                + "        resolver.insert(BOOKMARKS_URI, null);\n"
                                + "        resolver.delete(BOOKMARKS_URI, null, null);\n"
                                + "        resolver.update(BOOKMARKS_URI, null, null, null);\n"
                                + "\n"
                                + "        // Framework (external) annotation\n"
                                + "//REMOVED        resolver.query(android.provider.Browser.BOOKMARKS_URI, null, null, null, null);\n"
                                + "\n"
                                + "        // TODO: Look for more complex URI manipulations\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void myStartActivity(String s1, String s2, \n"
                                + "                                       @RequiresPermission Intent intent) {\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void myReadResolverMethod(String s1, @RequiresPermission.Read(@RequiresPermission) Uri uri) {\n"
                                + "    }\n"
                                + "\n"
                                + "    public static void myWriteResolverMethod(@RequiresPermission.Read(@RequiresPermission) Uri uri) {\n"
                                + "    }\n"
                                + "    \n"
                                + "    public static void testCustomMethods() {\n"
                                + "        myStartActivity(\"\", null, new Intent(ACTION_CALL));\n"
                                + "        myReadResolverMethod(\"\", BOOKMARKS_URI);\n"
                                + "        myWriteResolverMethod(BOOKMARKS_URI);\n"
                                + "    }\n"
                                + "}\n"),
                        mRequirePermissionAnnotation
                ));
    }

    public void testCombinedIntDefAndIntRange() throws Exception {
        assertEquals(""
                        + "src/test/pkg/X.java:27: Error: Must be one of: X.LENGTH_INDEFINITE, X.LENGTH_SHORT, X.LENGTH_LONG [WrongConstant]\n"
                        + "        setDuration(UNRELATED); /// ERROR: Not right intdef, even if it's in the right number range\n"
                        + "                    ~~~~~~~~~\n"
                        + "src/test/pkg/X.java:28: Error: Must be one of: X.LENGTH_INDEFINITE, X.LENGTH_SHORT, X.LENGTH_LONG or value must be ≥ 10 (was -5) [WrongConstant]\n"
                        + "        setDuration(-5); // ERROR (not right int def or value\n"
                        + "                    ~~\n"
                        + "src/test/pkg/X.java:29: Error: Must be one of: X.LENGTH_INDEFINITE, X.LENGTH_SHORT, X.LENGTH_LONG or value must be ≥ 10 (was 8) [WrongConstant]\n"
                        + "        setDuration(8); // ERROR (not matching number range)\n"
                        + "                    ~\n"
                        + "3 errors, 0 warnings\n",
                lintProject(
                        getManifestWithPermissions(14, 23),
                        java("src/test/pkg/X.java", ""
                                + "\n"
                                + "package test.pkg;\n"
                                + "\n"
                                + "import android.support.annotation.IntDef;\n"
                                + "import android.support.annotation.IntRange;\n"
                                + "\n"
                                + "import java.lang.annotation.Retention;\n"
                                + "import java.lang.annotation.RetentionPolicy;\n"
                                + "\n"
                                + "@SuppressWarnings({\"UnusedParameters\", \"unused\", \"SpellCheckingInspection\"})\n"
                                + "public class X {\n"
                                + "\n"
                                + "    public static final int UNRELATED = 500;\n"
                                + "\n"
                                + "    @IntDef({LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG})\n"
                                + "    @IntRange(from = 10)\n"
                                + "    @Retention(RetentionPolicy.SOURCE)\n"
                                + "    public @interface Duration {}\n"
                                + "\n"
                                + "    public static final int LENGTH_INDEFINITE = -2;\n"
                                + "    public static final int LENGTH_SHORT = -1;\n"
                                + "    public static final int LENGTH_LONG = 0;\n"
                                + "    public void setDuration(@Duration int duration) {\n"
                                + "    }\n"
                                + "\n"
                                + "    public void test() {\n"
                                + "        setDuration(UNRELATED); /// ERROR: Not right intdef, even if it's in the right number range\n"
                                + "        setDuration(-5); // ERROR (not right int def or value\n"
                                + "        setDuration(8); // ERROR (not matching number range)\n"
                                + "        setDuration(8000); // OK (@IntRange applies)\n"
                                + "        setDuration(LENGTH_INDEFINITE); // OK (@IntDef)\n"
                                + "        setDuration(LENGTH_LONG); // OK (@IntDef)\n"
                                + "        setDuration(LENGTH_SHORT); // OK (@IntDef)\n"
                                + "    }\n"
                                + "}\n"),
                        copy("src/android/support/annotation/IntDef.java.txt", "src/android/support/annotation/IntDef.java"),
                        copy("src/android/support/annotation/IntRange.java.txt", "src/android/support/annotation/IntRange.java")
                ));
    }


}
