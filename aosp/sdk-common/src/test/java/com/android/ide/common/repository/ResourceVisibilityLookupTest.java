/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.ide.common.repository;

import static com.android.SdkConstants.FN_PUBLIC_TXT;
import static com.android.SdkConstants.FN_RESOURCE_TEXT;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Dependencies;
import com.android.builder.model.Variant;
import com.android.ide.common.resources.ResourceUrl;
import com.android.resources.ResourceType;
import com.android.testutils.TestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import junit.framework.TestCase;

import org.easymock.IExpectationSetters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResourceVisibilityLookupTest extends TestCase {
    public void test() throws IOException {
        AndroidLibrary library = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
                        + ""
                        + "dimen activity_vertical\n"
                        + "id action_settings\n"
                        + "layout activity_main\n"
        );

        ResourceVisibilityLookup visibility = ResourceVisibilityLookup.create(library);
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertFalse(visibility.isPrivate(ResourceType.ID, "action_settings"));
        assertFalse(visibility.isPrivate(ResourceType.LAYOUT, "activity_main"));
        //noinspection ConstantConditions
        assertTrue(visibility.isPrivate(ResourceUrl.parse("@dimen/activity_horizontal_margin")));

        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "activity_vertical")); // public
        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "unknown")); // not in this library
    }

    public void testModelVersions() throws IOException {
        AndroidLibrary library = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
                        + ""
                        + "dimen activity_vertical\n"
                        + "id action_settings\n"
                        + "layout activity_main\n"
        );

        AndroidArtifact mockArtifact = createMockArtifact(Collections.singletonList(library));
        Variant variant = createMockVariant(mockArtifact);

        AndroidProject project;

        project = createMockProject("1.0.1", 0);
        assertTrue(new ResourceVisibilityLookup.Provider().get(project, variant).isEmpty());


        project = createMockProject("1.1", 0);
        assertTrue(new ResourceVisibilityLookup.Provider().get(project, variant).isEmpty());

        project = createMockProject("1.2", 2);
        assertTrue(new ResourceVisibilityLookup.Provider().get(project, variant).isEmpty());

        project = createMockProject("1.3.0", 3);
        assertFalse(new ResourceVisibilityLookup.Provider().get(project, variant).isEmpty());

        project = createMockProject("2.5", 45);
        assertFalse(new ResourceVisibilityLookup.Provider().get(project, variant).isEmpty());

        ResourceVisibilityLookup visibility =new ResourceVisibilityLookup.Provider().get(project,
                variant);
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertFalse(visibility.isPrivate(ResourceType.ID, "action_settings"));
    }

    public void testAllPrivate() throws IOException {
        AndroidLibrary library = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
        );

        ResourceVisibilityLookup visibility = ResourceVisibilityLookup.create(library);
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertTrue(visibility.isPrivate(ResourceType.ID, "action_settings"));
        assertTrue(visibility.isPrivate(ResourceType.LAYOUT, "activity_main"));
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_vertical_margin"));

        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "unknown")); // not in this library
    }

    public void testNotDeclared() throws IOException {
        AndroidLibrary library = createMockLibrary("", null);

        ResourceVisibilityLookup visibility = ResourceVisibilityLookup.create(library);
        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertFalse(visibility.isPrivate(ResourceType.ID, "action_settings"));
        assertFalse(visibility.isPrivate(ResourceType.LAYOUT, "activity_main"));
        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "activity_vertical"));

        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "unknown")); // not in this library
    }

    public void testCombined() throws IOException {
        AndroidLibrary library1 = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
        );
        AndroidLibrary library2 = createMockLibrary(
                ""
                        + "int layout foo 0x7f030001\n"
                        + "int layout bar 0x7f060000\n",
                ""
                        + "layout foo\n"
        );

        List<AndroidLibrary> androidLibraries = Arrays.asList(library1, library2);
        ResourceVisibilityLookup visibility = ResourceVisibilityLookup
                .create(androidLibraries, null);
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertTrue(visibility.isPrivate(ResourceType.ID, "action_settings"));
        assertTrue(visibility.isPrivate(ResourceType.LAYOUT, "activity_main"));
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_vertical_margin"));
        assertFalse(visibility.isPrivate(ResourceType.LAYOUT, "foo"));
        assertTrue(visibility.isPrivate(ResourceType.LAYOUT, "bar"));

        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "unknown")); // not in this library
    }

    public void testDependency() throws IOException {
        AndroidLibrary library1 = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
        );
        AndroidLibrary library2 = createMockLibrary(
                ""
                        + "int layout foo 0x7f030001\n"
                        + "int layout bar 0x7f060000\n",
                ""
                        + "layout foo\n",
                Collections.singletonList(library1)
        );

        List<AndroidLibrary> androidLibraries = Arrays.asList(library1, library2);
        ResourceVisibilityLookup visibility = ResourceVisibilityLookup
                .create(androidLibraries, null);
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_horizontal_margin"));
        assertTrue(visibility.isPrivate(ResourceType.ID, "action_settings"));
        assertTrue(visibility.isPrivate(ResourceType.LAYOUT, "activity_main"));
        assertTrue(visibility.isPrivate(ResourceType.DIMEN, "activity_vertical_margin"));
        assertFalse(visibility.isPrivate(ResourceType.LAYOUT, "foo"));
        assertTrue(visibility.isPrivate(ResourceType.LAYOUT, "bar"));

        assertFalse(visibility.isPrivate(ResourceType.DIMEN, "unknown")); // not in this library
    }

    public void testManager() throws IOException {
        AndroidLibrary library = createMockLibrary(
                ""
                        + "int dimen activity_horizontal_margin 0x7f030000\n"
                        + "int dimen activity_vertical_margin 0x7f030001\n"
                        + "int id action_settings 0x7f060000\n"
                        + "int layout activity_main 0x7f020000\n"
                        + "int menu menu_main 0x7f050000\n"
                        + "int string action_settings 0x7f040000\n"
                        + "int string app_name 0x7f040001\n"
                        + "int string hello_world 0x7f040002",
                ""
        );
        ResourceVisibilityLookup.Provider provider = new ResourceVisibilityLookup.Provider();
        assertSame(provider.get(library), provider.get(library));
        assertTrue(provider.get(library).isPrivate(ResourceType.DIMEN,
                "activity_horizontal_margin"));

        AndroidArtifact artifact = createMockArtifact(Collections.singletonList(library));
        assertSame(provider.get(artifact), provider.get(artifact));
        assertTrue(provider.get(artifact).isPrivate(ResourceType.DIMEN,
                "activity_horizontal_margin"));
    }

    public static AndroidProject createMockProject(String modelVersion, int apiVersion) {
        AndroidProject project = createNiceMock(AndroidProject.class);
        expect(project.getApiVersion()).andReturn(apiVersion).anyTimes();
        expect(project.getModelVersion()).andReturn(modelVersion).anyTimes();
        replay(project);

        return project;
    }

    public static Variant createMockVariant(AndroidArtifact artifact) {
        Variant variant = createNiceMock(Variant.class);
        expect(variant.getMainArtifact()).andReturn(artifact).anyTimes();
        replay(variant);
        return variant;

    }

    public static AndroidArtifact createMockArtifact(List<AndroidLibrary> libraries) {
        Dependencies dependencies = createNiceMock(Dependencies.class);
        expect(dependencies.getLibraries()).andReturn(libraries).anyTimes();
        replay(dependencies);

        AndroidArtifact artifact = createNiceMock(AndroidArtifact.class);
        expect(artifact.getDependencies()).andReturn(dependencies).anyTimes();
        replay(artifact);

        return artifact;
    }

    public static AndroidLibrary createMockLibrary(String allResources, String publicResources)
            throws IOException {
        return createMockLibrary(allResources, publicResources,
                Collections.<AndroidLibrary>emptyList());
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
        AndroidLibrary library = createNiceMock(AndroidLibrary.class);
        expect(library.getPublicResources()).andReturn(publicTxtFile).anyTimes();

        // Work around wildcard capture
        //expect(mock.getLibraryDependencies()).andReturn(dependencies).anyTimes();
        IExpectationSetters setter = expect(library.getLibraryDependencies());
        //noinspection unchecked
        setter.andReturn(dependencies);
        setter.anyTimes();

        replay(library);
        return library;
    }
}