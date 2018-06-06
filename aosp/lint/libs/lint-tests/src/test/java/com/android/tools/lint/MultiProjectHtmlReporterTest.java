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

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.HardcodedValuesDetector;
import com.android.tools.lint.checks.ManifestDetector;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiProjectHtmlReporterTest  extends AbstractCheckTest {
    public void test() throws Exception {
        File dir = new File(getTargetDir(), "report");
        try {
            LintCliClient client = new LintCliClient() {
                @Override
                IssueRegistry getRegistry() {
                    if (mRegistry == null) {
                        mRegistry = new IssueRegistry()  {
                            @NonNull
                            @Override
                            public List<Issue> getIssues() {
                                return Arrays.asList(
                                        ManifestDetector.USES_SDK,
                                        HardcodedValuesDetector.ISSUE,
                                        // Not reported, but for the disabled-list
                                        ManifestDetector.MOCK_LOCATION);
                            }
                        };
                    }
                    return mRegistry;
                }
            };

            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
            MultiProjectHtmlReporter reporter = new MultiProjectHtmlReporter(client, dir);
            Project project = Project.create(client, new File("/foo/bar/Foo"),
                    new File("/foo/bar/Foo"));

            Warning warning1 = new Warning(ManifestDetector.USES_SDK,
                    "<uses-sdk> tag should specify a target API level (the highest verified " +
                            "version; when running on later versions, compatibility behaviors may " +
                            "be enabled) with android:targetSdkVersion=\"?\"",
                    Severity.WARNING, project);
            warning1.line = 6;
            warning1.file = new File("/foo/bar/Foo/AndroidManifest.xml");
            warning1.errorLine = "    <uses-sdk android:minSdkVersion=\"8\" />\n    ^\n";
            warning1.path = "AndroidManifest.xml";
            warning1.location = Location.create(warning1.file,
                    new DefaultPosition(6, 4, 198), new DefaultPosition(6, 42, 236));

            Warning warning2 = new Warning(HardcodedValuesDetector.ISSUE,
                    "[I18N] Hardcoded string \"Fooo\", should use @string resource",
                    Severity.WARNING, project);
            warning2.line = 11;
            warning2.file = new File("/foo/bar/Foo/res/layout/main.xml");
            warning2.errorLine = " (java.lang.String)         android:text=\"Fooo\" />\n" +
                    "        ~~~~~~~~~~~~~~~~~~~\n";
            warning2.path = "res/layout/main.xml";
            warning2.location = Location.create(warning2.file,
                    new DefaultPosition(11, 8, 377), new DefaultPosition(11, 27, 396));

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);

            reporter.write(0, 2, warnings);

            String report = Files.toString(new File(dir, "index.html"), Charsets.UTF_8);

            // Replace the timestamp to make golden file comparison work
            String timestampPrefix = "Check performed at ";
            int begin = report.indexOf(timestampPrefix);
            assertTrue(begin != -1);
            begin += timestampPrefix.length();
            int end = report.indexOf(".<br/>", begin);
            assertTrue(end != -1);
            report = report.substring(0, begin) + "$DATE" + report.substring(end);

            // NOTE: If you change the output, please validate it manually in
            //  http://validator.w3.org/#validate_by_input
            // before updating the following
            assertEquals(""
                    + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
                    + "<head>\n"
                    + "<title>Lint Report</title>\n"
                    + "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://fonts.googleapis.com/css?family=Roboto\" />\n"
                    + "<link rel=\"stylesheet\" type=\"text/css\" href=\"index_files/hololike.css\" />\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "<h1>Lint Report</h1>\n"
                    + "<div class=\"titleSeparator\"></div>\n"
                    + "Check performed at $DATE.<br/>\n"
                    + "0 errors and 2 warnings found:\n"
                    + "<br/><br/>\n"
                    + "<table class=\"overview\">\n"
                    + "<tr><th>Project</th><th class=\"countColumn\"><img border=\"0\" align=\"top\" src=\"index_files/lint-error.png\" alt=\"Error\" />\n"
                    + "Errors</th><th class=\"countColumn\"><img border=\"0\" align=\"top\" src=\"index_files/lint-warning.png\" alt=\"Warning\" />\n"
                    + "Warnings</th></tr>\n"
                    + "<tr><td><a href=\"Foo.html\">Foo</a></td><td class=\"countColumn\">0</td><td class=\"countColumn\">2</td></tr>\n"
                    + "</table>\n"
                    + "</body>\n"
                    + "</html>\n",
                    report);

            assertTrue(new File(dir, "index_files" + File.separator + "hololike.css").exists());
            assertTrue(new File(dir, "index_files" + File.separator + "lint-warning.png").exists());
            assertTrue(new File(dir, "index_files" + File.separator + "lint-error.png").exists());
            assertTrue(new File(dir, "Foo.html").exists());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            dir.delete();
        }
    }

    @Override
    protected Detector getDetector() {
        fail("Not used in this test");
        return null;
    }
}
