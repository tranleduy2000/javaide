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

import static com.android.tools.lint.checks.TypographyDetector.FRACTION_PATTERN;
import static com.android.tools.lint.checks.TypographyDetector.GRAVE_QUOTATION;
import static com.android.tools.lint.checks.TypographyDetector.HYPHEN_RANGE_PATTERN;
import static com.android.tools.lint.checks.TypographyDetector.SINGLE_QUOTE;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class TypographyDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new TypographyDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "res/values/typography.xml:17: Warning: Replace \"-\" with an \"en dash\" character (–, &#8211;) ? [TypographyDashes]\n" +
            "    <string name=\"ndash\">For ages 3-5</string>\n" +
            "                         ^\n" +
            "res/values/typography.xml:18: Warning: Replace \"-\" with an \"en dash\" character (–, &#8211;) ? [TypographyDashes]\n" +
            "    <string name=\"ndash2\">Copyright 2007 - 2011</string>\n" +
            "                          ^\n" +
            "res/values/typography.xml:20: Warning: Replace \"--\" with an \"em dash\" character (—, &#8212;) ? [TypographyDashes]\n" +
            "    <string name=\"mdash\">Not found -- please try again</string>\n" +
            "                         ^\n" +
            "res/values/typography.xml:24: Warning: Replace \"-\" with an \"en dash\" character (–, &#8211;) ? [TypographyDashes]\n" +
            "        <item>Ages 3-5</item>\n" +
            "              ^\n" +
            "res/values/typography.xml:15: Warning: Replace \"...\" with ellipsis character (…, &#8230;) ? [TypographyEllipsis]\n" +
            "    <string name=\"ellipsis\">40 times...</string>\n" +
            "                            ^\n" +
            "res/values/typography.xml:12: Warning: Use fraction character ½ (&#189;) instead of 1/2 ? [TypographyFractions]\n" +
            "    <string name=\"fraction1\">5 1/2 times</string>\n" +
            "                             ^\n" +
            "res/values/typography.xml:13: Warning: Use fraction character ¼ (&#188;) instead of 1/4 ? [TypographyFractions]\n" +
            "    <string name=\"fraction4\">1/4 times</string>\n" +
            "                             ^\n" +
            "res/values/typography.xml:25: Warning: Use fraction character ½ (&#189;) instead of 1/2 ? [TypographyFractions]\n" +
            "        <item>Age 5 1/2</item>\n" +
            "              ^\n" +
            "res/values/typography.xml:3: Warning: Replace straight quotes ('') with directional quotes (‘’, &#8216; and &#8217;) ? [TypographyQuotes]\n" +
            "    <string name=\"home_title\">Home 'Sample'</string>\n" +
            "                              ^\n" +
            "res/values/typography.xml:5: Warning: Replace straight quotes (\") with directional quotes (“”, &#8220; and &#8221;) ? [TypographyQuotes]\n" +
            "    <string name=\"show_all_apps2\">Show \"All\"</string>\n" +
            "                                  ^\n" +
            "res/values/typography.xml:6: Warning: Replace straight quotes (\") with directional quotes (“”, &#8220; and &#8221;) ? [TypographyQuotes]\n" +
            "    <string name=\"escaped\">Skip \\\"All\\\"</string>\n" +
            "                           ^\n" +
            "res/values/typography.xml:7: Warning: Replace apostrophe (') with typographic apostrophe (’, &#8217;) ? [TypographyQuotes]\n" +
            "    <string name=\"single\">Android's</string>\n" +
            "                          ^\n" +
            "res/values/typography.xml:9: Warning: Replace apostrophe (') with typographic apostrophe (’, &#8217;) ? [TypographyQuotes]\n" +
            "    <string name=\"badquotes1\">`First'</string>\n" +
            "                              ^\n" +
            "res/values/typography.xml:10: Warning: Avoid quoting with grave accents; use apostrophes or better yet directional quotes instead [TypographyQuotes]\n" +
            "    <string name=\"badquotes2\">``second''</string>\n" +
            "                              ^\n" +
            "res/values/typography.xml:11: Warning: Replace straight quotes ('') with directional quotes (‘’, &#8216; and &#8217;) ? [TypographyQuotes]\n" +
            "    <string name=\"notbadquotes\">Type Option-` then 'Escape'</string>\n" +
            "                                ^\n" +
            "res/values/typography.xml:8: Warning: Replace (c) with copyright symbol © (&#169;) ? [TypographyOther]\n" +
            "    <string name=\"copyright\">(c) 2011</string>\n" +
            "                             ^\n" +
            "0 errors, 16 warnings\n" +
            "",

            lintProject("res/values/typography.xml"));
    }

    public void testAnalytics() throws Exception {
        assertEquals(""
                + "No warnings.",

                lintProject("res/values/analytics.xml"));
    }

    public void testPlurals() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=82588
        assertEquals(""
                + "res/values/plurals_typography.xml:5: Warning: Replace \"-\" with an \"en dash\" character (–, &#8211;) ? [TypographyDashes]\n"
                + "        <item quantity=\"one\">For ages 3-5</item>\n"
                + "                             ^\n"
                + "res/values/plurals_typography.xml:6: Warning: Use fraction character ¼ (&#188;) instead of 1/4 ? [TypographyFractions]\n"
                + "        <item quantity=\"few\">1/4 times</item>\n"
                + "                             ^\n"
                + "res/values/plurals_typography.xml:14: Warning: Avoid quoting with grave accents; use apostrophes or better yet directional quotes instead [TypographyQuotes]\n"
                + "        <item>``second''</item>\n"
                + "              ^\n"
                + "0 errors, 3 warnings\n",

                lintProject("res/values/plurals_typography.xml"));
    }

    public void testSingleQuotesRange() {
        assertTrue(SINGLE_QUOTE.matcher("Foo: 'bar'").matches());
        assertTrue(SINGLE_QUOTE.matcher("'Foo': bar").matches());
        assertTrue(SINGLE_QUOTE.matcher("\"'foo'\"").matches());
        assertTrue(SINGLE_QUOTE.matcher("\"'foo bar'\"").matches());

        assertFalse(SINGLE_QUOTE.matcher("foo bar'").matches());
        assertFalse(SINGLE_QUOTE.matcher("Mind your P's and Q's").matches());

        // This isn't asserted by the regexp: checked independently in
        // the detector. The goal here is to assert that we need to
        // have some text on either side of the quotes.
        //assertFalse(SINGLE_QUOTE.matcher("'foo bar'").matches());
    }

    public void testGraveRegexp() {
        assertTrue(GRAVE_QUOTATION.matcher("`a'").matches());
        assertTrue(GRAVE_QUOTATION.matcher(" `a' ").matches());
        assertTrue(GRAVE_QUOTATION.matcher(" ``a'' ").matches());
        assertFalse(GRAVE_QUOTATION.matcher("`a''").matches());
    }

    public void testFractionRegexp() {
        assertTrue(FRACTION_PATTERN.matcher("fraction 1/2.").matches());
        assertTrue(FRACTION_PATTERN.matcher("1/2").matches());
        assertTrue(FRACTION_PATTERN.matcher("1/3").matches());
        assertTrue(FRACTION_PATTERN.matcher("1/4").matches());
        assertTrue(FRACTION_PATTERN.matcher("3/4").matches());
        assertTrue(FRACTION_PATTERN.matcher("1 / 2").matches());
        assertTrue(FRACTION_PATTERN.matcher("1 / 3").matches());
        assertTrue(FRACTION_PATTERN.matcher("1 / 4").matches());
        assertTrue(FRACTION_PATTERN.matcher("3 / 4").matches());

        assertFalse(FRACTION_PATTERN.matcher("3 // 4").matches());
        assertFalse(FRACTION_PATTERN.matcher("11 / 2").matches());
        assertFalse(FRACTION_PATTERN.matcher("1 / 22").matches());
    }

    public void testNDashRegexp() {
        assertTrue(HYPHEN_RANGE_PATTERN.matcher("3-4").matches());
        assertTrue(HYPHEN_RANGE_PATTERN.matcher("13- 14").matches());
        assertTrue(HYPHEN_RANGE_PATTERN.matcher("13 - 14").matches());
        assertTrue(HYPHEN_RANGE_PATTERN.matcher("The range is 13 - 14").matches());
        assertTrue(HYPHEN_RANGE_PATTERN.matcher("13 - 14.").matches());

        assertFalse(HYPHEN_RANGE_PATTERN.matcher("13 - x").matches());
        assertFalse(HYPHEN_RANGE_PATTERN.matcher("x - 14").matches());
        assertFalse(HYPHEN_RANGE_PATTERN.matcher("x-y").matches());
        assertFalse(HYPHEN_RANGE_PATTERN.matcher("-y").matches());
        assertFalse(HYPHEN_RANGE_PATTERN.matcher("x-").matches());
    }
}
