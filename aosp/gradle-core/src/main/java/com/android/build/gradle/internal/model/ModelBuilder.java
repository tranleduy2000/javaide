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

package com.android.build.gradle.internal.model;

import static com.android.builder.model.AndroidProject.ARTIFACT_MAIN;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.OutputFile;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.api.ApkOutputFile;
import com.android.build.gradle.internal.BuildTypeData;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.ProductFlavorData;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.dsl.CoreNdkOptions;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.ApkVariantOutputData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.build.gradle.internal.variant.TestedVariantData;
import com.android.builder.Version;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantType;
import com.android.builder.model.AaptOptions;
import com.android.builder.model.AndroidArtifact;
import com.android.builder.model.AndroidArtifactOutput;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ArtifactMetaData;
import com.android.builder.model.JavaArtifact;
import com.android.builder.model.LintOptions;
import com.android.builder.model.NativeLibrary;
import com.android.builder.model.NativeToolchain;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;
import com.android.builder.model.SourceProvider;
import com.android.builder.model.SourceProviderContainer;
import com.android.builder.model.SyncIssue;
import com.android.sdklib.AndroidVersion;
import com.android.sdklib.IAndroidTarget;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Builder for the custom Android model.
 */
public class ModelBuilder implements ToolingModelBuilder {

    @NonNull
    private final AndroidBuilder androidBuilder;
    @NonNull
    private final AndroidConfig config;
    @NonNull
    private final ExtraModelInfo extraModelInfo;
    @NonNull
    private final VariantManager variantManager;
    @NonNull
    private final TaskManager taskManager;
    @NonNull
    private final NdkHandler ndkHandler;
    @NonNull
    private Map<Abi, NativeToolchain> toolchains;
    @NonNull
    private NativeLibraryFactory nativeLibFactory;

    private final boolean isLibrary;

    public ModelBuilder(
            @NonNull AndroidBuilder androidBuilder,
            @NonNull VariantManager variantManager,
            @NonNull TaskManager taskManager,
            @NonNull AndroidConfig config,
            @NonNull ExtraModelInfo extraModelInfo,
            @NonNull NdkHandler ndkHandler,
            @NonNull NativeLibraryFactory nativeLibraryFactory,
            boolean isLibrary) {
        this.androidBuilder = androidBuilder;
        this.config = config;
        this.extraModelInfo = extraModelInfo;
        this.variantManager = variantManager;
        this.taskManager = taskManager;
        this.ndkHandler = ndkHandler;
        this.nativeLibFactory = nativeLibraryFactory;
        this.isLibrary = isLibrary;
    }

    public static void clearCaches() {
        DependenciesImpl.clearCaches();
    }

    @Override
    public boolean canBuild(String modelName) {
        // The default name for a model is the name of the Java interface.
        return modelName.equals(AndroidProject.class.getName());
    }

