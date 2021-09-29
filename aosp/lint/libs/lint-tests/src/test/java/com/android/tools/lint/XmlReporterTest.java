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

package com.android.tools.lint;

import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.HardcodedValuesDetector;
import com.android.tools.lint.checks.ManifestDetector;
import com.android.tools.lint.checks.TypographyDetector;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.utils.PositionXmlParser;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("javadoc")
public class XmlReporterTest extends AbstractCheckTest {
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
            XmlReporter reporter = new XmlReporter(client, file);
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
            warning2.errorLine = "        android:text=\"Fooo\" />\n" +
                          "        ~~~~~~~~~~~~~~~~~~~\n";
            warning2.path = "res/layout/main.xml";
            warning2.location = Location.create(warning2.file,
                    new DefaultPosition(11, 8, 377), new DefaultPosition(11, 27, 396));

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);

            reporter.write(0, 2, warnings);

            String report = Files.toString(file, Charsets.UTF_8);
            assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<issues format=\"4\" by=\"lint unittest\">\n" +
                "\n" +
                "    <issue\n" +
                "        id=\"UsesMinSdkAttributes\"\n" +
                "        severity=\"Warning\"\n" +
                "        message=\"&lt;uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=&quot;?&quot;\"\n" +
                "        category=\"Correctness\"\n" +
                "        priority=\"9\"\n" +
                "        summary=\"Minimum SDK and target SDK attributes not defined\"\n" +
                "        explanation=\"The manifest should contain a `&lt;uses-sdk>` element which defines the minimum API Level required for the application to run, as well as the target version (the highest API level you have tested the version for.)\"\n" +
                "        url=\"http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\"\n" +
                "        urls=\"http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\"\n" +
                "        errorLine1=\"    &lt;uses-sdk android:minSdkVersion=&quot;8&quot; />\"\n" +
                "        errorLine2=\"    ^\">\n" +
                "        <location\n" +
                "            file=\"AndroidManifest.xml\"\n" +
                "            line=\"7\"\n" +
                "            column=\"5\"/>\n" +
                "    </issue>\n" +
                "\n" +
                "    <issue\n" +
                "        id=\"HardcodedText\"\n" +
                "        severity=\"Warning\"\n" +
                "        message=\"[I18N] Hardcoded string &quot;Fooo&quot;, should use @string resource\"\n" +
                "        category=\"Internationalization\"\n" +
                "        priority=\"5\"\n" +
                "        summary=\"Hardcoded text\"\n" +
                "        explanation=\"Hardcoding text attributes directly in layout files is bad for several reasons:\n" +
                "\n" +
                "* When creating configuration variations (for example for landscape or portrait)you have to repeat the actual text (and keep it up to date when making changes)\n" +
                "\n" +
                "* The application cannot be translated to other languages by just adding new translations for existing string resources.\n" +
                "\n" +
                "In Android Studio and Eclipse there are quickfixes to automatically extract this hardcoded string into a resource lookup.\"\n" +
                "        errorLine1=\"        android:text=&quot;Fooo&quot; />\"\n" +
                "        errorLine2=\"        ~~~~~~~~~~~~~~~~~~~\">\n" +
                "        <location\n" +
                "            file=\"res/layout/main.xml\"\n" +
                "            line=\"12\"\n" +
                "            column=\"9\"/>\n" +
                "    </issue>\n" +
                "\n" +
                "</issues>\n",
                report);

            // Make sure the XML is valid
            Document document = PositionXmlParser.parse(report);
            assertNotNull(document);
            assertEquals(2, document.getElementsByTagName("issue").getLength());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public void testFullPaths() throws Exception {
        File file = new File(getTargetDir(), "report");
        try {
            LintCliClient client = new LintCliClient() {
                @Override
                String getRevision() {
                    return "unittest"; // Hardcode version to keep unit test output stable
                }
            };
            client.mFlags.setFullPath(true);

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            XmlReporter reporter = new XmlReporter(client, file);
            Project project = Project.create(client, new File("/foo/bar/Foo"),
                    new File("/foo/bar/Foo"));

            Warning warning1 = new Warning(ManifestDetector.USES_SDK,
                    "<uses-sdk> tag should specify a target API level (the highest verified " +
                    "version; when running on later versions, compatibility behaviors may " +
                    "be enabled) with android:targetSdkVersion=\"?\"",
                    Severity.WARNING, project);
            warning1.line = 6;
            warning1.file = new File("/foo/bar/../Foo/AndroidManifest.xml");
            warning1.errorLine = "    <uses-sdk android:minSdkVersion=\"8\" />\n    ^\n";
            warning1.path = "AndroidManifest.xml";
            warning1.location = Location.create(warning1.file,
                    new DefaultPosition(6, 4, 198), new DefaultPosition(6, 42, 236));

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

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);
            warnings.add(warning2);

            reporter.write(0, 2, warnings);

            String report = Files.toString(file, Charsets.UTF_8);
            assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<issues format=\"4\" by=\"lint unittest\">\n" +
                "\n" +
                "    <issue\n" +
                "        id=\"UsesMinSdkAttributes\"\n" +
                "        severity=\"Warning\"\n" +
                "        message=\"&lt;uses-sdk> tag should specify a target API level (the highest verified version; when running on later versions, compatibility behaviors may be enabled) with android:targetSdkVersion=&quot;?&quot;\"\n" +
                "        category=\"Correctness\"\n" +
                "        priority=\"9\"\n" +
                "        summary=\"Minimum SDK and target SDK attributes not defined\"\n" +
                "        explanation=\"The manifest should contain a `&lt;uses-sdk>` element which defines the minimum API Level required for the application to run, as well as the target version (the highest API level you have tested the version for.)\"\n" +
                "        url=\"http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\"\n" +
                "        urls=\"http://developer.android.com/guide/topics/manifest/uses-sdk-element.html\"\n" +
                "        errorLine1=\"    &lt;uses-sdk android:minSdkVersion=&quot;8&quot; />\"\n" +
                "        errorLine2=\"    ^\">\n" +
                "        <location\n" +
                "            file=\"/foo/Foo/AndroidManifest.xml\"\n" +
                "            line=\"7\"\n" +
                "            column=\"5\"/>\n" +
                "    </issue>\n" +
                "\n" +
                "    <issue\n" +
                "        id=\"HardcodedText\"\n" +
                "        severity=\"Warning\"\n" +
                "        message=\"[I18N] Hardcoded string &quot;Fooo&quot;, should use @string resource\"\n" +
                "        category=\"Internationalization\"\n" +
                "        priority=\"5\"\n" +
                "        summary=\"Hardcoded text\"\n" +
                "        explanation=\"Hardcoding text attributes directly in layout files is bad for several reasons:\n" +
                "\n" +
                "* When creating configuration variations (for example for landscape or portrait)you have to repeat the actual text (and keep it up to date when making changes)\n" +
                "\n" +
                "* The application cannot be translated to other languages by just adding new translations for existing string resources.\n" +
                "\n" +
                "In Android Studio and Eclipse there are quickfixes to automatically extract this hardcoded string into a resource lookup.\"\n" +
                "        errorLine1=\"        android:text=&quot;Fooo&quot; />\"\n" +
                "        errorLine2=\"        ~~~~~~~~~~~~~~~~~~~\">\n" +
                "        <location\n" +
                "            file=\"/foo/bar/Foo/res/layout/main.xml\"\n" +
                "            line=\"12\"\n" +
                "            column=\"9\"/>\n" +
                "    </issue>\n" +
                "\n" +
                "</issues>\n",
                report);

            // Make sure the XML is valid
            Document document = PositionXmlParser.parse(report);
            assertNotNull(document);
            assertEquals(2, document.getElementsByTagName("issue").getLength());
        } finally {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
    }

    public void testNonPrintableChars() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=56205
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
            XmlReporter reporter = new XmlReporter(client, file);
            Project project = Project.create(client, new File("/foo/bar/Foo"),
                    new File("/foo/bar/Foo"));

            Warning warning1 = new Warning(TypographyDetector.FRACTIONS,
                    String.format("Use fraction character %1$c (%2$s) instead of %3$s ?",
                            '\u00BC', "&#188;", "1/4"), Severity.WARNING, project);
            warning1.line = 592;
            warning1.file = new File("/foo/bar/Foo/AndroidManifest.xml");
            warning1.errorLine =
                    "        <string name=\"user_registration_name3_3\">Register 3/3</string>\n" +
                    "                                             ^";
            warning1.path = "res/values-en/common_strings.xml";
            warning1.location = Location.create(warning1.file,
                    new DefaultPosition(592, 46, -1), null);

            List<Warning> warnings = new ArrayList<Warning>();
            warnings.add(warning1);

            reporter.write(0, 2, warnings);

            String report = Files.toString(file, Charsets.UTF_8);
            assertEquals(""
                    + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<issues format=\"4\" by=\"lint unittest\">\n"
                    + "\n"
                    + "    <issue\n"
                    + "        id=\"TypographyFractions\"\n"
                    + "        severity=\"Warning\"\n"
                    + "        message=\"Use fraction character ¼ (&amp;#188;) instead of 1/4 ?\"\n"
                    + "        category=\"Usability:Typography\"\n"
                    + "        priority=\"5\"\n"
                    + "        summary=\"Fraction string can be replaced with fraction character\"\n"
                    + "        explanation=\"You can replace certain strings, such as 1/2, and 1/4, with dedicated characters for these, such as ½ (&amp;#189;) and ¼ (&amp;#188;). This can help make the text more readable.\"\n"
                    + "        url=\"http://en.wikipedia.org/wiki/Number_Forms\"\n"
                    + "        urls=\"http://en.wikipedia.org/wiki/Number_Forms\">\n"
                    + "        <location\n"
                    + "            file=\"AndroidManifest.xml\"\n"
                    + "            line=\"593\"\n"
                    + "            column=\"47\"/>\n"
                    + "    </issue>\n"
                    + "\n"
                    + "</issues>\n",
                    report);

            // Make sure the XML is valid
            Document document = PositionXmlParser.parse(report);
            assertNotNull(document);
            assertEquals(1, document.getElementsByTagName("issue").getLength());
            String explanation =  ((Element)document.getElementsByTagName("issue").item(0)).
                    getAttribute("explanation");
            assertEquals(TypographyDetector.FRACTIONS.getExplanation(TextFormat.RAW),
                    explanation);
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
