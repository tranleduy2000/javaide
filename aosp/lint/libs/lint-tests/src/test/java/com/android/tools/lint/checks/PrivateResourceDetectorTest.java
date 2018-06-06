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

import static com.android.SdkConstants.FN_PUBLIC_TXT;
import static com.android.SdkConstants.FN_RESOURCE_TEXT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.Variant;
import com.android.testutils.TestUtils;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Project;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.mockito.stubbing.OngoingStubbing;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("javadoc")
public class PrivateResourceDetectorTest extends AbstractCheckTest {
    @Override
    protected Detector getDetector() {
        return new PrivateResourceDetector();
    }

    public void testPrivateInXml() throws Exception {
        assertEquals(""
                + "res/layout/private.xml:11: Warning: The resource @string/my_private_string is marked as private in the library [PrivateResource]\n"
                + "            android:text=\"@string/my_private_string\" />\n"
                + "                          ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
                lintProject(xml("res/layout/private.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        + "              android:id=\"@+id/newlinear\"\n"
                        + "              android:orientation=\"vertical\"\n"
                        + "              android:layout_width=\"match_parent\"\n"
                        + "              android:layout_height=\"match_parent\">\n"
                        + "\n"
                        + "    <TextView\n"
                        + "            android:layout_width=\"wrap_content\"\n"
                        + "            android:layout_height=\"wrap_content\"\n"
                        + "            android:text=\"@string/my_private_string\" />\n"
                        + "\n"
                        + "    <TextView\n"
                        + "            android:layout_width=\"wrap_content\"\n"
                        + "            android:layout_height=\"wrap_content\"\n"
                        + "            android:text=\"@string/my_public_string\" />\n"
                        + "</LinearLayout>\n")));
    }

    public void testPrivateInJava() throws Exception {
        assertEquals(""
                + ""
                + "src/test/pkg/Private.java:3: Warning: The resource @string/my_private_string is marked as private in the library [PrivateResource]\n"
                + "        int x = R.string.my_private_string; // ERROR\n"
                + "                ~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 1 warnings\n",
                lintProject(java("src/test/pkg/Private.java", ""
                        + "public class PrivateResourceDetectorTest {\n"
                        + "    void test() {\n"
                        + "        int x = R.string.my_private_string; // ERROR\n"
                        + "        int y = R.string.my_public_string; // OK\n"
                        + "        int y = android.R.string.my_private_string; // OK (not in project namespace)\n"
                        + "    }\n"
                        + "}\n")));
    }

    public void testOverride() throws Exception {
        assertEquals(""
                + "res/layout/my_private_layout.xml: Warning: Overriding @layout/my_private_layout which is marked as private in the library. If deliberate, use tools:override=\"true\", otherwise pick a different name. [PrivateResource]\n"
                + "res/values/strings.xml:5: Warning: Overriding @string/my_private_string which is marked as private in the library. If deliberate, use tools:override=\"true\", otherwise pick a different name. [PrivateResource]\n"
                + "    <string name=\"my_private_string\">String 1</string>\n"
                + "                  ~~~~~~~~~~~~~~~~~\n"
                + "res/values/strings.xml:9: Warning: Overriding @string/my_private_string which is marked as private in the library. If deliberate, use tools:override=\"true\", otherwise pick a different name. [PrivateResource]\n"
                + "    <item type=\"string\" name=\"my_private_string\">String 1</item>\n"
                + "                              ~~~~~~~~~~~~~~~~~\n"
                + "res/values/strings.xml:12: Warning: Overriding @string/my_private_string which is marked as private in the library. If deliberate, use tools:override=\"true\", otherwise pick a different name. [PrivateResource]\n"
                + "    <string tools:override=\"false\" name=\"my_private_string\">String 2</string>\n"
                + "                                         ~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 4 warnings\n",
                lintProject(xml("res/values/strings.xml", ""
                        + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources xmlns:tools=\"http://schemas.android.com/tools\">\n"
                        + "\n"
                        + "    <string name=\"app_name\">LibraryProject</string>\n"
                        + "    <string name=\"my_private_string\">String 1</string>\n"
                        + "    <string name=\"my_public_string\">String 2</string>\n"
                        + "    <string name=\"string3\"> @my_private_string </string>\n"
                        + "    <string name=\"string4\"> @my_public_string </string>\n"
                        + "    <item type=\"string\" name=\"my_private_string\">String 1</item>\n"
                        + "    <dimen name=\"my_private_string\">String 1</dimen>\n" // unrelated
                        + "    <string tools:ignore=\"PrivateResource\" name=\"my_private_string\">String 2</string>\n"
                        + "    <string tools:override=\"false\" name=\"my_private_string\">String 2</string>\n"
                        + "    <string tools:override=\"true\" name=\"my_private_string\">String 2</string>\n"
                        + "\n"
                        + "</resources>\n"),
                        xml("res/layout/my_private_layout.xml", "<LinearLayout/>"),
                        xml("res/layout/my_public_layout.xml", "<LinearLayout/>")));
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
                        // First version which supported private resources; this does not
                        // need to track later versions we release
                        return createMockProject("1.3.0-alpha2", 3);
                    }

                    @Nullable
                    @Override
                    public Variant getCurrentVariant() {
                        try {
                            AndroidLibrary library = createMockLibrary(
                                    ""
                                            + "int string my_private_string 0x7f040000\n"
                                            + "int string my_public_string 0x7f040001\n"
                                            + "int layout my_private_layout 0x7f040002\n",
                                    ""
                                            + ""
                                            + "string my_public_string\n",
                                    Collections.<AndroidLibrary>emptyList()
                            );
                            AndroidArtifact artifact = createMockArtifact(
                                    Collections.singletonList(library));

                            Variant variant = mock(Variant.class);
                            when(variant.getMainArtifact()).thenReturn(artifact);
                            return variant;
                        } catch (Exception e) {
                            fail(e.toString());
                            return null;
                        }
                    }
                };
            }
        };
    }

    public static AndroidProject createMockProject(String modelVersion, int apiVersion) {
        AndroidProject project = mock(AndroidProject.class);
        when(project.getApiVersion()).thenReturn(apiVersion);
        when(project.getModelVersion()).thenReturn(modelVersion);

        return project;
    }

    public static AndroidArtifact createMockArtifact(List<AndroidLibrary> libraries) {
        Dependencies dependencies = mock(Dependencies.class);
        when(dependencies.getLibraries()).thenReturn(libraries);

        AndroidArtifact artifact = mock(AndroidArtifact.class);
        when(artifact.getDependencies()).thenReturn(dependencies);

        return artifact;
    }

    public static AndroidLibrary createMockLibrary(String allResources, String publicResources,
            List<AndroidLibrary> dependencies)
            throws IOException {
        final File tempDir = TestUtils.createTempDirDeletedOnExit();

        Files.write(allResources, new File(tempDir, FN_RESOURCE_TEXT), Charsets.UTF_8);
        File publicTxtFile = new File(tempDir, FN_PUBLIC_TXT);
        if (publicResources != null) {
            Files.write(publicResources, publicTxtFile, Charsets.UTF_8);
        }
        AndroidLibrary library = mock(AndroidLibrary.class);
        when(library.getPublicResources()).thenReturn(publicTxtFile);

        // Work around wildcard capture
        //when(mock.getLibraryDependencies()).thenReturn(dependencies);
        List libraryDependencies = library.getLibraryDependencies();
        OngoingStubbing<List> setter = when(libraryDependencies);
        setter.thenReturn(dependencies);
        return library;
    }
}
