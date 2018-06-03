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

import static com.android.build.gradle.internal.TaskManager.DIR_BUNDLES;
import static com.android.builder.model.AndroidProject.FD_GENERATED;
import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.coverage.JacocoInstrumentTask;
import com.android.build.gradle.internal.tasks.CheckManifest;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.tasks.MergeJavaResourcesTask;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.BinaryFileProviderTask;
import com.android.build.gradle.tasks.Dex;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.GenerateResValues;
import com.android.build.gradle.tasks.JackTask;
import com.android.build.gradle.tasks.JavaResourcesProvider;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.NdkCompile;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.build.gradle.tasks.RenderscriptCompile;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.signing.SignedJarBuilder;
import com.android.utils.FileUtils;
import com.android.utils.StringHelper;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A scope containing data for a specific variant.
 */
public class VariantScope {

    @NonNull
    private GlobalScope globalScope;
    @NonNull
    private BaseVariantData<? extends BaseVariantOutputData> variantData;

    @Nullable
    private Collection<Object> ndkBuildable;
    @Nullable
    private Collection<File> ndkSoFolder;
    @Nullable
    private File ndkObjFolder;
    @NonNull
    private Map<Abi, File> ndkDebuggableLibraryFolders = Maps.newHashMap();

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

    private AndroidTask<RenderscriptCompile> renderscriptCompileTask;
    private AndroidTask<AidlCompile> aidlCompileTask;
    @Nullable
    private AndroidTask<MergeResources> mergeResourcesTask;
    @Nullable
    private AndroidTask<MergeAssets> mergeAssetsTask;
    private AndroidTask<GenerateBuildConfig> generateBuildConfigTask;
    private AndroidTask<GenerateResValues> generateResValuesTask;

    @Nullable
    private AndroidTask<Dex> dexTask;
    @Nullable
    private AndroidTask jacocoIntrumentTask;

    private AndroidTask<Sync> processJavaResourcesTask;
    private AndroidTask<MergeJavaResourcesTask> mergeJavaResourcesTask;
    private JavaResourcesProvider javaResourcesProvider;
    private AndroidTask<NdkCompile> ndkCompileTask;

    /** @see BaseVariantData#javaCompilerTask */
    @Nullable
    private AndroidTask<? extends AbstractCompile> javaCompilerTask;
    @Nullable
    private AndroidTask<JavaCompile> javacTask;
    @Nullable
    private AndroidTask<JackTask> jackTask;

    // empty anchor compile task to set all compilations tasks as dependents.
    private AndroidTask<Task> compileTask;
    private AndroidTask<JacocoInstrumentTask> jacocoInstrumentTask;

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

    @Nullable
    public Collection<Object> getNdkBuildable() {
        return ndkBuildable;
    }

    public void setNdkBuildable(@NonNull Collection<Object> ndkBuildable) {
        this.ndkBuildable = ndkBuildable;
    }

    @Nullable
    public Collection<File> getNdkSoFolder() {
        return ndkSoFolder;
    }

    public void setNdkSoFolder(@NonNull Collection<File> ndkSoFolder) {
        this.ndkSoFolder = ndkSoFolder;
    }

    @Nullable
    public File getNdkObjFolder() {
        return ndkObjFolder;
    }

    public void setNdkObjFolder(@NonNull File ndkObjFolder) {
        this.ndkObjFolder = ndkObjFolder;
    }

    /**
     * Return the folder containing the shared object with debugging symbol for the specified ABI.
     */
    @Nullable
    public File getNdkDebuggableLibraryFolders(@NonNull Abi abi) {
        return ndkDebuggableLibraryFolders.get(abi);
    }

    public void addNdkDebuggableLibraryFolders(@NonNull Abi abi, @NonNull File searchPath) {
        this.ndkDebuggableLibraryFolders.put(abi, searchPath);
    }

    @NonNull
    public Set<File> getJniFolders() {
        assert getNdkSoFolder() != null;

        VariantConfiguration config = getVariantConfiguration();
        ApkVariantData apkVariantData = (ApkVariantData) variantData;
        // for now only the project's compilation output.
        Set<File> set = Sets.newHashSet();
        set.addAll(getNdkSoFolder());
        set.add(getRenderscriptLibOutputDir());
        set.addAll(config.getLibraryJniFolders());
        set.addAll(config.getJniLibsList());

        if (config.getMergedFlavor().getRenderscriptSupportModeEnabled() != null &&
                config.getMergedFlavor().getRenderscriptSupportModeEnabled()) {
            File rsLibs = globalScope.getAndroidBuilder().getSupportNativeLibFolder();
            if (rsLibs != null && rsLibs.isDirectory()) {
                set.add(rsLibs);
            }
        }
        return set;
    }

    @Nullable
    public BaseVariantData getTestedVariantData() {
        return variantData instanceof TestVariantData ?
                (BaseVariantData) ((TestVariantData) variantData).getTestedVariantData() :
                null;
    }


