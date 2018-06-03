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
package com.android.build.gradle;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.api.VariantFilter;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.LoggingUtil;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.SourceSetSourceProviderWrapper;
import com.android.build.gradle.internal.coverage.JacocoExtension;
import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.dsl.AdbOptions;
import com.android.build.gradle.internal.dsl.AndroidSourceSetFactory;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.dsl.PreprocessingOptions;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.dsl.TestOptions;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.BuilderConstants;
import com.android.builder.core.LibraryRequest;
import com.android.builder.model.SourceProvider;
import com.android.builder.sdk.TargetInfo;
import com.android.builder.testing.api.DeviceProvider;
import com.android.builder.testing.api.TestServer;
import com.android.sdklib.repository.FullRevision;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.SourceSet;
import org.gradle.internal.reflect.Instantiator;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base 'android' extension for all android plugins.
 *
 * <p>This is never used directly. Instead,
 *<ul>
 * <li>Plugin <code>com.android.application</code> uses {@link AppExtension}</li>
 * <li>Plugin <code>com.android.library</code> uses {@link LibraryExtension}</li>
 * <li>Plugin <code>com.android.test</code> uses {@link TestedExtension}</li>
 * </ul>
 */
@SuppressWarnings("UnnecessaryInheritDoc")
public abstract class BaseExtension implements AndroidConfig {

    private String target;
    private FullRevision buildToolsRevision;
    private List<LibraryRequest> libraryRequests = Lists.newArrayList();

    /** Default config, shared by all flavors. */
    final ProductFlavor defaultConfig;

    /** Options for aapt, tool for packaging resources. */
    final AaptOptions aaptOptions;

    /** Lint options. */
    final LintOptions lintOptions;

    /** Dex options. */
    final DexOptions dexOptions;

    /** Options for running tests. */
    final TestOptions testOptions;

    /** Compile options */
    final CompileOptions compileOptions;

    /** Packaging options. */
    final PackagingOptions packagingOptions;

    /** Options to control resources preprocessing. Not finalized yet.*/
    final PreprocessingOptions preprocessingOptions;

    /** JaCoCo options. */
    final JacocoExtension jacoco;

    /**
     * APK splits options.
     *
     * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/user-guide/apk-splits">APK Splits</a>.
     */
    final Splits splits;

    /** All product flavors used by this project. */
    final NamedDomainObjectContainer<CoreProductFlavor> productFlavors;

    /** Build types used by this project. */
    final NamedDomainObjectContainer<BuildType> buildTypes;

    /** Signing configs used by this project. */
    final NamedDomainObjectContainer<SigningConfig> signingConfigs;

    private ExtraModelInfo extraModelInfo;

    protected Project project;

    /** Adb options */
    final AdbOptions adbOptions;

    /** A prefix to be used when creating new resources. Used by Studio */
    String resourcePrefix;

    List<String> flavorDimensionList;

    private String defaultPublishConfig = "release";
    private boolean publishNonDefault = false;

    private Action<VariantFilter> variantFilter;

    private final List<DeviceProvider> deviceProviderList = Lists.newArrayList();
    private final List<TestServer> testServerList = Lists.newArrayList();

    private final AndroidBuilder androidBuilder;

    private final SdkHandler sdkHandler;

    protected Logger logger;

    private boolean isWritable = true;

    /**
     * The source sets container.
     */
    final NamedDomainObjectContainer<AndroidSourceSet> sourceSetsContainer;

