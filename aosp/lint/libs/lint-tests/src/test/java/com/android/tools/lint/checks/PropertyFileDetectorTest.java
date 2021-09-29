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

import static com.android.tools.lint.checks.PropertyFileDetector.suggestEscapes;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;

import java.io.File;

public class PropertyFileDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new PropertyFileDetector();
    }

    public void test() throws Exception {
        assertEquals(""
                + "local.properties:11: Error: Windows file separators (\\) and drive letter separators (':') must be escaped (\\\\) in property files; use C\\:\\\\my\\\\path\\\\to\\\\sdk [PropertyEscape]\n"
                + "windows.dir=C:\\my\\path\\to\\sdk\n"
                + "             ~~~~~~~~~~~~~~\n"
                + "local.properties:14: Error: Windows file separators (\\) and drive letter separators (':') must be escaped (\\\\) in property files; use C\\:\\\\Documents and Settings\\\\UserName\\\\Local Settings\\\\Application Data\\\\Android\\\\android-studio\\\\sdk [PropertyEscape]\n"
                + "ok.sdk.dir=C:\\\\Documents and Settings\\\\UserName\\\\Local Settings\\\\Application Data\\\\Android\\\\android-studio\\\\sdk\n"
                + "            ~\n"
                + "2 errors, 0 warnings\n",
                lintProject("local.properties=>local.properties"));
    }

    public void testGetSuggestedEscape() {
        assertEquals("C:\\\\my\\\\path\\\\to\\\\sdk", PropertyFileDetector.getSuggestedEscape(
                "Windows file separators (\\) must be escaped (\\\\); use C:\\\\my\\\\path\\\\to\\\\sdk",
                TEXT));
        assertEquals("C\\:\\\\my\\\\path\\\\to\\\\sdk", PropertyFileDetector.getSuggestedEscape(
                "local.properties:11: Error: Windows file separators (\\) and drive letter separators (':') must be escaped (\\\\) in property files; use C\\:\\\\my\\\\path\\\\to\\\\sdk",
                TEXT));
    }

    public void testUseHttpInsteadOfHttps() throws Exception {
        assertEquals(""
                + "gradle/wrapper/gradle-wrapper.properties:5: Warning: Replace HTTP with HTTPS for better security; use https\\://services.gradle.org/distributions/gradle-2.1-all.zip [UsingHttp]\n"
                + "distributionUrl=http\\://services.gradle.org/distributions/gradle-2.1-all.zip\n"
                + "                ~~~~\n"
                + "0 errors, 1 warnings\n",
                lintProject("gradle_http.properties=>gradle/wrapper/gradle-wrapper.properties"));
    }

    public void testIssue92789() throws Exception {
        // Regression test for https://code.google.com/p/android/issues/detail?id=92789
        assertEquals(""
                + "local.properties:1: Error: Windows file separators (\\) and drive letter separators (':') must be escaped (\\\\) in property files; use D\\:\\\\development\\\\android-sdks [PropertyEscape]\n"
                + "sdk.dir=D:\\\\development\\\\android-sdks\n"
                + "         ~\n"
                + "1 errors, 0 warnings\n",
                lintProject("local2.properties=>local.properties"));
    }

    @Override
    protected void checkReportedError(@NonNull Context context, @NonNull Issue issue,
            @NonNull Severity severity, @Nullable Location location, @NonNull String message) {
        assertNotNull(message, PropertyFileDetector.getSuggestedEscape(message, TEXT));
    }

    public void testSuggestEscapes() {
        assertEquals("", suggestEscapes(""));
        assertEquals("foo", suggestEscapes("foo"));
        assertEquals("foo/bar", suggestEscapes("foo/bar"));
        assertEquals("c\\:\\\\foo\\\\bar", suggestEscapes("c\\:\\\\foo\\\\bar"));
        assertEquals("c\\:\\\\foo\\\\bar", suggestEscapes("c:\\\\foo\\bar"));
    }

    @Override
    protected TestLintClient createClient() {
        return new TestLintClient() {
            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                return new Project(this, dir, referenceDir) {
                    @Override
                    public boolean isGradleProject() {
                        return true;
                    }

                    @Nullable
                    @Override
                    public AndroidProject getGradleProjectModel() {
                        AndroidProject project = mock(AndroidProject.class);
                        when(project.getResourcePrefix()).thenReturn("unit_test_prefix_");
                        return project;
                    }
                };
            }
        };
    }
}
