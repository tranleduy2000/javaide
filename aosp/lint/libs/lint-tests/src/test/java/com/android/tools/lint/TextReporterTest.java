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

package com.android.tools.lint;

import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.HardcodedValuesDetector;
import com.android.tools.lint.checks.ManifestDetector;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TextReporterTest extends AbstractCheckTest {
    public void test() throws Exception {
        File file = new File(getTargetDir(), "report");
        try {
            LintCliClient client = new LintCliClient() {
                @Override
                String getRevision() {
                    return "unittest"; // Hardcode version to keep unit test output stable
                }
            };
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            TextReporter reporter = new TextReporter(client, client.mFlags, file, writer, true);
            Project project = Project.create(client, new File("/foo/bar/Foo"),
                    new File("/foo/bar/Foo"));
            client.mFlags.setShowEverything(true);

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
            Location secondary = Location.create(warning1.file,
                    new DefaultPosition(7, 4, 198), new DefaultPosition(7, 42, 236));
            secondary.setMessage("Secondary location");
            warning1.location.setSecondary(secondary);

            Warning warning2 = new Warning(HardcodedValuesDetector.ISSUE,
                    "[I18N] Hardcoded string \"Fooo\", should use @string resource",
                    Severity.WARNING, project);
            warning2.line = 11;
            warning2.file = new File("/foo/bar/Foo/res/layout/main.xml");
            warning2.errorLine = "        android:text=\"Fooo\" />\n" +
                    "        ~~~~~~~~~~~~~~~~~~~\n";
            warning2.path = "res/layout/main.xml";
            warning2.location = Location.create(warning2.file,
                    new DefaultPosition(11, 8, 377), new DefaultPosition(11, 27, 396));
            secondary = Location.create(warning1.file,
                    new DefaultPosition(7, 4, 198), new DefaultPosition(7, 42, 236));
            secondary.setMessage("Secondary location");
            warning2.location.setSecondary(secondary);
            Location tertiary = Location.create(warning2.file,
                    new DefaultPosition(5, 4, 198), new DefaultPosition(5, 42, 236));
            secondary.setSecondary(tertiary);

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);
            Collections.sort(warnings);

            reporter.write(0, 2, warnings);

            String report = Files.toString(file, Charsets.UTF_8);
            assertEquals(""
                    + "AndroidManifest.xml:7: Warning: <uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=\"?\" [UsesMinSdkAttributes]\n"
                    + "    <uses-sdk android:minSdkVersion=\"8\" />\n"
                    + "    ^\n"
                    + "    AndroidManifest.xml:8: Secondary location\n"
                    + "res/layout/main.xml:12: Warning: [I18N] Hardcoded string \"Fooo\", should use @string resource [HardcodedText]\n"
                    + "        android:text=\"Fooo\" />\n"
                    + "        ~~~~~~~~~~~~~~~~~~~\n"
                    + "    AndroidManifest.xml:8: Secondary location\n"
                    + "Also affects: res/layout/main.xml:6\n"
                    + "0 errors, 2 warnings\n",
                    report);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public void testWithExplanations() throws Exception {
        File file = new File(getTargetDir(), "report");
        try {
            LintCliClient client = new LintCliClient() {
                @Override
                String getRevision() {
                    return "unittest"; // Hardcode version to keep unit test output stable
                }
            };
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            TextReporter reporter = new TextReporter(client, client.mFlags, file, writer, true);
            client.mFlags.setExplainIssues(true);
            Project project = Project.create(client, new File("/foo/bar/Foo"),
                    new File("/foo/bar/Foo"));
            client.mFlags.setShowEverything(true);

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
            Location secondary = Location.create(warning1.file,
                    new DefaultPosition(7, 4, 198), new DefaultPosition(7, 42, 236));
            secondary.setMessage("Secondary location");
            warning1.location.setSecondary(secondary);

            Warning warning2 = new Warning(HardcodedValuesDetector.ISSUE,
                    "[I18N] Hardcoded string \"Fooo\", should use @string resource",
                    Severity.WARNING, project);
            warning2.line = 11;
            warning2.file = new File("/foo/bar/Foo/res/layout/main.xml");
            warning2.errorLine = "        android:text=\"Fooo\" />\n" +
                    "        ~~~~~~~~~~~~~~~~~~~\n";
            warning2.path = "res/layout/main.xml";
            warning2.location = Location.create(warning2.file,
                    new DefaultPosition(11, 8, 377), new DefaultPosition(11, 27, 396));
            secondary = Location.create(warning1.file,
                    new DefaultPosition(7, 4, 198), new DefaultPosition(7, 42, 236));
            secondary.setMessage("Secondary location");
            warning2.location.setSecondary(secondary);
            Location tertiary = Location.create(warning2.file,
                    new DefaultPosition(5, 4, 198), new DefaultPosition(5, 42, 236));
            secondary.setSecondary(tertiary);

            // Add another warning of the same type as warning 1 to make sure we
            // (1) sort the warnings of the same issue together and (2) only print
            // the explanation twice1
            Warning warning3 = new Warning(ManifestDetector.USES_SDK,
                    "<uses-sdk> tag should specify a target API level (the highest verified " +
                            "version; when running on later versions, compatibility behaviors may " +
                            "be enabled) with android:targetSdkVersion=\"?\"",
                    Severity.WARNING, project);
            warning3.line = 8;
            warning3.file = new File("/foo/bar/Foo/AndroidManifest.xml");
            warning3.errorLine = "    <uses-sdk android:minSdkVersion=\"8\" />\n    ^\n";
            warning3.path = "AndroidManifest.xml";
            warning3.location = Location.create(warning3.file,
                    new DefaultPosition(8, 4, 198), new DefaultPosition(8, 42, 236));

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);
            warnings.add(warning3);
            Collections.sort(warnings);

            reporter.write(0, 3, warnings);

            String report = Files.toString(file, Charsets.UTF_8);
            assertEquals(""
                    + "AndroidManifest.xml:7: Warning: <uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=\"?\" [UsesMinSdkAttributes]\n"
                    + "    <uses-sdk android:minSdkVersion=\"8\" />\n"
                    + "    ^\n"
                    + "    AndroidManifest.xml:8: Secondary location\n"
                    + "AndroidManifest.xml:9: Warning: <uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=\"?\" [UsesMinSdkAttributes]\n"
                    + "    <uses-sdk android:minSdkVersion=\"8\" />\n"
                    + "    ^\n"
                    + "\n"
                    + "   Explanation for issues of type \"UsesMinSdkAttributes\":\n"
                    + "   The manifest should contain a <uses-sdk> element which defines the minimum\n"
                    + "   API Level required for the application to run, as well as the target\n"
                    + "   version (the highest API level you have tested the version for.)\n"
                    + "\n"
                    + "   http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\n"
                    + "\n"
                    + "res/layout/main.xml:12: Warning: [I18N] Hardcoded string \"Fooo\", should use @string resource [HardcodedText]\n"
                    + "        android:text=\"Fooo\" />\n"
                    + "        ~~~~~~~~~~~~~~~~~~~\n"
                    + "    AndroidManifest.xml:8: Secondary location\n"
                    + "Also affects: res/layout/main.xml:6\n"
                    + "\n"
                    + "   Explanation for issues of type \"HardcodedText\":\n"
                    + "   Hardcoding text attributes directly in layout files is bad for several\n"
                    + "   reasons:\n"
                    + "\n"
                    + "   * When creating configuration variations (for example for landscape or\n"
                    + "   portrait)you have to repeat the actual text (and keep it up to date when\n"
                    + "   making changes)\n"
                    + "\n"
                    + "   * The application cannot be translated to other languages by just adding\n"
                    + "   new translations for existing string resources.\n"
                    + "\n"
                    + "   In Android Studio and Eclipse there are quickfixes to automatically extract\n"
                    + "   this hardcoded string into a resource lookup.\n"
                    + "\n"
                    + "0 errors, 3 warnings\n",
                    report);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    @Override
    protected Detector getDetector() {
        fail("Not used in this test");
        return null;
    }
}