    @Override
    public Object buildAll(String modelName, Project project) {
        Collection<? extends SigningConfig> signingConfigs = config.getSigningConfigs();

        // Get the boot classpath. This will ensure the target is configured.
        List<String> bootClasspath = androidBuilder.getBootClasspathAsStrings();

        List<File> frameworkSource = Collections.emptyList();

        // List of extra artifacts, with all test variants added.
        List<ArtifactMetaData> artifactMetaDataList = Lists.newArrayList(
                extraModelInfo.getExtraArtifacts());

        for (VariantType variantType : VariantType.getTestingTypes()) {
            artifactMetaDataList.add(new ArtifactMetaDataImpl(
                    variantType.getArtifactName(),
                    true /*isTest*/,
                    variantType.getArtifactType()));
        }

        LintOptions lintOptions = com.android.build.gradle.internal.dsl.LintOptions.create(
                config.getLintOptions());

        AaptOptions aaptOptions = AaptOptionsImpl.create(config.getAaptOptions());

        List<SyncIssue> syncIssues = Lists.newArrayList(extraModelInfo.getSyncIssues().values());

        List<String> flavorDimensionList = (config.getFlavorDimensionList() != null ?
                config.getFlavorDimensionList() : Lists.<String>newArrayList());

        toolchains = createNativeToolchainModelMap(ndkHandler);

        DefaultAndroidProject androidProject = new DefaultAndroidProject(
                Version.ANDROID_GRADLE_PLUGIN_VERSION,
                project.getName(),
                flavorDimensionList,
                androidBuilder.getTarget() != null ? androidBuilder.getTarget().hashString() : "",
                bootClasspath,
                frameworkSource,
                cloneSigningConfigs(config.getSigningConfigs()),
                aaptOptions,
                artifactMetaDataList,
                findUnresolvedDependencies(syncIssues),
                syncIssues,
                config.getCompileOptions(),
                lintOptions,
                project.getBuildDir(),
                config.getResourcePrefix(),
                ImmutableList.copyOf(toolchains.values()),
                isLibrary,
                Version.BUILDER_MODEL_API_VERSION);

        androidProject.setDefaultConfig(ProductFlavorContainerImpl.createProductFlavorContainer(
                variantManager.getDefaultConfig(),
                extraModelInfo.getExtraFlavorSourceProviders(
                        variantManager.getDefaultConfig().getProductFlavor().getName())));

        for (BuildTypeData btData : variantManager.getBuildTypes().values()) {
            androidProject.addBuildType(BuildTypeContainerImpl.create(
                    btData,
                    extraModelInfo.getExtraBuildTypeSourceProviders(btData.getBuildType().getName())));
        }
        for (ProductFlavorData pfData : variantManager.getProductFlavors().values()) {
            androidProject.addProductFlavors(ProductFlavorContainerImpl.createProductFlavorContainer(
                    pfData,
                    extraModelInfo.getExtraFlavorSourceProviders(pfData.getProductFlavor().getName())));
        }

        for (BaseVariantData<? extends BaseVariantOutputData> variantData : variantManager.getVariantDataList()) {
            if (!variantData.getType().isForTesting()) {
                androidProject.addVariant(createVariant(variantData));
            }
        }

        return androidProject;
    }

    /**
     * Create a map of ABI to NativeToolchain
     */
    public static Map<Abi, NativeToolchain> createNativeToolchainModelMap(
            @NonNull NdkHandler ndkHandler) {
        if (!ndkHandler.isNdkDirConfigured()) {
            return ImmutableMap.of();
        }

        Map<Abi, NativeToolchain> toolchains = Maps.newHashMap();

        for (Abi abi : ndkHandler.getSupportedAbis()) {
            toolchains.put(
                    abi,
                    new NativeToolchainImpl(
                            ndkHandler.getToolchain().getName() + "-" + abi.getName(),
                            ndkHandler.getCCompiler(abi),
                            ndkHandler.getCppCompiler(abi)));
        }
        return toolchains;
    }

    @NonNull
    private VariantImpl createVariant(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        AndroidArtifact mainArtifact = createAndroidArtifact(ARTIFACT_MAIN, variantData);

        GradleVariantConfiguration variantConfiguration = variantData.getVariantConfiguration();

        String variantName = variantConfiguration.getFullName();

        List<AndroidArtifact> extraAndroidArtifacts = Lists.newArrayList(
                extraModelInfo.getExtraAndroidArtifacts(variantName));
        // Make sure all extra artifacts are serializable.
        Collection<JavaArtifact> extraJavaArtifacts = extraModelInfo.getExtraJavaArtifacts(
                variantName);
        List<JavaArtifact> clonedExtraJavaArtifacts = Lists.newArrayListWithCapacity(
                extraJavaArtifacts.size());
        for (JavaArtifact javaArtifact : extraJavaArtifacts) {
            clonedExtraJavaArtifacts.add(JavaArtifactImpl.clone(javaArtifact));
        }

        if (variantData instanceof TestedVariantData) {
            for (VariantType variantType : VariantType.getTestingTypes()) {
                TestVariantData testVariantData = ((TestedVariantData) variantData).getTestVariantData(variantType);
                if (testVariantData != null) {
                    VariantType type = testVariantData.getType();
                    if (type != null) {
                        switch (type) {
                            case ANDROID_TEST:
                                extraAndroidArtifacts.add(createAndroidArtifact(
                                        variantType.getArtifactName(),
                                        testVariantData));
                                break;
                            case UNIT_TEST:
                                clonedExtraJavaArtifacts.add(createUnitTestsJavaArtifact(
                                        variantType,
                                        testVariantData));
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Unsupported test variant type ${variantType}.");
                        }
                    }
                }
            }
        }

        // if the target is a codename, override the model value.
        ApiVersion sdkVersionOverride = null;

        // we know the getTargetInfo won't return null here.
        @SuppressWarnings("ConstantConditions")
        IAndroidTarget androidTarget = androidBuilder.getTargetInfo().getTarget();

        AndroidVersion version = androidTarget.getVersion();
        if (version.getCodename() != null) {
            sdkVersionOverride = ApiVersionImpl.clone(version);
        }

        return new VariantImpl(
                variantName,
                variantConfiguration.getBaseName(),
                variantConfiguration.getBuildType().getName(),
                getProductFlavorNames(variantData),
                ProductFlavorImpl.cloneFlavor(
                        variantConfiguration.getMergedFlavor(),
                        sdkVersionOverride,
                        sdkVersionOverride),
                mainArtifact,
                extraAndroidArtifacts,
                clonedExtraJavaArtifacts);
    }

