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

import static com.android.tools.lint.checks.LocaleFolderDetector.suggestBcp47Correction;

import com.android.tools.lint.detector.api.Detector;

@SuppressWarnings("javadoc")
public class LocaleFolderDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new LocaleFolderDetector();
    }

    public void testDeprecated() throws Exception {
        assertEquals(""
            + "res/values-he: Warning: The locale folder \"he\" should be called \"iw\" instead; see the java.util.Locale documentation [LocaleFolder]\n"
            + "res/values-id: Warning: The locale folder \"id\" should be called \"in\" instead; see the java.util.Locale documentation [LocaleFolder]\n"
            + "res/values-yi: Warning: The locale folder \"yi\" should be called \"ji\" instead; see the java.util.Locale documentation [LocaleFolder]\n"
            + "0 errors, 3 warnings\n",

            lintProject(
                    "res/values/strings.xml",
                    "res/values/strings.xml=>res/values-no/strings.xml",
                    "res/values/strings.xml=>res/values-he/strings.xml",
                    "res/values/strings.xml=>res/values-id/strings.xml",
                    "res/values/strings.xml=>res/values-yi/strings.xml")
        );
    }

    public void testSuspiciousRegion() throws Exception {
        assertEquals(""
                + "res/values-ff-rNO: Warning: Suspicious language and region combination ff (Fulah) with NO (Norway): language ff is usually paired with: SN (Senegal), CM (Cameroon), GN (Guinea), MR (Mauritania) [WrongRegion]\n"
                + "res/values-nb-rSE: Warning: Suspicious language and region combination nb (Norwegian Bokm\u00e5l) with SE (Sweden): language nb is usually paired with: NO (Norway), SJ (Svalbard & Jan Mayen) [WrongRegion]\n"
                + "res/values-sv-rSV: Warning: Suspicious language and region combination sv (Swedish) with SV (El Salvador): language sv is usually paired with: SE (Sweden), AX (Åland Islands), FI (Finland) [WrongRegion]\n"
                + "0 errors, 3 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values/strings.xml=>res/values-no/strings.xml",
                        "res/values/strings.xml=>res/values-nb-rNO/strings.xml",
                        "res/values/strings.xml=>res/values-nb-rSJ/strings.xml",
                        "res/values/strings.xml=>res/values-nb-rSE/strings.xml",
                        "res/values/strings.xml=>res/values-sv-rSV/strings.xml",
                        "res/values/strings.xml=>res/values-ff-rNO/strings.xml"
                )
        );
    }

    public void testAlpha3() throws Exception {
        assertEquals(""
                + "res/values-b+nor+NOR: Warning: For compatibility, should use 2-letter language codes when available; use no instead of nor [UseAlpha2]\n"
                + "res/values-b+nor+NOR: Warning: For compatibility, should use 2-letter region codes when available; use NO instead of nor [UseAlpha2]\n"
                + "0 errors, 2 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values/strings.xml=>res/values-no/strings.xml",
                        "res/values/strings.xml=>res/values-b+kok+IN//strings.xml", // OK
                        "res/values/strings.xml=>res/values-b+nor+NOR/strings.xml" // Not OK
                )
        );
    }

    public void testInvalidFolder() throws Exception {
        assertEquals(""
                + "res/values-ldtrl-mnc123: Error: Invalid resource folder: make sure qualifiers appear in the correct order, are spelled correctly, etc. [InvalidResourceFolder]\n"
                + "res/values-no-rNOR: Error: Invalid resource folder; did you mean b+no+NO ? [InvalidResourceFolder]\n"
                + "2 errors, 0 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values/strings.xml=>res/values-ldtrl-mnc123/strings.xml",
                        "res/values/strings.xml=>res/values-kok-rIN//strings.xml",
                        "res/values/strings.xml=>res/values-no-rNOR/strings.xml"
                )
        );
    }

    public void testConflictingScripts() throws Exception {
        assertEquals(""
                + "res/values-b+en+Scr1: Error: Multiple locale folders for language en map to a single folder in versions < API 21: values-b+en+Scr2, values-b+en+Scr1 [InvalidResourceFolder]\n"
                + "    res/values-b+en+Scr2: <No location-specific message\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/strings.xml",
                        "res/values/strings.xml=>res/values-b+en+Scr1/strings.xml",
                        "res/values/strings.xml=>res/values-b+en+Scr2/strings.xml",
                        "res/values/strings.xml=>res/values-b+en+Scr3-v21/strings.xml",
                        "res/values/strings.xml=>res/values-b+fr+Scr1-v21/strings.xml",
                        "res/values/strings.xml=>res/values-b+fr+Scr2-v21/strings.xml",
                        "res/values/strings.xml=>res/values-b+no+Scr1/strings.xml",
                        "res/values/strings.xml=>res/values-b+no+Scr2-v21/strings.xml",
                        "res/values/strings.xml=>res/values-b+se+Scr1/strings.xml",
                        "res/values/strings.xml=>res/values-b+de+Scr1+DE/strings.xml",
                        "res/values/strings.xml=>res/values-b+de+Scr2+AT/strings.xml"
                )
        );
    }

    public void testBcpReplacement() {
        assertEquals("b+no+NO", suggestBcp47Correction("values-nor-rNO"));
        assertEquals("b+no+NO", suggestBcp47Correction("values-nor-rNOR"));
        assertEquals("b+es+419", suggestBcp47Correction("values-es-419"));
        assertNull(suggestBcp47Correction("values-car"));
        assertNull(suggestBcp47Correction("values-b+foo+bar"));
    }
}