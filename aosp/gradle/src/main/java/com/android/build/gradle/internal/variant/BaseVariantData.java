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
package com.android.build.gradle.internal.variant;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.dsl.Splits;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.CheckManifest;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.BinaryFileProviderTask;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.GenerateResValues;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.builder.core.VariantType;
import com.android.builder.model.SourceProvider;
import com.android.ide.common.res2.ResourceSet;
import com.android.utils.StringHelper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

import org.gradle.api.Task;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base data about a variant.
 */
public abstract class BaseVariantData<T extends BaseVariantOutputData> {

    @NonNull
    protected final TaskManager taskManager;
    @NonNull
    private final AndroidConfig androidConfig;
    @NonNull
    private final GradleVariantConfiguration variantConfiguration;
    // Needed for ModelBuilder.  Should be removed once VariantScope can replace BaseVariantData.
    @NonNull
    private final VariantScope scope;
    private final List<T> outputs = Lists.newArrayListWithExpectedSize(4);
    public Task preBuildTask;
    public PrepareDependenciesTask prepareDependenciesTask;
    public ProcessAndroidResources generateRClassTask;
    public Task sourceGenTask;
    public Task resourceGenTask;
    public Task assetGenTask;
    public CheckManifest checkManifestTask;
    public AidlCompile aidlCompileTask;
    public MergeResources mergeResourcesTask;
    public MergeAssets mergeAssetsTask;
    public GenerateBuildConfig generateBuildConfigTask;
    public GenerateResValues generateResValuesTask;
    public Copy copyApkTask;
    public Sync processJavaResourcesTask;
    /**
     * Can be JavaCompile depending on user's settings.
     */
    public AbstractCompile javaCompilerTask;
    public JavaCompile javacTask;
    public Jar classesJarTask;

    // empty anchor compile task to set all compilations tasks as dependents.
    public Task compileTask;
    public FileSupplier mappingFileProviderTask;
    public BinaryFileProviderTask binayFileProviderTask;
    public Task obfuscationTask;
    // Task to assemble the variant and all its output.
    public Task assembleVariantTask;
    /**
     * If true, variant outputs will be considered signed. Only set if you manually set the outputs
     * to point to signed files built by other tasks.
     */
    public boolean outputsAreSigned = false;
    private VariantDependencies variantDependency;
    private Object[] javaSources;
    private List<File> extraGeneratedSourceFolders;
    private List<File> extraGeneratedResFolders;
    private Set<String> densityFilters;
    private Set<String> languageFilters;
    private SplitHandlingPolicy mSplitHandlingPolicy;

    public BaseVariantData(
            @NonNull AndroidConfig androidConfig,
            @NonNull TaskManager taskManager,
            @NonNull GradleVariantConfiguration variantConfiguration) {
        this.androidConfig = androidConfig;
        this.variantConfiguration = variantConfiguration;
        this.taskManager = taskManager;

        // eventually, this will require a more open ended comparison.
        mSplitHandlingPolicy =
                androidConfig.getGeneratePureSplits()
                        && variantConfiguration.getMinSdkVersion().getApiLevel() >= 21
                        ? SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY
                        : SplitHandlingPolicy.PRE_21_POLICY;

        // warn the user in case we are forced to ignore the generatePureSplits flag.
        if (androidConfig.getGeneratePureSplits()
                && mSplitHandlingPolicy != SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY) {
            Logging.getLogger(BaseVariantData.class).warn(
                    String.format("Variant %s, MinSdkVersion %s is too low (<21) "
                                    + "to support pure splits, reverting to full APKs",
                            variantConfiguration.getFullName(),
                            variantConfiguration.getMinSdkVersion().getApiLevel()));
        }
        scope = new VariantScope(taskManager.getGlobalScope(), this);
    }