    private JavaArtifactImpl createUnitTestsJavaArtifact(
            @NonNull VariantType variantType,
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        SourceProviders sourceProviders = determineSourceProviders(variantData);
        DependenciesImpl dependencies = DependenciesImpl.cloneDependencies(variantData,
                androidBuilder);

        List<File> extraGeneratedSourceFolders = variantData.getExtraGeneratedSourceFolders();
        return new JavaArtifactImpl(
                variantType.getArtifactName(),
                variantData.assembleVariantTask.getName(),
                variantData.compileTask.getName(),
                Sets.newHashSet(variantData.prepareDependenciesTask.getName(),
                        taskManager.createMockableJar.getName()),
                extraGeneratedSourceFolders != null ? extraGeneratedSourceFolders : Collections.<File>emptyList(),
                (variantData.javacTask != null) ?
                        variantData.javacTask.getDestinationDir() :
                        variantData.getScope().getJavaOutputDir(),
                variantData.getScope().getJavaResourcesDestinationDir(),
                taskManager.createMockableJar.getOutputFile(),
                dependencies,
                sourceProviders.variantSourceProvider,
                sourceProviders.multiFlavorSourceProvider);
    }

    /**
     * Create a NativeLibrary for each ABI.
     */
    private Collection<NativeLibrary> createNativeLibraries(
            @NonNull Collection<Abi> abis,
            @NonNull VariantScope scope) {
        Collection<NativeLibrary> nativeLibraries = Lists.newArrayListWithCapacity(abis.size());
        for (Abi abi : abis) {
            NativeToolchain toolchain = toolchains.get(abi);
            if (toolchain == null) {
                continue;
            }
            Optional<NativeLibrary> lib = nativeLibFactory.create(scope, toolchain.getName(), abi);
            if (lib.isPresent()) {
                nativeLibraries.add(lib.get());
            }
        }
        return nativeLibraries;
    }

