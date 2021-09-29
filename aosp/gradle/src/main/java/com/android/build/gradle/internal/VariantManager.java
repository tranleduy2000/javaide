/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.AndroidGradleOptions;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider;
import com.android.build.gradle.internal.api.VariantFilter;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.internal.profile.SpanRecorders;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantType;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;
import com.android.utils.StringHelper;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.internal.reflect.Instantiator;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.android.builder.core.BuilderConstants.LINT;

/**
 * Class to create, manage variants.
 */
public class VariantManager implements VariantModel {

    protected static final String COM_ANDROID_SUPPORT_MULTIDEX =
            "com.android.support:multidex:1.0.1";

    @NonNull
    private final Project project;
    @NonNull
    private final AndroidBuilder androidBuilder;
    @NonNull
    private final AndroidConfig extension;
    @NonNull
    private final VariantFactory variantFactory;
    @NonNull
    private final TaskManager taskManager;
    @NonNull
    private final Instantiator instantiator;
    @NonNull
    private final Map<String, BuildTypeData> buildTypes = Maps.newHashMap();
    @NonNull
    private final Map<String, ProductFlavorData<CoreProductFlavor>> productFlavors = Maps.newHashMap();
    @NonNull
    private final Map<String, SigningConfig> signingConfigs = Maps.newHashMap();
    @NonNull
    private final ReadOnlyObjectProvider readOnlyObjectProvider = new ReadOnlyObjectProvider();
    @NonNull
    private final VariantFilter variantFilter = new VariantFilter(readOnlyObjectProvider);
    @NonNull
    private final List<BaseVariantData<? extends BaseVariantOutputData>> variantDataList = Lists.newArrayList();
    @NonNull
    private ProductFlavorData<CoreProductFlavor> defaultConfigData;
    @Nullable
    private SigningConfig signingOverride;

    public VariantManager(
            @NonNull Project project,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull AndroidConfig extension,
            @NonNull VariantFactory variantFactory,
            @NonNull TaskManager taskManager,
            @NonNull Instantiator instantiator) {
        this.extension = extension;
        this.androidBuilder = androidBuilder;
        this.project = project;
        this.variantFactory = variantFactory;
        this.taskManager = taskManager;
        this.instantiator = instantiator;

        DefaultAndroidSourceSet mainSourceSet =
                (DefaultAndroidSourceSet) extension.getSourceSets().getByName(extension.getDefaultConfig().getName());

        defaultConfigData = new ProductFlavorData<>(
                extension.getDefaultConfig(), mainSourceSet,
                project);
        signingOverride = createSigningOverride();
    }

    private static void createCompoundSourceSets(
            @NonNull List<? extends ProductFlavor> productFlavorList,
            GradleVariantConfiguration variantConfig,
            NamedDomainObjectContainer<AndroidSourceSet> sourceSetsContainer) {
        if (!productFlavorList.isEmpty() && !variantConfig.getType().isSingleBuildType()) {
            DefaultAndroidSourceSet variantSourceSet =
                    (DefaultAndroidSourceSet) sourceSetsContainer.maybeCreate(
                            computeSourceSetName(
                                    variantConfig.getFullName(),
                                    variantConfig.getType()));
            variantConfig.setVariantSourceProvider(variantSourceSet);
        }

        if (productFlavorList.size() > 1) {
            DefaultAndroidSourceSet multiFlavorSourceSet =
                    (DefaultAndroidSourceSet) sourceSetsContainer.maybeCreate(
                            computeSourceSetName(
                                    variantConfig.getFlavorName(),
                                    variantConfig.getType()));
            variantConfig.setMultiFlavorSourceProvider(multiFlavorSourceSet);
        }
    }

    /**
     * Turns a string into a valid source set name for the given {@link VariantType}, e.g.
     * "fooBarUnitTest" becomes "testFooBar".
     */
    @NonNull
    private static String computeSourceSetName(
            @NonNull String name,
            @NonNull VariantType variantType) {
        if (name.endsWith(variantType.getSuffix())) {
            name = name.substring(0, name.length() - variantType.getSuffix().length());
        }

        if (!variantType.getPrefix().isEmpty()) {
            name = variantType.getPrefix() + StringHelper.capitalize(name);
        }

        return name;
    }

    private static void checkName(@NonNull String name, @NonNull String displayName) {
        if (LINT.equals(name)) {
            throw new RuntimeException(String.format(
                    "%1$s names cannot be %2$s", displayName, LINT));
        }
    }

    private static void checkPrefix(String name, String displayName, String prefix) {
        if (name.startsWith(prefix)) {
            throw new RuntimeException(String.format(
                    "%1$s names cannot start with '%2$s'", displayName, prefix));
        }
    }

