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
public class LocaleDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new LocaleDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/LocaleTest.java:11: Warning: Implicitly using the default locale is a common source of bugs: Use toUpperCase(Locale) instead [DefaultLocale]\n" +
            "        System.out.println(\"WRONG\".toUpperCase());\n" +
            "                                   ~~~~~~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:16: Warning: Implicitly using the default locale is a common source of bugs: Use toLowerCase(Locale) instead [DefaultLocale]\n" +
            "        System.out.println(\"WRONG\".toLowerCase());\n" +
            "                                   ~~~~~~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:20: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %f\", 1.0f); // Implies locale\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:21: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %1$f\", 1.0f);\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:22: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %e\", 1.0f);\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:23: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %d\", 1.0f);\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:24: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %g\", 1.0f);\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:25: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %g\", 1.0f);\n" +
            "               ~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:26: Warning: Implicitly using the default locale is a common source of bugs: Use String.format(Locale, ...) instead [DefaultLocale]\n" +
            "        String.format(\"WRONG: %1$tm %1$te,%1$tY\",\n" +
            "               ~~~~~~\n" +
            "0 errors, 9 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                    "res/layout/onclick.xml=>res/layout/onclick.xml",
                    "bytecode/LocaleTest.java.txt=>src/test/pkg/LocaleTest.java",
                    "bytecode/LocaleTest.class.data=>bin/classes/test/pkg/LocaleTest.class"
                    ));
    }
}