    private AndroidArtifact createAndroidArtifact(
            @NonNull String name,
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        VariantScope scope = variantData.getScope();
        GradleVariantConfiguration variantConfiguration = variantData.getVariantConfiguration();

        SigningConfig signingConfig = variantConfiguration.getSigningConfig();
        String signingConfigName = null;
        if (signingConfig != null) {
            signingConfigName = signingConfig.getName();
        }

        SourceProviders sourceProviders = determineSourceProviders(variantData);

        // get the outputs
        List<? extends BaseVariantOutputData> variantOutputs = variantData.getOutputs();
        List<AndroidArtifactOutput> outputs = Lists.newArrayListWithCapacity(variantOutputs.size());

        CoreNdkOptions ndkConfig = variantData.getVariantConfiguration().getNdkConfig();
        Collection<NativeLibrary> nativeLibraries = ImmutableList.of();
        if (ndkHandler.getNdkDirectory() != null) {
            if (config.getSplits().getAbi().isEnable()) {
                nativeLibraries = createNativeLibraries(
                        config.getSplits().getAbi().isUniversalApk()
                                ? ndkHandler.getSupportedAbis()
                                : createAbiList(config.getSplits().getAbiFilters()),
                        scope);
            } else {
                if (ndkConfig.getAbiFilters() == null || ndkConfig.getAbiFilters().isEmpty()) {
                    nativeLibraries = createNativeLibraries(
                            ndkHandler.getSupportedAbis(),
                            scope);
                } else {
                    nativeLibraries = createNativeLibraries(
                            createAbiList(ndkConfig.getAbiFilters()),
                            scope);
                }
            }
        }

        for (BaseVariantOutputData variantOutputData : variantOutputs) {
            int intVersionCode;
            if (variantOutputData instanceof ApkVariantOutputData) {
                intVersionCode =  variantOutputData.getVersionCode();
            } else {
                Integer versionCode = variantConfiguration.getMergedFlavor().getVersionCode();
                intVersionCode = versionCode != null ? versionCode : 1;
            }

            ImmutableCollection.Builder<OutputFile> outputFiles = ImmutableList.builder();

            // add the main APK
            outputFiles.add(new OutputFileImpl(
                    variantOutputData.getMainOutputFile().getFilters(),
                    variantOutputData.getMainOutputFile().getType().name(),
                    variantOutputData.getOutputFile()));

            for (ApkOutputFile splitApk : variantOutputData.getOutputs()) {
                if (splitApk.getType() == OutputFile.OutputType.SPLIT) {
                    outputFiles.add(new OutputFileImpl(
                            splitApk.getFilters(), OutputFile.SPLIT, splitApk.getOutputFile()));
                }
            }

            // add the main APK.
            outputs.add(new AndroidArtifactOutputImpl(
                    outputFiles.build(),
                    "assemble" + variantOutputData.getFullName(),
                    variantOutputData.getScope().getManifestOutputFile(),
                    intVersionCode));
        }

        return new AndroidArtifactImpl(
                name,
                outputs,
                variantData.assembleVariantTask == null ? scope.getTaskName("assemble") : variantData.assembleVariantTask.getName(),
                variantConfiguration.isSigningReady() || variantData.outputsAreSigned,
                signingConfigName,
                variantConfiguration.getApplicationId(),
                // TODO: Need to determine the tasks' name when the tasks may not be created
                // in component plugin.
                scope.getSourceGenTask() == null ? scope.getTaskName("generate", "Sources") : scope.getSourceGenTask().getName(),
                scope.getCompileTask() == null ? scope.getTaskName("compile", "Sources") : scope.getCompileTask().getName(),
                getGeneratedSourceFolders(variantData),
                getGeneratedResourceFolders(variantData),
                (variantData.javacTask != null) ?
                        variantData.javacTask.getDestinationDir() :
                        scope.getJavaOutputDir(),
                scope.getJavaResourcesDestinationDir(),
                DependenciesImpl.cloneDependencies(variantData, androidBuilder),
                sourceProviders.variantSourceProvider,
                sourceProviders.multiFlavorSourceProvider,
                variantConfiguration.getSupportedAbis(),
                nativeLibraries,
                variantConfiguration.getMergedBuildConfigFields(),
                variantConfiguration.getMergedResValues());
    }

    private static Collection<Abi> createAbiList(Collection<String> abiNames) {
        ImmutableList.Builder<Abi> builder = ImmutableList.builder();
        for (String abiName : abiNames) {
            builder.add(Abi.getByName(abiName));
        }
        return builder.build();
    }

    private static SourceProviders determineSourceProviders(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        SourceProvider variantSourceProvider =
                variantData.getVariantConfiguration().getVariantSourceProvider();
        SourceProvider multiFlavorSourceProvider =
                variantData.getVariantConfiguration().getMultiFlavorSourceProvider();

        return new SourceProviders(
                variantSourceProvider != null ?
                        SourceProviderImpl.cloneProvider(variantSourceProvider) :
                        null,
                multiFlavorSourceProvider != null ?
                        SourceProviderImpl.cloneProvider(multiFlavorSourceProvider) :
                        null);
    }

