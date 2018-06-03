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

package com.android.build.gradle.internal.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.NdkCompile;
import com.android.build.gradle.tasks.RenderscriptCompile;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SourceProvider;
import com.google.common.collect.Lists;

import org.gradle.api.Task;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Base class for variants.
 *
 * This is a wrapper around the internal data model, in order to control what is accessible
 * through the external API.
 */
abstract class BaseVariantImpl implements BaseVariant {

    @NonNull
    protected AndroidBuilder androidBuilder;

    @NonNull
    protected ReadOnlyObjectProvider readOnlyObjectProvider;

    protected List<BaseVariantOutput> outputs = Lists.newArrayListWithExpectedSize(1);

    BaseVariantImpl(
            @NonNull AndroidBuilder androidBuilder,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        this.androidBuilder = androidBuilder;
        this.readOnlyObjectProvider = readOnlyObjectProvider;
    }

    @NonNull
    protected abstract BaseVariantData<?> getVariantData();

    public void addOutputs(@NonNull List<BaseVariantOutput> outputs) {
       this.outputs.addAll(outputs);
    }

    @Override
    @NonNull
    public String getName() {
        return getVariantData().getVariantConfiguration().getFullName();
    }

    @Override
    @NonNull
    public String getDescription() {
        return getVariantData().getDescription();
    }

    @Override
    @NonNull
    public String getDirName() {
        return getVariantData().getVariantConfiguration().getDirName();
    }

    @Override
    @NonNull
    public String getBaseName() {
        return getVariantData().getVariantConfiguration().getBaseName();
    }

    @NonNull
    @Override
    public String getFlavorName() {
        return getVariantData().getVariantConfiguration().getFlavorName();
    }

    @NonNull
    @Override
    public List<BaseVariantOutput> getOutputs() {
        return outputs;
    }

    @Override
    @NonNull
    public BuildType getBuildType() {
        return readOnlyObjectProvider.getBuildType(
                getVariantData().getVariantConfiguration().getBuildType());
    }

    @Override
    @NonNull
    public List<ProductFlavor> getProductFlavors() {
        return new ImmutableFlavorList(
                getVariantData().getVariantConfiguration().getProductFlavors(),
                readOnlyObjectProvider);
    }

    @Override
    @NonNull
    public ProductFlavor getMergedFlavor() {
        return getVariantData().getVariantConfiguration().getMergedFlavor();
    }

    @NonNull
    @Override
    public List<SourceProvider> getSourceSets() {
        return getVariantData().getVariantConfiguration().getSortedSourceProviders();
    }

    @Override
    @NonNull
    public String getApplicationId() {
        return getVariantData().getApplicationId();
    }

    @Override
    @NonNull
    public Task getPreBuild() {
        return getVariantData().preBuildTask;
    }

    @Override
    @NonNull
    public Task getCheckManifest() {
        return getVariantData().checkManifestTask;
    }

    @Override
    @NonNull
    public AidlCompile getAidlCompile() {
        return getVariantData().aidlCompileTask;
    }

    @Override
    @NonNull
    public RenderscriptCompile getRenderscriptCompile() {
        return getVariantData().renderscriptCompileTask;
    }

    @Override
    public MergeResources getMergeResources() {
        return getVariantData().mergeResourcesTask;
    }

    @Override
    public MergeAssets getMergeAssets() {
        return getVariantData().mergeAssetsTask;
    }

    @Override
    public GenerateBuildConfig getGenerateBuildConfig() {
        return getVariantData().generateBuildConfigTask;
    }

    @Override
    @Nullable
    public JavaCompile getJavaCompile() {
        return getVariantData().javacTask;
    }

    @NonNull
    @Override
    public AbstractCompile getJavaCompiler() {
        return getVariantData().javaCompilerTask;
    }

    @NonNull
    @Override
    public NdkCompile getNdkCompile() {
        return getVariantData().ndkCompileTask;
    }

    @Nullable
    @Override
    public Task getObfuscation() {
        return getVariantData().obfuscationTask;
    }

    @Nullable
    @Override
    public File getMappingFile() {
        return getVariantData().getMappingFile();
    }

    @Override
    @NonNull
    public Sync getProcessJavaResources() {
        return getVariantData().processJavaResourcesTask;
    }

    @Override
    @Nullable
    public Task getAssemble() {
        return getVariantData().assembleVariantTask;
    }

    @Override
    public void addJavaSourceFoldersToModel(@NonNull File... generatedSourceFolders) {
        getVariantData().addJavaSourceFoldersToModel(generatedSourceFolders);
    }

    @Override
    public void addJavaSourceFoldersToModel(@NonNull Collection<File> generatedSourceFolders) {
        getVariantData().addJavaSourceFoldersToModel(generatedSourceFolders);
    }

    @Override
    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull File... sourceFolders) {
        getVariantData().registerJavaGeneratingTask(task, sourceFolders);
    }

    @Override
    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull Collection<File> sourceFolders) {
        getVariantData().registerJavaGeneratingTask(task, sourceFolders);
    }

    @Override
    public void registerResGeneratingTask(@NonNull Task task, @NonNull File... generatedResFolders) {
        getVariantData().registerResGeneratingTask(task, generatedResFolders);
    }

    @Override
    public void registerResGeneratingTask(@NonNull Task task, @NonNull Collection<File> generatedResFolders) {
        getVariantData().registerResGeneratingTask(task, generatedResFolders);
    }

    @Override
    public void buildConfigField(@NonNull String type, @NonNull String name,
            @NonNull String value) {
        getVariantData().getVariantConfiguration().addBuildConfigField(type, name, value);
    }

    @Override
    public void resValue(@NonNull String type, @NonNull String name, @NonNull String value) {
        getVariantData().getVariantConfiguration().addResValue(type, name, value);
    }

    @Override
    public void setOutputsAreSigned(boolean isSigned) {
        getVariantData().outputsAreSigned = isSigned;
    }

    @Override
    public boolean getOutputsAreSigned() {
        return getVariantData().outputsAreSigned;
    }
}
