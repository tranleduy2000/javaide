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

package com.android.tools.lint.client.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.AbstractCheckTest;
import com.android.tools.lint.checks.UnusedResourceDetector;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProjectTest extends AbstractCheckTest {
    @Override
    protected boolean ignoreSystemErrors() {
        return false;
    }

    public void testCycle() throws Exception {
        // Ensure that a cycle in library project dependencies doesn't cause
        // infinite directory traversal
        File master = getProjectDir("MasterProject",
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties", // RECURSIVE - points to self
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );

        assertEquals(""
                + "MasterProject/project.properties: Error: Circular library dependencies; check your project.properties files carefully [LintError]\n"
                + "1 errors, 0 warnings\n",

                checkLint(Arrays.asList(master, library)));
    }

    public void testInvalidLibraryReferences1() throws Exception {
        TestClient client = new TestClient();
        File dir = new File("project");
        TestProject project1 = new TestProject(client, dir);
        client.registerProject(dir, project1);
        project1.setDirectLibraries(Collections.<Project>singletonList(project1));
        List<Project> libraries = project1.getAllLibraries();
        assertNotNull(libraries);
        assertEquals(
                "Warning: Internal lint error: cyclic library dependency for Project [dir=project]",
                client.getLoggedOutput());
    }

    public void testInvalidLibraryReferences2() throws Exception {
        TestClient client = new TestClient();
        File dir1 = new File("project1");
        File dir2 = new File("project2");
        TestProject project1 = new TestProject(client, dir1);
        client.registerProject(dir1, project1);
        TestProject project2 = new TestProject(client, dir2);
        client.registerProject(dir2, project2);
        project2.setDirectLibraries(Collections.<Project>singletonList(project1));
        project1.setDirectLibraries(Collections.<Project>singletonList(project2));
        List<Project> libraries = project1.getAllLibraries();
        assertNotNull(libraries);
        assertEquals(
                "Warning: Internal lint error: cyclic library dependency for Project [dir=project1]",
                client.getLoggedOutput());
        assertEquals(1, libraries.size());
        assertSame(project2, libraries.get(0));
        assertEquals(1, project2.getAllLibraries().size());
        assertSame(project1, project2.getAllLibraries().get(0));
    }

    public void testOkLibraryReferences() throws Exception {
        TestClient client = new TestClient();
        File dir1 = new File("project1");
        File dir2 = new File("project2");
        File dir3 = new File("project3");
        TestProject project1 = new TestProject(client, dir1);
        client.registerProject(dir1, project1);
        TestProject project2 = new TestProject(client, dir2);
        client.registerProject(dir2, project2);
        TestProject project3 = new TestProject(client, dir3);
        client.registerProject(dir3, project3);
        project1.setDirectLibraries(Arrays.<Project>asList(project2, project3));
        project2.setDirectLibraries(Collections.<Project>singletonList(project3));
        project3.setDirectLibraries(Collections.<Project>emptyList());
        List<Project> libraries = project1.getAllLibraries();
        assertNotNull(libraries);
        assertEquals(
                "",
                client.getLoggedOutput());
        assertEquals(2, libraries.size());
        assertTrue(libraries.contains(project2));
        assertTrue(libraries.contains(project3));
        assertEquals(1, project2.getAllLibraries().size());
        assertSame(project3, project2.getAllLibraries().get(0));
        assertTrue(project3.getAllLibraries().isEmpty());
    }

    private class TestClient extends TestLintClient {
        @SuppressWarnings("StringBufferField")
        private StringBuilder mLog = new StringBuilder();

        @Override
        public void log(@NonNull Severity severity, @Nullable Throwable exception,
                @Nullable String format, @Nullable Object... args) {
            assertNotNull(format);
            mLog.append(severity.getDescription()).append(": ");
            mLog.append(String.format(format, args));
        }

        public String getLoggedOutput() {
            return mLog.toString();
        }
    }

    private static class TestProject extends Project {
        protected TestProject(@NonNull LintClient client, @NonNull File dir) {
            super(client, dir, dir);
        }

        public void setDirectLibraries(List<Project> libraries) {
            mDirectLibraries = libraries;
        }
    }

    public void testDependsOn1() throws Exception {
        File dir = getProjectDir("MyProject",
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java",
                "bytecode/classes.jar=>libs/android-support-v4.jar"
        );
        TestClient client = new TestClient();
        TestProject project1 = new TestProject(client, dir);
        client.registerProject(dir, project1);
        assertNull(project1.dependsOn("unknown:library"));
        assertTrue(project1.dependsOn("com.android.support:support-v4"));
    }

    public void testDependsOn2() throws Exception {
        File dir = getProjectDir("MyProject",
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java",
                "bytecode/classes.jar=>libs/support-v4-13.0.0-f5279ca6f213451a9dfb870f714ce6e6.jar"
        );
        TestClient client = new TestClient();
        TestProject project1 = new TestProject(client, dir);
        client.registerProject(dir, project1);
        assertNull(project1.dependsOn("unknown:library"));
        assertTrue(project1.dependsOn("com.android.support:support-v4"));
    }
    @Override
    protected Detector getDetector() {
        return new UnusedResourceDetector();
    }
}
