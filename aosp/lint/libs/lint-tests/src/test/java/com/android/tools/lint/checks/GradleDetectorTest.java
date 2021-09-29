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

import static com.android.SdkConstants.GRADLE_PLUGIN_MINIMUM_VERSION;
import static com.android.SdkConstants.GRADLE_PLUGIN_RECOMMENDED_VERSION;
import static com.android.tools.lint.checks.GradleDetector.ACCIDENTAL_OCTAL;
import static com.android.tools.lint.checks.GradleDetector.COMPATIBILITY;
import static com.android.tools.lint.checks.GradleDetector.DEPENDENCY;
import static com.android.tools.lint.checks.GradleDetector.DEPRECATED;
import static com.android.tools.lint.checks.GradleDetector.GRADLE_GETTER;
import static com.android.tools.lint.checks.GradleDetector.GRADLE_PLUGIN_COMPATIBILITY;
import static com.android.tools.lint.checks.GradleDetector.PATH;
import static com.android.tools.lint.checks.GradleDetector.PLUS;
import static com.android.tools.lint.checks.GradleDetector.REMOTE_VERSION;
import static com.android.tools.lint.checks.GradleDetector.STRING_INTEGER;
import static com.android.tools.lint.checks.GradleDetector.getNamedDependency;
import static com.android.tools.lint.checks.GradleDetector.getNewValue;
import static com.android.tools.lint.checks.GradleDetector.getOldValue;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.Dependencies;
import com.android.builder.model.MavenCoordinates;
import com.android.builder.model.Variant;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.DefaultPosition;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.utils.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>NOTE</b>: Most GradleDetector unit tests are in the Studio plugin, as tests
 * for IntellijGradleDetector
 */
public class GradleDetectorTest extends AbstractCheckTest {