    BaseExtension(
            @NonNull final ProjectInternal project,
            @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull SdkHandler sdkHandler,
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs,
            @NonNull ExtraModelInfo extraModelInfo,
            final boolean isLibrary) {
        this.androidBuilder = androidBuilder;
        this.sdkHandler = sdkHandler;
        this.buildTypes = buildTypes;
        //noinspection unchecked
        this.productFlavors = (NamedDomainObjectContainer) productFlavors;
        this.signingConfigs = signingConfigs;
        this.extraModelInfo = extraModelInfo;
        this.project = project;

        logger = Logging.getLogger(this.getClass());

        defaultConfig = instantiator.newInstance(ProductFlavor.class, BuilderConstants.MAIN,
                project, instantiator, project.getLogger());

        aaptOptions = instantiator.newInstance(AaptOptions.class);
        dexOptions = instantiator.newInstance(DexOptions.class);
        lintOptions = instantiator.newInstance(LintOptions.class);
        testOptions = instantiator.newInstance(TestOptions.class);
        compileOptions = instantiator.newInstance(CompileOptions.class);
        packagingOptions = instantiator.newInstance(PackagingOptions.class);
        preprocessingOptions = instantiator.newInstance(PreprocessingOptions.class);
        jacoco = instantiator.newInstance(JacocoExtension.class);
        adbOptions = instantiator.newInstance(AdbOptions.class);
        splits = instantiator.newInstance(Splits.class, instantiator);

        sourceSetsContainer = project.container(AndroidSourceSet.class,
                new AndroidSourceSetFactory(instantiator, project, isLibrary));

        sourceSetsContainer.whenObjectAdded(new Action<AndroidSourceSet>() {
            @Override
            public void execute(AndroidSourceSet sourceSet) {
                ConfigurationContainer configurations = project.getConfigurations();

                createConfiguration(
                        configurations,
                        sourceSet.getCompileConfigurationName(),
                        "Classpath for compiling the " + sourceSet.getName() + " sources.");

                String packageConfigDescription;
                if (isLibrary) {
                    packageConfigDescription
                            = "Classpath only used when publishing '" + sourceSet.getName() + "'.";
                } else {
                    packageConfigDescription
                            = "Classpath packaged with the compiled '" + sourceSet.getName() + "' classes.";
                }
                createConfiguration(
                        configurations,
                        sourceSet.getPackageConfigurationName(),
                        packageConfigDescription);

                createConfiguration(
                        configurations,
                        sourceSet.getProvidedConfigurationName(),
                        "Classpath for only compiling the " + sourceSet.getName() + " sources.");

                createConfiguration(
                        configurations,
                        sourceSet.getWearAppConfigurationName(),
                        "Link to a wear app to embed for object '" + sourceSet.getName() + "'.");

                sourceSet.setRoot(String.format("src/%s", sourceSet.getName()));
            }
        });

        sourceSetsContainer.create(defaultConfig.getName());
    }

    /**
     * Disallow further modification on the extension.
     */
    public void disableWrite() {
        isWritable = false;
    }

    protected void checkWritability() {
        if (!isWritable) {
            throw new GradleException(
                    "Android tasks have already been created.\n" +
                            "This happens when calling android.applicationVariants,\n" +
                            "android.libraryVariants or android.testVariants.\n" +
                            "Once these methods are called, it is not possible to\n" +
                            "continue configuring the model.");
        }
    }

    protected void createConfiguration(
            @NonNull ConfigurationContainer configurations,
            @NonNull String configurationName,
            @NonNull String configurationDescription) {
        logger.info("Creating configuration {}", configurationName);

        Configuration configuration = configurations.findByName(configurationName);
        if (configuration == null) {
            configuration = configurations.create(configurationName);
        }
        configuration.setVisible(false);
        configuration.setDescription(configurationDescription);
    }

    /**
     * Sets the compile SDK version, based on full SDK version string, e.g.
     * <code>android-21</code> for Lollipop.
     */
    public void compileSdkVersion(String version) {
        checkWritability();
        this.target = version;
    }

    /**
     * Sets the compile SDK version, based on API level, e.g. 21 for Lollipop.
     */
    public void compileSdkVersion(int apiLevel) {
        compileSdkVersion("android-" + apiLevel);
    }

    public void setCompileSdkVersion(int apiLevel) {
        compileSdkVersion(apiLevel);
    }

    public void setCompileSdkVersion(String target) {
        compileSdkVersion(target);
    }

    /**
     * Request the use a of Library. The library is then added to the classpath.
     * @param name the name of the library.
     */
    public void useLibrary(String name) {
        useLibrary(name, true);
    }

    /**
     * Request the use a of Library. The library is then added to the classpath.
     * @param name the name of the library.
     * @param required if using the library requires a manifest entry, the  entry will
     * indicate that the library is not required.
     */
    public void useLibrary(String name, boolean required) {
        libraryRequests.add(new LibraryRequest(name, required));
    }

