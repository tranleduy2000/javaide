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
public class DateFormatDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new DateFormatDetector();
    }

    public void test() throws Exception {
        assertEquals(
            "src/test/pkg/LocaleTest.java:32: Warning: To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale) with for example Locale.US for ASCII dates. [SimpleDateFormat]\n" +
            "        new SimpleDateFormat(); // WRONG\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:33: Warning: To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale) with for example Locale.US for ASCII dates. [SimpleDateFormat]\n" +
            "        new SimpleDateFormat(\"yyyy-MM-dd\"); // WRONG\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "src/test/pkg/LocaleTest.java:34: Warning: To get local formatting use getDateInstance(), getDateTimeInstance(), or getTimeInstance(), or use new SimpleDateFormat(String template, Locale locale) with for example Locale.US for ASCII dates. [SimpleDateFormat]\n" +
            "        new SimpleDateFormat(\"yyyy-MM-dd\", DateFormatSymbols.getInstance()); // WRONG\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "0 errors, 3 warnings\n",

            lintProject(
                    "bytecode/.classpath=>.classpath",
                    "project.properties19=>project.properties",
                    "bytecode/AndroidManifest.xml=>AndroidManifest.xml",
                    "res/layout/onclick.xml=>res/layout/onclick.xml",
                    "bytecode/LocaleTest.java.txt=>src/test/pkg/LocaleTest.java",
                    "bytecode/LocaleTest.class.data=>bin/classes/test/pkg/LocaleTest.class"
                    ));
    }
}