    // Precomputed file paths.

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
    public File getRenderscriptSourceOutputDir() {
        return new File(globalScope.getGeneratedDir(),
                "source/rs/" + variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getRenderscriptLibOutputDir() {
        return new File(globalScope.getGeneratedDir(),
                "rs/" + variantData.getVariantConfiguration().getDirName() + "/lib");
    }

    @NonNull
    public File getSymbolLocation() {
        return new File(globalScope.getIntermediatesDir() + "/symbols/" +
                variantData.getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getFinalResourcesDir() {
        return Objects.firstNonNull(resourceOutputDir, getDefaultMergeResourcesOutputDir());
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
        return new File(globalScope.getBuildDir() + "/"  + FD_GENERATED + "/source/buildConfig/"
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
    public File getRenderscriptResOutputDir() {
        return getGeneratedResourcesDir("rs");
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

    /**
     * Returns the location of an intermediate directory that can be used by the Jack toolchain
     * to store states necessary to support incremental compilation.
     * @return a variant specific directory.
     */
    @NonNull
    public File getJackIncrementalDir() {
        return new File(globalScope.getIntermediatesDir(),
                "incremental/jack/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getJackTempDir() {
        return new File(globalScope.getIntermediatesDir(),
                "tmp/jack/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getJillPackagedLibrariesDir() {
        return new File(globalScope.getIntermediatesDir(),
                "jill/" + getVariantConfiguration().getDirName() + "/packaged");
    }

    @NonNull
    public File getJillRuntimeLibrariesDir() {
        return new File(globalScope.getIntermediatesDir(),
                "jill/" + getVariantConfiguration().getDirName() + "/runtime");
    }

    @NonNull
    public File getJackDestinationDir() {
        return new File(globalScope.getIntermediatesDir(),
                "dex/" + getVariantConfiguration().getDirName());
    }

    @NonNull
    public File getJackClassesZip() {
        return new File(globalScope.getIntermediatesDir(),
                "packaged/" + getVariantConfiguration().getDirName() + "/classes.zip");
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

    public File getMappingFile() {
        return new File(globalScope.getOutputsDir(),
                "/mapping/" + getVariantConfiguration().getDirName() + "/mapping.txt");
    }

    // Tasks getters/setters.

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

    public AndroidTask<RenderscriptCompile> getRenderscriptCompileTask() {
        return renderscriptCompileTask;
    }

    public void setRenderscriptCompileTask(
            AndroidTask<RenderscriptCompile> renderscriptCompileTask) {
        this.renderscriptCompileTask = renderscriptCompileTask;
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

    SignedJarBuilder.IZipEntryFilter packagingOptionsFilter;

    public void setPackagingOptionsFilter(SignedJarBuilder.IZipEntryFilter filter) {
        this.packagingOptionsFilter = filter;
    }

    /**
     * Returns the {@link SignedJarBuilder.IZipEntryFilter} instance
     * that manages all resources inclusion in the final APK following the rules defined in
     * {@link com.android.builder.model.PackagingOptions} settings.
     */
    public SignedJarBuilder.IZipEntryFilter getPackagingOptionsFilter() {
        return packagingOptionsFilter;
    }

    public void setMergeJavaResourcesTask(AndroidTask<MergeJavaResourcesTask> mergeJavaResourcesTask) {
        this.mergeJavaResourcesTask = mergeJavaResourcesTask;
    }

    /**
     * Returns the task extracting java resources from libraries and merging those with java
     * resources coming from the variant's source folders.
     * @return the task merging resources.
     */
    public AndroidTask<MergeJavaResourcesTask> getMergeJavaResourcesTask() {
        return mergeJavaResourcesTask;
    }

    public void setJavaResourcesProvider(JavaResourcesProvider javaResourcesProvider) {
        this.javaResourcesProvider = javaResourcesProvider;
    }

    /**
     * Returns the {@link JavaResourcesProvider} responsible for providing final merged and possibly
     * obfuscated java resources for inclusion in the final APK. The provider might change during
     * the variant build process.
     * @return the java resources provider.
     */
    public JavaResourcesProvider getJavaResourcesProvider() {
        return javaResourcesProvider;
    }

    @Nullable
    public AndroidTask<? extends AbstractCompile> getJavaCompilerTask() {
        return javaCompilerTask;
    }

    @Nullable
    public AndroidTask<JackTask> getJackTask() {
        return jackTask;
    }

    public void setJackTask(
            @Nullable AndroidTask<JackTask> jackTask) {
        this.jackTask = jackTask;
    }

    @Nullable
    public AndroidTask<JavaCompile> getJavacTask() {
        return javacTask;
    }

    public void setJavacTask(
            @Nullable AndroidTask<JavaCompile> javacTask) {
        this.javacTask = javacTask;
    }

    public void setJavaCompilerTask(
            @NonNull AndroidTask<? extends AbstractCompile> javaCompileTask) {
        this.javaCompilerTask = javaCompileTask;
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
