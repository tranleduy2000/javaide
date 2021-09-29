/*
 * Copyright (C) 2013 The Android Open Source Project
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

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("javadoc")
public class PluralsDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new PluralsDetector();
    }

    public void test1() throws Exception {
        mEnabled = Sets.newHashSet(PluralsDetector.MISSING, PluralsDetector.EXTRA);
        assertEquals(""
                + "res/values-pl/plurals2.xml:3: Error: For locale \"pl\" (Polish) the following quantities should also be defined: many [MissingQuantity]\n"
                + "    <plurals name=\"numberOfSongsAvailable\">\n"
                + "    ^\n"
                + "1 errors, 0 warnings\n",

            lintProject(
                 "res/values/plurals.xml",
                 "res/values/plurals2.xml",
                 "res/values-pl/plurals2.xml"));
    }

    public void test2() throws Exception {
        mEnabled = Sets.newHashSet(PluralsDetector.MISSING, PluralsDetector.EXTRA);
        assertEquals(""
                + "res/values-cs/plurals3.xml:3: Error: For locale \"cs\" (Czech) the following quantities should also be defined: few [MissingQuantity]\n" +
                "  <plurals name=\"draft\">\n" +
                "  ^\n" +
                "res/values-zh-rCN/plurals3.xml:3: Warning: For language \"zh\" (Chinese) the following quantities are not relevant: one [UnusedQuantity]\n" +
                "  <plurals name=\"draft\">\n" +
                "  ^\n" +
                "res/values-zh-rCN/plurals3.xml:7: Warning: For language \"zh\" (Chinese) the following quantities are not relevant: one [UnusedQuantity]\n" +
                "  <plurals name=\"title_day_dialog_content\">\n" +
                "  ^\n" +
                "1 errors, 2 warnings\n",

                lintProject(
                        "res/values-zh-rCN/plurals3.xml",
                        "res/values-cs/plurals3.xml"));
    }

    public void testEmptyPlural() throws Exception {
        mEnabled = Sets.newHashSet(PluralsDetector.MISSING, PluralsDetector.EXTRA);
        assertEquals(""
                + "res/values/plurals4.xml:3: Error: There should be at least one quantity string in this <plural> definition [MissingQuantity]\n"
                + "   <plurals name=\"minutes_until_num\">\n"
                + "   ^\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/plurals4.xml"));
    }

    public void testPolish() throws Exception {
        // Test for https://code.google.com/p/android/issues/detail?id=67803
        mEnabled = Sets.newHashSet(PluralsDetector.MISSING, PluralsDetector.EXTRA);
        assertEquals(""
                + "res/values-pl/plurals5.xml:3: Error: For locale \"pl\" (Polish) the following quantities should also be defined: many [MissingQuantity]\n"
                + "    <plurals name=\"my_plural\">\n"
                + "    ^\n"
                + "res/values-pl/plurals5.xml:3: Warning: For language \"pl\" (Polish) the following quantities are not relevant: zero [UnusedQuantity]\n"
                + "    <plurals name=\"my_plural\">\n"
                + "    ^\n"
                + "1 errors, 1 warnings\n",

                lintProject(
                        "res/values/plurals5.xml=>res/values-pl/plurals5.xml"));
    }

    public void testRussian() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=75799
        mEnabled = Sets.newHashSet(PluralsDetector.MISSING, PluralsDetector.EXTRA);
        assertEquals("No warnings.",

                lintProject(
                        "res/values-ru/plurals6.xml=>res/values-ru/plurals6.xml"));
    }

    public void testImpliedQuantity() throws Exception {
        mEnabled = Collections.singleton(PluralsDetector.IMPLIED_QUANTITY);
        assertEquals(""
                + "res/values-sl/plurals2.xml:4: Error: The quantity 'one' matches more than one specific number in this locale (1, 101, 201, 301, 401, 501, 601, 701, 1001, \u2026), but the message did not include a formatting argument (such as %d). This is usually an internationalization error. See full issue explanation for more. [ImpliedQuantity]\n"
                + "        <item quantity=\"one\">Znaleziono jedną piosenkę.</item>\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "res/values/plurals.xml",
                        "res/values/plurals2.xml",
                        "res/values-pl/plurals2.xml",
                        // Simulate locale message for locale which has multiple values for one
                        "res/values-pl/plurals2.xml=>res/values-sl/plurals2.xml"));
    }

    private Set<Issue> mEnabled = new HashSet<Issue>();

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }
}
