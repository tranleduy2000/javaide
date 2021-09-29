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

@SuppressWarnings("javadoc")
public class HtmlReporterTest extends AbstractCheckTest {
    public void test() throws Exception {
        //noinspection ResultOfMethodCallIgnored
        File projectDir = Files.createTempDir();
        File buildDir = new File(projectDir, "build");
        File reportFile = new File(buildDir, "report");
        //noinspection ResultOfMethodCallIgnored
        buildDir.mkdirs();

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

            HtmlReporter reporter = new HtmlReporter(client, reportFile);
            File res = new File(projectDir, "res");
            File layout = new File(res, "layout");
            File main = new File(layout, "main.xml");
            File manifest = new File(projectDir, "AndroidManifest.xml");
            Project project = Project.create(client, projectDir, projectDir);
            Warning warning1 = new Warning(ManifestDetector.USES_SDK,
                    "<uses-sdk> tag should specify a target API level (the highest verified " +
                    "version; when running on later versions, compatibility behaviors may " +
                    "be enabled) with android:targetSdkVersion=\"?\"",
                    Severity.WARNING, project);
            warning1.line = 6;
            warning1.file = manifest;
            warning1.errorLine = "    <uses-sdk android:minSdkVersion=\"8\" />\n    ^\n";
            warning1.path = "AndroidManifest.xml";
            warning1.location = Location.create(warning1.file,
                    new DefaultPosition(6, 4, 198), new DefaultPosition(6, 42, 236));

            Warning warning2 = new Warning(HardcodedValuesDetector.ISSUE,
                    "[I18N] Hardcoded string \"Fooo\", should use @string resource",
                    Severity.WARNING, project);
            warning2.line = 11;
            warning2.file = main;
            warning2.errorLine = " (java.lang.String)         android:text=\"Fooo\" />\n" +
                          "        ~~~~~~~~~~~~~~~~~~~\n";
            warning2.path = "res/layout/main.xml";
            warning2.location = Location.create(warning2.file,
                    new DefaultPosition(11, 8, 377), new DefaultPosition(11, 27, 396));

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);

            reporter.write(0, 2, warnings);

