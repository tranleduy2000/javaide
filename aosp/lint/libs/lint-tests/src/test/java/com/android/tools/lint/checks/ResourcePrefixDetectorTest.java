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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidProject;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Project;

import java.io.File;
import java.util.Arrays;

public class ResourcePrefixDetectorTest extends AbstractCheckTest {

    @Override
    protected Detector getDetector() {
        return new ResourcePrefixDetector();
    }

    public void testResourceFiles() throws Exception {
        assertEquals(""
            + "res/drawable-mdpi/frame.png: Error: Resource named 'frame' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_frame' ? [ResourceName]\n"
            + "res/layout/layout1.xml:2: Error: Resource named 'layout1' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_layout1' ? [ResourceName]\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "^\n"
            + "res/menu/menu.xml:2: Error: Resource named 'menu' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_menu' ? [ResourceName]\n"
            + "<menu xmlns:android=\"http://schemas.android.com/apk/res/android\" >\n"
            + "^\n"
            + "3 errors, 0 warnings\n",
            lintProject(
                "res/layout/layout1.xml",
                "res/menu/menu.xml",
                "res/layout/layout1.xml=>res/layout/unit_test_prefix_ok.xml",
                "res/drawable-mdpi/frame.png",
                "res/drawable-mdpi/frame.png=>res/drawable/unit_test_prefix_ok1.png",
                "res/drawable-mdpi/frame.png=>res/drawable/unit_test_prefix_ok2.9.png"
            ));
    }

    public void testValues() throws Exception {
        assertEquals(""
            + "res/values/customattr.xml:2: Error: Resource named 'ContentFrame' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_ContentFrame' ? [ResourceName]\n"
            + "    <declare-styleable name=\"ContentFrame\">\n"
            + "                       ~~~~~~~~~~~~~~~~~~~\n"
            + "res/values/customattr.xml:3: Error: Resource named 'content' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_content' ? [ResourceName]\n"
            + "        <attr name=\"content\" format=\"reference\" />\n"
            + "              ~~~~~~~~~~~~~~\n"
            + "res/values/customattr.xml:4: Error: Resource named 'contentId' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_contentId' ? [ResourceName]\n"
            + "        <attr name=\"contentId\" format=\"reference\" />\n"
            + "              ~~~~~~~~~~~~~~~~\n"
            + "res/layout/customattrlayout.xml:2: Error: Resource named 'customattrlayout' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_customattrlayout' ? [ResourceName]\n"
            + "<foo.bar.ContentFrame\n"
            + "^\n"
            + "4 errors, 0 warnings\n",

            lintProject(
                    "res/values/customattr.xml",
                    "res/layout/customattrlayout.xml",
                    "unusedR.java.txt=>gen/my/pkg/R.java",
                    "AndroidManifest.xml"));
    }

    public void testMultiProject() throws Exception {
        File master = getProjectDir("MasterProject",
                // Master project
                "multiproject/main-manifest.xml=>AndroidManifest.xml",
                "multiproject/main.properties=>project.properties",
                "multiproject/MainCode.java.txt=>src/foo/main/MainCode.java"
        );
        File library = getProjectDir("LibraryProject",
                // Library project
                "multiproject/library-manifest.xml=>AndroidManifest.xml",
                "multiproject/library.properties=>project.properties",
                "multiproject/LibraryCode.java.txt=>src/foo/library/LibraryCode.java",
                "multiproject/strings.xml=>res/values/strings.xml"
        );
        assertEquals(""
            + "LibraryProject/res/values/strings.xml:4: Error: Resource named 'app_name' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_app_name' ? [ResourceName]\n"
            + "    <string name=\"app_name\">LibraryProject</string>\n"
            + "            ~~~~~~~~~~~~~~~\n"
            + "LibraryProject/res/values/strings.xml:5: Error: Resource named 'string1' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_string1' ? [ResourceName]\n"
            + "    <string name=\"string1\">String 1</string>\n"
            + "            ~~~~~~~~~~~~~~\n"
            + "LibraryProject/res/values/strings.xml:6: Error: Resource named 'string2' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_string2' ? [ResourceName]\n"
            + "    <string name=\"string2\">String 2</string>\n"
            + "            ~~~~~~~~~~~~~~\n"
            + "LibraryProject/res/values/strings.xml:7: Error: Resource named 'string3' does not start with the project's resource prefix 'unit_test_prefix_'; rename to 'unit_test_prefix_string3' ? [ResourceName]\n"
            + "    <string name=\"string3\">String 3</string>\n"
            + "            ~~~~~~~~~~~~~~\n"
            + "4 errors, 0 warnings\n",

            checkLint(Arrays.asList(master, library)).replace("/TESTROOT/",""));
    }

    // TODO: Test suppressing root level tag

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
