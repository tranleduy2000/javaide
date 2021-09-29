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

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.OutputFile;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dependency.LibraryDependencyImpl;
import com.android.build.gradle.internal.dependency.ManifestDependencyImpl;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.dsl.PackagingOptions;
import com.android.build.gradle.internal.publishing.ApkPublishArtifact;
import com.android.build.gradle.internal.publishing.MappingPublishArtifact;
import com.android.build.gradle.internal.publishing.MetadataPublishArtifact;
import com.android.build.gradle.internal.scope.AndroidTask;
import com.android.build.gradle.internal.scope.AndroidTaskRegistry;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantOutputScope;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.CheckManifest;
import com.android.build.gradle.internal.tasks.ExtractJavaResourcesTask;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.tasks.MergeJavaResourcesTask;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.internal.tasks.SourceSetsTask;
import com.android.build.gradle.internal.tasks.multidex.CreateMainDexList;
import com.android.build.gradle.internal.tasks.multidex.CreateManifestKeepList;
import com.android.build.gradle.internal.tasks.multidex.JarMergingTask;
import com.android.build.gradle.internal.tasks.multidex.RetraceMainDexList;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.ApkVariantOutputData;
import com.android.build.gradle.internal.variant.ApplicationVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.AndroidJarTask;
import com.android.build.gradle.tasks.AndroidProGuardTask;
import com.android.build.gradle.tasks.CompatibleScreensManifest;
import com.android.build.gradle.tasks.Dex;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.GenerateResValues;
import com.android.build.gradle.tasks.JavaResourcesProvider;
import com.android.build.gradle.tasks.Lint;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeManifests;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.PackageApplication;
import com.android.build.gradle.tasks.PackageSplitRes;
import com.android.build.gradle.tasks.PreDex;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.build.gradle.tasks.ProcessManifest;
import com.android.build.gradle.tasks.ShrinkResources;
import com.android.build.gradle.tasks.SplitZipAlign;
import com.android.build.gradle.tasks.ZipAlign;
import com.android.build.gradle.tasks.factory.JavaCompileConfigAction;
import com.android.build.gradle.tasks.factory.ProGuardTaskConfigAction;
import com.android.build.gradle.tasks.factory.ProcessJavaResConfigAction;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.sdk.TargetInfo;
import com.android.utils.StringHelper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import groovy.lang.Closure;
import proguard.gradle.ProGuardTask;

import static com.android.build.OutputFile.DENSITY;
import static com.android.sdklib.BuildToolInfo.PathId.ZIP_ALIGN;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Manages tasks creation.
 */
public abstract class TaskManager {

    public static final String DEFAULT_PROGUARD_CONFIG_FILE = "proguard-android.txt";

    public static final String DIR_BUNDLES = "bundles";
    protected static final String LINT_COMPILE = "compileLint";
    private static final String BUILD_GROUP = BasePlugin.BUILD_GROUP;
    private static final String ANDROID_GROUP = "Android";
    // Task names
    // TODO: Convert to AndroidTask.
    private static final String MAIN_PREBUILD = "preBuild";
    private static final String SOURCE_SETS = "sourceSets";

    private static final String LINT = "lint";
    private final GlobalScope globalScope;
    protected Project project;
    protected AndroidBuilder androidBuilder;
    protected SdkHandler sdkHandler;
    protected AndroidConfig extension;
    protected ToolingModelBuilderRegistry toolingRegistry;
    private DependencyManager dependencyManager;
    private AndroidTaskRegistry androidTasks = new AndroidTaskRegistry();
    private Logger logger;

    public TaskManager(
            Project project,
            AndroidBuilder androidBuilder,
            AndroidConfig extension,
            SdkHandler sdkHandler,
            DependencyManager dependencyManager,
            ToolingModelBuilderRegistry toolingRegistry) {
        this.project = project;
        this.androidBuilder = androidBuilder;
        this.sdkHandler = sdkHandler;
        this.extension = extension;
        this.toolingRegistry = toolingRegistry;
        this.dependencyManager = dependencyManager;
        logger = Logging.getLogger(this.getClass());

        globalScope = new GlobalScope(
                project,
                androidBuilder,
                checkNotNull((String) project.getProperties().get("archivesBaseName")),
                extension,
                sdkHandler,
                toolingRegistry);
    }

    private static <T> void addAllIfNotNull(@NonNull Collection<T> main, @Nullable Collection<T> toAdd) {
        if (toAdd != null) {
            main.addAll(toAdd);
        }
    }

    /**
     * Makes the given task the one used by top-level "compile" task.
     */
    public static void setJavaCompilerTask(
            @NonNull AndroidTask<? extends AbstractCompile> javaCompilerTask,
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope) {
        scope.getCompileTask().dependsOn(tasks, javaCompilerTask);
        scope.setJavaCompilerTask(javaCompilerTask);

        // TODO: Get rid of it once we stop keeping tasks in variant data.
        //noinspection VariableNotUsedInsideIf
        if (scope.getVariantData().javacTask != null) {
            // This is not the experimental plugin, let's update variant data, so Variants API
            // keeps working.
            scope.getVariantData().javaCompilerTask = (AbstractCompile) tasks.named(javaCompilerTask.getName());
        }

    }

    /**
     * Is the given variant relevant for lint?
     */
    private static boolean isLintVariant(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> baseVariantData) {
        return true;
    }

    public static void createJarTask(@NonNull TaskFactory tasks, @NonNull final VariantScope scope) {
        final BaseVariantData variantData = scope.getVariantData();

        final GradleVariantConfiguration config = variantData.getVariantConfiguration();
        tasks.create(
                scope.getTaskName("jar", "Classes"),
                AndroidJarTask.class,
                new Action<AndroidJarTask>() {
                    @Override
                    public void execute(AndroidJarTask jarTask) {
                        jarTask.setArchiveName("classes.jar");
                        jarTask.setDestinationDir(new File(
                                scope.getGlobalScope().getIntermediatesDir(),
                                "packaged/" + config.getDirName() + "/"));
                        jarTask.from(scope.getJavaOutputDir());
                        jarTask.dependsOn(scope.getJavacTask().getName());
                        variantData.binayFileProviderTask = jarTask;
                    }

                });
    }

