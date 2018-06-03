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

package com.android.build.gradle.internal;

import static com.android.SdkConstants.FN_ANNOTATIONS_ZIP;
import static com.android.SdkConstants.LIBS_FOLDER;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.scope.AndroidTask;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.MergeFileTask;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.LibVariantOutputData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.build.gradle.internal.variant.VariantHelper;
import com.android.build.gradle.tasks.ExtractAnnotations;
import com.android.build.gradle.tasks.MergeResources;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.BuilderConstants;
import com.android.builder.dependency.LibraryBundle;
import com.android.builder.dependency.LibraryDependency;
import com.android.builder.dependency.ManifestDependency;
import com.android.builder.model.AndroidLibrary;
import com.android.builder.model.MavenCoordinates;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * TaskManager for creating tasks in an Android library project.
 */
public class LibraryTaskManager extends TaskManager {

    private static final String ANNOTATIONS = "annotations";

    private Task assembleDefault;

    public LibraryTaskManager (
            Project project,
            AndroidBuilder androidBuilder,
            AndroidConfig extension,
            SdkHandler sdkHandler,
            DependencyManager dependencyManager,
            ToolingModelBuilderRegistry toolingRegistry) {
        super(project, androidBuilder, extension, sdkHandler, dependencyManager, toolingRegistry);
    }