    @NonNull
    @Override
    public ProductFlavorData<CoreProductFlavor> getDefaultConfig() {
        return defaultConfigData;
    }

    @Override
    @NonNull
    public Map<String, BuildTypeData> getBuildTypes() {
        return buildTypes;
    }

    @Override
    @NonNull
    public Map<String, ProductFlavorData<CoreProductFlavor>> getProductFlavors() {
        return productFlavors;
    }

    @Override
    @NonNull
    public Map<String, SigningConfig> getSigningConfigs() {
        return signingConfigs;
    }

    public void addSigningConfig(@NonNull SigningConfig signingConfig) {
        signingConfigs.put(signingConfig.getName(), signingConfig);
    }

    /**
     * Adds new BuildType, creating a BuildTypeData, and the associated source set,
     * and adding it to the map.
     *
     * @param buildType the build type.
     */
    public void addBuildType(@NonNull CoreBuildType buildType) {
        String name = buildType.getName();
        checkName(name, "BuildType");

        if (productFlavors.containsKey(name)) {
            throw new RuntimeException("BuildType names cannot collide with ProductFlavor names");
        }

        DefaultAndroidSourceSet mainSourceSet = (DefaultAndroidSourceSet) extension.getSourceSets().maybeCreate(name);

        BuildTypeData buildTypeData = new BuildTypeData(
                buildType, project, mainSourceSet);

        buildTypes.put(name, buildTypeData);
    }

    /**
     * Adds a new ProductFlavor, creating a ProductFlavorData and associated source sets,
     * and adding it to the map.
     *
     * @param productFlavor the product flavor
     */
    public void addProductFlavor(@NonNull CoreProductFlavor productFlavor) {
        String name = productFlavor.getName();
        checkName(name, "ProductFlavor");

        if (buildTypes.containsKey(name)) {
            throw new RuntimeException("ProductFlavor names cannot collide with BuildType names");
        }

        DefaultAndroidSourceSet mainSourceSet = (DefaultAndroidSourceSet) extension.getSourceSets().maybeCreate(
                productFlavor.getName());

        ProductFlavorData<CoreProductFlavor> productFlavorData =
                new ProductFlavorData<>(
                        productFlavor,
                        mainSourceSet,
                        project);

        productFlavors.put(productFlavor.getName(), productFlavorData);
    }

    /**
     * Return a list of all created VariantData.
     */
    @NonNull
    public List<BaseVariantData<? extends BaseVariantOutputData>> getVariantDataList() {
        return variantDataList;
    }

    /**
     * Variant/Task creation entry point.
     */
    public void createAndroidTasks() {
        variantFactory.validateModel(this);
        variantFactory.preVariantWork(project);

        final TaskFactory tasks = new TaskContainerAdaptor(project.getTasks());
        if (variantDataList.isEmpty()) {
            populateVariantDataList();
        }

        for (BaseVariantData<? extends BaseVariantOutputData> variantData : variantDataList) {
            createTasksForVariantData(tasks, variantData);
        }
    }