    /**
     * Gets the list of filter values for a filter type either from the user specified build.gradle
     * settings or through a discovery mechanism using folders names.
     *
     * @param resourceSets the list of source folders to discover from.
     * @param filterType   the filter type
     * @param splits       the variant's configuration for splits.
     * @return a possibly empty list of filter value for this filter type.
     */
    @NonNull
    private static Set<String> getFilters(
            @NonNull List<ResourceSet> resourceSets,
            @NonNull DiscoverableFilterType filterType,
            @NonNull Splits splits) {

        Set<String> filtersList = new HashSet<String>();
        if (filterType.isAuto(splits)) {
            filtersList.addAll(getAllFilters(resourceSets, filterType.folderPrefix));
        } else {
            filtersList.addAll(filterType.getConfiguredFilters(splits));
        }
        return filtersList;
    }

    /**
     * Discover all sub-folders of all the {@link ResourceSet#getSourceFiles()} which names are
     * starting with one of the provided prefixes.
     *
     * @param resourceSets the list of sources {@link ResourceSet}
     * @param prefixes     the list of prefixes to look for folders.
     * @return a possibly empty list of folders.
     */
    @NonNull
    private static List<String> getAllFilters(List<ResourceSet> resourceSets, String... prefixes) {
        List<String> providedResFolders = new ArrayList<String>();
        for (ResourceSet resourceSet : resourceSets) {
            for (File resFolder : resourceSet.getSourceFiles()) {
                File[] subResFolders = resFolder.listFiles();
                if (subResFolders != null) {
                    for (File subResFolder : subResFolders) {
                        for (String prefix : prefixes) {
                            if (subResFolder.getName().startsWith(prefix)) {
                                providedResFolders
                                        .add(subResFolder.getName().substring(prefix.length()));
                            }
                        }
                    }
                }
            }
        }
        return providedResFolders;
    }

    public SplitHandlingPolicy getSplitHandlingPolicy() {
        return mSplitHandlingPolicy;
    }

    @NonNull
    protected abstract T doCreateOutput(
            OutputFile.OutputType outputType,
            Collection<FilterData> filters);

    @NonNull
    public T createOutput(OutputFile.OutputType outputType,
                          Collection<FilterData> filters) {
        T data = doCreateOutput(outputType, filters);

        // if it's the first time we add an output, mark previous output as part of a multi-output
        // setup.
        if (outputs.size() == 1) {
            outputs.get(0).setMultiOutput(true);
            data.setMultiOutput(true);
        } else if (outputs.size() > 1) {
            data.setMultiOutput(true);
        }

        outputs.add(data);
        return data;
    }

    @NonNull
    public List<T> getOutputs() {
        return outputs;
    }

    @NonNull
    public GradleVariantConfiguration getVariantConfiguration() {
        return variantConfiguration;
    }

    @NonNull
    public VariantDependencies getVariantDependency() {
        return variantDependency;
    }

    public void setVariantDependency(@NonNull VariantDependencies variantDependency) {
        this.variantDependency = variantDependency;
    }

    @NonNull
    public abstract String getDescription();

    @NonNull
    public String getApplicationId() {
        return variantConfiguration.getApplicationId();
    }

    @NonNull
    protected String getCapitalizedBuildTypeName() {
        return StringHelper.capitalize(variantConfiguration.getBuildType().getName());
    }

    @NonNull
    protected String getCapitalizedFlavorName() {
        return StringHelper.capitalize(variantConfiguration.getFlavorName());
    }

    public VariantType getType() {
        return variantConfiguration.getType();
    }

    @NonNull
    public String getName() {
        return variantConfiguration.getFullName();
    }

    @Nullable
    public List<File> getExtraGeneratedSourceFolders() {
        return extraGeneratedSourceFolders;
    }

    @Nullable
    public List<File> getExtraGeneratedResFolders() {
        return extraGeneratedResFolders;
    }

    public void addJavaSourceFoldersToModel(@NonNull File... generatedSourceFolders) {
        if (extraGeneratedSourceFolders == null) {
            extraGeneratedSourceFolders = Lists.newArrayList();
        }

        Collections.addAll(extraGeneratedSourceFolders, generatedSourceFolders);
    }