    private File mSdkDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if (mSdkDir != null) {
            deleteFile(mSdkDir);
            mSdkDir = null;
        }
    }

    /** Creates a mock SDK installation structure, containing a fixed set of dependencies */
    private File getMockSupportLibraryInstallation() {
        if (mSdkDir == null) {
            // Make fake SDK "installation" such that we can predict the set
            // of Maven repositories discovered by this test
            mSdkDir = Files.createTempDir();

            String[] paths = new String[]{
                    // Android repository
                    "extras/android/m2repository/com/android/support/appcompat-v7/18.0.0/appcompat-v7-18.0.0.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/19.0.0/appcompat-v7-19.0.0.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/19.0.1/appcompat-v7-19.0.1.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/19.1.0/appcompat-v7-19.1.0.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/20.0.0/appcompat-v7-20.0.0.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/21.0.0/appcompat-v7-21.0.0.aar",
                    "extras/android/m2repository/com/android/support/appcompat-v7/21.0.2/appcompat-v7-21.0.2.aar",
                    "extras/android/m2repository/com/android/support/cardview-v7/21.0.0/cardview-v7-21.0.0.aar",
                    "extras/android/m2repository/com/android/support/cardview-v7/21.0.2/cardview-v7-21.0.2.aar",
                    "extras/android/m2repository/com/android/support/support-v13/20.0.0/support-v13-20.0.0.aar",
                    "extras/android/m2repository/com/android/support/support-v13/21.0.0/support-v13-21.0.0.aar",
                    "extras/android/m2repository/com/android/support/support-v13/21.0.2/support-v13-21.0.2.aar",
                    "extras/android/m2repository/com/android/support/support-v4/20.0.0/support-v4-20.0.0.aar",
                    "extras/android/m2repository/com/android/support/support-v4/21.0.0/support-v4-21.0.0.aar",
                    "extras/android/m2repository/com/android/support/support-v4/21.0.2/support-v4-21.0.2.aar",

                    // Google repository
                    "extras/google/m2repository/com/google/android/gms/play-services/3.1.36/play-services-3.1.36.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/3.1.59/play-services-3.1.59.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/3.2.25/play-services-3.2.25.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/3.2.65/play-services-3.2.65.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/4.0.30/play-services-4.0.30.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/4.1.32/play-services-4.1.32.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/4.2.42/play-services-4.2.42.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/4.3.23/play-services-4.3.23.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/4.4.52/play-services-4.4.52.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/5.0.89/play-services-5.0.89.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/6.1.11/play-services-6.1.11.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services/6.1.71/play-services-6.1.71.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services-wearable/5.0.77/play-services-wearable-5.0.77.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services-wearable/6.1.11/play-services-wearable-6.1.11.aar",
                    "extras/google/m2repository/com/google/android/gms/play-services-wearable/6.1.71/play-services-wearable-6.1.71.aar",
                    "extras/google/m2repository/com/google/android/support/wearable/1.0.0/wearable-1.0.0.aar"
            };

            for (String path : paths) {
                File file = new File(mSdkDir, path.replace('/', File.separatorChar));
                File parent = file.getParentFile();
                if (!parent.exists()) {
                    boolean ok = parent.mkdirs();
                    assertTrue(ok);
                }
                try {
                    boolean created = file.createNewFile();
                    assertTrue(created);
                } catch (IOException e) {
                    fail(e.toString());
                }
            }
        }

        return mSdkDir;
    }

    public void testGetOldValue() {
        assertEquals("11.0.2", getOldValue(DEPENDENCY,
                "A newer version of com.google.guava:guava than 11.0.2 is available: 17.0.0",
                TEXT));
        assertNull(getOldValue(DEPENDENCY, "Bogus", TEXT));
        assertNull(getOldValue(DEPENDENCY, "bogus", TEXT));
        // targetSdkVersion 20, compileSdkVersion 19: Should replace targetVersion 20 with 19
        assertEquals("20", getOldValue(DEPENDENCY,
                "The targetSdkVersion (20) should not be higher than the compileSdkVersion (19)",
                TEXT));
        assertEquals("'19'", getOldValue(STRING_INTEGER,
                "Use an integer rather than a string here (replace '19' with just 19)", TEXT));
        assertEquals("android", getOldValue(DEPRECATED,
                "'android' is deprecated; use 'com.android.application' instead", TEXT));
        assertEquals("android-library", getOldValue(DEPRECATED,
                "'android-library' is deprecated; use 'com.android.library' instead", TEXT));
        assertEquals("packageName", getOldValue(DEPRECATED,
                "Deprecated: Replace 'packageName' with 'applicationId'", TEXT));
        assertEquals("packageNameSuffix", getOldValue(DEPRECATED,
                "Deprecated: Replace 'packageNameSuffix' with 'applicationIdSuffix'", TEXT));
        assertEquals("18.0.0", getOldValue(DEPENDENCY,
                "Old buildToolsVersion 18.0.0; recommended version is 19.1 or later", TEXT));
    }

    public void testGetNewValue() {
        assertEquals("17.0.0", getNewValue(DEPENDENCY,
                "A newer version of com.google.guava:guava than 11.0.2 is available: 17.0.0",
                TEXT));
        assertNull(getNewValue(DEPENDENCY,
                "A newer version of com.google.guava:guava than 11.0.2 is available", TEXT));
        assertNull(getNewValue(DEPENDENCY, "bogus", TEXT));
        // targetSdkVersion 20, compileSdkVersion 19: Should replace targetVersion 20 with 19
        assertEquals("19", getNewValue(DEPENDENCY,
                "The targetSdkVersion (20) should not be higher than the compileSdkVersion (19)",
                TEXT));
        assertEquals("19", getNewValue(STRING_INTEGER,
                "Use an integer rather than a string here (replace '19' with just 19)", TEXT));
        assertEquals("com.android.application", getNewValue(DEPRECATED,
                "'android' is deprecated; use 'com.android.application' instead", TEXT));
        assertEquals("com.android.library", getNewValue(DEPRECATED,
                "'android-library' is deprecated; use 'com.android.library' instead", TEXT));
        assertEquals("applicationId", getNewValue(DEPRECATED,
                "Deprecated: Replace 'packageName' with 'applicationId'", TEXT));
        assertEquals("applicationIdSuffix", getNewValue(DEPRECATED,
                "Deprecated: Replace 'packageNameSuffix' with 'applicationIdSuffix'", TEXT));
        assertEquals("19.1", getNewValue(DEPENDENCY,
                "Old buildToolsVersion 18.0.0; recommended version is 19.1 or later", TEXT));
    }

    public void test() throws Exception {
        mEnabled = Sets.newHashSet(COMPATIBILITY, DEPRECATED, DEPENDENCY, PLUS);
        assertEquals(""
            + "build.gradle:25: Error: This support library should not use a lower version (13) than the targetSdkVersion (17) [GradleCompatible]\n"
            + "    compile 'com.android.support:appcompat-v7:13.0.0'\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "build.gradle:1: Warning: 'android' is deprecated; use 'com.android.application' instead [GradleDeprecated]\n"
            + "apply plugin: 'android'\n"
            + "~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "build.gradle:5: Warning: Old buildToolsVersion 19.0.0; recommended version is 19.1 or later [GradleDependency]\n"
            + "    buildToolsVersion \"19.0.0\"\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "build.gradle:24: Warning: A newer version of com.google.guava:guava than 11.0.2 is available: 18.0 [GradleDependency]\n"
            + "    freeCompile 'com.google.guava:guava:11.0.2'\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "build.gradle:25: Warning: A newer version of com.android.support:appcompat-v7 than 13.0.0 is available: 21.0.2 [GradleDependency]\n"
            + "    compile 'com.android.support:appcompat-v7:13.0.0'\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "build.gradle:23: Warning: Avoid using + in version numbers; can lead to unpredictable and unrepeatable builds (com.android.support:appcompat-v7:+) [GradleDynamicVersion]\n"
            + "    compile 'com.android.support:appcompat-v7:+'\n"
            + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
            + "1 errors, 5 warnings\n",

            lintProject("gradle/Dependencies.gradle=>build.gradle"));
    }

    public void testCompatibility() throws Exception {
        mEnabled = Collections.singleton(COMPATIBILITY);
        assertEquals(""
                + "build.gradle:16: Error: This support library should not use a lower version (18) than the targetSdkVersion (19) [GradleCompatible]\n"
                + "    compile 'com.android.support:support-v4:18.0.0'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject("gradle/Compatibility.gradle=>build.gradle"));
    }

    public void testIncompatiblePlugin() throws Exception {
        mEnabled = Collections.singleton(GRADLE_PLUGIN_COMPATIBILITY);
        assertEquals(""
                + "build.gradle:6: Error: You must use a newer version of the Android Gradle plugin. The minimum supported version is " + GRADLE_PLUGIN_MINIMUM_VERSION + " and the recommended version is " + GRADLE_PLUGIN_RECOMMENDED_VERSION + " [AndroidGradlePluginVersion]\n"
                + "    classpath 'com.android.tools.build:gradle:0.1.0'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject("gradle/IncompatiblePlugin.gradle=>build.gradle"));
    }

    public void testSetter() throws Exception {
        mEnabled = Collections.singleton(GRADLE_GETTER);
        assertEquals(""
                        + "build.gradle:18: Error: Bad method name: pick a unique method name which does not conflict with the implicit getters for the defaultConfig properties. For example, try using the prefix compute- instead of get-. [GradleGetter]\n"
                        + "        versionCode getVersionCode\n"
                        + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "build.gradle:19: Error: Bad method name: pick a unique method name which does not conflict with the implicit getters for the defaultConfig properties. For example, try using the prefix compute- instead of get-. [GradleGetter]\n"
                        + "        versionName getVersionName\n"
                        + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "2 errors, 0 warnings\n",

                lintProject("gradle/Setter.gradle=>build.gradle"));
    }

    public void testDependencies() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals(""
                + "build.gradle:5: Warning: Old buildToolsVersion 19.0.0; recommended version is 19.1 or later [GradleDependency]\n"
                + "    buildToolsVersion \"19.0.0\"\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "build.gradle:24: Warning: A newer version of com.google.guava:guava than 11.0.2 is available: 18.0 [GradleDependency]\n"
                + "    freeCompile 'com.google.guava:guava:11.0.2'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "build.gradle:25: Warning: A newer version of com.android.support:appcompat-v7 than 13.0.0 is available: 21.0.2 [GradleDependency]\n"
                + "    compile 'com.android.support:appcompat-v7:13.0.0'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 3 warnings\n",

                lintProject("gradle/Dependencies.gradle=>build.gradle"));
    }

    public void testLongHandDependencies() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals(""
                + "build.gradle:9: Warning: A newer version of com.android.support:support-v4 than 19.0 is available: 21.0.2 [GradleDependency]\n"
                + "    compile group: 'com.android.support', name: 'support-v4', version: '19.0'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject("gradle/DependenciesProps.gradle=>build.gradle"));
    }

    public void testDependenciesMinSdkVersion() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals(""
                + "build.gradle:13: Warning: Using the appcompat library when minSdkVersion >= 14 and compileSdkVersion < 21 is not necessary [GradleDependency]\n"
                + "    compile 'com.android.support:appcompat-v7:+'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject("gradle/Dependencies14.gradle=>build.gradle"));
    }

    public void testDependenciesMinSdkVersionLollipop() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals("No warnings.",
                lintProject("gradle/Dependencies14_21.gradle=>build.gradle"));
    }

    public void testDependenciesNoMicroVersion() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=77594
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals(""
                + "build.gradle:13: Warning: A newer version of com.google.code.gson:gson than 2.2 is available: 2.3 [GradleDependency]\n"
                + "    compile 'com.google.code.gson:gson:2.2'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",

                lintProject("gradle/DependenciesGson.gradle=>build.gradle"));
    }

    public void testPaths() throws Exception {
        mEnabled = Collections.singleton(PATH);
        assertEquals(""
                        + "build.gradle:4: Warning: Do not use Windows file separators in .gradle files; use / instead [GradlePath]\n"
                        + "    compile files('my\\\\libs\\\\http.jar')\n"
                        + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "build.gradle:5: Warning: Avoid using absolute paths in .gradle files [GradlePath]\n"
                        + "    compile files('/libs/android-support-v4.jar')\n"
                        + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 2 warnings\n",

                lintProject("gradle/Paths.gradle=>build.gradle"));
    }

    public void testIdSuffix() throws Exception {
        mEnabled = Collections.singleton(PATH);
        assertEquals(""
                        + "build.gradle:6: Warning: Package suffix should probably start with a \".\" [GradlePath]\n"
                        + "            applicationIdSuffix \"debug\"\n"
                        + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",

                lintProject("gradle/IdSuffix.gradle=>build.gradle"));
    }

    public void testPackage() throws Exception {
        mEnabled = Collections.singleton(DEPRECATED);
        assertEquals(""
                + "build.gradle:5: Warning: Deprecated: Replace 'packageName' with 'applicationId' [GradleDeprecated]\n"
                + "        packageName 'my.pkg'\n"
                + "        ~~~~~~~~~~~~~~~~~~~~\n"
                + "build.gradle:9: Warning: Deprecated: Replace 'packageNameSuffix' with 'applicationIdSuffix' [GradleDeprecated]\n"
                + "            packageNameSuffix \".debug\"\n"
                + "            ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject("gradle/Package.gradle=>build.gradle"));
    }

    public void testPlus() throws Exception {
        mEnabled = Collections.singleton(PLUS);
        assertEquals(""
                + "build.gradle:9: Warning: Avoid using + in version numbers; can lead to unpredictable and unrepeatable builds (com.android.support:appcompat-v7:+) [GradleDynamicVersion]\n"
                + "    compile 'com.android.support:appcompat-v7:+'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "build.gradle:10: Warning: Avoid using + in version numbers; can lead to unpredictable and unrepeatable builds (com.android.support:support-v4:21.0.+) [GradleDynamicVersion]\n"
                + "    compile group: 'com.android.support', name: 'support-v4', version: '21.0.+'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n",

                lintProject("gradle/Plus.gradle=>build.gradle"));
    }

    public void testStringInt() throws Exception {
        mEnabled = Collections.singleton(STRING_INTEGER);
        assertEquals(""
                        + "build.gradle:4: Error: Use an integer rather than a string here (replace '19' with just 19) [StringShouldBeInt]\n"
                        + "    compileSdkVersion '19'\n"
                        + "    ~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "build.gradle:7: Error: Use an integer rather than a string here (replace '8' with just 8) [StringShouldBeInt]\n"
                        + "        minSdkVersion '8'\n"
                        + "        ~~~~~~~~~~~~~~~~~\n"
                        + "build.gradle:8: Error: Use an integer rather than a string here (replace '16' with just 16) [StringShouldBeInt]\n"
                        + "        targetSdkVersion '16'\n"
                        + "        ~~~~~~~~~~~~~~~~~~~~~\n"
                        + "3 errors, 0 warnings\n",

                lintProject("gradle/StringInt.gradle=>build.gradle"));
    }

    public void testSuppressLine2() throws Exception {
        mEnabled = null;
        assertEquals("No warnings.",

                lintProject("gradle/SuppressLine2.gradle=>build.gradle"));
    }

    public void testDeprecatedPluginId() throws Exception {
        mEnabled = Sets.newHashSet(DEPRECATED);
        assertEquals(""
                        + "build.gradle:4: Warning: 'android' is deprecated; use 'com.android.application' instead [GradleDeprecated]\n"
                        + "apply plugin: 'android'\n"
                        + "~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "build.gradle:5: Warning: 'android-library' is deprecated; use 'com.android.library' instead [GradleDeprecated]\n"
                        + "apply plugin: 'android-library'\n"
                        + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 2 warnings\n",

                lintProject("gradle/DeprecatedPluginId.gradle=>build.gradle"));
    }

    public void testIgnoresGStringsInDependencies() throws Exception {
        mEnabled = null;
        assertEquals("No warnings.",

                lintProject("gradle/IgnoresGStringsInDependencies.gradle=>build.gradle"));
    }

    public void testAccidentalOctal() throws Exception {
        mEnabled = Collections.singleton(ACCIDENTAL_OCTAL);
        assertEquals(""
                + "build.gradle:13: Error: The leading 0 turns this number into octal which is probably not what was intended (interpreted as 8) [AccidentalOctal]\n"
                + "        versionCode 010\n"
                + "        ~~~~~~~~~~~~~~~\n"
                + "build.gradle:16: Error: The leading 0 turns this number into octal which is probably not what was intended (and it is not a valid octal number) [AccidentalOctal]\n"
                + "        versionCode 01 // line suffix comments are not handled correctly\n"
                + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "2 errors, 0 warnings\n",

                lintProject("gradle/AccidentalOctal.gradle=>build.gradle"));
    }

    public void testBadPlayServicesVersion() throws Exception {
        mEnabled = Collections.singleton(COMPATIBILITY);
        assertEquals(""
                + "build.gradle:5: Error: Version 5.2.08 should not be used; the app can not be published with this version. Use version 6.1.71 instead. [GradleCompatible]\n"
                + "    compile 'com.google.android.gms:play-services:5.2.08'\n"
                + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings\n",

                lintProject("gradle/PlayServices.gradle=>build.gradle"));
    }

    public void testRemoteVersions() throws Exception {
        mEnabled = Collections.singleton(REMOTE_VERSION);
        try {
            HashMap<String, String> data = Maps.newHashMap();
            GradleDetector.sMockData = data;
            data.put("http://search.maven.org/solrsearch/select?q=g:%22joda-time%22+AND+a:%22joda-time%22&core=gav&rows=1&wt=json",
                    "{\"responseHeader\":{\"status\":0,\"QTime\":1,\"params\":{\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"sort\":\"score desc,timestamp desc,g asc,a asc,v desc\",\"indent\":\"off\",\"q\":\"g:\\\"joda-time\\\" AND a:\\\"joda-time\\\"\",\"core\":\"gav\",\"wt\":\"json\",\"rows\":\"1\",\"version\":\"2.2\"}},\"response\":{\"numFound\":17,\"start\":0,\"docs\":[{\"id\":\"joda-time:joda-time:2.3\",\"g\":\"joda-time\",\"a\":\"joda-time\",\"v\":\"2.3\",\"p\":\"jar\",\"timestamp\":1376674285000,\"tags\":[\"replace\",\"time\",\"library\",\"date\",\"handling\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\".pom\"]}]}}");
            data.put("http://search.maven.org/solrsearch/select?q=g:%22com.squareup.dagger%22+AND+a:%22dagger%22&core=gav&rows=1&wt=json",
                    "{\"responseHeader\":{\"status\":0,\"QTime\":1,\"params\":{\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"sort\":\"score desc,timestamp desc,g asc,a asc,v desc\",\"indent\":\"off\",\"q\":\"g:\\\"com.squareup.dagger\\\" AND a:\\\"dagger\\\"\",\"core\":\"gav\",\"wt\":\"json\",\"rows\":\"1\",\"version\":\"2.2\"}},\"response\":{\"numFound\":5,\"start\":0,\"docs\":[{\"id\":\"com.squareup.dagger:dagger:1.2.1\",\"g\":\"com.squareup.dagger\",\"a\":\"dagger\",\"v\":\"1.2.1\",\"p\":\"jar\",\"timestamp\":1392614597000,\"tags\":[\"dependency\",\"android\",\"injector\",\"java\",\"fast\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\"-tests.jar\",\".jar\",\".pom\"]}]}}");

            assertEquals(""
                    + "build.gradle:9: Warning: A newer version of joda-time:joda-time than 2.1 is available: 2.3 [NewerVersionAvailable]\n"
                    + "    compile 'joda-time:joda-time:2.1'\n"
                    + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "build.gradle:10: Warning: A newer version of com.squareup.dagger:dagger than 1.2.0 is available: 1.2.1 [NewerVersionAvailable]\n"
                    + "    compile 'com.squareup.dagger:dagger:1.2.0'\n"
                    + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "0 errors, 2 warnings\n",

                    lintProject("gradle/RemoteVersions.gradle=>build.gradle"));
        } finally {
            GradleDetector.sMockData = null;
        }
    }

    public void testRemoteVersionsWithPreviews() throws Exception {
        // If the most recent version is a rc version, query for all versions
        mEnabled = Collections.singleton(REMOTE_VERSION);
        try {
            HashMap<String, String> data = Maps.newHashMap();
            GradleDetector.sMockData = data;
            data.put("http://search.maven.org/solrsearch/select?q=g:%22com.google.guava%22+AND+a:%22guava%22&core=gav&rows=1&wt=json",
                    "{\"responseHeader\":{\"status\":0,\"QTime\":0,\"params\":{\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"sort\":\"score desc,timestamp desc,g asc,a asc,v desc\",\"indent\":\"off\",\"q\":\"g:\\\"com.google.guava\\\" AND a:\\\"guava\\\"\",\"core\":\"gav\",\"wt\":\"json\",\"rows\":\"1\",\"version\":\"2.2\"}},\"response\":{\"numFound\":38,\"start\":0,\"docs\":[{\"id\":\"com.google.guava:guava:18.0-rc1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"18.0-rc1\",\"p\":\"bundle\",\"timestamp\":1407266204000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]}]}}");
            data.put("http://search.maven.org/solrsearch/select?q=g:%22com.google.guava%22+AND+a:%22guava%22&core=gav&wt=json",
                    "{\"responseHeader\":{\"status\":0,\"QTime\":1,\"params\":{\"fl\":\"id,g,a,v,p,ec,timestamp,tags\",\"sort\":\"score desc,timestamp desc,g asc,a asc,v desc\",\"indent\":\"off\",\"q\":\"g:\\\"com.google.guava\\\" AND a:\\\"guava\\\"\",\"core\":\"gav\",\"wt\":\"json\",\"version\":\"2.2\"}},\"response\":{\"numFound\":38,\"start\":0,\"docs\":[{\"id\":\"com.google.guava:guava:18.0-rc1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"18.0-rc1\",\"p\":\"bundle\",\"timestamp\":1407266204000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:17.0\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"17.0\",\"p\":\"bundle\",\"timestamp\":1398199666000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:17.0-rc2\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"17.0-rc2\",\"p\":\"bundle\",\"timestamp\":1397162341000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:17.0-rc1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"17.0-rc1\",\"p\":\"bundle\",\"timestamp\":1396985408000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:16.0.1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"16.0.1\",\"p\":\"bundle\",\"timestamp\":1391467528000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:16.0\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"16.0\",\"p\":\"bundle\",\"timestamp\":1389995088000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:16.0-rc1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"16.0-rc1\",\"p\":\"bundle\",\"timestamp\":1387495574000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"dependency\",\"that\",\"more\",\"utility\",\"guava\",\"javax\",\"only\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:15.0\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"15.0\",\"p\":\"bundle\",\"timestamp\":1378497169000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"inject\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"that\",\"more\",\"utility\",\"guava\",\"dependencies\",\"javax\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\",\"-cdi1.0.jar\"]},{\"id\":\"com.google.guava:guava:15.0-rc1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"15.0-rc1\",\"p\":\"bundle\",\"timestamp\":1377542588000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"inject\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"that\",\"more\",\"utility\",\"guava\",\"dependencies\",\"javax\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]},{\"id\":\"com.google.guava:guava:14.0.1\",\"g\":\"com.google.guava\",\"a\":\"guava\",\"v\":\"14.0.1\",\"p\":\"bundle\",\"timestamp\":1363305439000,\"tags\":[\"spec\",\"libraries\",\"classes\",\"google\",\"inject\",\"code\",\"expanded\",\"much\",\"include\",\"annotation\",\"that\",\"more\",\"utility\",\"guava\",\"dependencies\",\"javax\",\"core\",\"suite\",\"collections\"],\"ec\":[\"-javadoc.jar\",\"-sources.jar\",\".jar\",\"-site.jar\",\".pom\"]}]}}");

            assertEquals(""
                    + "build.gradle:9: Warning: A newer version of com.google.guava:guava than 11.0.2 is available: 17.0 [NewerVersionAvailable]\n"
                    + "    compile 'com.google.guava:guava:11.0.2'\n"
                    + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "build.gradle:10: Warning: A newer version of com.google.guava:guava than 16.0-rc1 is available: 18.0.0-rc1 [NewerVersionAvailable]\n"
                    + "    compile 'com.google.guava:guava:16.0-rc1'\n"
                    + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "0 errors, 2 warnings\n",

                    lintProject("gradle/RemoteVersions2.gradle=>build.gradle"));
        } finally {
            GradleDetector.sMockData = null;
        }
    }

    public void testPreviewVersions() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        // This test only works when SdkConstants.GRADLE_PLUGIN_RECOMMENDED_VERSION contains
        // a preview string:
        if (!GRADLE_PLUGIN_RECOMMENDED_VERSION.startsWith("1.0.0-rc")) {
            return;
        }
        assertEquals(""
                        + "build.gradle:6: Warning: A newer version of com.android.tools.build:gradle than 1.0.0-rc0 is available: " + GRADLE_PLUGIN_RECOMMENDED_VERSION + " [GradleDependency]\n"
                        + "        classpath 'com.android.tools.build:gradle:1.0.0-rc0'\n"
                        + "        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                        + "0 errors, 1 warnings\n",

                lintProject("gradle/PreviewDependencies.gradle=>build.gradle"));
    }


    public void testDependenciesInVariables() throws Exception {
        mEnabled = Collections.singleton(DEPENDENCY);
        assertEquals(""
                    + "build.gradle:10: Warning: A newer version of com.google.android.gms:play-services-wearable than 5.0.77 is available: 6.1.71 [GradleDependency]\n"
                    + "    compile \"com.google.android.gms:play-services-wearable:${GPS_VERSION}\"\n"
                    + "    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                    + "0 errors, 1 warnings\n",

                lintProject("gradle/DependenciesVariable.gradle=>build.gradle"));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        if (issue == DEPENDENCY && message.startsWith("Using the appcompat library when ")) {
            // No data embedded in this specific message
            return;
        }

        // Issues we're supporting getOldFrom
        if (issue == DEPENDENCY
                || issue == STRING_INTEGER
                || issue == DEPRECATED
                || issue == PLUS) {
            assertNotNull("Could not extract message tokens from " + message,
                    GradleDetector.getOldValue(issue, message, TEXT));
        }

        if (issue == DEPENDENCY
                || issue == STRING_INTEGER
                || issue == DEPRECATED) {
            assertNotNull("Could not extract message tokens from " + message,
                    GradleDetector.getNewValue(issue, message, TEXT));
        }

        if (issue == COMPATIBILITY) {
            if (message.startsWith("Version ")) {
                assertNotNull("Could not extract message tokens from " + message,
                        GradleDetector.getNewValue(issue, message, TEXT));
            }
        }
    }

    public void testGetNamedDependency() {
        assertEquals("com.android.support:support-v4:21.0.+", getNamedDependency(
                "group: 'com.android.support', name: 'support-v4', version: '21.0.+'"
        ));
        assertEquals("com.android.support:support-v4:21.0.+", getNamedDependency(
                "name:'support-v4', group: \"com.android.support\", version: '21.0.+'"
        ));
        assertEquals("junit:junit:4.+", getNamedDependency(
                "group: 'junit', name: 'junit', version: '4.+'"
        ));
        assertEquals("com.android.support:support-v4:19.0.+", getNamedDependency(
                "group: 'com.android.support', name: 'support-v4', version: '19.0.+'"
        ));
        assertEquals("com.google.guava:guava:11.0.1", getNamedDependency(
                "group: 'com.google.guava', name: 'guava', version: '11.0.1', transitive: false"
        ));
        assertEquals("com.google.api-client:google-api-client:1.6.0-beta", getNamedDependency(
                "group: 'com.google.api-client', name: 'google-api-client', version: '1.6.0-beta', transitive: false"
        ));
        assertEquals("org.robolectric:robolectric:2.3-SNAPSHOT", getNamedDependency(
                "group: 'org.robolectric', name: 'robolectric', version: '2.3-SNAPSHOT'"
        ));
    }

    // -------------------------------------------------------------------------------------------
    // Test infrastructure below here
    // -------------------------------------------------------------------------------------------

    static final Implementation IMPLEMENTATION = new Implementation(
            GroovyGradleDetector.class,
            Scope.GRADLE_SCOPE);
    static {
        for (Issue issue : new BuiltinIssueRegistry().getIssues()) {
            if (issue.getImplementation().getDetectorClass() == GradleDetector.class) {
                issue.setImplementation(IMPLEMENTATION);
            }
        }
    }

    @Override
    protected Detector getDetector() {
        return new GroovyGradleDetector();
    }

    private Set<Issue> mEnabled;

    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && (mEnabled == null || mEnabled.contains(issue));
            }
        };
    }

    @Override
    protected TestLintClient createClient() {
        return new TestLintClient() {
            @Nullable
            @Override
            public File getSdkHome() {
                return getMockSupportLibraryInstallation();
            }

            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                if (!"testDependenciesInVariables".equals(getName())) {
                    return super.createProject(dir, referenceDir);
                }

                return new Project(this, dir, referenceDir) {
                    @Override
                    public boolean isGradleProject() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public Variant getCurrentVariant() {
                        /*
                        Simulate variant which has an AndroidLibrary with
                        resolved coordinates

                        com.google.android.gms:play-services-wearable:5.0.77"
                         */
                        MavenCoordinates coordinates = mock(MavenCoordinates.class);
                        when(coordinates.getGroupId()).thenReturn("com.google.android.gms");
                        when(coordinates.getArtifactId()).thenReturn("play-services-wearable");
                        when(coordinates.getVersion()).thenReturn("5.0.77");

                        AndroidLibrary library = mock(AndroidLibrary.class);
                        when(library.getResolvedCoordinates()).thenReturn(coordinates);
                        List<AndroidLibrary> libraries = Collections.singletonList(library);

                        Dependencies dependencies = mock(Dependencies.class);
                        when(dependencies.getLibraries()).thenReturn(libraries);

                        AndroidArtifact artifact = mock(AndroidArtifact.class);
                        when(artifact.getDependencies()).thenReturn(dependencies);

                        Variant variant = mock(Variant.class);
                        when(variant.getMainArtifact()).thenReturn(artifact);
                        return variant;
                    }
                };
            }
        };
    }

    // Copy of com.android.build.gradle.tasks.GroovyGradleDetector (with "static" added as
    // a modifier, and the unused field IMPLEMENTATION removed, and with fail(t.toString())
    // inserted into visitBuildScript's catch handler.
    //
    // THIS CODE DUPLICATION IS NOT AN IDEAL SITUATION! But, it's preferable to a lack of
    // tests.
    //
    // A more proper fix would be to extract the groovy detector into a library shared by
    // the testing framework and the gradle plugin.

    public static class GroovyGradleDetector extends GradleDetector {
        @Override
        public void visitBuildScript(@NonNull final Context context, Map<String, Object> sharedData) {
            try {
                visitQuietly(context, sharedData);
            } catch (Throwable t) {
                // ignore
                // Parsing the build script can involve class loading that we sometimes can't
                // handle. This happens for example when running lint in build-system/tests/api/.
                // This is a lint limitation rather than a user error, so don't complain
                // about these. Consider reporting a Issue#LINT_ERROR.
                fail(t.toString());
            }
        }

        private void visitQuietly(@NonNull final Context context,
                @SuppressWarnings("UnusedParameters") Map<String, Object> sharedData) {
            String source = context.getContents();
            if (source == null) {
                return;
            }

            List<ASTNode> astNodes = new AstBuilder().buildFromString(source);
            GroovyCodeVisitor visitor = new CodeVisitorSupport() {
                private List<MethodCallExpression> mMethodCallStack = Lists.newArrayList();
                @Override
                public void visitMethodCallExpression(MethodCallExpression expression) {
                    mMethodCallStack.add(expression);
                    super.visitMethodCallExpression(expression);
                    Expression arguments = expression.getArguments();
                    String parent = expression.getMethodAsString();
                    String parentParent = getParentParent();
                    if (arguments instanceof ArgumentListExpression) {
                        ArgumentListExpression ale = (ArgumentListExpression)arguments;
                        List<Expression> expressions = ale.getExpressions();
                        if (expressions.size() == 1 &&
                                expressions.get(0) instanceof ClosureExpression) {
                            if (isInterestingBlock(parent, parentParent)) {
                                ClosureExpression closureExpression =
                                        (ClosureExpression)expressions.get(0);
                                Statement block = closureExpression.getCode();
                                if (block instanceof BlockStatement) {
                                    BlockStatement bs = (BlockStatement)block;
                                    for (Statement statement : bs.getStatements()) {
                                        if (statement instanceof ExpressionStatement) {
                                            ExpressionStatement e = (ExpressionStatement)statement;
                                            if (e.getExpression() instanceof MethodCallExpression) {
                                                checkDslProperty(parent,
                                                        (MethodCallExpression)e.getExpression(),
                                                        parentParent);
                                            }
                                        } else if (statement instanceof ReturnStatement) {
                                            // Single item in block
                                            ReturnStatement e = (ReturnStatement)statement;
                                            if (e.getExpression() instanceof MethodCallExpression) {
                                                checkDslProperty(parent,
                                                        (MethodCallExpression)e.getExpression(),
                                                        parentParent);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (arguments instanceof TupleExpression) {
                        if (isInterestingStatement(parent, parentParent)) {
                            TupleExpression te = (TupleExpression) arguments;
                            Map<String, String> namedArguments = Maps.newHashMap();
                            List<String> unnamedArguments = Lists.newArrayList();
                            for (Expression subExpr : te.getExpressions()) {
                                if (subExpr instanceof NamedArgumentListExpression) {
                                    NamedArgumentListExpression nale = (NamedArgumentListExpression) subExpr;
                                    for (MapEntryExpression mae : nale.getMapEntryExpressions()) {
                                        namedArguments.put(mae.getKeyExpression().getText(),
                                                mae.getValueExpression().getText());
                                    }
                                }
                            }
                            checkMethodCall(context, parent, parentParent, namedArguments, unnamedArguments, expression);
                        }
                    }
                    assert !mMethodCallStack.isEmpty();
                    assert mMethodCallStack.get(mMethodCallStack.size() - 1) == expression;
                    mMethodCallStack.remove(mMethodCallStack.size() - 1);
                }

                private String getParentParent() {
                    for (int i = mMethodCallStack.size() - 2; i >= 0; i--) {
                        MethodCallExpression expression = mMethodCallStack.get(i);
                        Expression arguments = expression.getArguments();
                        if (arguments instanceof ArgumentListExpression) {
                            ArgumentListExpression ale = (ArgumentListExpression)arguments;
                            List<Expression> expressions = ale.getExpressions();
                            if (expressions.size() == 1 &&
                                    expressions.get(0) instanceof ClosureExpression) {
                                return expression.getMethodAsString();
                            }
                        }
                    }

                    return null;
                }

                private void checkDslProperty(String parent, MethodCallExpression c,
                        String parentParent) {
                    String property = c.getMethodAsString();
                    if (isInterestingProperty(property, parent, getParentParent())) {
                        String value = getText(c.getArguments());
                        checkDslPropertyAssignment(context, property, value, parent, parentParent, c, c);
                    }
                }

                private String getText(ASTNode node) {
                    String source = context.getContents();
                    Pair<Integer, Integer> offsets = getOffsets(node, context);
                    return source.substring(offsets.getFirst(), offsets.getSecond());
                }
            };

            for (ASTNode node : astNodes) {
                node.visit(visitor);
            }
        }

        @NonNull
        private static Pair<Integer, Integer> getOffsets(ASTNode node, Context context) {
            if (node.getLastLineNumber() == -1 && node instanceof TupleExpression) {
                // Workaround: TupleExpressions yield bogus offsets, so use its
                // children instead
                TupleExpression exp = (TupleExpression) node;
                List<Expression> expressions = exp.getExpressions();
                if (!expressions.isEmpty()) {
                    return Pair.of(
                        getOffsets(expressions.get(0), context).getFirst(),
                        getOffsets(expressions.get(expressions.size() - 1), context).getSecond());
                }
            }
            String source = context.getContents();
            assert source != null; // because we successfully parsed
            int start = 0;
            int end = source.length();
            int line = 1;
            int startLine = node.getLineNumber();
            int startColumn = node.getColumnNumber();
            int endLine = node.getLastLineNumber();
            int endColumn = node.getLastColumnNumber();
            int column = 1;
            for (int index = 0, len = end; index < len; index++) {
                if (line == startLine && column == startColumn) {
                    start = index;
                }
                if (line == endLine && column == endColumn) {
                    end = index;
                    break;
                }

                char c = source.charAt(index);
                if (c == '\n') {
                    line++;
                    column = 1;
                } else {
                    column++;
                }
            }

            return Pair.of(start, end);
        }

        @Override
        protected int getStartOffset(@NonNull Context context, @NonNull Object cookie) {
            ASTNode node = (ASTNode) cookie;
            Pair<Integer, Integer> offsets = getOffsets(node, context);
            return offsets.getFirst();
        }

        @Override
        protected Location createLocation(@NonNull Context context, @NonNull Object cookie) {
            ASTNode node = (ASTNode) cookie;
            Pair<Integer, Integer> offsets = getOffsets(node, context);
            int fromLine = node.getLineNumber() - 1;
            int fromColumn = node.getColumnNumber() - 1;
            int toLine = node.getLastLineNumber() - 1;
            int toColumn = node.getLastColumnNumber() - 1;
            return Location.create(context.file,
                    new DefaultPosition(fromLine, fromColumn, offsets.getFirst()),
                    new DefaultPosition(toLine, toColumn, offsets.getSecond()));
        }
    }
}