    public void buildToolsVersion(String version) {
        checkWritability();
        buildToolsRevision = FullRevision.parseRevision(version);
    }

    /**
     * <strong>Required.</strong> Version of the build tools to use.
     *
     * <p>Value assigned to this property is parsed and stored in a normalized form, so reading it
     * back may give a slightly different string.
     */
    @Override
    public String getBuildToolsVersion() {
        return buildToolsRevision.toString();
    }

    public void setBuildToolsVersion(String version) {
        buildToolsVersion(version);
    }

    /**
     * Configures the build types.
     */
    public void buildTypes(Action<? super NamedDomainObjectContainer<BuildType>> action) {
        checkWritability();
        action.execute(buildTypes);
    }

    /**
     * Configures the product flavors.
     */
    public void productFlavors(Action<? super NamedDomainObjectContainer<CoreProductFlavor>> action) {
        checkWritability();
        action.execute(productFlavors);
    }

    /**
     * Configures the signing configs.
     */
    public void signingConfigs(Action<? super NamedDomainObjectContainer<SigningConfig>> action) {
        checkWritability();
        action.execute(signingConfigs);
    }

    public void flavorDimensions(String... dimensions) {
        checkWritability();
        flavorDimensionList = Arrays.asList(dimensions);
    }

    /**
     * Configures the source sets. Note that the Android plugin uses its own implementation of
     * source sets, {@link AndroidSourceSet}.
     */
    public void sourceSets(Action<NamedDomainObjectContainer<AndroidSourceSet>> action) {
        checkWritability();
        action.execute(sourceSetsContainer);
    }

    /**
     * All source sets. Note that the Android plugin uses its own implementation of
     * source sets, {@link AndroidSourceSet}.
     */
    @Override
    public NamedDomainObjectContainer<AndroidSourceSet> getSourceSets() {
        return sourceSetsContainer;
    }

    /**
     * The default configuration, inherited by all build flavors (if any are defined).
     */
    public void defaultConfig(Action<ProductFlavor> action) {
        checkWritability();
        action.execute(defaultConfig);
    }

    /**
     * Configures aapt options.
     */
    public void aaptOptions(Action<AaptOptions> action) {
        checkWritability();
        action.execute(aaptOptions);
    }

    /**
     * Configures dex options.
     */
    public void dexOptions(Action<DexOptions> action) {
        checkWritability();
        action.execute(dexOptions);
    }

    /**
     * Configure lint options.
     */
    public void lintOptions(Action<LintOptions> action) {
        checkWritability();
        action.execute(lintOptions);
    }

    /** Configures the test options. */
    public void testOptions(Action<TestOptions> action) {
        checkWritability();
        action.execute(testOptions);
    }

    /**
     * Configures compile options.
     */
    public void compileOptions(Action<CompileOptions> action) {
        checkWritability();
        action.execute(compileOptions);
    }

    /**
     * Configures packaging options.
     */
    public void packagingOptions(Action<PackagingOptions> action) {
        checkWritability();
        action.execute(packagingOptions);
    }

    /**
     * Configures preprocessing options.
     */
    public void preprocessingOptions(Action<PreprocessingOptions> action) {
        checkWritability();
        action.execute(preprocessingOptions);
    }

    /**
     * Configures JaCoCo options.
     */
    public void jacoco(Action<JacocoExtension> action) {
        checkWritability();
        action.execute(jacoco);
    }

    /**
     * Configures adb options.
     */
    public void adbOptions(Action<AdbOptions> action) {
        checkWritability();
        action.execute(adbOptions);
    }

    /**
     * Configures APK splits.
     */
    public void splits(Action<Splits> action) {
        checkWritability();
        action.execute(splits);
    }

    public void deviceProvider(DeviceProvider deviceProvider) {
        checkWritability();
        deviceProviderList.add(deviceProvider);
    }

    @Override
    @NonNull
    public List<DeviceProvider> getDeviceProviders() {
        return deviceProviderList;
    }

    public void testServer(TestServer testServer) {
        checkWritability();
        testServerList.add(testServer);
    }

