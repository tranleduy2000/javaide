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
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidArtifactOutput;
import com.android.builder.model.ClassField;
import com.android.builder.model.Dependencies;
import com.android.builder.model.NativeLibrary;
import com.android.builder.model.SourceProvider;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of AndroidArtifact that is serializable
 */
public class AndroidArtifactImpl extends BaseArtifactImpl implements AndroidArtifact, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final Collection<AndroidArtifactOutput> outputs;
    private final boolean isSigned;
    @Nullable
    private final String signingConfigName;
    @NonNull
    private final String applicationId;
    @NonNull
    private final String sourceGenTaskName;

    @NonNull
    private final List<File> generatedResourceFolders;
    @Nullable
    private final Set<String> abiFilters;
    @NonNull
    private final Collection<NativeLibrary> nativeLibraries;
    @NonNull
    private final Map<String, ClassField> buildConfigFields;
    @NonNull
    private final Map<String, ClassField> resValues;

    AndroidArtifactImpl(
            @NonNull String name,
            @NonNull Collection<AndroidArtifactOutput> outputs,
            @NonNull String assembleTaskName,
            boolean isSigned,
            @Nullable String signingConfigName,
            @NonNull String applicationId,
            @NonNull String sourceGenTaskName,
            @NonNull String compileTaskName,
            @NonNull List<File> generatedSourceFolders,
            @NonNull List<File> generatedResourceFolders,
            @NonNull File classesFolder,
            @NonNull File javaResourcesFolder,
            @NonNull Dependencies dependencies,
            @Nullable SourceProvider variantSourceProvider,
            @Nullable SourceProvider multiFlavorSourceProviders,
            @Nullable Set<String> abiFilters,
            @NonNull Collection<NativeLibrary> nativeLibraries,
            @NonNull Map<String,ClassField> buildConfigFields,
            @NonNull Map<String,ClassField> resValues) {
        super(name, assembleTaskName, compileTaskName, classesFolder, javaResourcesFolder,
                dependencies, variantSourceProvider, multiFlavorSourceProviders,
                generatedSourceFolders);

        this.outputs = outputs;
        this.isSigned = isSigned;
        this.signingConfigName = signingConfigName;
        this.applicationId = applicationId;
        this.sourceGenTaskName = sourceGenTaskName;
        this.generatedResourceFolders = generatedResourceFolders;
        this.abiFilters = abiFilters;
        this.nativeLibraries = nativeLibraries;
        this.buildConfigFields = buildConfigFields;
        this.resValues = resValues;
    }

    @NonNull
    @Override
    public Collection<AndroidArtifactOutput> getOutputs() {
        return outputs;
    }

    @Override
    public boolean isSigned() {
        return isSigned;
    }

    @Nullable
    @Override
    public String getSigningConfigName() {
        return signingConfigName;
    }

    @NonNull
    @Override
    public String getApplicationId() {
        return applicationId;
    }

    @NonNull
    @Override
    public String getSourceGenTaskName() {
        return sourceGenTaskName;
    }

    @NonNull
    @Override
    public Set<String> getIdeSetupTaskNames() {
        return Sets.newHashSet(getSourceGenTaskName());
    }

    @NonNull
    @Override
    public List<File> getGeneratedResourceFolders() {
        return generatedResourceFolders;
    }

    @Nullable
    @Override
    public Set<String> getAbiFilters() {
        return abiFilters;
    }

    @NonNull
    @Override
    public Collection<NativeLibrary> getNativeLibraries() {
        return nativeLibraries;
    }

    @NonNull
    @Override
    public Map<String, ClassField> getBuildConfigFields() {
        return buildConfigFields;
    }

    @NonNull
    @Override
    public Map<String, ClassField> getResValues() {
        return resValues;
    }
}
