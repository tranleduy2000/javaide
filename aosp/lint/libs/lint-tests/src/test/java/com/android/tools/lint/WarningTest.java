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
package com.android.tools.lint;

import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.UnusedResourceDetector;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.client.api.LintRequest;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WarningTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new UnusedResourceDetector();
    }

    @Override
    protected boolean isEnabled(Issue issue) {
        return true;
    }

    public void testComparator() throws Exception {
        File projectDir = getProjectDir(null, // Rename .txt files to .java
                "src/my/pkg/Test.java.txt=>src/my/pkg/Test.java",
                "gen/my/pkg/R.java.txt=>gen/my/pkg/R.java",
                "AndroidManifest.xml",
                "res/layout/accessibility.xml");

        final AtomicReference<List<Warning>> warningsHolder = new AtomicReference<List<Warning>>();
        TestLintClient lintClient = new TestLintClient() {
            @Override
            public String analyze(List<File> files) throws Exception {
                //String analyze = super.analyze(files);
                mDriver = new LintDriver(new CustomIssueRegistry(), this);
                configureDriver(mDriver);
                mDriver.analyze(new LintRequest(this, files).setScope(getLintScope(files)));
                warningsHolder.set(mWarnings);
                return null;
            }
        };
        List<File> files = Collections.singletonList(projectDir);
        lintClient.analyze(files);

        List<Warning> warnings = warningsHolder.get();
        Warning prev = null;
        for (Warning warning : warnings) {
            if (prev != null) {
                boolean equals = warning.equals(prev);
                assertEquals(equals, prev.equals(warning));
                int compare = warning.compareTo(prev);
                assertEquals(equals, compare == 0);
                assertEquals(-compare, prev.compareTo(warning));
            }
            prev = warning;
        }

        Collections.sort(warnings);

        Warning prev2 = prev;
        prev = null;
        for (Warning warning : warnings) {
            if (prev != null && prev2 != null) {
                assertTrue(warning.compareTo(prev) > 0);
                assertTrue(prev.compareTo(prev2) > 0);
                assertTrue(warning.compareTo(prev2) > 0);

                assertTrue(prev.compareTo(warning) < 0);
                assertTrue(prev2.compareTo(prev) < 0);
                assertTrue(prev2.compareTo(warning) < 0);
            }
            prev2 = prev;
            prev = warning;
        }
    }
}