    /**
     * Create assemble task for VariantData.
     */
    private void createAssembleTaskForVariantData(
            TaskFactory tasks,
            final BaseVariantData<?> variantData) {
        BuildTypeData buildTypeData =
                buildTypes.get(variantData.getVariantConfiguration().getBuildType().getName());

        if (productFlavors.isEmpty()) {
            // Reuse assemble task for build type if there is no product flavor.
            variantData.assembleVariantTask = buildTypeData.getAssembleTask();
        } else {
            variantData.assembleVariantTask = taskManager.createAssembleTask(tasks, variantData);

            // setup the task dependencies
            // build type
            buildTypeData.getAssembleTask().dependsOn(variantData.assembleVariantTask);

            // each flavor
            GradleVariantConfiguration variantConfig = variantData.getVariantConfiguration();
            for (CoreProductFlavor flavor : variantConfig.getProductFlavors()) {
                productFlavors.get(flavor.getName()).getAssembleTask()
                        .dependsOn(variantData.assembleVariantTask);
            }

            // assembleTask for this flavor(dimension), created on demand if needed.
            if (variantConfig.getProductFlavors().size() > 1) {
                final String name = StringHelper.capitalize(variantConfig.getFlavorName());
                final String variantAssembleTaskName = "assemble" + name;
                if (!tasks.containsKey(variantAssembleTaskName)) {
                    tasks.create(variantAssembleTaskName, new Action<Task>() {
                        @Override
                        public void execute(Task task) {
                            task.setDescription(
                                    "Assembles all builds for flavor combination: " + name);
                            task.setGroup("Build");
                            task.dependsOn(variantData.assembleVariantTask);

                        }
                    });
                }
                tasks.named("assemble", new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        task.dependsOn(variantAssembleTaskName);
                    }
                });
            }
        }
    }

    /**
     * Create tasks for the specified variantData.
     */
    public void createTasksForVariantData(
            final TaskFactory tasks,
            final BaseVariantData<? extends BaseVariantOutputData> variantData) {

        // Add dependency of assemble task on assemble build type task.
        tasks.named("assemble", new Action<Task>() {
            @Override
            public void execute(Task task) {
                BuildTypeData buildTypeData = buildTypes.get(
                        variantData.getVariantConfiguration().getBuildType().getName());
                task.dependsOn(buildTypeData.getAssembleTask());
            }
        });


        createAssembleTaskForVariantData(tasks, variantData);
        taskManager.createTasksForVariantData(tasks, variantData);
    }

    /**
     * Create all variants.
     */
    public void populateVariantDataList() {
        if (productFlavors.isEmpty()) {
            createVariantDataForProductFlavors(Collections.<ProductFlavor>emptyList());
        } else {
            List<String> flavorDimensionList = extension.getFlavorDimensionList();

            // Create iterable to get GradleProductFlavor from ProductFlavorData.
            Iterable<CoreProductFlavor> flavorDsl =
                    Iterables.transform(
                            productFlavors.values(),
                            new Function<ProductFlavorData<CoreProductFlavor>, CoreProductFlavor>() {
                                @Override
                                public CoreProductFlavor apply(
                                        ProductFlavorData<CoreProductFlavor> data) {
                                    return data.getProductFlavor();
                                }
                            });

            // Get a list of all combinations of product flavors.
            List<ProductFlavorCombo<CoreProductFlavor>> flavorComboList =
                    ProductFlavorCombo.createCombinations(
                            flavorDimensionList,
                            flavorDsl);

            for (ProductFlavorCombo<CoreProductFlavor> flavorCombo : flavorComboList) {
                //noinspection unchecked
                createVariantDataForProductFlavors(
                        (List<ProductFlavor>) (List) flavorCombo.getFlavorList());
            }
        }
    }

    /**
     * Create a VariantData for a specific combination of BuildType and ProductFlavor list.
     */
    public BaseVariantData<? extends BaseVariantOutputData> createVariantData(
            @NonNull com.android.builder.model.BuildType buildType,
            @NonNull List<? extends ProductFlavor> productFlavorList) {
        BuildTypeData buildTypeData = buildTypes.get(buildType.getName());

        GradleVariantConfiguration variantConfig = new GradleVariantConfiguration(
                defaultConfigData.getProductFlavor(),
                defaultConfigData.getSourceSet(),
                buildTypeData.getBuildType(),
                buildTypeData.getSourceSet(),
                variantFactory.getVariantConfigurationType(),
                signingOverride);

        // sourceSetContainer in case we are creating variant specific sourceSets.
        NamedDomainObjectContainer<AndroidSourceSet> sourceSetsContainer = extension
                .getSourceSets();

        // We must first add the flavors to the variant config, in order to get the proper
        // variant-specific and multi-flavor name as we add/create the variant providers later.
        for (ProductFlavor productFlavor : productFlavorList) {
            ProductFlavorData<CoreProductFlavor> data = productFlavors.get(
                    productFlavor.getName());

            String dimensionName = productFlavor.getDimension();
            if (dimensionName == null) {
                dimensionName = "";
            }

            variantConfig.addProductFlavor(
                    data.getProductFlavor(),
                    data.getSourceSet(),
                    dimensionName);
        }

        createCompoundSourceSets(productFlavorList, variantConfig, sourceSetsContainer);

        // Add the container of dependencies.
        // The order of the libraries is important, in descending order:
        // variant-specific, build type, multi-flavor, flavor1, flavor2, ..., defaultConfig.
        // variant-specific if the full combo of flavors+build type. Does not exist if no flavors.
        // multi-flavor is the combination of all flavor dimensions. Does not exist if <2 dimension.
        final List<ConfigurationProvider> variantProviders =
                Lists.newArrayListWithExpectedSize(productFlavorList.size() + 4);

        // 1. add the variant-specific if applicable.
        if (!productFlavorList.isEmpty()) {
            variantProviders.add(
                    new ConfigurationProviderImpl(
                            project,
                            (DefaultAndroidSourceSet) variantConfig.getVariantSourceProvider()));
        }

        // 2. the build type.
        variantProviders.add(buildTypeData.getMainProvider());

        // 3. the multi-flavor combination
        if (productFlavorList.size() > 1) {
            variantProviders.add(
                    new ConfigurationProviderImpl(
                            project,
                            (DefaultAndroidSourceSet) variantConfig.getMultiFlavorSourceProvider()));
        }

        // 4. the flavors.
        for (ProductFlavor productFlavor : productFlavorList) {
            variantProviders.add(productFlavors.get(productFlavor.getName()).getMainProvider());
        }

        // 5. The defaultConfig
        variantProviders.add(defaultConfigData.getMainProvider());

        // Done. Create the variant and get its internal storage object.
        BaseVariantData<?> variantData =
                variantFactory.createVariantData(variantConfig, taskManager);

        final VariantDependencies variantDep = VariantDependencies.compute(
                project, variantConfig.getFullName(),
                isVariantPublished(),
                variantData.getType(),
                null,
                variantProviders.toArray(new ConfigurationProvider[variantProviders.size()]));
        variantData.setVariantDependency(variantDep);

        if (variantConfig.isMultiDexEnabled() && variantConfig.isLegacyMultiDexMode()) {
            project.getDependencies().add(
                    variantDep.getCompileConfiguration().getName(), COM_ANDROID_SUPPORT_MULTIDEX);
            project.getDependencies().add(
                    variantDep.getPackageConfiguration().getName(), COM_ANDROID_SUPPORT_MULTIDEX);
        }

        SpanRecorders.record(project, ExecutionType.RESOLVE_DEPENDENCIES,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() {
                        taskManager.resolveDependencies(
                                variantDep
                        );
                        return null;
                    }
                }, new Recorder.Property(SpanRecorders.VARIANT, variantConfig.getFullName()));

        variantConfig.setDependencies(variantDep);

        return variantData;
    }

    /**
     * Creates VariantData for a specified list of product flavor.
     * <p>
     * This will create VariantData for all build types of the given flavors.
     *
     * @param productFlavorList the flavor(s) to build.
     */
    private void createVariantDataForProductFlavors(
            @NonNull List<ProductFlavor> productFlavorList) {

        CoreProductFlavor defaultConfig = defaultConfigData.getProductFlavor();

        Action<com.android.build.gradle.api.VariantFilter> variantFilterAction =
                extension.getVariantFilter();

        for (BuildTypeData buildTypeData : buildTypes.values()) {
            boolean ignore = false;
            if (variantFilterAction != null) {
                variantFilter.reset(defaultConfig, buildTypeData.getBuildType(), productFlavorList);
                variantFilterAction.execute(variantFilter);
                ignore = variantFilter.isIgnore();
            }

            if (!ignore) {
                BaseVariantData<?> variantData = createVariantData(
                        buildTypeData.getBuildType(),
                        productFlavorList);
                variantDataList.add(variantData);

                GradleVariantConfiguration variantConfig = variantData.getVariantConfiguration();
                ThreadRecorder.get().record(
                        ExecutionType.VARIANT_CONFIG,
                        Recorder.EmptyBlock,
                        new Recorder.Property(
                                "project",
                                project.getName()),
                        new Recorder.Property(
                                "variant",
                                variantData.getName()),
                        new Recorder.Property(
                                "use_minify",
                                Boolean.toString(variantConfig.isMinifyEnabled())),
                        new Recorder.Property(
                                "use_multi_dex",
                                Boolean.toString(variantConfig.isMultiDexEnabled())),
                        new Recorder.Property(
                                "multi_dex_legacy",
                                Boolean.toString(variantConfig.isLegacyMultiDexMode())));


            }
        }

    }

    private boolean isVariantPublished() {
        return extension.getPublishNonDefault();
    }

    private SigningConfig createSigningOverride() {
        AndroidGradleOptions.SigningOptions signingOptions =
                AndroidGradleOptions.getSigningOptions(project);
        if (signingOptions != null) {
            com.android.build.gradle.internal.dsl.SigningConfig signingConfigDsl =
                    new com.android.build.gradle.internal.dsl.SigningConfig("externalOverride");

            signingConfigDsl.setStoreFile(new File(signingOptions.storeFile));
            signingConfigDsl.setStorePassword(signingOptions.keyPassword);
            signingConfigDsl.setKeyAlias(signingOptions.keyAlias);
            signingConfigDsl.setKeyPassword(signingOptions.keyPassword);

            if (signingOptions.storeType != null) {
                signingConfigDsl.setStoreType(signingOptions.storeType);
            }

            return signingConfigDsl;
        }
        return null;
    }

}