            String report = Files.toString(reportFile, Charsets.UTF_8);

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
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><title>Lint Report</title>\n"
                    + "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://fonts.googleapis.com/css?family=Roboto\" />\n"
                    + "<link rel=\"stylesheet\" type=\"text/css\" href=\"report_files/hololike.css\" />\n"
                    + "<script language=\"javascript\" type=\"text/javascript\"> \n"
                    + "<!--\n"
                    + "function reveal(id) {\n"
                    + "if (document.getElementById) {\n"
                    + "document.getElementById(id).style.display = 'block';\n"
                    + "document.getElementById(id+'Link').style.display = 'none';\n"
                    + "}\n"
                    + "}\n"
                    + "//--> \n"
                    + "</script>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "<h1>Lint Report</h1>\n"
                    + "<div class=\"titleSeparator\"></div>\n"
                    + "Check performed at $DATE.<br/>\n"
                    + "0 errors and 2 warnings found:<br/><br/>\n"
                    + "<table class=\"overview\">\n"
                    + "<tr><td></td><td class=\"categoryColumn\"><a href=\"#Correctness\">Correctness</a>\n"
                    + "</td></tr>\n"
                    + "<tr>\n"
                    + "<td class=\"countColumn\">1</td><td class=\"issueColumn\"><img border=\"0\" align=\"top\" src=\"report_files/lint-warning.png\" alt=\"Warning\" />\n"
                    + "<a href=\"#UsesMinSdkAttributes\">UsesMinSdkAttributes: Minimum SDK and target SDK attributes not defined</a>\n"
                    + "</td></tr>\n"
                    + "<tr><td></td><td class=\"categoryColumn\"><a href=\"#Internationalization\">Internationalization</a>\n"
                    + "</td></tr>\n"
                    + "<tr>\n"
                    + "<td class=\"countColumn\">1</td><td class=\"issueColumn\"><img border=\"0\" align=\"top\" src=\"report_files/lint-warning.png\" alt=\"Warning\" />\n"
                    + "<a href=\"#HardcodedText\">HardcodedText: Hardcoded text</a>\n"
                    + "</td></tr>\n"
                    + "</table>\n"
                    + "<br/>\n"
                    + "<a name=\"Correctness\"></a>\n"
                    + "<div class=\"category\"><a href=\"#\" title=\"Return to top\">Correctness</a><div class=\"categorySeparator\"></div>\n"
                    + "</div>\n"
                    + "<a name=\"UsesMinSdkAttributes\"></a>\n"
                    + "<div class=\"issue\">\n"
                    + "<div class=\"id\"><a href=\"#\" title=\"Return to top\">UsesMinSdkAttributes: Minimum SDK and target SDK attributes not defined</a><div class=\"issueSeparator\"></div>\n"
                    + "</div>\n"
                    + "<div class=\"warningslist\">\n"
                    + "<span class=\"location\"><a href=\"../AndroidManifest.xml\">AndroidManifest.xml</a>:7</span>: <span class=\"message\">&lt;uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=\"?\"</span><br />\n"
                    + "</div>\n"
                    + "<div class=\"metadata\">Priority: 9 / 10<br/>\n"
                    + "Category: Correctness</div>\n"
                    + "Severity: <span class=\"warning\">Warning</span><div class=\"summary\">\n"
                    + "Explanation: Minimum SDK and target SDK attributes not defined.</div>\n"
                    + "<div class=\"explanation\">\n"
                    + "The manifest should contain a <code>&lt;uses-sdk></code> element which defines the minimum API Level required for the application to run, as well as the target version (the highest API level you have tested the version for.)\n"
                    + "</div>\n"
                    + "<br/><div class=\"moreinfo\">More info: <a href=\"http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\">http://developer.android.com/guide/topics/manifest/uses-sdk-element.html</a>\n"
                    + "</div><br/>To suppress this error, use the issue id \"UsesMinSdkAttributes\" as explained in the <a href=\"#SuppressInfo\">Suppressing Warnings and Errors</a> section.<br/>\n"
                    + "</div>\n"
                    + "\n"
                    + "<a name=\"Internationalization\"></a>\n"
                    + "<div class=\"category\"><a href=\"#\" title=\"Return to top\">Internationalization</a><div class=\"categorySeparator\"></div>\n"
                    + "</div>\n"
                    + "<a name=\"HardcodedText\"></a>\n"
                    + "<div class=\"issue\">\n"
                    + "<div class=\"id\"><a href=\"#\" title=\"Return to top\">HardcodedText: Hardcoded text</a><div class=\"issueSeparator\"></div>\n"
                    + "</div>\n"
                    + "<div class=\"warningslist\">\n"
                    + "<span class=\"location\"><a href=\"../res/layout/main.xml\">res/layout/main.xml</a>:12</span>: <span class=\"message\">[I18N] Hardcoded string \"Fooo\", should use @string resource</span><br />\n"
                    + "</div>\n"
                    + "<div class=\"metadata\">Priority: 5 / 10<br/>\n"
                    + "Category: Internationalization</div>\n"
                    + "Severity: <span class=\"warning\">Warning</span><div class=\"summary\">\n"
                    + "Explanation: Hardcoded text.</div>\n"
                    + "<div class=\"explanation\">\n"
                    + "Hardcoding text attributes directly in layout files is bad for several reasons:<br/>\n"
                    + "<br/>\n"
                    + "* When creating configuration variations (for example for landscape or portrait)you have to repeat the actual text (and keep it up to date when making changes)<br/>\n"
                    + "<br/>\n"
                    + "* The application cannot be translated to other languages by just adding new translations for existing string resources.<br/>\n"
                    + "<br/>\n"
                    + "In Android Studio and Eclipse there are quickfixes to automatically extract this hardcoded string into a resource lookup.\n"
                    + "</div>\n"
                    + "<br/><div class=\"moreinfo\">More info: </div><br/>To suppress this error, use the issue id \"HardcodedText\" as explained in the <a href=\"#SuppressInfo\">Suppressing Warnings and Errors</a> section.<br/>\n"
                    + "</div>\n"
                    + "\n"
                    + "<a name=\"MissingIssues\"></a>\n"
                    + "<div class=\"category\">Disabled Checks<div class=\"categorySeparator\"></div>\n"
                    + "</div>\n"
                    + "The following issues were not run by lint, either because the check is not enabled by default, or because it was disabled with a command line flag or via one or more lint.xml configuration files in the project directories.\n"
                    + "<br/><br/>\n"
                    + "\n"
                    + "<a name=\"SuppressInfo\"></a>\n"
                    + "<div class=\"category\">Suppressing Warnings and Errors<div class=\"categorySeparator\"></div>\n"
                    + "</div>\n"
                    + "Lint errors can be suppressed in a variety of ways:<br/>\n"
                    + "<br/>\n"
                    + "1. With a <code>@SuppressLint</code> annotation in the Java code<br/>\n"
                    + "2. With a <code>tools:ignore</code> attribute in the XML file<br/>\n"
                    + "3. With ignore flags specified in the <code>build.gradle</code> file, as explained below<br/>\n"
                    + "4. With a <code>lint.xml</code> configuration file in the project<br/>\n"
                    + "5. With a <code>lint.xml</code> configuration file passed to lint via the --config flag<br/>\n"
                    + "6. With the --ignore flag passed to lint.<br/>\n"
                    + "<br/>\n"
                    + "To suppress a lint warning with an annotation, add a <code>@SuppressLint(\"id\")</code> annotation on the class, method or variable declaration closest to the warning instance you want to disable. The id can be one or more issue id's, such as <code>\"UnusedResources\"</code> or <code>{\"UnusedResources\",\"UnusedIds\"}</code>, or it can be <code>\"all\"</code> to suppress all lint warnings in the given scope.<br/>\n"
                    + "<br/>\n"
                    + "To suppress a lint warning in an XML file, add a <code>tools:ignore=\"id\"</code> attribute on the element containing the error, or one of its surrounding elements. You also need to define the namespace for the tools prefix on the root element in your document, next to the <code>xmlns:android</code> declaration:<br/>\n"
                    + "<code>xmlns:tools=\"http://schemas.android.com/tools\"</code><br/>\n"
                    + "<br/>\n"
                    + "To suppress a lint warning in a <code>build.gradle</code> file, add a section like this:<br/>\n"
                    + "<br/>\n"
                    + "android {<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;lintOptions {<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;disable 'TypographyFractions','TypographyQuotes'<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;}<br/>\n"
                    + "}<br/>\n"
                    + "<br/>\n"
                    + "Here we specify a comma separated list of issue id's after the disable command. You can also use <code>warning</code> or <code>error</code> instead of <code>disable</code> to change the severity of issues.<br/>\n"
                    + "<br/>\n"
                    + "To suppress lint warnings with a configuration XML file, create a file named <code>lint.xml</code> and place it at the root directory of the project in which it applies.<br/>\n"
                    + "<br/>\n"
                    + "The format of the <code>lint.xml</code> file is something like the following:<br/>\n"
                    + "<br/>\n"
                    + "&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?><br/>\n"
                    + "&lt;lint><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;!-- Disable this given check in this project --><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;issue id=\"IconMissingDensityFolder\" severity=\"ignore\" /><br/>\n"
                    + "<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;!-- Ignore the ObsoleteLayoutParam issue in the given files --><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;issue id=\"ObsoleteLayoutParam\"><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;ignore path=\"res/layout/activation.xml\" /><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;ignore path=\"res/layout-xlarge/activation.xml\" /><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;/issue><br/>\n"
                    + "<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;!-- Ignore the UselessLeaf issue in the given file --><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;issue id=\"UselessLeaf\"><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;ignore path=\"res/layout/main.xml\" /><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;/issue><br/>\n"
                    + "<br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;!-- Change the severity of hardcoded strings to \"error\" --><br/>\n"
                    + "&nbsp;&nbsp;&nbsp;&nbsp;&lt;issue id=\"HardcodedText\" severity=\"error\" /><br/>\n"
                    + "&lt;/lint><br/>\n"
                    + "<br/>\n"
                    + "To suppress lint checks from the command line, pass the --ignore flag with a comma separated list of ids to be suppressed, such as:<br/>\n"
                    + "<code>$ lint --ignore UnusedResources,UselessLeaf /my/project/path</code><br/>\n"
                    + "<br/>\n"
                    + "For more information, see <a href=\"http://g.co/androidstudio/suppressing-lint-warnings\">http://g.co/androidstudio/suppressing-lint-warnings</a><br/>\n"
                    + "\n"
                    + "\n"
                    + "</body>\n"
                    + "</html>",
                report);
        } finally {
            deleteFile(projectDir);
        }
    }

    @Override
    protected Detector getDetector() {
        fail("Not used in this test");
        return null;
    }
}