    public static void optionalDependsOn(@NonNull Task main, @NonNull List<?> dependencies) {
        for (Object dependency : dependencies) {
            if (dependency != null) {
                main.dependsOn(dependency);
            }
        }
    }

    @NonNull
    private static List<ManifestDependencyImpl> getManifestDependencies(
            List<LibraryDependency> libraries) {
        List<ManifestDependencyImpl> list = Lists.newArrayListWithCapacity(libraries.size());

        for (LibraryDependency lib : libraries) {
            // get the dependencies
            List<ManifestDependencyImpl> children = getManifestDependencies(lib.getDependencies());
            list.add(new ManifestDependencyImpl(lib.getName(), lib.getManifest(), children));
        }

        return list;
    }

    private boolean isVerbose() {
        return project.getLogger().isEnabled(LogLevel.INFO);
    }

    private boolean isDebugLog() {
        return project.getLogger().isEnabled(LogLevel.DEBUG);
    }

    /**
     * Creates the tasks for a given BaseVariantData.
     */
    public abstract void createTasksForVariantData(@NonNull TaskFactory tasks,
                                                   @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData);

    public GlobalScope getGlobalScope() {
        return globalScope;
    }

    protected AndroidConfig getExtension() {
        return extension;
    }

    public void resolveDependencies(
            @NonNull VariantDependencies variantDeps) {
        dependencyManager.resolveDependencies(variantDeps);
    }

    /**
     * Create tasks before the evaluation (on plugin apply). This is useful for tasks that
     * could be referenced by custom build logic.
     */
    public void createTasksBeforeEvaluate(@NonNull TaskFactory tasks) {
        tasks.create(MAIN_PREBUILD);

        tasks.create(SOURCE_SETS, SourceSetsTask.class, new Action<SourceSetsTask>() {
            @Override
            public void execute(SourceSetsTask sourceSetsTask) {
                sourceSetsTask.setConfig(extension);
                sourceSetsTask.setDescription(
                        "Prints out all the source sets defined in this project.");
                sourceSetsTask.setGroup(ANDROID_GROUP);
            }
        });

        tasks.create(LINT, Lint.class, new Action<Lint>() {
            @Override
            public void execute(Lint lintTask) {
                lintTask.setDescription("Runs lint on all variants.");
                lintTask.setVariantName("");
                lintTask.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
                lintTask.setLintOptions(getExtension().getLintOptions());
                lintTask.setSdkHome(sdkHandler.getSdkFolder());
                lintTask.setToolingRegistry(toolingRegistry);
            }
        });
        tasks.named(JavaBasePlugin.CHECK_TASK_NAME, new Action<Task>() {
            @Override
            public void execute(Task it) {
                it.dependsOn(LINT);
            }
        });
        createLintCompileTask(tasks);
    }

