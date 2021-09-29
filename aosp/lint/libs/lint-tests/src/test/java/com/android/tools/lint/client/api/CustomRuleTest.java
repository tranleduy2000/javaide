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
package com.android.tools.lint.client.api;

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.HardcodedValuesDetector;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class CustomRuleTest extends AbstractCheckTest {
    private List<File> myGlobalJars = Collections.emptyList();
    private List<File> myProjectJars = Collections.emptyList();

    public void test() throws Exception {
        File projectDir = getProjectDir(null,
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "rules/appcompat.jar.data=>lint.jar",
                "rules/AppCompatTest.java.txt=>src/test/pkg/AppCompatTest.java",
                "rules/AppCompatTest.class.data=>bin/classes/test/pkg/AppCompatTest.class"
        );

        File lintJar = new File(projectDir, "lint.jar");
        assertTrue(lintJar.getPath(), lintJar.isFile());

        myProjectJars = Collections.singletonList(lintJar);
        assertEquals(""
                + "src/test/pkg/AppCompatTest.java:7: Warning: Should use getSupportActionBar instead of getActionBar name [AppCompatMethod]\n"
                + "        getActionBar();                    // ERROR\n"
                + "        ~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:10: Warning: Should use startSupportActionMode instead of startActionMode name [AppCompatMethod]\n"
                + "        startActionMode(null);             // ERROR\n"
                + "        ~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:13: Warning: Should use supportRequestWindowFeature instead of requestWindowFeature name [AppCompatMethod]\n"
                + "        requestWindowFeature(0);           // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:16: Warning: Should use setSupportProgressBarVisibility instead of setProgressBarVisibility name [AppCompatMethod]\n"
                + "        setProgressBarVisibility(true);    // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:17: Warning: Should use setSupportProgressBarIndeterminate instead of setProgressBarIndeterminate name [AppCompatMethod]\n"
                + "        setProgressBarIndeterminate(true);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:18: Warning: Should use setSupportProgressBarIndeterminateVisibility instead of setProgressBarIndeterminateVisibility name [AppCompatMethod]\n"
                + "        setProgressBarIndeterminateVisibility(true);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 6 warnings\n",
                checkLint(Collections.singletonList(projectDir)));
    }

    public void test2() throws Exception {
        File projectDir = getProjectDir(null,
                "apicheck/classpath=>.classpath",
                "apicheck/minsdk1.xml=>AndroidManifest.xml",
                "rules/appcompat.jar.data=>lint.jar",
                "rules/AppCompatTest.java.txt=>src/test/pkg/AppCompatTest.java",
                "rules/AppCompatTest.class.data=>bin/classes/test/pkg/AppCompatTest.class"
        );

        File lintJar = new File(projectDir, "lint.jar");
        assertTrue(lintJar.getPath(), lintJar.isFile());

        myGlobalJars = Collections.singletonList(lintJar);
        assertEquals(""
                + "src/test/pkg/AppCompatTest.java:7: Warning: Should use getSupportActionBar instead of getActionBar name [AppCompatMethod]\n"
                + "        getActionBar();                    // ERROR\n"
                + "        ~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:10: Warning: Should use startSupportActionMode instead of startActionMode name [AppCompatMethod]\n"
                + "        startActionMode(null);             // ERROR\n"
                + "        ~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:13: Warning: Should use supportRequestWindowFeature instead of requestWindowFeature name [AppCompatMethod]\n"
                + "        requestWindowFeature(0);           // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:16: Warning: Should use setSupportProgressBarVisibility instead of setProgressBarVisibility name [AppCompatMethod]\n"
                + "        setProgressBarVisibility(true);    // ERROR\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:17: Warning: Should use setSupportProgressBarIndeterminate instead of setProgressBarIndeterminate name [AppCompatMethod]\n"
                + "        setProgressBarIndeterminate(true);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/AppCompatTest.java:18: Warning: Should use setSupportProgressBarIndeterminateVisibility instead of setProgressBarIndeterminateVisibility name [AppCompatMethod]\n"
                + "        setProgressBarIndeterminateVisibility(true);\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 6 warnings\n",
                checkLint(Collections.singletonList(projectDir)));
    }

    @Override
    protected TestLintClient createClient() {
        return new TestLintClient() {
            @NonNull
            @Override
            public List<File> findGlobalRuleJars() {
                return myGlobalJars;
            }

            @NonNull
            @Override
            public List<File> findRuleJars(@NonNull Project project) {
                return myProjectJars;
            }
        };
    }

    @Override
    protected boolean isEnabled(Issue issue) {
        // Allow other issues than the one returned by getDetector below
        return true;
    }

    @Override
    protected Detector getDetector() {
        return new HardcodedValuesDetector();
    }
}
