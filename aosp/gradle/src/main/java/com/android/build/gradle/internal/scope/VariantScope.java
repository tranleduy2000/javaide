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

package com.android.build.gradle.internal.scope;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.tasks.CheckManifest;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.tasks.MergeJavaResourcesTask;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.BinaryFileProviderTask;
import com.android.build.gradle.tasks.Dex;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.GenerateResValues;
import com.android.build.gradle.tasks.JavaResourcesProvider;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.signing.SignedJarBuilder;
import com.android.utils.FileUtils;
import com.android.utils.StringHelper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Set;

import static com.android.build.gradle.internal.TaskManager.DIR_BUNDLES;
import static com.android.builder.model.AndroidProject.FD_GENERATED;
import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

/**
 * A scope containing data for a specific variant.
 */
public class VariantScope {

    SignedJarBuilder.IZipEntryFilter packagingOptionsFilter;
    @NonNull
    private GlobalScope globalScope;
    @NonNull
    private BaseVariantData<? extends BaseVariantOutputData> variantData;
    @Nullable
    private File mergeResourceOutputDir;
    // Tasks
    private AndroidTask<Task> preBuildTask;
    private AndroidTask<PrepareDependenciesTask> prepareDependenciesTask;
    private AndroidTask<ProcessAndroidResources> generateRClassTask;
    private AndroidTask<Task> sourceGenTask;
    private AndroidTask<Task> resourceGenTask;
    private AndroidTask<Task> assetGenTask;
    private AndroidTask<CheckManifest> checkManifestTask;
    private AndroidTask<AidlCompile> aidlCompileTask;
    @Nullable
    private AndroidTask<MergeResources> mergeResourcesTask;
    @Nullable
    private AndroidTask<MergeAssets> mergeAssetsTask;
    private AndroidTask<GenerateBuildConfig> generateBuildConfigTask;
    private AndroidTask<GenerateResValues> generateResValuesTask;
    @Nullable
    private AndroidTask<Dex> dexTask;
    private AndroidTask<Sync> processJavaResourcesTask;
    private AndroidTask<MergeJavaResourcesTask> mergeJavaResourcesTask;
    private JavaResourcesProvider javaResourcesProvider;
    /**
     * @see BaseVariantData#javaCompilerTask
     */
    @Nullable
    private AndroidTask<? extends AbstractCompile> javaCompilerTask;
    @Nullable
    private AndroidTask<JavaCompile> javacTask;
    // empty anchor compile task to set all compilations tasks as dependents.
    private AndroidTask<Task> compileTask;
    private FileSupplier mappingFileProviderTask;
    private AndroidTask<BinaryFileProviderTask> binayFileProviderTask;
    // TODO : why is Jack not registered as the obfuscationTask ???
    private AndroidTask<? extends Task> obfuscationTask;
    private File resourceOutputDir;

    public VariantScope(
            @NonNull GlobalScope globalScope,
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        this.globalScope = globalScope;
        this.variantData = variantData;
    }

    @NonNull
    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    @NonNull
    public BaseVariantData<? extends BaseVariantOutputData> getVariantData() {
        return variantData;
    }

    @NonNull
    public GradleVariantConfiguration getVariantConfiguration() {
        return variantData.getVariantConfiguration();
    }

    @NonNull
    public String getTaskName(@NonNull String prefix) {
        return getTaskName(prefix, "");
    }

    @NonNull
    public String getTaskName(@NonNull String prefix, @NonNull String suffix) {
        return prefix + StringHelper.capitalize(getVariantConfiguration().getFullName()) + suffix;
    }


    // Precomputed file paths.

    @NonNull
    public Set<File> getJniFolders() {
        VariantConfiguration config = getVariantConfiguration();
        ApkVariantData apkVariantData = (ApkVariantData) variantData;
        // for now only the project's compilation output.
        Set<File> set = Sets.newHashSet();
        set.addAll(config.getLibraryJniFolders());
        set.addAll(config.getJniLibsList());

        return set;
    }

