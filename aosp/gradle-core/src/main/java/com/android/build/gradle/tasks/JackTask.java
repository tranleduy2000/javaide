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

package com.android.build.gradle.tasks;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.AbstractAndroidCompile;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.internal.variant.ApplicationVariantData;
import com.android.build.gradle.tasks.factory.AbstractCompilesUtil;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.tasks.Job;
import com.android.builder.tasks.JobContext;
import com.android.builder.tasks.Task;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.sdklib.BuildToolInfo;
import com.android.sdklib.repository.FullRevision;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Jack task.
 */
@ParallelizableTask
public class JackTask extends AbstractAndroidCompile
        implements FileSupplier, BinaryFileProviderTask, JavaResourcesProvider {

    public static final FullRevision JACK_MIN_REV = new FullRevision(21, 1, 0);

    private AndroidBuilder androidBuilder;

    private boolean isVerbose;
    private boolean isDebugLog;

    private Collection<File> packagedLibraries;
    private Collection<File> proguardFiles;
    private Collection<File> jarJarRuleFiles;

    private boolean debug;

    private File tempFolder;
    private File jackFile;
    private File javaResourcesFolder;

    private File mappingFile;

    private boolean multiDexEnabled;

    private int minSdkVersion;

    private String javaMaxHeapSize;

    private File incrementalDir;

    @Override
    @TaskAction
    public void compile() {
        final Job<Void> job = new Job<Void>(getName(), new Task<Void>() {
            @Override
            public void run(@NonNull Job<Void> job, @NonNull JobContext<Void> context)
                    throws IOException {
                try {
                    JackTask.this.doMinification();
                } catch (ProcessException e) {
                    throw new IOException(e);
                }
            }

        });
        try {
            SimpleWorkQueue.push(job);

            // wait for the task completion.
            job.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

    }

    private void doMinification() throws ProcessException, IOException {

        if (System.getenv("USE_JACK_API") != null) {
            androidBuilder.convertByteCodeUsingJackApis(
                    getDestinationDir(),
                    getJackFile(),
                    getClasspath().getFiles(),
                    getPackagedLibraries(),
                    getSource().getFiles(),
                    getProguardFiles(),
                    getMappingFile(),
                    getJarJarRuleFiles(),
                    getIncrementalDir(),
                    getJavaResourcesFolder(),
                    isMultiDexEnabled(),
                    getMinSdkVersion());
        } else {
            // no incremental support through command line so far.
            androidBuilder.convertByteCodeWithJack(
                    getDestinationDir(),
                    getJackFile(),
                    computeBootClasspath(),
                    getPackagedLibraries(),
                    computeEcjOptionFile(),
                    getProguardFiles(),
                    getMappingFile(),
                    getJarJarRuleFiles(),
                    isMultiDexEnabled(),
                    getMinSdkVersion(),
                    isDebugLog,
                    getJavaMaxHeapSize(),
                    new LoggedProcessOutputHandler(androidBuilder.getLogger()));
        }

    }

    private File computeEcjOptionFile() throws IOException {
        File folder = getTempFolder();
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        File file = new File(folder, "ecj-options.txt");

        StringBuilder sb = new StringBuilder();

        for (File sourceFile : getSource().getFiles()) {
            sb.append(sourceFile.getAbsolutePath()).append("\n");
        }

        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();

        Files.write(sb.toString(), file, Charsets.UTF_8);

        return file;
    }

    private String computeBootClasspath() {
        return Joiner.on(':').join(
                Iterables.transform(getClasspath().getFiles(), GET_ABSOLUTE_PATH));
    }

    private static final Function<File, String> GET_ABSOLUTE_PATH = new Function<File, String>() {
        @Override
        public String apply(File file) {
            return file.getAbsolutePath();
        }
    };


    @InputFile
    public File getJackExe() {
        return new File(
                androidBuilder.getTargetInfo().getBuildTools().getPath(BuildToolInfo.PathId.JACK));
    }

    public AndroidBuilder getAndroidBuilder() {
        return androidBuilder;
    }

    public void setAndroidBuilder(AndroidBuilder androidBuilder) {
        this.androidBuilder = androidBuilder;
    }

    public boolean getIsVerbose() {
        return isVerbose;
    }

    public void setIsVerbose(boolean isVerbose) {
        this.isVerbose = isVerbose;
    }

    public boolean getIsDebugLog() {
        return isDebugLog;
    }

    public void setIsDebugLog(boolean isDebugLog) {
        this.isDebugLog = isDebugLog;
    }

    @InputFiles
    public Collection<File> getPackagedLibraries() {
        return packagedLibraries;
    }

    public void setPackagedLibraries(Collection<File> packagedLibraries) {
        this.packagedLibraries = packagedLibraries;
    }

    @InputFiles
    @Optional
    public Collection<File> getProguardFiles() {
        return proguardFiles;
    }

    public void setProguardFiles(Collection<File> proguardFiles) {
        this.proguardFiles = proguardFiles;
    }

    @InputFiles
    @Optional
    public Collection<File> getJarJarRuleFiles() {
        return jarJarRuleFiles;
    }

    public void setJarJarRuleFiles(Collection<File> jarJarRuleFiles) {
        this.jarJarRuleFiles = jarJarRuleFiles;
    }

    @Input
    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public File getTempFolder() {
        return tempFolder;
    }

    public void setTempFolder(File tempFolder) {
        this.tempFolder = tempFolder;
    }

    @OutputFile
    public File getJackFile() {
        return jackFile;
    }

    public void setJackFile(File jackFile) {
        this.jackFile = jackFile;
    }

    @OutputFile
    @Optional
    public File getMappingFile() {
        return mappingFile;
    }

    public void setMappingFile(File mappingFile) {
        this.mappingFile = mappingFile;
    }

    @Input
    public boolean isMultiDexEnabled() {
        return multiDexEnabled;
    }

    public void setMultiDexEnabled(boolean multiDexEnabled) {
        this.multiDexEnabled = multiDexEnabled;
    }

    @Input
    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(int minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    @Input
    @Optional
    public String getJavaMaxHeapSize() {
        return javaMaxHeapSize;
    }

    public void setJavaMaxHeapSize(String javaMaxHeapSize) {
        this.javaMaxHeapSize = javaMaxHeapSize;
    }

    @Input
    @Optional
    public File getIncrementalDir() {
        return incrementalDir;
    }

    public void setIncrementalDir(File incrementalDir) {
        this.incrementalDir = incrementalDir;
    }

    @Input
    @Optional
    public File getJavaResourcesFolder() {
        return javaResourcesFolder;
    }

    public void setJavaResourcesFolder(File javaResourcesFolder) {
        this.javaResourcesFolder = javaResourcesFolder;
    }

    @NonNull
    @Override
    public ImmutableList<JavaResourcesLocation> getJavaResourcesLocations() {
        return ImmutableList.of(
                new JavaResourcesLocation(Type.FOLDER, getDestinationDir()));
    }

    @Override
    @NonNull
    public BinaryFileProviderTask.Artifact getArtifact() {
        return new BinaryFileProviderTask.Artifact(
                BinaryFileProviderTask.BinaryArtifactType.JACK,
                getJackFile());
    }

    // ----- FileSupplierTask ----
    @NonNull
    @Override
    public org.gradle.api.Task getTask() {
        return this;
    }

    @Override
    public File get() {
        return getMappingFile();
    }

    public static class ConfigAction implements TaskConfigAction<JackTask> {

        private final VariantScope scope;
        private final boolean isVerbose;
        private final boolean isDebugLog;

        public ConfigAction(VariantScope scope, boolean isVerbose, boolean isDebugLog) {
            this.scope = scope;
            this.isVerbose = isVerbose;
            this.isDebugLog = isDebugLog;
        }

        @Override
        public String getName() {
            return scope.getTaskName("compile", "JavaWithJack");
        }

        @Override
        public Class<JackTask> getType() {
            return JackTask.class;
        }

        @Override
        public void execute(JackTask jackTask) {
            jackTask.setIsVerbose(isVerbose);
            jackTask.setIsDebugLog(isDebugLog);

            GlobalScope globalScope = scope.getGlobalScope();

            jackTask.androidBuilder = globalScope.getAndroidBuilder();
            jackTask.setJavaMaxHeapSize(
                    globalScope.getExtension().getDexOptions().getJavaMaxHeapSize());

            jackTask.setSource(scope.getVariantData().getJavaSources());

            final GradleVariantConfiguration config = scope.getVariantData().getVariantConfiguration();
            jackTask.setMultiDexEnabled(config.isMultiDexEnabled());
            jackTask.setMinSdkVersion(config.getMinSdkVersion().getApiLevel());
            jackTask.incrementalDir  = scope.getJackIncrementalDir();

            // if the tested variant is an app, add its classpath. For the libraries,
            // it's done automatically since the classpath includes the library output as a normal
            // dependency.
            if (scope.getTestedVariantData() instanceof ApplicationVariantData) {
                ConventionMappingHelper.map(jackTask, "classpath", new Callable<FileCollection>() {
                    @Override
                    public FileCollection call() throws Exception {
                        Project project = scope.getGlobalScope().getProject();
                        return project.fileTree(scope.getJillRuntimeLibrariesDir()).plus(
                                project.fileTree(
                                        scope.getTestedVariantData().getScope()
                                                .getJillRuntimeLibrariesDir())).plus(
                                project.fileTree(
                                        scope.getTestedVariantData().getScope().getJackClassesZip()
                                ));
                    }
                });
            } else {
                ConventionMappingHelper.map(jackTask, "classpath", new Callable<FileCollection>() {
                    @Override
                    public FileCollection call() throws Exception {
                        return scope.getGlobalScope().getProject().fileTree(
                                scope.getJillRuntimeLibrariesDir());
                    }
                });
            }

            ConventionMappingHelper.map(jackTask, "packagedLibraries", new Callable<Collection<File>>() {
                @Override
                public Collection<File> call() throws Exception {
                    return scope.getGlobalScope().getProject()
                            .fileTree(scope.getJillPackagedLibrariesDir()).getFiles();
                }
            });

            jackTask.setDestinationDir(scope.getJackDestinationDir());
            jackTask.setJackFile(scope.getJackClassesZip());
            jackTask.setTempFolder(new File(scope.getGlobalScope().getIntermediatesDir(),
                    "/tmp/jack/" + scope.getVariantConfiguration().getDirName()));

            jackTask.setJavaResourcesFolder(scope.getJavaResourcesDestinationDir());
            scope.setJavaResourcesProvider(jackTask);

            if (config.isMinifyEnabled()) {
                ConventionMappingHelper.map(jackTask, "proguardFiles", new Callable<List<File>>() {
                    @Override
                    public List<File> call() throws Exception {
                        // since all the output use the same resources, we can use the first output
                        // to query for a proguard file.
                        File sdkDir = scope.getGlobalScope().getSdkHandler().getAndCheckSdkFolder();
                        File defaultProguardFile =  new File(sdkDir,
                                SdkConstants.FD_TOOLS + File.separatorChar
                                        + SdkConstants.FD_PROGUARD + File.separatorChar
                                        + TaskManager.DEFAULT_PROGUARD_CONFIG_FILE);

                        List<File> proguardFiles = config.getProguardFiles(true /*includeLibs*/,
                                ImmutableList.of(defaultProguardFile));
                        File proguardResFile = scope.getProcessAndroidResourcesProguardOutputFile();
                        proguardFiles.add(proguardResFile);
                        // for tested app, we only care about their aapt config since the base
                        // configs are the same files anyway.
                        if (scope.getTestedVariantData() != null) {
                            proguardResFile = scope.getTestedVariantData().getScope()
                                    .getProcessAndroidResourcesProguardOutputFile();
                            proguardFiles.add(proguardResFile);
                        }

                        return proguardFiles;
                    }
                });

                jackTask.mappingFile = new File(scope.getProguardOutputFolder(), "mapping.txt");
            }


            ConventionMappingHelper.map(jackTask, "jarJarRuleFiles", new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    List<File> jarJarRuleFiles = Lists.newArrayListWithCapacity(
                            config.getJarJarRuleFiles().size());
                    Project project = scope.getGlobalScope().getProject();
                    for (File file: config.getJarJarRuleFiles()) {
                        jarJarRuleFiles.add(project.file(file));
                    }
                    return jarJarRuleFiles;
                }
            });

            AbstractCompilesUtil.configureLanguageLevel(
                    jackTask,
                    scope.getGlobalScope().getExtension().getCompileOptions(),
                    scope.getGlobalScope().getExtension().getCompileSdkVersion()
            );

            scope.getVariantData().jackTask = jackTask;
            scope.getVariantData().javaCompilerTask = jackTask;
            scope.getVariantData().mappingFileProviderTask = jackTask;
            scope.getVariantData().binayFileProviderTask = jackTask;
        }
    }
}