    @Override
    public void createTasksForVariantData(
            @NonNull final TaskFactory tasks,
            @NonNull final BaseVariantData<? extends BaseVariantOutputData> variantData) {
        final LibraryVariantData libVariantData = (LibraryVariantData) variantData;
        final GradleVariantConfiguration variantConfig = variantData.getVariantConfiguration();
        CoreBuildType buildType = variantConfig.getBuildType();

        final VariantScope variantScope = variantData.getScope();

        final String dirName = variantConfig.getDirName();

        createAnchorTasks(tasks, variantScope);

        createCheckManifestTask(tasks, variantScope);

        // Add a task to create the res values
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_GENERATE_RES_VALUES_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createGenerateResValuesTask(tasks, variantScope);
                        return null;
                    }
                });

        // Add a task to process the manifest(s)
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_MERGE_MANIFEST_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createMergeLibManifestsTask(tasks, variantScope);
                        return null;
                    }
                });

        // Add a task to compile renderscript files.
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_CREATE_RENDERSCRIPT_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createRenderscriptTask(tasks, variantScope);
                        return null;
                    }
                });

        AndroidTask<MergeResources> packageRes = ThreadRecorder.get().record(
                ExecutionType.LIB_TASK_MANAGER_CREATE_MERGE_RESOURCES_TASK,
                new Recorder.Block<AndroidTask<MergeResources>>() {
                    @Override
                    public AndroidTask<MergeResources> call() throws Exception {
                        // Create a merge task to only merge the resources from this library and not
                        // the dependencies. This is what gets packaged in the aar.
                        AndroidTask<MergeResources> mergeResourceTask =
                                basicCreateMergeResourcesTask(
                                        tasks,
                                        variantScope,
                                        "package",
                                        new File(
                                                variantScope.getGlobalScope().getIntermediatesDir(),
                                                DIR_BUNDLES + "/" + variantScope
                                                        .getVariantConfiguration().getDirName() +
                                                        "/res"),
                                        false /*includeDependencies*/,
                                        false /*process9Patch*/);

                        if (variantData.getVariantDependency().hasNonOptionalLibraries()) {
                            // Add a task to merge the resource folders, including the libraries, in order to
                            // generate the R.txt file with all the symbols, including the ones from
                            // the dependencies.
                            createMergeResourcesTask(tasks, variantScope);
                        }

                        mergeResourceTask.configure(tasks,
                                new Action<Task>() {
                                    @Override
                                    public void execute(Task task) {
                                        MergeResources mergeResourcesTask = (MergeResources) task;
                                        mergeResourcesTask.setPublicFile(new File(
                                                variantScope.getGlobalScope().getIntermediatesDir(),
                                                DIR_BUNDLES + "/" + dirName + "/" +
                                                        SdkConstants.FN_PUBLIC_TXT));
                                    }
                                });

                        return mergeResourceTask;
                    }
                });

        // Add a task to merge the assets folders
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_MERGE_ASSETS_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() {
                        createMergeAssetsTask(tasks, variantScope);
                        return null;
                    }
                });

        // Add a task to create the BuildConfig class
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_BUILD_CONFIG_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createBuildConfigTask(tasks, variantScope);
                        return null;
                    }
                });

        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_PROCESS_RES_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        // Add a task to generate resource source files, directing the location
                        // of the r.txt file to be directly in the bundle.
                        createProcessResTask(tasks, variantScope,
                                new File(variantScope.getGlobalScope().getIntermediatesDir(),
                                        DIR_BUNDLES + "/" + dirName),
                                false /*generateResourcePackage*/);

                        // process java resources
                        createProcessJavaResTasks(tasks, variantScope);
                        return null;
                    }
                });

        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_AIDL_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createAidlTask(tasks, variantScope);
                        return null;
                    }
                });

        // Add a compile task
        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_COMPILE_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        AndroidTask<JavaCompile> javacTask = createJavacTask(tasks, variantScope);
                        TaskManager.setJavaCompilerTask(javacTask, tasks, variantScope);
                        return null;
                    }
                });

        // package the prebuilt native libs into the bundle folder
        final Sync packageJniLibs = project.getTasks().create(
                variantScope.getTaskName("package", "JniLibs"),
                Sync.class);

        // Add dependencies on NDK tasks if NDK plugin is applied.
        if (isNdkTaskNeeded) {
            // Add NDK tasks
            ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_NDK_TASK,
                    new Recorder.Block<Void>() {
                        @Override
                        public Void call() throws Exception {
                            createNdkTasks(variantScope);
                            packageJniLibs.dependsOn(variantData.ndkCompileTask);
                            packageJniLibs.from(variantData.ndkCompileTask.getSoFolder())
                                    .include("**/*.so");
                            return null;
                        }
                    });
        } else {
            if (variantData.compileTask != null) {
                variantData.compileTask.dependsOn(getNdkBuildable(variantData));
            } else {
                variantScope.getCompileTask().dependsOn(tasks, getNdkBuildable(variantData));
            }
        }

        Sync packageRenderscript = ThreadRecorder.get().record(
                ExecutionType.LIB_TASK_MANAGER_CREATE_PACKAGING_TASK,
                new Recorder.Block<Sync>() {
                    @Override
                    public Sync call() throws Exception {
                        // package from 2 sources.
                        packageJniLibs.from(variantConfig.getJniLibsList())
                                .include("**/*.so");
                        packageJniLibs.into(new File(
                                variantScope.getGlobalScope().getIntermediatesDir(),
                                DIR_BUNDLES + "/" + dirName + "/jni"));

                        // package the renderscript header files files into the bundle folder
                        Sync packageRenderscript = project.getTasks().create(
                                variantScope.getTaskName("package", "Renderscript"), Sync.class);
                        // package from 3 sources. the order is important to make sure the override works well.
                        packageRenderscript.from(variantConfig.getRenderscriptSourceList())
                                .include("**/*.rsh");
                        packageRenderscript.into(new File(
                                variantScope.getGlobalScope().getIntermediatesDir(),
                                DIR_BUNDLES + "/" + dirName + "/" + SdkConstants.FD_RENDERSCRIPT));
                        return packageRenderscript;
                    }
                });

        // merge consumer proguard files from different build types and flavors
        MergeFileTask mergeProGuardFileTask = ThreadRecorder.get().record(
                ExecutionType.LIB_TASK_MANAGER_CREATE_MERGE_PROGUARD_FILE_TASK,
                new Recorder.Block<MergeFileTask>() {
                    @Override
                    public MergeFileTask call() throws Exception {
                        MergeFileTask mergeProGuardFileTask = project.getTasks().create(
                                variantScope.getTaskName("merge", "ProguardFiles"),
                                MergeFileTask.class);
                        mergeProGuardFileTask.setVariantName(variantConfig.getFullName());
                        mergeProGuardFileTask.setInputFiles(
                                project.files(variantConfig.getConsumerProguardFiles())
                                        .getFiles());
                        mergeProGuardFileTask.setOutputFile(new File(
                                variantScope.getGlobalScope().getIntermediatesDir(),
                                DIR_BUNDLES + "/" + dirName + "/" + LibraryBundle.FN_PROGUARD_TXT));
                        return mergeProGuardFileTask;
                    }

                });

        // copy lint.jar into the bundle folder
        Copy lintCopy = project.getTasks().create(
                variantScope.getTaskName("copy", "Lint"), Copy.class);
        lintCopy.dependsOn(LINT_COMPILE);
        lintCopy.from(new File(
                variantScope.getGlobalScope().getIntermediatesDir(),
                "lint/lint.jar"));
        lintCopy.into(new File(
                variantScope.getGlobalScope().getIntermediatesDir(),
                DIR_BUNDLES + "/" + dirName));

        final Zip bundle = project.getTasks().create(variantScope.getTaskName("bundle"), Zip.class);

        if (variantData.getVariantDependency().isAnnotationsPresent()) {
            libVariantData.generateAnnotationsTask =
                    createExtractAnnotations(project, variantData);
        }
        if (libVariantData.generateAnnotationsTask != null) {
            bundle.dependsOn(libVariantData.generateAnnotationsTask);
        }

        final boolean instrumented = variantConfig.getBuildType().isTestCoverageEnabled();

        // data holding dependencies and input for the dex. This gets updated as new
        // post-compilation steps are inserted between the compilation and dx.
        final PostCompilationData pcDataTemp = new PostCompilationData();

        final PostCompilationData pcData = ThreadRecorder.get().record(
                ExecutionType.LIB_TASK_MANAGER_CREATE_POST_COMPILATION_TASK,
                new Recorder.Block<PostCompilationData>() {
                    @Override
                    public PostCompilationData call() throws Exception {
                        pcDataTemp.setClassGeneratingTasks(Collections.singletonList(
                                variantScope.getJavacTask().getName()));
                        pcDataTemp.setLibraryGeneratingTasks(Collections.singletonList(
                                variantData.getVariantDependency().getPackageConfiguration()
                                        .getBuildDependencies()));
                        pcDataTemp.setInputFilesCallable(new Callable<List<File>>() {
                            @Override
                            public List<File> call() throws Exception {
                                return new ArrayList<File>(
                                        variantData.javacTask.getOutputs().getFiles().getFiles());
                            }

                        });
                        pcDataTemp.setInputDir(variantScope.getJavaOutputDir());
                        pcDataTemp.setInputLibraries(Collections.<File>emptyList());

                        // if needed, instrument the code
                        if (instrumented) {
                            return createJacocoTask(tasks, variantScope, pcDataTemp);
                        }
                        return pcDataTemp;
                    }
                });
        checkState(pcData != null);

        if (buildType.isMinifyEnabled()) {
            // run proguard on output of compile task
            ThreadRecorder.get().record(
                    ExecutionType.LIB_TASK_MANAGER_CREATE_PROGUARD_TASK,
                    new Recorder.Block<Void>() {
                        @Override
                        public Void call() throws Exception {
                            File outFile = maybeCreateProguardTasks(tasks, variantScope,
                                    pcData);
                            checkNotNull(outFile);
                            pcData.setInputFiles(Collections.singletonList(outFile));
                            pcData.setInputDirCallable(null);
                            pcData.setInputLibraries(Collections.<File>emptyList());
                            return null;
                        }
                    });
        } else {
            // package the local jar in libs/
            ThreadRecorder.get().record(
                    ExecutionType.LIB_TASK_MANAGER_CREATE_PACKAGE_LOCAL_JAR,
                    new Recorder.Block<Void>() {
                        @Override
                        public Void call() throws Exception {
                            Sync packageLocalJar = project.getTasks().create(
                                    variantScope.getTaskName("package", "LocalJar"), Sync.class);
                            packageLocalJar.from(
                                    DependencyManager
                                            .getPackagedLocalJarFileList(
                                                    variantData.getVariantDependency())
                                            .toArray());
                            packageLocalJar.into(new File(
                                    variantScope.getGlobalScope().getIntermediatesDir(),
                                    DIR_BUNDLES + "/" + dirName + "/" + LIBS_FOLDER));

                            // add the input libraries. This is only going to be the agent jar if applicable
                            // due to how inputLibraries is initialized.
                            // TODO: clean this.
                            packageLocalJar.from(pcData.getInputLibrariesCallable());
                            TaskManager.optionalDependsOn(
                                    packageLocalJar,
                                    pcData.getLibraryGeneratingTasks());
                            pcData.setLibraryGeneratingTasks(
                                    Collections.singletonList(packageLocalJar));

                            // jar the classes.
                            Jar jar = project.getTasks().create(
                                    variantScope.getTaskName("package", "Jar"), Jar.class);
                            jar.dependsOn(variantScope.getMergeJavaResourcesTask().getName());

                            // add the class files (whether they are instrumented or not.
                            jar.from(pcData.getInputDirCallable());
                            TaskManager.optionalDependsOn(jar, pcData.getClassGeneratingTasks());
                            pcData.setClassGeneratingTasks(Collections.singletonList(jar));

                            jar.from(variantScope.getJavaResourcesDestinationDir());

                            jar.setDestinationDir(new File(
                                    variantScope.getGlobalScope().getIntermediatesDir(),
                                    DIR_BUNDLES + "/" + dirName));
                            jar.setArchiveName("classes.jar");

                            String packageName = variantConfig.getPackageFromManifest();
                            if (packageName == null) {
                                throw new BuildException("Failed to read manifest", null);
                            }

                            packageName = packageName.replace(".", "/");

                            jar.exclude(packageName + "/R.class");
                            jar.exclude(packageName + "/R$*.class");
                            if (!getExtension().getPackageBuildConfig()) {
                                jar.exclude(packageName + "/Manifest.class");
                                jar.exclude(packageName + "/Manifest$*.class");
                                jar.exclude(packageName + "/BuildConfig.class");
                            }

                            if (libVariantData.generateAnnotationsTask != null) {
                                // In case extract annotations strips out private typedef annotation classes
                                jar.dependsOn(libVariantData.generateAnnotationsTask);
                            }
                            return null;
                        }
                    });
        }

        bundle.dependsOn(packageRes.getName(), packageRenderscript, lintCopy, packageJniLibs,
                mergeProGuardFileTask);
        TaskManager.optionalDependsOn(bundle, pcData.getClassGeneratingTasks());
        TaskManager.optionalDependsOn(bundle, pcData.getLibraryGeneratingTasks());

        bundle.setDescription("Assembles a bundle containing the library in " +
                variantConfig.getFullName() + ".");
        bundle.setDestinationDir(new File(variantScope.getGlobalScope().getOutputsDir(), "aar"));
        bundle.setArchiveName(project.getName() + "-" + variantConfig.getBaseName() + "."
                + BuilderConstants.EXT_LIB_ARCHIVE);
        bundle.setExtension(BuilderConstants.EXT_LIB_ARCHIVE);
        bundle.from(new File(
                variantScope.getGlobalScope().getIntermediatesDir(),
                DIR_BUNDLES + "/" + dirName));
        bundle.from(new File(
                variantScope.getGlobalScope().getIntermediatesDir(),
                ANNOTATIONS + "/" + dirName));

        // get the single output for now, though that may always be the case for a library.
        LibVariantOutputData variantOutputData = libVariantData.getOutputs().get(0);
        variantOutputData.packageLibTask = bundle;

        variantData.assembleVariantTask.dependsOn(bundle);
        variantOutputData.assembleTask = variantData.assembleVariantTask;

        if (getExtension().getDefaultPublishConfig().equals(variantConfig.getFullName())) {
            VariantHelper.setupDefaultConfig(project,
                    variantData.getVariantDependency().getPackageConfiguration());

            // add the artifact that will be published
            project.getArtifacts().add("default", bundle);

            getAssembleDefault().dependsOn(variantData.assembleVariantTask);
        }

        // also publish the artifact with its full config name
        if (getExtension().getPublishNonDefault()) {
            project.getArtifacts().add(
                    variantData.getVariantDependency().getPublishConfiguration().getName(), bundle);
            bundle.setClassifier(
                    variantData.getVariantDependency().getPublishConfiguration().getName());
        }

        // configure the variant to be testable.
        variantConfig.setOutput(new LibraryBundle(
                bundle.getArchivePath(),
                new File(variantScope.getGlobalScope().getIntermediatesDir(),
                        DIR_BUNDLES + "/" + dirName),
                variantData.getName(),
                project.getPath()) {
            @Override
            @Nullable
            public String getProjectVariant() {
                return variantData.getName();
            }

            @NonNull
            @Override
            public List<LibraryDependency> getDependencies() {
                return variantConfig.getDirectLibraries();
            }

            @NonNull
            @Override
            public List<? extends AndroidLibrary> getLibraryDependencies() {
                return variantConfig.getDirectLibraries();
            }

            @NonNull
            @Override
            public List<? extends ManifestDependency> getManifestDependencies() {
                return variantConfig.getDirectLibraries();
            }

            @Override
            @Nullable
            public MavenCoordinates getRequestedCoordinates() {
                return null;
            }

            @Override
            @Nullable
            public MavenCoordinates getResolvedCoordinates() {
                return null;
            }

            @Override
            @NonNull
            protected File getJarsRootFolder() {
                return getFolder();
            }

            @Override
            public boolean isOptional() {
                return false;
            }

        });

        ThreadRecorder.get().record(ExecutionType.LIB_TASK_MANAGER_CREATE_LINT_TASK,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createLintTasks(tasks, variantScope);
                        return null;
                    }
                });
    }

    public ExtractAnnotations createExtractAnnotations(
            final Project project,
            final BaseVariantData variantData) {
        final GradleVariantConfiguration config = variantData.getVariantConfiguration();

        final ExtractAnnotations task = project.getTasks().create(
                variantData.getScope().getTaskName("extract", "Annotations"),
                ExtractAnnotations.class);
        task.setDescription(
                "Extracts Android annotations for the " + variantData.getVariantConfiguration()
                        .getFullName()
                        + " variant into the archive file");
        task.setGroup(BasePlugin.BUILD_GROUP);
        task.variant = variantData;
        task.setDestinationDir(new File(
                variantData.getScope().getGlobalScope().getIntermediatesDir(),
                ANNOTATIONS + "/" + config.getDirName()));
        task.output = new File(task.getDestinationDir(), FN_ANNOTATIONS_ZIP);
        task.classDir = new File(variantData.getScope().getGlobalScope().getIntermediatesDir(),
                "classes/" + variantData.getVariantConfiguration().getDirName());
        task.setSource(variantData.getJavaSources());
        task.encoding = getExtension().getCompileOptions().getEncoding();
        task.setSourceCompatibility(
                getExtension().getCompileOptions().getSourceCompatibility().toString());
        ConventionMappingHelper.map(task, "classpath", new Callable<ConfigurableFileCollection>() {
            @Override
            public ConfigurableFileCollection call() throws Exception {
                return project.files(androidBuilder.getCompileClasspath(config));
            }
        });
        task.dependsOn(variantData.getScope().getJavacTask().getName());

        // Setup the boot classpath just before the task actually runs since this will
        // force the sdk to be parsed. (Same as in compileTask)
        task.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                if (task instanceof ExtractAnnotations) {
                    ExtractAnnotations extractAnnotations = (ExtractAnnotations) task;
                    extractAnnotations.bootClasspath = androidBuilder.getBootClasspathAsStrings();
                }
            }
        });

        return task;
    }

    private Task getAssembleDefault() {
        if (assembleDefault == null) {
            assembleDefault = project.getTasks().findByName("assembleDefault");
        }
        return assembleDefault;
    }
}