    @NonNull
    public File getDexOutputFolder() {
        return new File(globalScope.getIntermediatesDir(), "/dex/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public FileCollection getJavaClasspath() {
        return getGlobalScope().getProject().files(
                getGlobalScope().getAndroidBuilder().getCompileClasspath(
                        getVariantData().getVariantConfiguration()));
    }

    @NonNull
    public File getJavaOutputDir() {
        return new File(globalScope.getIntermediatesDir(), "/classes/" +
                variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getJavaDependencyCache() {
        return new File(globalScope.getIntermediatesDir(), "/dependency-cache/" +
                variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getPreDexOutputDir() {
        return new File(globalScope.getIntermediatesDir(), "/pre-dexed/" +
                getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getProguardOutputFile() {
        return (variantData instanceof LibraryVariantData) ?
                new File(globalScope.getIntermediatesDir(),
                        DIR_BUNDLES + "/" + getVariantConfiguration().getDirName()
                                + "/classes.jar") :
                new File(globalScope.getIntermediatesDir(),
                        "/classes-proguard/" + getVariantConfiguration().getDirName()
                                + "/classes.jar");
    }

    @NonNull
    public File getProguardComponentsJarFile() {
        return new File(globalScope.getIntermediatesDir(), "multi-dex/" + getVariantConfiguration().getDirName()
                + "/componentClasses.jar");
    }

    @NonNull
    public File getJarMergingOutputFile() {
        return new File(globalScope.getIntermediatesDir(), "multi-dex/" + getVariantConfiguration().getDirName()
                + "/allclasses.jar");
    }

    @NonNull
    public File getManifestKeepListFile() {
        return new File(globalScope.getIntermediatesDir(), "multi-dex/" + getVariantConfiguration().getDirName()
                + "/manifest_keep.txt");
    }

    @NonNull
    public File getMainDexListFile() {
        return new File(globalScope.getIntermediatesDir(), "multi-dex/" + getVariantConfiguration().getDirName()
                + "/maindexlist.txt");
    }

    @NonNull
    public File getSymbolLocation() {
        return new File(globalScope.getIntermediatesDir() + "/symbols/" +
                variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getFinalResourcesDir() {
        return MoreObjects.firstNonNull(resourceOutputDir, getDefaultMergeResourcesOutputDir());
    }

    public void setResourceOutputDir(@NonNull File resourceOutputDir) {
        this.resourceOutputDir = resourceOutputDir;
    }

    @NonNull
    public File getDefaultMergeResourcesOutputDir() {
        return new File(globalScope.getIntermediatesDir(),
                "/res/merged/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getMergeResourcesOutputDir() {
        if (mergeResourceOutputDir == null) {
            return getDefaultMergeResourcesOutputDir();
        }
        return mergeResourceOutputDir;
    }

    public void setMergeResourceOutputDir(@Nullable File mergeResourceOutputDir) {
        this.mergeResourceOutputDir = mergeResourceOutputDir;
    }

    @NonNull
    public File getMergeAssetsOutputDir() {
        return getVariantConfiguration().getType() == VariantType.LIBRARY ?
                new File(globalScope.getIntermediatesDir(),
                        DIR_BUNDLES + "/" + getVariantConfiguration().getDirName() +
                                "/assets") :
                new File(globalScope.getIntermediatesDir(),
                        "/assets/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getBuildConfigSourceOutputDir() {
        return new File(globalScope.getBuildDir() + "/" + FD_GENERATED + "/source/buildConfig/"
                + variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getGeneratedResourcesDir(String name) {
        return FileUtils.join(
                globalScope.getGeneratedDir(),
                "res",
                name,
                getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getGeneratedResOutputDir() {
        return getGeneratedResourcesDir("resValues");
    }

    @NonNull
    public File getGeneratedPngsOutputDir() {
        return getGeneratedResourcesDir("pngs");
    }

    @NonNull
    public File getPackagedJarsJavaResDestinationDir() {
        return new File(globalScope.getIntermediatesDir(),
                "packagedJarsJavaResources/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getSourceFoldersJavaResDestinationDir() {
        return new File(globalScope.getIntermediatesDir(),
                "sourceFolderJavaResources/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getJavaResourcesDestinationDir() {
        return new File(globalScope.getIntermediatesDir(),
                "javaResources/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getRClassSourceOutputDir() {
        return new File(globalScope.getGeneratedDir(),
                "source/r/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getAidlSourceOutputDir() {
        return new File(globalScope.getGeneratedDir(),
                "source/aidl/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getAidlIncrementalDir() {
        return new File(globalScope.getIntermediatesDir(),
                "incremental/aidl/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getAidlParcelableDir() {
        return new File(globalScope.getIntermediatesDir(),
                DIR_BUNDLES + "/" + getVariantConfiguration().getDirName() + "/aidl");
    }

    @NonNull
    public File getProguardOutputFolder() {
        return new File(globalScope.getBuildDir(), "/" + FD_OUTPUTS + "/mapping/" +
                getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getProcessAndroidResourcesProguardOutputFile() {
        return new File(globalScope.getIntermediatesDir(),
                "/proguard-rules/" + getVariantConfiguration().getDirName() + "/aapt_rules.txt");
    }

    // Tasks getters/setters.

    public File getMappingFile() {
        return new File(globalScope.getOutputsDir(),
                "/mapping/" + getVariantConfiguration().getDirName() + "/mapping.txt");
    }

    public AndroidTask<Task> getPreBuildTask() {
        return preBuildTask;
    }

    public void setPreBuildTask(
            AndroidTask<Task> preBuildTask) {
        this.preBuildTask = preBuildTask;
    }

    public AndroidTask<PrepareDependenciesTask> getPrepareDependenciesTask() {
        return prepareDependenciesTask;
    }

    public void setPrepareDependenciesTask(
            AndroidTask<PrepareDependenciesTask> prepareDependenciesTask) {
        this.prepareDependenciesTask = prepareDependenciesTask;
    }

    public AndroidTask<ProcessAndroidResources> getGenerateRClassTask() {
        return generateRClassTask;
    }

    public void setGenerateRClassTask(
            AndroidTask<ProcessAndroidResources> generateRClassTask) {
        this.generateRClassTask = generateRClassTask;
    }

    public AndroidTask<Task> getSourceGenTask() {
        return sourceGenTask;
    }

    public void setSourceGenTask(
            AndroidTask<Task> sourceGenTask) {
        this.sourceGenTask = sourceGenTask;
    }

    public AndroidTask<Task> getResourceGenTask() {
        return resourceGenTask;
    }

    public void setResourceGenTask(
            AndroidTask<Task> resourceGenTask) {
        this.resourceGenTask = resourceGenTask;
    }

    public AndroidTask<Task> getAssetGenTask() {
        return assetGenTask;
    }

    public void setAssetGenTask(
            AndroidTask<Task> assetGenTask) {
        this.assetGenTask = assetGenTask;
    }

    public AndroidTask<CheckManifest> getCheckManifestTask() {
        return checkManifestTask;
    }

    public void setCheckManifestTask(
            AndroidTask<CheckManifest> checkManifestTask) {
        this.checkManifestTask = checkManifestTask;
    }

    public AndroidTask<AidlCompile> getAidlCompileTask() {
        return aidlCompileTask;
    }

    public void setAidlCompileTask(
            AndroidTask<AidlCompile> aidlCompileTask) {
        this.aidlCompileTask = aidlCompileTask;
    }

    @Nullable
    public AndroidTask<MergeResources> getMergeResourcesTask() {
        return mergeResourcesTask;
    }

    public void setMergeResourcesTask(
            @Nullable AndroidTask<MergeResources> mergeResourcesTask) {
        this.mergeResourcesTask = mergeResourcesTask;
    }

    @Nullable
    public AndroidTask<MergeAssets> getMergeAssetsTask() {
        return mergeAssetsTask;
    }

    public void setMergeAssetsTask(
            @Nullable AndroidTask<MergeAssets> mergeAssetsTask) {
        this.mergeAssetsTask = mergeAssetsTask;
    }

    public AndroidTask<GenerateBuildConfig> getGenerateBuildConfigTask() {
        return generateBuildConfigTask;
    }

    public void setGenerateBuildConfigTask(
            AndroidTask<GenerateBuildConfig> generateBuildConfigTask) {
        this.generateBuildConfigTask = generateBuildConfigTask;
    }

    public AndroidTask<GenerateResValues> getGenerateResValuesTask() {
        return generateResValuesTask;
    }

    public void setGenerateResValuesTask(
            AndroidTask<GenerateResValues> generateResValuesTask) {
        this.generateResValuesTask = generateResValuesTask;
    }

    @Nullable
    public AndroidTask<Dex> getDexTask() {
        return dexTask;
    }

    public void setDexTask(@Nullable AndroidTask<Dex> dexTask) {
        this.dexTask = dexTask;
    }

    public AndroidTask<Sync> getProcessJavaResourcesTask() {
        return processJavaResourcesTask;
    }

    public void setProcessJavaResourcesTask(
            AndroidTask<Sync> processJavaResourcesTask) {
        this.processJavaResourcesTask = processJavaResourcesTask;
    }

    /**
     * Returns the {@link SignedJarBuilder.IZipEntryFilter} instance
     * that manages all resources inclusion in the final APK following the rules defined in
     * {@link com.android.builder.model.PackagingOptions} settings.
     */
    public SignedJarBuilder.IZipEntryFilter getPackagingOptionsFilter() {
        return packagingOptionsFilter;
    }

    public void setPackagingOptionsFilter(SignedJarBuilder.IZipEntryFilter filter) {
        this.packagingOptionsFilter = filter;
    }

    /**
     * Returns the task extracting java resources from libraries and merging those with java
     * resources coming from the variant's source folders.
     *
     * @return the task merging resources.
     */
    public AndroidTask<MergeJavaResourcesTask> getMergeJavaResourcesTask() {
        return mergeJavaResourcesTask;
    }

    public void setMergeJavaResourcesTask(AndroidTask<MergeJavaResourcesTask> mergeJavaResourcesTask) {
        this.mergeJavaResourcesTask = mergeJavaResourcesTask;
    }

    /**
     * Returns the {@link JavaResourcesProvider} responsible for providing final merged and possibly
     * obfuscated java resources for inclusion in the final APK. The provider might change during
     * the variant build process.
     *
     * @return the java resources provider.
     */
    public JavaResourcesProvider getJavaResourcesProvider() {
        return javaResourcesProvider;
    }

    public void setJavaResourcesProvider(JavaResourcesProvider javaResourcesProvider) {
        this.javaResourcesProvider = javaResourcesProvider;
    }

    @Nullable
    public AndroidTask<? extends AbstractCompile> getJavaCompilerTask() {
        return javaCompilerTask;
    }

    public void setJavaCompilerTask(
            @NonNull AndroidTask<? extends AbstractCompile> javaCompileTask) {
        this.javaCompilerTask = javaCompileTask;
    }

    @Nullable
    public AndroidTask<JavaCompile> getJavacTask() {
        return javacTask;
    }

    public void setJavacTask(
            @Nullable AndroidTask<JavaCompile> javacTask) {
        this.javacTask = javacTask;
    }

    public AndroidTask<Task> getCompileTask() {
        return compileTask;
    }

    public void setCompileTask(
            AndroidTask<Task> compileTask) {
        this.compileTask = compileTask;
    }

    public AndroidTask<? extends Task> getObfuscationTask() {
        return obfuscationTask;
    }

    public void setObfuscationTask(
            AndroidTask<? extends Task> obfuscationTask) {
        this.obfuscationTask = obfuscationTask;
    }
}
