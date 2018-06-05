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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.Dependencies;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.SourceProvider;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Implementation of JavaArtifact that is serializable
 */
public class JavaArtifactImpl extends BaseArtifactImpl implements JavaArtifact, Serializable {
    private static final long serialVersionUID = 1L;

    private final Set<String> ideSetupTaskNames;

    @Nullable
    private final File mockablePlatformJar;

    public static JavaArtifactImpl clone(@NonNull JavaArtifact javaArtifact) {
        SourceProvider variantSP = javaArtifact.getVariantSourceProvider();
        SourceProvider flavorsSP = javaArtifact.getMultiFlavorSourceProvider();

        return new JavaArtifactImpl(
                javaArtifact.getName(),
                javaArtifact.getAssembleTaskName(),
                javaArtifact.getCompileTaskName(),
                javaArtifact.getIdeSetupTaskNames(),
                javaArtifact.getGeneratedSourceFolders(),
                javaArtifact.getClassesFolder(),
                javaArtifact.getJavaResourcesFolder(),
                javaArtifact.getMockablePlatformJar(),
                DependenciesImpl.cloneDependenciesForJavaArtifacts(javaArtifact.getDependencies()),
                variantSP != null ? SourceProviderImpl.cloneProvider(variantSP) : null,
                flavorsSP != null ? SourceProviderImpl.cloneProvider(flavorsSP) : null);
    }

    public JavaArtifactImpl(@NonNull String name,
                            @NonNull String assembleTaskName,
                            @NonNull String compileTaskName,
                            @NonNull Iterable<String> ideSetupTaskNames,
                            @NonNull Collection<File> generatedSourceFolders,
                            @NonNull File classesFolder,
                            @NonNull File javaResourcesFolder,
                            @Nullable File mockablePlatformJar,
                            @NonNull Dependencies dependencies,
                            @Nullable SourceProvider variantSourceProvider,
                            @Nullable SourceProvider multiFlavorSourceProviders) {
        super(name, assembleTaskName, compileTaskName, classesFolder, javaResourcesFolder,
                dependencies,
                variantSourceProvider, multiFlavorSourceProviders, generatedSourceFolders);
        this.ideSetupTaskNames = Sets.newHashSet(ideSetupTaskNames);
        this.mockablePlatformJar = mockablePlatformJar;
    }

    @NonNull
    @Override
    public Set<String> getIdeSetupTaskNames() {
        return ideSetupTaskNames;
    }

    @Override
    @Nullable
    public File getMockablePlatformJar() {
        return mockablePlatformJar;
    }
}