    public void createMergeAppManifestsTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope variantScope) {

        ApplicationVariantData appVariantData =
                (ApplicationVariantData) variantScope.getVariantData();
        Set<String> screenSizes = appVariantData.getCompatibleScreens();

        // loop on all outputs. The only difference will be the name of the task, and location
        // of the generated manifest
        for (final BaseVariantOutputData vod : appVariantData.getOutputs()) {
            VariantOutputScope scope = vod.getScope();

            AndroidTask<CompatibleScreensManifest> csmTask = null;
            if (vod.getMainOutputFile().getFilter(DENSITY) != null) {
                csmTask = androidTasks.create(tasks,
                        new CompatibleScreensManifest.ConfigAction(scope, screenSizes));
                scope.setCompatibleScreensManifestTask(csmTask);
            }

            scope.setManifestProcessorTask(androidTasks.create(tasks,
                    new MergeManifests.ConfigAction(scope)));

            if (csmTask != null) {
                scope.getManifestProcessorTask().dependsOn(tasks, csmTask);
            }
        }
    }

    public void createMergeLibManifestsTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope) {

        AndroidTask<ProcessManifest> processManifest = androidTasks.create(tasks,
                new ProcessManifest.ConfigAction(scope));

        processManifest.dependsOn(tasks, scope.getVariantData().prepareDependenciesTask);

        BaseVariantOutputData variantOutputData = scope.getVariantData().getOutputs().get(0);
        variantOutputData.getScope().setManifestProcessorTask(processManifest);
    }

    public AndroidTask<MergeResources> createMergeResourcesTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope) {
        return basicCreateMergeResourcesTask(
                tasks,
                scope,
                "merge",
                null /*outputLocation*/,
                true /*includeDependencies*/,
                true /*process9patch*/);
    }

    public AndroidTask<MergeResources> basicCreateMergeResourcesTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope,
            @NonNull String taskNamePrefix,
            @Nullable File outputLocation,
            final boolean includeDependencies,
            final boolean process9Patch) {
        AndroidTask<MergeResources> mergeResourcesTask = androidTasks.create(tasks,
                new MergeResources.ConfigAction(
                        scope,
                        taskNamePrefix,
                        outputLocation,
                        includeDependencies,
                        process9Patch));
        mergeResourcesTask.dependsOn(tasks,
                scope.getVariantData().prepareDependenciesTask,
                scope.getResourceGenTask());
        scope.setMergeResourcesTask(mergeResourcesTask);
        scope.setResourceOutputDir(
                MoreObjects.firstNonNull(outputLocation, scope.getDefaultMergeResourcesOutputDir()));
        return scope.getMergeResourcesTask();
    }

    public void createMergeAssetsTask(TaskFactory tasks, VariantScope scope) {
        AndroidTask<MergeAssets> mergeAssetsTask = androidTasks.create(tasks, new MergeAssets.ConfigAction(scope));
        mergeAssetsTask.dependsOn(tasks,
                scope.getVariantData().prepareDependenciesTask,
                scope.getAssetGenTask());
        scope.setMergeAssetsTask(mergeAssetsTask);
    }

    public void createBuildConfigTask(@NonNull TaskFactory tasks, @NonNull VariantScope scope) {
        AndroidTask<GenerateBuildConfig> generateBuildConfigTask =
                androidTasks.create(tasks, new GenerateBuildConfig.ConfigAction(scope));
        scope.setGenerateBuildConfigTask(generateBuildConfigTask);
        scope.getSourceGenTask().dependsOn(tasks, generateBuildConfigTask.getName());
        generateBuildConfigTask.dependsOn(tasks, scope.getCheckManifestTask());
    }

    public void createGenerateResValuesTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope) {
        AndroidTask<GenerateResValues> generateResValuesTask = androidTasks.create(
                tasks, new GenerateResValues.ConfigAction(scope));
        scope.getResourceGenTask().dependsOn(tasks, generateResValuesTask);
    }

    public void createProcessResTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope,
            boolean generateResourcePackage) {
        File symbolLocation = new File(globalScope.getIntermediatesDir(), "symbols/" + scope.getVariantData().getVariantConfiguration().getDirName());
        createProcessResTask(tasks, scope, symbolLocation, generateResourcePackage);
    }

    public void createProcessResTask(
            @NonNull TaskFactory tasks,
            @NonNull VariantScope scope,
            @Nullable File symbolLocation,
            boolean generateResourcePackage) {
        BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();

        variantData.calculateFilters(scope.getGlobalScope().getExtension().getSplits());

        // loop on all outputs. The only difference will be the name of the task, and location
        // of the generated data.
        for (BaseVariantOutputData vod : variantData.getOutputs()) {
            final VariantOutputScope variantOutputScope = vod.getScope();
            ProcessAndroidResources.ConfigAction configAction = new ProcessAndroidResources.ConfigAction(variantOutputScope, symbolLocation, generateResourcePackage);
            variantOutputScope.setProcessResourcesTask(androidTasks.create(tasks, configAction));
            variantOutputScope.getProcessResourcesTask().dependsOn(tasks,
                    variantOutputScope.getManifestProcessorTask(),
                    scope.getMergeResourcesTask(),
                    scope.getMergeAssetsTask());

            if (vod.getMainOutputFile().getFilter(DENSITY) == null) {
                scope.setGenerateRClassTask(variantOutputScope.getProcessResourcesTask());
                scope.getSourceGenTask().optionalDependsOn(tasks, variantOutputScope.getProcessResourcesTask());
            }

        }

    }

    /**
     * Creates the split resources packages task if necessary. AAPT will produce split packages for
     * all --split provided parameters. These split packages should be signed and moved unchanged to
     * the APK build output directory.
     */
    public void createSplitResourcesTasks(@NonNull VariantScope scope) {
        BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();

        checkState(variantData.getSplitHandlingPolicy().equals(
                BaseVariantData.SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY),
                "Can only create split resources tasks for pure splits.");

        final VariantConfiguration config = variantData.getVariantConfiguration();
        Set<String> densityFilters = variantData.getFilters(OutputFile.FilterType.DENSITY);
        Set<String> abiFilters = variantData.getFilters(OutputFile.FilterType.ABI);
        Set<String> languageFilters = variantData.getFilters(OutputFile.FilterType.LANGUAGE);

        List<? extends BaseVariantOutputData> outputs = variantData.getOutputs();
        if (outputs.size() != 1) {
            throw new RuntimeException(
                    "In release 21 and later, there can be only one main APK, " +
                            "found " + outputs.size());
        }

        final BaseVariantOutputData variantOutputData = outputs.get(0);
        VariantOutputScope variantOutputScope = variantOutputData.getScope();
        variantOutputData.packageSplitResourcesTask = project.getTasks().create(
                scope.getTaskName("package", "SplitResources"),
                PackageSplitRes.class);
        variantOutputData.packageSplitResourcesTask.setInputDirectory(
                variantOutputScope.getProcessResourcePackageOutputFile().getParentFile());
        variantOutputData.packageSplitResourcesTask.setDensitySplits(densityFilters);
        variantOutputData.packageSplitResourcesTask.setLanguageSplits(languageFilters);
        variantOutputData.packageSplitResourcesTask.setOutputBaseName(config.getBaseName());
        variantOutputData.packageSplitResourcesTask.setSigningConfig(config.getSigningConfig());
        variantOutputData.packageSplitResourcesTask.setOutputDirectory(new File(
                scope.getGlobalScope().getIntermediatesDir(), "splits/" + config.getDirName()));
        variantOutputData.packageSplitResourcesTask.setAndroidBuilder(androidBuilder);
        variantOutputData.packageSplitResourcesTask.setVariantName(config.getFullName());
        variantOutputData.packageSplitResourcesTask.dependsOn(
                variantOutputScope.getProcessResourcesTask().getName());

        SplitZipAlign zipAlign = project.getTasks().create(
                scope.getTaskName("zipAlign", "SplitPackages"),
                SplitZipAlign.class);
        zipAlign.setVariantName(config.getFullName());
        ConventionMappingHelper.map(zipAlign, "zipAlignExe", new Callable<File>() {
            @Override
            public File call() throws Exception {
                final TargetInfo info = androidBuilder.getTargetInfo();
                if (info == null) {
                    return null;
                }
                String path = info.getBuildTools().getPath(ZIP_ALIGN);
                if (path == null) {
                    return null;
                }
                return new File(path);
            }
        });

        zipAlign.setOutputDirectory(new File(scope.getGlobalScope().getBuildDir(), "outputs/apk"));
        ConventionMappingHelper.map(zipAlign, "densityOrLanguageInputFiles",
                new Callable<List<File>>() {
                    @Override
                    public List<File> call() {
                        return variantOutputData.packageSplitResourcesTask.getOutputFiles();
                    }
                });
        zipAlign.setOutputBaseName(config.getBaseName());
        zipAlign.setAbiFilters(abiFilters);
        zipAlign.setLanguageFilters(languageFilters);
        zipAlign.setDensityFilters(densityFilters);
        File metadataDirectory = new File(zipAlign.getOutputDirectory().getParentFile(),
                "metadata");
        zipAlign.setApkMetadataFile(new File(metadataDirectory, config.getFullName() + ".mtd"));
        ((ApkVariantOutputData) variantOutputData).splitZipAlign = zipAlign;
        zipAlign.dependsOn(variantOutputData.packageSplitResourcesTask);
    }

    /**
     * Creates the java resources processing tasks.
     * <p>
     * The java processing will happen in three steps :
     * <ul>{@link ExtractJavaResourcesTask} will extract all java resources from packaged jar files
     * dependencies. Each jar file will be extracted in a separate folder. Each folder will be
     * located under {@link VariantScope#getPackagedJarsJavaResDestinationDir()}</ul>
     * <ul>{@link ProcessJavaResConfigAction} will sync all source folders into a single folder
     * identified by {@link VariantScope#getSourceFoldersJavaResDestinationDir()}</ul>
     * <ul>{@link MergeJavaResourcesTask} will take all these folders and will create a single
     * merged folder with the {@link PackagingOptions} settings applied. The folder is located at
     * {@link VariantScope#getJavaResourcesDestinationDir()}</ul>
     * <p>
     * the result of 3 is the final set of java resources to can be either directly embedded in
     * the resulting APK or fed into the obfuscation tool to produce obfuscated resources.
     *
     * @param tasks tasks factory to create tasks.
     * @param scope the variant scope we are operating under.
     */
    public void createProcessJavaResTasks(@NonNull TaskFactory tasks, @NonNull VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();

        // first create the incremental task that will extract all libraries java resources
        // in separate folders.
        AndroidTask<ExtractJavaResourcesTask> extractJavaResourcesTask = androidTasks
                .create(tasks, new ExtractJavaResourcesTask.Config(scope));

        // now copy the source folders java resources into the temporary location, mainly to
        // maintain the PluginDsl COPY semantics.
        scope.setProcessJavaResourcesTask(
                androidTasks.create(tasks, new ProcessJavaResConfigAction(scope)));

        // and create the merge tasks that will merge everything.
        AndroidTask<MergeJavaResourcesTask> mergeJavaResourcesTask = androidTasks
                .create(tasks, new MergeJavaResourcesTask.Config(scope));
        // the merge task is the official provider for merged java resources to be bundled in the
        // final variant specific APK, this may change if obfuscation is turned on.
        scope.setJavaResourcesProvider(
                JavaResourcesProvider.Adapter.build(tasks, mergeJavaResourcesTask));

        // set the dependencies.
        extractJavaResourcesTask.dependsOn(tasks, variantData.prepareDependenciesTask);
        scope.getProcessJavaResourcesTask().dependsOn(tasks, extractJavaResourcesTask);
        mergeJavaResourcesTask.dependsOn(tasks, scope.getProcessJavaResourcesTask());

        scope.setMergeJavaResourcesTask(mergeJavaResourcesTask);

    }

    public void createAidlTask(@NonNull TaskFactory tasks, @NonNull VariantScope scope) {
        scope.setAidlCompileTask(androidTasks.create(tasks, new AidlCompile.ConfigAction(scope)));
        scope.getSourceGenTask().dependsOn(tasks, scope.getAidlCompileTask());
        scope.getAidlCompileTask().dependsOn(tasks, scope.getVariantData().prepareDependenciesTask);
    }

    /**
     * Creates the task for creating *.class files using javac. These tasks are created regardless
     * of whether Jack is used or not, but assemble will not depend on them if it is. They are
     * always used when running unit tests.
     */
    public AndroidTask<JavaCompile> createJavacTask(
            @NonNull final TaskFactory tasks,
            @NonNull final VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
        final AndroidTask<JavaCompile> javacTask = androidTasks.create(tasks,
                new JavaCompileConfigAction(scope));
        scope.setJavacTask(javacTask);

        javacTask.optionalDependsOn(tasks, scope.getSourceGenTask());
        javacTask.dependsOn(tasks,
                scope.getVariantData().prepareDependenciesTask,
                scope.getMergeJavaResourcesTask());

        // TODO - dependency information for the compile classpath is being lost.
        // Add a temporary approximation
        javacTask.dependsOn(tasks,
                scope.getVariantData().getVariantDependency().getCompileConfiguration()
                        .getBuildDependencies());

        // Create jar task for uses by external modules.
        if (variantData.getVariantDependency().getClassesConfiguration() != null) {
            tasks.create(scope.getTaskName("package", "JarArtifact"), Jar.class, new Action<Jar>() {
                @Override
                public void execute(Jar jar) {
                    variantData.classesJarTask = jar;
                    jar.dependsOn(javacTask.getName());

                    // add the class files (whether they are instrumented or not.
                    jar.from(scope.getJavaOutputDir());

                    jar.setDestinationDir(new File(
                            scope.getGlobalScope().getIntermediatesDir(),
                            "classes-jar/" +
                                    variantData.getVariantConfiguration().getDirName()));
                    jar.setArchiveName("classes.jar");
                }
            });
        }

        return javacTask;
    }

    // TODO - should compile src/lint/java from src/lint/java and jar it into build/lint/lint.jar
    private void createLintCompileTask(TaskFactory tasks) {

        // TODO: move doFirst into dedicated task class.
        tasks.create(LINT_COMPILE, Task.class,
                new Action<Task>() {
                    @Override
                    public void execute(Task lintCompile) {
                        final File outputDir =
                                new File(getGlobalScope().getIntermediatesDir(), "lint");

                        lintCompile.doFirst(new Action<Task>() {
                            @Override
                            public void execute(Task task) {
                                // create the directory for lint output if it does not exist.
                                if (!outputDir.exists()) {
                                    boolean mkdirs = outputDir.mkdirs();
                                    if (!mkdirs) {
                                        throw new GradleException(
                                                "Unable to create lint output directory.");
                                    }
                                }
                            }
                        });
                    }
                });
    }

    /**
     * Add tasks for running lint on individual variants. We've already added a
     * lint task earlier which runs on all variants.
     */
    public void createLintTasks(TaskFactory tasks, final VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> baseVariantData =
                scope.getVariantData();
        if (!isLintVariant(baseVariantData)) {
            return;
        }

        // wire the main lint task dependency.
        tasks.named(LINT, new Action<Task>() {
            @Override
            public void execute(Task it) {
                it.dependsOn(LINT_COMPILE);
                it.dependsOn(scope.getJavacTask().getName());
            }
        });

        AndroidTask<Lint> variantLintCheck = androidTasks.create(
                tasks, new Lint.ConfigAction(scope));
        variantLintCheck.dependsOn(tasks, LINT_COMPILE, scope.getJavacTask());
    }

    private void createLintVitalTask(@NonNull ApkVariantData variantData) {
        checkState(getExtension().getLintOptions().isCheckReleaseBuilds());
        // TODO: re-enable with Jack when possible
        if (!variantData.getVariantConfiguration().getBuildType().isDebuggable()) {
            String variantName = variantData.getVariantConfiguration().getFullName();
            String capitalizedVariantName = StringHelper.capitalize(variantName);
            String taskName = "lintVital" + capitalizedVariantName;
            final Lint lintReleaseCheck = project.getTasks().create(taskName, Lint.class);
            // TODO: Make this task depend on lintCompile too (resolve initialization order first)
            optionalDependsOn(lintReleaseCheck, variantData.javacTask);
            lintReleaseCheck.setLintOptions(getExtension().getLintOptions());
            lintReleaseCheck.setSdkHome(sdkHandler.getSdkFolder());
            lintReleaseCheck.setVariantName(variantName);
            lintReleaseCheck.setToolingRegistry(toolingRegistry);
            lintReleaseCheck.setFatalOnly(true);
            lintReleaseCheck.setDescription(
                    "Runs lint on just the fatal issues in the " + capitalizedVariantName
                            + " build.");
            //variantData.assembleVariantTask.dependsOn lintReleaseCheck

            // If lint is being run, we do not need to run lint vital.
            // TODO: Find a better way to do this.
            project.getGradle().getTaskGraph().whenReady(new Closure<Void>(this, this) {
                public void doCall(TaskExecutionGraph taskGraph) {
                    if (taskGraph.hasTask(LINT)) {
                        lintReleaseCheck.setEnabled(false);
                    }
                }
            });
        }
    }

    /**
     * Creates the post-compilation tasks for the given Variant.
     * <p>
     * These tasks create the dex file from the .class files, plus optional intermediary steps like
     * proguard and jacoco
     */
    public void createPostCompilationTasks(TaskFactory tasks, @NonNull final VariantScope scope) {
        checkNotNull(scope.getJavacTask());

        final ApkVariantData variantData = (ApkVariantData) scope.getVariantData();
        final GradleVariantConfiguration config = variantData.getVariantConfiguration();

        // data holding dependencies and input for the dex. This gets updated as new
        // post-compilation steps are inserted between the compilation and dx.
        PostCompilationData pcData = new PostCompilationData();
        pcData.setClassGeneratingTasks(Collections.singletonList(scope.getJavacTask().getName()));
        pcData.setLibraryGeneratingTasks(ImmutableList.of(variantData.prepareDependenciesTask,
                variantData.getVariantDependency().getPackageConfiguration()
                        .getBuildDependencies()));
        pcData.setInputFilesCallable(new Callable<List<File>>() {
            @Override
            public List<File> call() {
                return new ArrayList<File>(
                        variantData.javacTask.getOutputs().getFiles().getFiles());
            }

        });
        pcData.setInputDir(scope.getJavaOutputDir());

        pcData.setJavaResourcesInputDir(scope.getJavaResourcesDestinationDir());

        pcData.setInputLibrariesCallable(new Callable<List<File>>() {
            @Override
            public List<File> call() {
                return new ArrayList<File>(
                        scope.getGlobalScope().getAndroidBuilder().getPackagedJars(config));
            }

        });

        boolean isMinifyEnabled = config.isMinifyEnabled();
        boolean isMultiDexEnabled = config.isMultiDexEnabled();
        boolean isLegacyMultiDexMode = config.isLegacyMultiDexMode();

        // ----- Minify next ----
        File outFile = maybeCreateProguardTasks(tasks, scope, pcData);
        if (outFile != null) {
            pcData.setInputFiles(Collections.singletonList(outFile));
            pcData.setInputLibraries(Collections.<File>emptyList());
        } else if ((getExtension().getDexOptions().getPreDexLibraries() && !isMultiDexEnabled) || (
                isMultiDexEnabled && !isLegacyMultiDexMode)) {

            AndroidTask<PreDex> preDexTask = androidTasks
                    .create(tasks, new PreDex.ConfigAction(scope, pcData));

            // update dependency.
            preDexTask.dependsOn(tasks, pcData.getLibraryGeneratingTasks());
            pcData.setLibraryGeneratingTasks(Collections.singletonList(preDexTask.getName()));

            // update inputs
            if (isMultiDexEnabled) {
                pcData.setInputLibraries(Collections.<File>emptyList());

            } else {
                pcData.setInputLibrariesCallable(new Callable<List<File>>() {
                    @Override
                    public List<File> call() {
                        return new ArrayList<File>(
                                project.fileTree(scope.getPreDexOutputDir()).getFiles());
                    }

                });
            }
        }

        AndroidTask<CreateMainDexList> createMainDexListTask = null;
        AndroidTask<RetraceMainDexList> retraceTask = null;

        // ----- Multi-Dex support
        if (isMultiDexEnabled && isLegacyMultiDexMode) {
            if (!isMinifyEnabled) {
                // create a task that will convert the output of the compilation
                // into a jar. This is needed by the multi-dex input.
                AndroidTask<JarMergingTask> jarMergingTask = androidTasks.create(tasks,
                        new JarMergingTask.ConfigAction(scope, pcData));

                // update dependencies
                jarMergingTask.optionalDependsOn(tasks,
                        pcData.getClassGeneratingTasks(),
                        pcData.getLibraryGeneratingTasks());
                pcData.setLibraryGeneratingTasks(
                        Collections.singletonList(jarMergingTask.getName()));
                pcData.setClassGeneratingTasks(Collections.singletonList(jarMergingTask.getName()));

                // Update the inputs
                pcData.setInputFiles(Collections.singletonList(scope.getJarMergingOutputFile()));
                pcData.setInputDirCallable(null);
                pcData.setInputLibraries(Collections.<File>emptyList());
            }

            // ----------
            // Create a task to collect the list of manifest entry points which are
            // needed in the primary dex
            AndroidTask<CreateManifestKeepList> manifestKeepListTask = androidTasks.create(tasks,
                    new CreateManifestKeepList.ConfigAction(scope, pcData));
            manifestKeepListTask.dependsOn(tasks,
                    variantData.getOutputs().get(0).getScope().getManifestProcessorTask());

            // ----------
            // Create a proguard task to shrink the classes to manifest components
            AndroidTask<ProGuardTask> proguardComponentsTask =
                    androidTasks.create(tasks, new ProGuardTaskConfigAction(scope, pcData));

            // update dependencies
            proguardComponentsTask.dependsOn(tasks, manifestKeepListTask);
            proguardComponentsTask.optionalDependsOn(tasks,
                    pcData.getClassGeneratingTasks(),
                    pcData.getLibraryGeneratingTasks());

            // ----------
            // Compute the full list of classes for the main dex file
            createMainDexListTask =
                    androidTasks.create(tasks, new CreateMainDexList.ConfigAction(scope, pcData));
            createMainDexListTask.dependsOn(tasks, proguardComponentsTask);
            //createMainDexListTask.dependsOn { proguardMainDexTask }

            // ----------
            // If proguard is enabled, create a de-obfuscated list to aid debugging.
            if (isMinifyEnabled) {
                retraceTask = androidTasks.create(tasks,
                        new RetraceMainDexList.ConfigAction(scope, pcData));
                retraceTask.dependsOn(tasks, scope.getObfuscationTask(), createMainDexListTask);
            }

        }

        AndroidTask<Dex> dexTask = androidTasks.create(tasks, new Dex.ConfigAction(scope, pcData));
        scope.setDexTask(dexTask);

        // dependencies, some of these could be null
        dexTask.optionalDependsOn(tasks,
                pcData.getClassGeneratingTasks(),
                pcData.getLibraryGeneratingTasks(),
                createMainDexListTask,
                retraceTask);
    }

    /**
     * Creates the final packaging task, and optionally the zipalign task (if the variant is signed)
     *
     * @param publishApk if true the generated APK gets published.
     */
    public void createPackagingTask(@NonNull TaskFactory tasks, @NonNull VariantScope variantScope,
                                    boolean publishApk) {
        final ApkVariantData variantData = (ApkVariantData) variantScope.getVariantData();

        GradleVariantConfiguration config = variantData.getVariantConfiguration();
        boolean signedApk = variantData.isSigned();
        File apkLocation = new File(variantScope.getGlobalScope().getApkLocation());

        boolean multiOutput = variantData.getOutputs().size() > 1;

        // loop on all outputs. The only difference will be the name of the task, and location
        // of the generated data.
        for (final ApkVariantOutputData variantOutputData : variantData.getOutputs()) {
            VariantOutputScope variantOutputScope = variantOutputData.getScope();

            final String outputName = variantOutputData.getFullName();

            // When shrinking resources, rather than having the packaging task
            // directly map to the packageOutputFile of ProcessAndroidResources,
            // we insert the ShrinkResources task into the chain, such that its
            // input is the ProcessAndroidResources packageOutputFile, and its
            // output is what the PackageApplication task reads.
            AndroidTask<ShrinkResources> shrinkTask = null;

            if (config.isMinifyEnabled() && config.getBuildType().isShrinkResources()) {
                shrinkTask = androidTasks.create(
                        tasks, new ShrinkResources.ConfigAction(variantOutputScope));
                shrinkTask.dependsOn(tasks, variantScope.getObfuscationTask(),
                        variantOutputScope.getManifestProcessorTask(),
                        variantOutputScope.getProcessResourcesTask());
            }

            AndroidTask<PackageApplication> packageApp = androidTasks.create(
                    tasks, new PackageApplication.ConfigAction(variantOutputScope));

            packageApp.dependsOn(tasks, variantOutputScope.getProcessResourcesTask(),
                    variantOutputScope.getVariantScope().getMergeJavaResourcesTask());

            packageApp.optionalDependsOn(
                    tasks,
                    shrinkTask,
                    // TODO: When Jack is converted, add activeDexTask to VariantScope.
                    variantOutputScope.getVariantScope().getDexTask(),
                    variantOutputScope.getVariantScope().getJavaCompilerTask(),
                    variantData.javaCompilerTask, // TODO: Remove when Jack is converted to AndroidTask.
                    variantOutputData.packageSplitResourcesTask,
                    variantOutputData.packageSplitAbiTask);

            AndroidTask appTask = packageApp;

            if (signedApk) {
                if (variantData.getZipAlignEnabled()) {
                    AndroidTask<ZipAlign> zipAlignTask = androidTasks.create(
                            tasks, new ZipAlign.ConfigAction(variantOutputScope));
                    zipAlignTask.dependsOn(tasks, packageApp);
                    if (variantOutputData.splitZipAlign != null) {
                        zipAlignTask.dependsOn(tasks, variantOutputData.splitZipAlign);
                    }

                    appTask = zipAlignTask;
                }

            }

            checkState(variantData.assembleVariantTask != null);

            // Add an assemble task
            if (multiOutput) {
                // create a task for this output
                variantOutputData.assembleTask = createAssembleTask(variantOutputData);

                // variant assemble task depends on each output assemble task.
                variantData.assembleVariantTask.dependsOn(variantOutputData.assembleTask);
            } else {
                // single output
                variantOutputData.assembleTask = variantData.assembleVariantTask;
            }

            if (!signedApk && variantOutputData.packageSplitResourcesTask != null) {
                // in case we are not signing the resulting APKs and we have some pure splits
                // we should manually copy them from the intermediate location to the final
                // apk location unmodified.
                Copy copyTask = project.getTasks().create(
                        variantOutputScope.getTaskName("copySplit"), Copy.class);
                copyTask.setDestinationDir(apkLocation);
                copyTask.from(variantOutputData.packageSplitResourcesTask.getOutputDirectory());
                variantOutputData.assembleTask.dependsOn(copyTask);
                copyTask.mustRunAfter(appTask.getName());
            }

            variantOutputData.assembleTask.dependsOn(appTask.getName());

            if (publishApk) {
                final String projectBaseName = globalScope.getProjectBaseName();

                // if this variant is the default publish config or we also should publish non
                // defaults, proceed with declaring our artifacts.
                if (getExtension().getDefaultPublishConfig().equals(outputName)) {
                    appTask.configure(tasks, new Action<Task>() {
                        @Override
                        public void execute(Task packageTask) {
                            project.getArtifacts().add("default",
                                    new ApkPublishArtifact(projectBaseName,
                                            null,
                                            (FileSupplier) packageTask));
                        }

                    });

                    for (FileSupplier outputFileProvider :
                            variantOutputData.getSplitOutputFileSuppliers()) {
                        project.getArtifacts().add("default",
                                new ApkPublishArtifact(projectBaseName, null, outputFileProvider));
                    }

                    try {
                        if (variantOutputData.getMetadataFile() != null) {
                            project.getArtifacts().add("default-metadata",
                                    new MetadataPublishArtifact(projectBaseName, null,
                                            variantOutputData.getMetadataFile()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (variantData.getMappingFileProvider() != null) {
                        project.getArtifacts().add("default-mapping",
                                new MappingPublishArtifact(projectBaseName, null,
                                        variantData.getMappingFileProvider()));
                    }
                }

                if (getExtension().getPublishNonDefault()) {
                    appTask.configure(tasks, new Action<Task>() {
                        @Override
                        public void execute(Task packageTask) {
                            project.getArtifacts().add(
                                    variantData.getVariantDependency().getPublishConfiguration().getName(),
                                    new ApkPublishArtifact(
                                            projectBaseName,
                                            null,
                                            (FileSupplier) packageTask));
                        }

                    });

                    for (FileSupplier outputFileProvider :
                            variantOutputData.getSplitOutputFileSuppliers()) {
                        project.getArtifacts().add(
                                variantData.getVariantDependency().getPublishConfiguration().getName(),
                                new ApkPublishArtifact(
                                        projectBaseName,
                                        null,
                                        outputFileProvider));
                    }

                    try {
                        if (variantOutputData.getMetadataFile() != null) {
                            project.getArtifacts().add(
                                    variantData.getVariantDependency().getMetadataConfiguration().getName(),
                                    new MetadataPublishArtifact(
                                            projectBaseName,
                                            null,
                                            variantOutputData.getMetadataFile()));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (variantData.getMappingFileProvider() != null) {
                        project.getArtifacts().add(
                                variantData.getVariantDependency().getMappingConfiguration().getName(),
                                new MappingPublishArtifact(
                                        projectBaseName,
                                        null,
                                        variantData.getMappingFileProvider()));
                    }

                    if (variantData.classesJarTask != null) {
                        project.getArtifacts().add(
                                variantData.getVariantDependency().getClassesConfiguration().getName(),
                                variantData.classesJarTask);
                    }
                }
            }
        }

        if (getExtension().getLintOptions().isCheckReleaseBuilds()) {
            createLintVitalTask(variantData);
        }
    }

    public Task createAssembleTask(@NonNull final BaseVariantOutputData variantOutputData) {
        Task assembleTask =
                project.getTasks().create(variantOutputData.getScope().getTaskName("assemble"));
        return assembleTask;
    }

    public Task createAssembleTask(TaskFactory tasks,
                                   @NonNull final BaseVariantData<? extends BaseVariantOutputData> variantData) {
        Task assembleTask =
                project.getTasks().create(variantData.getScope().getTaskName("assemble"));
        return assembleTask;
    }

    /**
     * creates a zip align. This does not use convention mapping, and is meant to let other plugin
     * create zip align tasks.
     *
     * @param name       the name of the task
     * @param inputFile  the input file
     * @param outputFile the output file
     * @return the task
     */
    @NonNull
    public ZipAlign createZipAlignTask(
            @NonNull String name,
            @NonNull File inputFile,
            @NonNull File outputFile) {
        // Add a task to zip align application package
        ZipAlign zipAlignTask = project.getTasks().create(name, ZipAlign.class);

        zipAlignTask.setInputFile(inputFile);
        zipAlignTask.setOutputFile(outputFile);
        ConventionMappingHelper.map(zipAlignTask, "zipAlignExe", new Callable<File>() {
            @Override
            public File call() throws Exception {
                final TargetInfo info = androidBuilder.getTargetInfo();
                if (info != null) {
                    String path = info.getBuildTools().getPath(ZIP_ALIGN);
                    if (path != null) {
                        return new File(path);
                    }
                }

                return null;
            }
        });

        return zipAlignTask;
    }

    /**
     * Creates the proguarding task for the given Variant if necessary.
     *
     * @return null if the proguard task was not created, otherwise the expected outputFile.
     */
    @Nullable
    public File maybeCreateProguardTasks(
            @NonNull final TaskFactory tasks,
            @NonNull VariantScope scope,
            @NonNull final PostCompilationData pcData) {
        if (!scope.getVariantData().getVariantConfiguration().isMinifyEnabled()) {
            return null;
        }

        final AndroidTask<AndroidProGuardTask> proguardTask = androidTasks.create(
                tasks, new AndroidProGuardTask.ConfigAction(scope, pcData));
        scope.setObfuscationTask(proguardTask);
        scope.setJavaResourcesProvider(JavaResourcesProvider.Adapter.build(tasks, proguardTask));

        // update dependency.
        proguardTask.optionalDependsOn(tasks, pcData.getClassGeneratingTasks(),
                pcData.getLibraryGeneratingTasks());
        pcData.setLibraryGeneratingTasks(Collections.singletonList(proguardTask.getName()));
        pcData.setClassGeneratingTasks(Collections.singletonList(proguardTask.getName()));

        // Return output file.
        return scope.getProguardOutputFile();
    }

    public void createAnchorTasks(@NonNull TaskFactory tasks, @NonNull VariantScope scope) {
        createPreBuildTasks(scope);

        // also create sourceGenTask
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
        scope.setSourceGenTask(androidTasks.create(tasks,
                scope.getTaskName("generate", "Sources"),
                Task.class,
                new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        variantData.sourceGenTask = task;
                    }
                }));

        // and resGenTask
        scope.setResourceGenTask(androidTasks.create(tasks,
                scope.getTaskName("generate", "Resources"),
                Task.class,
                new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        variantData.resourceGenTask = task;
                    }
                }));

        scope.setAssetGenTask(androidTasks.create(tasks,
                scope.getTaskName("generate", "Assets"),
                Task.class,
                new Action<Task>() {
                    @Override
                    public void execute(Task task) {
                        variantData.assetGenTask = task;
                    }
                }));

        // and compile task
        createCompileAnchorTask(tasks, scope);
    }

    private void createPreBuildTasks(@NonNull VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
        variantData.preBuildTask = project.getTasks().create(scope.getTaskName("pre", "Build"));
        variantData.preBuildTask.dependsOn(MAIN_PREBUILD);

        PrepareDependenciesTask prepareDependenciesTask = project.getTasks().create(
                scope.getTaskName("prepare", "Dependencies"), PrepareDependenciesTask.class);

        variantData.prepareDependenciesTask = prepareDependenciesTask;
        prepareDependenciesTask.dependsOn(variantData.preBuildTask);

        prepareDependenciesTask.setAndroidBuilder(androidBuilder);
        prepareDependenciesTask.setVariantName(scope.getVariantConfiguration().getFullName());
        prepareDependenciesTask.setVariant(variantData);

        // for all libraries required by the configurations of this variant, make this task
        // depend on all the tasks preparing these libraries.
        VariantDependencies configurationDependencies = variantData.getVariantDependency();
        prepareDependenciesTask.addChecker(configurationDependencies.getChecker());

        for (LibraryDependencyImpl lib : configurationDependencies.getLibraries()) {
            dependencyManager.addDependencyToPrepareTask(variantData, prepareDependenciesTask, lib);
        }
    }

    private void createCompileAnchorTask(@NonNull TaskFactory tasks, @NonNull final VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
        scope.setCompileTask(androidTasks.create(tasks, new TaskConfigAction<Task>() {
            @Override
            public String getName() {
                return scope.getTaskName("compile", "Sources");
            }

            @Override
            public Class<Task> getType() {
                return Task.class;
            }

            @Override
            public void execute(Task task) {
                variantData.compileTask = task;
                variantData.compileTask.setGroup(BUILD_GROUP);
            }
        }));
        variantData.assembleVariantTask.dependsOn(scope.getCompileTask().getName());
    }

    public void createCheckManifestTask(@NonNull TaskFactory tasks, @NonNull VariantScope scope) {
        final BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();
        final String name = variantData.getVariantConfiguration().getFullName();
        scope.setCheckManifestTask(androidTasks.create(tasks,
                scope.getTaskName("check", "Manifest"),
                CheckManifest.class,
                new Action<CheckManifest>() {
                    @Override
                    public void execute(CheckManifest checkManifestTask) {
                        variantData.checkManifestTask = checkManifestTask;
                        checkManifestTask.setVariantName(name);
                        ConventionMappingHelper.map(checkManifestTask, "manifest",
                                new Callable<File>() {
                                    @Override
                                    public File call() throws Exception {
                                        return variantData.getVariantConfiguration()
                                                .getDefaultSourceSet().getManifestFile();
                                    }
                                });
                    }

                }));
        scope.getCheckManifestTask().dependsOn(tasks, variantData.preBuildTask);
        variantData.prepareDependenciesTask.dependsOn(scope.getCheckManifestTask().getName());
    }

    @NonNull
    protected Logger getLogger() {
        return logger;
    }

    @NonNull
    protected AndroidTaskRegistry getAndroidTasks() {
        return androidTasks;
    }

    private File getDefaultProguardFile(String name) {
        File sdkDir = sdkHandler.getAndCheckSdkFolder();
        return new File(sdkDir,
                SdkConstants.FD_TOOLS + File.separatorChar + SdkConstants.FD_PROGUARD
                        + File.separatorChar + name);
    }

}
