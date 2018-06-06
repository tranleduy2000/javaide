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

import static com.android.tools.lint.checks.MissingClassDetector.INNERCLASS;
import static com.android.tools.lint.checks.MissingClassDetector.INSTANTIATABLE;
import static com.android.tools.lint.checks.MissingClassDetector.MISSING;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("javadoc")
public class MissingClassDetectorTest extends AbstractCheckTest {
    private EnumSet<Scope> mScopes;
    private Set<Issue> mEnabled = new HashSet<Issue>();

    @Override
    protected Detector getDetector() {
        return new MissingClassDetector();
    }

    @Override
    protected EnumSet<Scope> getLintScope(List<File> file) {
        return mScopes;
    }

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }

    public void test() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING);
        assertEquals(
            "AndroidManifest.xml:13: Error: Class referenced in the manifest, test.pkg.TestProvider, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity android:name=\".TestProvider\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:14: Error: Class referenced in the manifest, test.pkg.TestProvider2, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <service android:name=\"test.pkg.TestProvider2\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:15: Error: Class referenced in the manifest, test.pkg.TestService, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <provider android:name=\".TestService\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:16: Error: Class referenced in the manifest, test.pkg.OnClickActivity, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <receiver android:name=\"OnClickActivity\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "AndroidManifest.xml:17: Error: Class referenced in the manifest, test.pkg.TestReceiver, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <service android:name=\"TestReceiver\" />\n" +
            "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "5 errors, 0 warnings\n",

            lintProject(
                "bytecode/AndroidManifestWrongRegs.xml=>AndroidManifest.xml",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "bytecode/.classpath=>.classpath"
            ));
    }

    public void testIncrementalInManifest() throws Exception {
        mScopes = Scope.MANIFEST_SCOPE;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
                "No warnings.",

                lintProject(
                    "bytecode/AndroidManifestWrongRegs.xml=>AndroidManifest.xml",
                    "bytecode/.classpath=>.classpath"
                ));
    }

    public void testNoWarningBeforeBuild() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/AndroidManifestWrongRegs.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath"
            ));
    }

    public void testOkClasses() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/AndroidManifestRegs.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
                "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
                "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
                "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
                "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
                "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
                "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class",
                "bytecode/TestReceiver.java.txt=>src/test/pkg/TestReceiver.java",
                "bytecode/TestReceiver.class.data=>bin/classes/test/pkg/TestReceiver.class"
            ));
    }

    public void testOkLibraries() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/AndroidManifestRegs.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "bytecode/classes.jar=>libs/classes.jar"
            ));
    }

    public void testLibraryProjects() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        File master = getProjectDir("MasterProject",
                // Master project
                "bytecode/AndroidManifestRegs.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
                "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
                "bytecode/.classpath=>.classpath"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
                "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
                "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
                "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
                "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class"
                // Missing TestReceiver: Test should complain about just that class
        );
        assertEquals(""
                + "MasterProject/AndroidManifest.xml:32: Error: Class referenced in the manifest, test.pkg.TestReceiver, was not found in the project or the libraries [MissingRegistered]\n"
                + "        <receiver android:name=\"TestReceiver\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

           checkLint(Arrays.asList(master, library)));
    }

    public void testIndirectLibraryProjects() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        File master = getProjectDir("MasterProject",
                // Master project
                "bytecode/AndroidManifestRegs.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
                "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
                "bytecode/.classpath=>.classpath"
        );
        File library2 = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest2.xml=>AndroidManifest.xml",
                "multiproject/library2.properties=>project.properties"
        );
        File library = getProjectDir("RealLibrary",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
                "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
                "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
                "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
                "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class"
                // Missing TestReceiver: Test should complain about just that class
        );
        assertEquals(""
                + "MasterProject/AndroidManifest.xml:32: Error: Class referenced in the manifest, test.pkg.TestReceiver, was not found in the project or the libraries [MissingRegistered]\n"
                + "        <receiver android:name=\"TestReceiver\" />\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                checkLint(Arrays.asList(master, library2, library)));
    }

    public void testInnerClassStatic() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "src/test/pkg/Foo.java:8: Error: This inner class should be static (test.pkg.Foo.Baz) [Instantiatable]\n" +
            "    public class Baz extends Activity {\n" +
            "    ^\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                "registration/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "registration/Foo.java.txt=>src/test/pkg/Foo.java",
                "registration/Foo.class.data=>bin/classes/test/pkg/Foo.class",
                "registration/Foo$Bar.class.data=>bin/classes/test/pkg/Foo$Bar.class",
                "registration/Foo$Baz.class.data=>bin/classes/test/pkg/Foo$Baz.class"
            ));
    }

    public void testInnerClassPublic() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "src/test/pkg/Foo/Bar.java:6: Error: The default constructor must be public [Instantiatable]\n" +
            "    private Bar() {\n" +
            "    ^\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                "registration/AndroidManifestInner.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "registration/Bar.java.txt=>src/test/pkg/Foo/Bar.java",
                "registration/Bar.class.data=>bin/classes/test/pkg/Foo/Bar.class"
            ));
    }

    public void testInnerClass() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "AndroidManifest.xml:14: Error: Class referenced in the manifest, test.pkg.Foo.Bar, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity\n" +
            "        ^\n" +
            "AndroidManifest.xml:23: Error: Class referenced in the manifest, test.pkg.Foo.Baz, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity\n" +
            "        ^\n" +
            "2 errors, 0 warnings\n",

            lintProject(
                "registration/AndroidManifest.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "registration/Foo.java.txt=>src/test/pkg/Foo.java"
            ));
    }

    public void testInnerClass2() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "AndroidManifest.xml:14: Error: Class referenced in the manifest, test.pkg.Foo.Bar, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity\n" +
            "        ^\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                "registration/AndroidManifestInner.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "registration/Bar.java.txt=>src/test/pkg/Foo/Bar.java"
            ));
    }

    public void testWrongSeparator1() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "AndroidManifest.xml:14: Error: Class referenced in the manifest, test.pkg.Foo.Bar, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity\n" +
            "        ^\n" +
            "1 errors, 0 warnings\n",

            lintProject(
                "registration/AndroidManifestWrong.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "registration/Bar.java.txt=>src/test/pkg/Foo/Bar.java"
            ));
    }

    public void testWrongSeparator2() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "AndroidManifest.xml:14: Error: Class referenced in the manifest, test.pkg.Foo.Bar, was not found in the project or the libraries [MissingRegistered]\n" +
            "        <activity\n" +
            "        ^\n" +
            "AndroidManifest.xml:15: Warning: Use '$' instead of '.' for inner classes (or use only lowercase letters in package names); replace \".Foo.Bar\" with \".Foo$Bar\" [InnerclassSeparator]\n" +
            "            android:name=\".Foo.Bar\"\n" +
            "            ~~~~~~~~~~~~~~~~~~~~~~~\n" +
            "1 errors, 1 warnings\n",

            lintProject(
                "registration/AndroidManifestWrong2.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "apicheck/ApiCallTest.class.data=>bin/classes/foo/bar/ApiCallTest.class",
                "registration/Bar.java.txt=>src/test/pkg/Foo/Bar.java"
            ));
    }

    public void testNoClassesWithLibraries() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(
            "No warnings.",

            lintProject(
                "bytecode/AndroidManifestWrongRegs.xml=>AndroidManifest.xml",
                "bytecode/.classpath=>.classpath",
                "bytecode/GetterTest.jar.data=>libs/foo.jar"
            ));
    }

    public void testFragment() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
            + "res/layout/fragment2.xml:7: Error: Class referenced in the layout file, my.app.Fragment, was not found in the project or the libraries [MissingRegistered]\n"
            + "    <fragment\n"
            + "    ^\n"
            + "res/layout/fragment2.xml:12: Error: Class referenced in the layout file, my.app.MyView, was not found in the project or the libraries [MissingRegistered]\n"
            + "    <view\n"
            + "    ^\n"
            + "res/layout/fragment2.xml:17: Error: Class referenced in the layout file, my.app.Fragment2, was not found in the project or the libraries [MissingRegistered]\n"
            + "    <fragment\n"
            + "    ^\n"
            + "src/test/pkg/Foo/Bar.java:6: Error: The default constructor must be public [Instantiatable]\n"
            + "    private Bar() {\n"
            + "    ^\n"
            + "4 errors, 0 warnings\n",

        lintProject(
            "bytecode/AndroidManifestRegs.xml=>AndroidManifest.xml",
            "bytecode/.classpath=>.classpath",
            "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
            "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class",
            "bytecode/TestService.java.txt=>src/test/pkg/TestService.java",
            "bytecode/TestService.class.data=>bin/classes/test/pkg/TestService.class",
            "bytecode/TestProvider.java.txt=>src/test/pkg/TestProvider.java",
            "bytecode/TestProvider.class.data=>bin/classes/test/pkg/TestProvider.class",
            "bytecode/TestProvider2.java.txt=>src/test/pkg/TestProvider2.java",
            "bytecode/TestProvider2.class.data=>bin/classes/test/pkg/TestProvider2.class",
            "bytecode/TestReceiver.java.txt=>src/test/pkg/TestReceiver.java",
            "bytecode/TestReceiver.class.data=>bin/classes/test/pkg/TestReceiver.class",
            "registration/Foo.java.txt=>src/test/pkg/Foo.java",
            "registration/Foo.class.data=>bin/classes/test/pkg/Foo.class",
            "registration/Bar.java.txt=>src/test/pkg/Foo/Bar.java",
            "registration/Bar.class.data=>bin/classes/test/pkg/Foo/Bar.class",

            "res/layout/fragment2.xml"
        ));
    }

    public void testAnalytics() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
                + "res/values/analytics.xml:13: Error: Class referenced in the analytics file, com.example.app.BaseActivity, was not found in the project or the libraries [MissingRegistered]\n"
                + "  <string name=\"com.example.app.BaseActivity\">Home</string>\n"
                + "  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "res/values/analytics.xml:14: Error: Class referenced in the analytics file, com.example.app.PrefsActivity, was not found in the project or the libraries [MissingRegistered]\n"
                + "  <string name=\"com.example.app.PrefsActivity\">Preferences</string>\n"
                + "  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",

            lintProject(
                "bytecode/.classpath=>.classpath",
                "res/values/analytics.xml",
                "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class"
            ));
    }

    public void testCustomView() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
                + "res/layout/customview.xml:21: Error: Class referenced in the layout file, foo.bar.Baz, was not found in the project or the libraries [MissingRegistered]\n"
                + "    <foo.bar.Baz\n"
                + "    ^\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "res/layout/customview.xml",
                        "bytecode/OnClickActivity.java.txt=>src/test/pkg/OnClickActivity.java",
                        "bytecode/OnClickActivity.class.data=>bin/classes/test/pkg/OnClickActivity.class"
                ));
    }

    public void testCustomViewInCapitalizedPackage() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "res/layout/customview3.xml",
                        "bytecode/CustomView3.java.txt=>src/test/bytecode/CustomView3.java",
                        "bytecode/CustomView3.class.data=>bin/classes/test/bytecode/CustomView3.class"
                ));
    }

    public void testCustomViewNotReferenced() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "bytecode/CustomView3.java.txt=>src/test/bytecode/CustomView3.java",
                        "bytecode/CustomView3.class.data=>bin/classes/test/bytecode/CustomView3.class"
                ));
    }


    public void testMissingClass() throws Exception {
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);
        assertEquals(""
                + "No warnings.",

                lintProject(
                        "bytecode/.classpath=>.classpath",
                        "bytecode/user_prefs_fragment.xml=>res/layout/user_prefs_fragment.xml",
                        "bytecode/ViewAndUpdatePreferencesActivity$UserPreferenceFragment.class.data=>bin/classes/course/examples/DataManagement/PreferenceActivity/ViewAndUpdatePreferencesActivity$UserPreferenceFragment.class"
                ));
    }

    public void testFragments() throws Exception {
        mScopes = Scope.MANIFEST_SCOPE;
        mEnabled = Sets.newHashSet(MISSING, INSTANTIATABLE, INNERCLASS);

        // Ensure that we don't do instantiation checks here since they are handled by
        // the FragmentDetector
        assertEquals(
                "No warnings.",

                lintProject(
                        "bytecode/FragmentTest$Fragment1.class.data=>bin/classes/test/pkg/FragmentTest$Fragment1.class",
                        "bytecode/FragmentTest$Fragment2.class.data=>bin/classes/test/pkg/FragmentTest$Fragment2.class",
                        "bytecode/FragmentTest$Fragment3.class.data=>bin/classes/test/pkg/FragmentTest$Fragment3.class",
                        "bytecode/FragmentTest$Fragment4.class.data=>bin/classes/test/pkg/FragmentTest$Fragment4.class",
                        "bytecode/FragmentTest$Fragment5.class.data=>bin/classes/test/pkg/FragmentTest$Fragment5.class",
                        "bytecode/FragmentTest$Fragment6.class.data=>bin/classes/test/pkg/FragmentTest$Fragment6.class",
                        "bytecode/FragmentTest$NotAFragment.class.data=>bin/classes/test/pkg/FragmentTest$NotAFragment.class",
                        "bytecode/FragmentTest.java.txt=>src/test/pkg/FragmentTest.java"));
    }

    public void testHeaders() throws Exception {
        // See https://code.google.com/p/android/issues/detail?id=51851
        mScopes = null;
        mEnabled = Sets.newHashSet(MISSING, INNERCLASS);
        assertEquals(""
                + "res/xml/prefs_headers.xml:3: Error: Class referenced in the preference header file, foo.bar.MyFragment.Missing, was not found in the project or the libraries [MissingRegistered]\n"
                + "<header\n"
                + "^\n"
                + "1 errors, 0 warnings\n",

                lintProject(
                        "bytecode/FragmentTest$Fragment1.class.data=>bin/classes/test/pkg/FragmentTest$Fragment1.class",
                        "bytecode/FragmentTest$Fragment2.class.data=>bin/classes/test/pkg/FragmentTest$Fragment2.class",
                        "bytecode/FragmentTest$Fragment3.class.data=>bin/classes/test/pkg/FragmentTest$Fragment3.class",
                        "bytecode/FragmentTest$Fragment4.class.data=>bin/classes/test/pkg/FragmentTest$Fragment4.class",
                        "bytecode/FragmentTest$Fragment5.class.data=>bin/classes/test/pkg/FragmentTest$Fragment5.class",
                        "bytecode/FragmentTest$Fragment6.class.data=>bin/classes/test/pkg/FragmentTest$Fragment6.class",
                        "bytecode/FragmentTest$NotAFragment.class.data=>bin/classes/test/pkg/FragmentTest$NotAFragment.class",
                        "bytecode/FragmentTest.java.txt=>src/test/pkg/FragmentTest.java",
                        "bytecode/.classpath=>.classpath",
                        "res/xml/prefs_headers.xml"));
    }


    public void testGetOldValue() {
        assertEquals(".Foo.Bar", MissingClassDetector.getOldValue(INNERCLASS,
                "Use '$' instead of '.' for inner classes (or use only lowercase letters in package names); replace \".Foo.Bar\" with \".Foo$Bar\" [InnerclassSeparator]",
                TEXT));
    }

    public void testGetNewValue() {
        assertEquals(".Foo$Bar", MissingClassDetector.getNewValue(INNERCLASS,
                "Use '$' instead of '.' for inner classes (or use only lowercase letters in package names); replace \".Foo.Bar\" with \".Foo$Bar\" [InnerclassSeparator]",
                TEXT));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        if (issue == INNERCLASS) {
            assertNotNull(message, MissingClassDetector.getOldValue(issue, message, TEXT));
            assertNotNull(message, MissingClassDetector.getNewValue(issue, message, TEXT));
        }
    }
}
