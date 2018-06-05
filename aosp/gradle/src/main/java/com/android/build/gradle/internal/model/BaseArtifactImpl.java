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
import com.android.builder.model.BaseArtifact;
import com.android.builder.model.Dependencies;
import com.android.builder.model.SourceProvider;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

/**
 * Implementation of BaseArtifact that is serializable
 */
abstract class BaseArtifactImpl implements BaseArtifact, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    protected final Collection<File> generatedSourceFolders;

    private final String name;
    @NonNull
    private final String assembleTaskName;
    @NonNull
    private final String compileTaskName;
    @NonNull
    private final File classesFolder;
    @NonNull
    private final File javaResourcesFolder;
    @NonNull
    private final Dependencies dependencies;
    @Nullable
    private final SourceProvider variantSourceProvider;
    @Nullable
    private final SourceProvider multiFlavorSourceProviders;


    BaseArtifactImpl(@NonNull String name,
            @NonNull String assembleTaskName,
            @NonNull String compileTaskName,
            @NonNull File classesFolder,
            @NonNull File javaResourcesFolder,
            @NonNull Dependencies dependencies,
            @Nullable SourceProvider variantSourceProvider,
            @Nullable SourceProvider multiFlavorSourceProviders,
            @NonNull Collection<File> generatedSourceFolders) {
        this.name = name;
        this.assembleTaskName = assembleTaskName;
        this.compileTaskName = compileTaskName;
        this.classesFolder = classesFolder;
        this.javaResourcesFolder = javaResourcesFolder;
        this.dependencies = dependencies;
        this.variantSourceProvider = variantSourceProvider;
        this.multiFlavorSourceProviders = multiFlavorSourceProviders;
        this.generatedSourceFolders = generatedSourceFolders;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String getCompileTaskName() {
        return compileTaskName;
    }

    @NonNull
    @Override
    public String getAssembleTaskName() {
        return assembleTaskName;
    }

    @NonNull
    @Override
    public File getClassesFolder() {
        return classesFolder;
    }

    @NonNull
    @Override
    public File getJavaResourcesFolder() {
        return javaResourcesFolder;
    }

    @NonNull
    @Override
    public Dependencies getDependencies() {
        return dependencies;
    }

    @Nullable
    @Override
    public SourceProvider getVariantSourceProvider() {
        return variantSourceProvider;
    }

    @Nullable
    @Override
    public SourceProvider getMultiFlavorSourceProvider() {
        return multiFlavorSourceProviders;
    }

    @NonNull
    @Override
    public Collection<File> getGeneratedSourceFolders() {
        return generatedSourceFolders;
    }
}