    @Override
    @NonNull
    public List<TestServer> getTestServers() {
        return testServerList;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends CoreProductFlavor> getProductFlavors() {
        return productFlavors;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends CoreBuildType> getBuildTypes() {
        return buildTypes;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<? extends com.android.builder.model.SigningConfig> getSigningConfigs() {
        return signingConfigs;
    }

    public void defaultPublishConfig(String value) {
        setDefaultPublishConfig(value);
    }

    public void publishNonDefault(boolean value) {
        publishNonDefault = value;
    }

    /**
     * Name of the configuration used to build the default artifact of this project.
     *
     * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Referencing-a-Library">
     * Referencing a Library</a>
     */
    @Override
    public String getDefaultPublishConfig() {
        return defaultPublishConfig;
    }

    public void setDefaultPublishConfig(String value) {
        defaultPublishConfig = value;
    }

    /**
     * Whether to publish artifacts for all configurations, not just the default one.
     *
     * <p>See <a href="http://tools.android.com/tech-docs/new-build-system/user-guide#TOC-Referencing-a-Library">
     * Referencing a Library</a>
     */
    @Override
    public boolean getPublishNonDefault() {
        return publishNonDefault;
    }

    public void variantFilter(Action<VariantFilter> filter) {
        setVariantFilter(filter);
    }

    public void setVariantFilter(Action<VariantFilter> filter) {
        variantFilter = filter;
    }

    /**
     * A variant filter to control which variants are excluded.
     * <p>The filter is an {@link Action} which is passed a single object of type
     * {@link com.android.build.gradle.internal.api.VariantFilter}. It should set the
     * {@link VariantFilter#setIgnore(boolean)} flag to filter out the given variant.
     */
    @Override
    public Action<VariantFilter> getVariantFilter() {
        return variantFilter;
    }

    @Override
    public AdbOptions getAdbOptions() {
        return adbOptions;
    }

    /** {@inheritDoc} */
    @Override
    public String getResourcePrefix() {
        return resourcePrefix;
    }

    @Override
    public List<String> getFlavorDimensionList() {
        return flavorDimensionList;
    }

    @Override
    public boolean getGeneratePureSplits() {
        return generatePureSplits;
    }

    /** {@inheritDoc} */
    @Override
    @Beta
    public PreprocessingOptions getPreprocessingOptions() {
        return preprocessingOptions;
    }

    public void resourcePrefix(String prefix) {
        resourcePrefix = prefix;
    }

    public abstract void addVariant(BaseVariant variant);

    public void registerArtifactType(@NonNull String name,
                                     boolean isTest,
                                     int artifactType) {
        extraModelInfo.registerArtifactType(name, isTest, artifactType);
    }

    public void registerBuildTypeSourceProvider(
            @NonNull String name,
            @NonNull BuildType buildType,
            @NonNull SourceProvider sourceProvider) {
        extraModelInfo.registerBuildTypeSourceProvider(name, buildType, sourceProvider);
    }

    public void registerProductFlavorSourceProvider(
            @NonNull String name,
            @NonNull CoreProductFlavor productFlavor,
            @NonNull SourceProvider sourceProvider) {
        extraModelInfo.registerProductFlavorSourceProvider(name, productFlavor, sourceProvider);
    }

    public void registerJavaArtifact(
            @NonNull String name,
            @NonNull BaseVariant variant,
            @NonNull String assembleTaskName,
            @NonNull String javaCompileTaskName,
            @NonNull Collection<File> generatedSourceFolders,
            @NonNull Iterable<String> ideSetupTaskNames,
            @NonNull Configuration configuration,
            @NonNull File classesFolder,
            @NonNull File javaResourceFolder,
            @Nullable SourceProvider sourceProvider) {
        extraModelInfo.registerJavaArtifact(name, variant, assembleTaskName,
                javaCompileTaskName, generatedSourceFolders, ideSetupTaskNames,
                configuration, classesFolder, javaResourceFolder, sourceProvider);
    }

    public void registerMultiFlavorSourceProvider(
            @NonNull String name,
            @NonNull String flavorName,
            @NonNull SourceProvider sourceProvider) {
        extraModelInfo.registerMultiFlavorSourceProvider(name, flavorName, sourceProvider);
    }

    @NonNull
    public SourceProvider wrapJavaSourceSet(@NonNull SourceSet sourceSet) {
        return new SourceSetSourceProviderWrapper(sourceSet);
    }

    /**
     * <strong>Required.</strong> Compile SDK version.
     *
     * <p>Your code will be compiled against the android.jar from this API level. You should
     * generally use the most up-to-date SDK version here. Use the Lint tool to make sure you don't
     * use APIs not available in earlier platform version without checking.
     *
     * <p>Setter can be called with a string like "android-21" or a number.
     *
     * <p>Value assigned to this property is parsed and stored in a normalized form, so reading it
     * back may give a slightly different string.
     */
    @Override
    public String getCompileSdkVersion() {
        return target;
    }

    @Override
    public FullRevision getBuildToolsRevision() {
        return buildToolsRevision;
    }

    @Override
    public Collection<LibraryRequest> getLibraryRequests() {
        return libraryRequests;
    }

    public File getSdkDirectory() {
        return sdkHandler.getSdkFolder();
    }

    public File getNdkDirectory() {
        return sdkHandler.getNdkFolder();
    }

    public List<File> getBootClasspath() {
        ensureTargetSetup();
        return androidBuilder.getBootClasspath();
    }

    public File getAdbExe() {
        return sdkHandler.getSdkInfo().getAdb();
    }

    public File getDefaultProguardFile(String name) {
        File sdkDir = sdkHandler.getAndCheckSdkFolder();
        return new File(sdkDir,
                SdkConstants.FD_TOOLS + File.separatorChar
                        + SdkConstants.FD_PROGUARD + File.separatorChar
                        + name);
    }

    // ---------------
    // TEMP for compatibility

    // by default, we do not generate pure splits
    boolean generatePureSplits = false;

    public void generatePureSplits(boolean flag) {
        if (flag) {
            logger.warn("Pure splits are not supported by PlayStore yet.");
        }
        this.generatePureSplits = flag;
    }

    private boolean enforceUniquePackageName = true;

    public void enforceUniquePackageName(boolean value) {
        if (!value) {
            LoggingUtil.displayDeprecationWarning(logger, project, "Support for libraries with same package name is deprecated and will be removed in a future release.");
        }
        enforceUniquePackageName = value;
    }

    public void setEnforceUniquePackageName(boolean value) {
        enforceUniquePackageName(value);
    }

    @Override
    public boolean getEnforceUniquePackageName() {
        return enforceUniquePackageName;
    }

    /** {@inheritDoc} */
    @Override
    public CoreProductFlavor getDefaultConfig() {
        return defaultConfig;
    }

    /** {@inheritDoc} */
    @Override
    public AaptOptions getAaptOptions() {
        return aaptOptions;
    }

    /** {@inheritDoc} */
    @Override
    public CompileOptions getCompileOptions() {
        return compileOptions;
    }

    /** {@inheritDoc} */
    @Override
    public DexOptions getDexOptions() {
        return dexOptions;
    }

    /** {@inheritDoc} */
    @Override
    public JacocoExtension getJacoco() {
        return jacoco;
    }

    /** {@inheritDoc} */
    @Override
    public LintOptions getLintOptions() {
        return lintOptions;
    }

    /** {@inheritDoc} */
    @Override
    public PackagingOptions getPackagingOptions() {
        return packagingOptions;
    }

    /** {@inheritDoc} */
    @Override
    public Splits getSplits() {
        return splits;
    }

    /** {@inheritDoc} */
    @Override
    public TestOptions getTestOptions() {
        return testOptions;
    }

    private void ensureTargetSetup() {
        // check if the target has been set.
        TargetInfo targetInfo = androidBuilder.getTargetInfo();
        if (targetInfo == null) {
            sdkHandler.initTarget(
                    getCompileSdkVersion(),
                    buildToolsRevision,
                    libraryRequests,
                    androidBuilder);
        }
    }

    // For compatibility with LibraryExtension.
    @Override
    public Boolean getPackageBuildConfig() {
        throw new GradleException("packageBuildConfig is not supported.");
    }
}