    @NonNull
    private static List<String> getProductFlavorNames(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {
        List<CoreProductFlavor> productFlavors = variantData.getVariantConfiguration()
                .getProductFlavors();

        List<String> flavorNames = Lists.newArrayListWithCapacity(productFlavors.size());

        for (ProductFlavor flavor : productFlavors) {
            flavorNames.add(flavor.getName());
        }

        return flavorNames;
    }

    @NonNull
    private static List<File> getGeneratedSourceFolders(
            @Nullable BaseVariantData<? extends BaseVariantOutputData> variantData) {
        if (variantData == null) {
            return Collections.emptyList();
        }

        List<File> extraFolders = variantData.getExtraGeneratedSourceFolders();

        List<File> folders;
        if (extraFolders != null) {
            folders = Lists.newArrayListWithExpectedSize(5 + extraFolders.size());
            folders.addAll(extraFolders);
        } else {
            folders = Lists.newArrayListWithExpectedSize(5);
        }

        VariantScope scope = variantData.getScope();

        // The R class is only generated by the first output.
        folders.add(scope.getRClassSourceOutputDir());

        folders.add(scope.getAidlSourceOutputDir());
        folders.add(scope.getBuildConfigSourceOutputDir());
        Boolean ndkMode = variantData.getVariantConfiguration().getMergedFlavor().getRenderscriptNdkModeEnabled();
        if (ndkMode == null || !ndkMode) {
            folders.add(scope.getRenderscriptSourceOutputDir());
        }

        return folders;
    }

    @NonNull
    private static List<File> getGeneratedResourceFolders(
            @Nullable BaseVariantData<? extends BaseVariantOutputData> variantData) {
        if (variantData == null) {
            return Collections.emptyList();
        }

        List<File> result;

        List<File> extraFolders = variantData.getExtraGeneratedResFolders();
        if (extraFolders != null && !extraFolders.isEmpty()) {
            result = Lists.newArrayListWithCapacity(extraFolders.size() + 2);

            result.addAll(extraFolders);
        } else {
            result = Lists.newArrayListWithCapacity(2);
        }

        VariantScope scope = variantData.getScope();

        result.add(scope.getRenderscriptResOutputDir());
        result.add(scope.getGeneratedResOutputDir());

        return result;
    }

    @NonNull
    private static Collection<SigningConfig> cloneSigningConfigs(
            @NonNull Collection<? extends SigningConfig> signingConfigs) {
        Collection<SigningConfig> results = Lists.newArrayListWithCapacity(signingConfigs.size());

        for (SigningConfig signingConfig : signingConfigs) {
            results.add(SigningConfigImpl.createSigningConfig(signingConfig));
        }

        return results;
    }

    @Nullable
    private static SourceProviderContainer getSourceProviderContainer(
            @NonNull Collection<SourceProviderContainer> items,
            @NonNull String name) {
        for (SourceProviderContainer item : items) {
            if (name.equals(item.getArtifactName())) {
                return item;
            }
        }

        return null;
    }

    private static class SourceProviders {
        protected SourceProviderImpl variantSourceProvider;
        protected SourceProviderImpl multiFlavorSourceProvider;

        public SourceProviders(
                SourceProviderImpl variantSourceProvider,
                SourceProviderImpl multiFlavorSourceProvider) {
            this.variantSourceProvider = variantSourceProvider;
            this.multiFlavorSourceProvider = multiFlavorSourceProvider;
        }
    }

    /**
     * Return the unresolved dependencies in SyncIssues
     */
    private static Collection<String> findUnresolvedDependencies(
            @NonNull Collection<SyncIssue> syncIssues) {
        List<String> unresolvedDependencies = Lists.newArrayList();

        for (SyncIssue issue : syncIssues) {
            if (issue.getType() == SyncIssue.TYPE_UNRESOLVED_DEPENDENCY) {
                unresolvedDependencies.add(issue.getData());
            }
        }
        return unresolvedDependencies;
    }
}