    public void addJavaSourceFoldersToModel(@NonNull Collection<File> generatedSourceFolders) {
        if (extraGeneratedSourceFolders == null) {
            extraGeneratedSourceFolders = Lists.newArrayList();
        }

        extraGeneratedSourceFolders.addAll(generatedSourceFolders);
    }

    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull File... generatedSourceFolders) {
        sourceGenTask.dependsOn(task);

        for (File f : generatedSourceFolders) {
            javacTask.source(f);
        }

        addJavaSourceFoldersToModel(generatedSourceFolders);
    }

    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull Collection<File> generatedSourceFolders) {
        sourceGenTask.dependsOn(task);

        for (File f : generatedSourceFolders) {
            javacTask.source(f);
        }

        addJavaSourceFoldersToModel(generatedSourceFolders);
    }

    public void registerResGeneratingTask(@NonNull Task task, @NonNull File... generatedResFolders) {
        // no need add the folders anywhere, the convention mapping closure for the MergeResources
        // action will pick them up from here
        resourceGenTask.dependsOn(task);

        if (extraGeneratedResFolders == null) {
            extraGeneratedResFolders = Lists.newArrayList();
        }

        Collections.addAll(extraGeneratedResFolders, generatedResFolders);
    }

    public void registerResGeneratingTask(@NonNull Task task, @NonNull Collection<File> generatedResFolders) {
        // no need add the folders anywhere, the convention mapping closure for the MergeResources
        // action will pick them up from here
        resourceGenTask.dependsOn(task);

        if (extraGeneratedResFolders == null) {
            extraGeneratedResFolders = Lists.newArrayList();
        }

        extraGeneratedResFolders.addAll(generatedResFolders);
    }

    /**
     * Calculates the filters for this variant. The filters can either be manually specified by
     * the user within the build.gradle or can be automatically discovered using the variant
     * specific folders.
     * <p>
     * This method must be called before {@link #getFilters(OutputFile.FilterType)}.
     *
     * @param splits the splits configuration from the build.gradle.
     */
    public void calculateFilters(Splits splits) {

        List<ResourceSet> resourceSets = variantConfiguration
                .getResourceSets(getGeneratedResFolders(), false);
        densityFilters = getFilters(resourceSets, DiscoverableFilterType.DENSITY, splits);
        languageFilters = getFilters(resourceSets, DiscoverableFilterType.LANGUAGE, splits);
    }

    /**
     * Returns the filters values (as manually specified or automatically discovered) for a
     * particular {@link com.android.build.OutputFile.FilterType}
     *
     * @param filterType the type of filter in question
     * @return a possibly empty set of filter values.
     * @throws IllegalStateException if {@link #calculateFilters(Splits)} has not been called prior
     *                               to invoking this method.
     */
    @NonNull
    public Set<String> getFilters(OutputFile.FilterType filterType) {
        if (densityFilters == null || languageFilters == null) {
            throw new IllegalStateException("calculateFilters method not called");
        }
        switch (filterType) {
            case DENSITY:
                return densityFilters;
            case LANGUAGE:
                return languageFilters;
            default:
                throw new RuntimeException("Unhandled filter type");
        }
    }

    /**
     * Returns the list of generated res folders for this variant.
     */
    private List<File> getGeneratedResFolders() {
        List<File> generatedResFolders = Lists.newArrayList(scope.getGeneratedResOutputDir());
        if (extraGeneratedResFolders != null) {
            generatedResFolders.addAll(extraGeneratedResFolders);
        }
        return generatedResFolders;
    }

    @NonNull
    public List<String> discoverListOfResourceConfigs() {
        List<String> resFoldersOnDisk = new ArrayList<String>();
        List<ResourceSet> resourceSets = variantConfiguration.getResourceSets(
                getGeneratedResFolders(), false /* no libraries resources */);
        resFoldersOnDisk.addAll(getAllFilters(
                resourceSets,
                DiscoverableFilterType.LANGUAGE.folderPrefix,
                DiscoverableFilterType.DENSITY.folderPrefix));
        return resFoldersOnDisk;
    }

    /**
     * Computes the Java sources to use for compilation. This Object[] contains
     * {@link org.gradle.api.file.FileCollection} and {@link File} instances
     */
    @NonNull
    public Object[] getJavaSources() {
        if (javaSources == null) {
            // Build the list of source folders.
            List<Object> sourceList = Lists.newArrayList();

            // First the actual source folders.
            List<SourceProvider> providers = variantConfiguration.getSortedSourceProviders();
            for (SourceProvider provider : providers) {
                sourceList.add(((AndroidSourceSet) provider).getJava().getSourceFiles());
            }

            // then all the generated src folders.
            if (getScope().getGenerateRClassTask() != null) {
                sourceList.add(getScope().getRClassSourceOutputDir());
            }

            // for the other, there's no duplicate so no issue.
            if (getScope().getGenerateBuildConfigTask() != null) {
                sourceList.add(scope.getBuildConfigSourceOutputDir());
            }

            if (getScope().getAidlCompileTask() != null) {
                sourceList.add(scope.getAidlSourceOutputDir());
            }

            javaSources = sourceList.toArray();
        }

        return javaSources;
    }

    /**
     * Returns a list of configuration name for wear connection, from highest to lowest priority.
     *
     * @return list of config.
     */
    @NonNull
    public List<String> getWearConfigNames() {
        List<SourceProvider> providers = variantConfiguration.getSortedSourceProviders();

        // this is the wrong order, so let's reverse it as we gather the names.
        final int count = providers.size();
        List<String> names = Lists.newArrayListWithCapacity(count);
        for (int i = count - 1; i >= 0; i--) {
            DefaultAndroidSourceSet sourceSet = (DefaultAndroidSourceSet) providers.get(i);

            names.add(sourceSet.getWearAppConfigurationName());
        }

        return names;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .addValue(variantConfiguration.getFullName())
                .toString();
    }

    @Nullable
    public FileSupplier getMappingFileProvider() {
        return mappingFileProviderTask;
    }

    @Nullable
    public File getMappingFile() {
        return mappingFileProviderTask != null ? mappingFileProviderTask.get() : null;
    }

    @NonNull
    public VariantScope getScope() {
        return scope;
    }

    public enum SplitHandlingPolicy {
        /**
         * Any release before L will create fake splits where each split will be the entire
         * application with the split specific resources.
         */
        PRE_21_POLICY,

        /**
         * Android L and after, the splits are pure splits where splits only contain resources
         * specific to the split characteristics.
         */
        RELEASE_21_AND_AFTER_POLICY
    }

    /**
     * Defines the discoverability attributes of filters.
     */
    private enum DiscoverableFilterType {

        DENSITY("drawable-") {
            @NonNull
            @Override
            Collection<String> getConfiguredFilters(@NonNull Splits splits) {
                return splits.getDensityFilters();
            }

            @Override
            boolean isAuto(@NonNull Splits splits) {
                return splits.getDensity().isAuto();
            }

        }, LANGUAGE("values-") {
            @NonNull
            @Override
            Collection<String> getConfiguredFilters(@NonNull Splits splits) {
                return splits.getLanguageFilters();
            }

            @Override
            boolean isAuto(@NonNull Splits splits) {
                return splits.getLanguage().isAuto();
            }
        };

        /**
         * Sets the folder prefix that filter specific resources must start with.
         */
        private String folderPrefix;

        DiscoverableFilterType(String folderPrefix) {
            this.folderPrefix = folderPrefix;
        }

        /**
         * Returns the applicable filters configured in the build.gradle for this filter type.
         *
         * @param splits the build.gradle splits configuration
         * @return a list of filters.
         */
        @NonNull
        abstract Collection<String> getConfiguredFilters(@NonNull Splits splits);

        /**
         * Returns true if the user wants the build system to auto discover the splits for this
         * split type.
         *
         * @param splits the build.gradle splits configuration.
         * @return true to use auto-discovery, false to use the build.gradle configuration.
         */
        abstract boolean isAuto(@NonNull Splits splits);
    }
}
