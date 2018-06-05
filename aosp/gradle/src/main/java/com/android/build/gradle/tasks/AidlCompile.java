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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.concurrency.GuardedBy;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.IncrementalTask;
import com.android.builder.compiling.DependencyFileProcessor;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.internal.incremental.DependencyData;
import com.android.builder.internal.incremental.DependencyDataStore;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.res2.FileStatus;
import com.android.utils.FileUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.gradle.api.file.FileTree;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.util.PatternSet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Task to compile aidl files. Supports incremental update.
 */
public class AidlCompile extends IncrementalTask {

    private static final String DEPENDENCY_STORE = "dependency.store";
    private static final PatternSet PATTERN_SET = new PatternSet().include("**/*.aidl");

    // ----- PUBLIC TASK API -----
    private File sourceOutputDir;
    private File aidlParcelableDir;

    // ----- PRIVATE TASK API -----
    @Input
    String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }
    private List<File> sourceDirs;
    private List<File> importDirs;

    @InputFiles
    FileTree getSourceFiles() {
        FileTree src = null;
        List<File> sources = getSourceDirs();
        if (!sources.isEmpty()) {
            src = getProject().files(sources).getAsFileTree().matching(PATTERN_SET);
        }
        return src == null ? getProject().files().getAsFileTree() : src;
    }

    private static class DepFileProcessor implements DependencyFileProcessor {

        @GuardedBy("this")
        List<DependencyData> dependencyDataList = Lists.newArrayList();

        List<DependencyData> getDependencyDataList() {
            return dependencyDataList;
        }

        @Override
        public DependencyData processFile(@NonNull File dependencyFile) throws IOException {
            DependencyData data = DependencyData.parseDependencyFile(dependencyFile);
            if (data != null) {
                synchronized (this) {
                    dependencyDataList.add(data);
                }
            }

            return data;
        }
    }

    @Override
    protected boolean isIncremental() {
        // TODO fix once dep file parsing is resolved.
        return false;
    }

    /**
     * Action methods to compile all the files.
     *
     * The method receives a {@link DependencyFileProcessor} to be used by the
     * {@link com.android.builder.internal.compiler.SourceSearcher.SourceFileProcessor} during
     * the compilation.
     *
     * @param dependencyFileProcessor a DependencyFileProcessor
     */
    private void compileAllFiles(DependencyFileProcessor dependencyFileProcessor)
            throws InterruptedException, ProcessException, LoggedErrorException, IOException {
        getBuilder().compileAllAidlFiles(
                getSourceDirs(),
                getSourceOutputDir(),
                getAidlParcelableDir(),
                getImportDirs(),
                dependencyFileProcessor,
                new LoggedProcessOutputHandler(getILogger()));
    }

    /**
     * Returns the import folders.
     */
    @NonNull
    private List<File> getImportFolders() {
        List<File> fullImportDir = Lists.newArrayList();
        fullImportDir.addAll(getImportDirs());
        fullImportDir.addAll(getSourceDirs());

        return fullImportDir;
    }

    /**
     * Compiles a single file.
     * @param sourceFolder the file to compile.
     * @param file the file to compile.
     * @param importFolders the import folders.
     * @param dependencyFileProcessor a DependencyFileProcessor
     */
    private void compileSingleFile(
            @NonNull File sourceFolder,
            @NonNull File file,
            @Nullable List<File> importFolders,
            @NonNull DependencyFileProcessor dependencyFileProcessor,
            @NonNull ProcessOutputHandler processOutputHandler)
            throws InterruptedException, ProcessException, LoggedErrorException, IOException {
        getBuilder().compileAidlFile(
                sourceFolder,
                file,
                getSourceOutputDir(),
                getAidlParcelableDir(),
                importFolders,
                dependencyFileProcessor,
                processOutputHandler);
    }

    @Override
    protected void doFullTaskAction() throws IOException {
        // this is full run, clean the previous output
        File destinationDir = getSourceOutputDir();
        File parcelableDir = getAidlParcelableDir();
        FileUtils.emptyFolder(destinationDir);
        if (parcelableDir != null) {
            FileUtils.emptyFolder(parcelableDir);
        }


        DepFileProcessor processor = new DepFileProcessor();

        try {
            compileAllFiles(processor);
        } catch (Exception e) {
            throw  new RuntimeException(e);
        }

        List<DependencyData> dataList = processor.getDependencyDataList();

        DependencyDataStore store = new DependencyDataStore();
        store.addData(dataList);

        try {
            store.saveTo(new File(getIncrementalFolder(), DEPENDENCY_STORE));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doIncrementalTaskAction(Map<File, FileStatus> changedInputs) throws IOException {
        File incrementalData = new File(getIncrementalFolder(), DEPENDENCY_STORE);
        DependencyDataStore store = new DependencyDataStore();
        Multimap<String, DependencyData> inputMap;
        try {
            inputMap = store.loadFrom(incrementalData);
        } catch (Exception ignored) {
            incrementalData.delete();
            getProject().getLogger().info(
                    "Failed to read dependency store: full task run!");
            doFullTaskAction();
            return;
        }

        final List<File> importFolders = getImportFolders();
        final DepFileProcessor processor = new DepFileProcessor();
        final ProcessOutputHandler processOutputHandler =
                new LoggedProcessOutputHandler(getILogger());

        // use an executor to parallelize the compilation of multiple files.
        WaitableExecutor<Void> executor = new WaitableExecutor<Void>();

        Map<String,DependencyData> mainFileMap = store.getMainFileMap();

        for (final Map.Entry<File, FileStatus> entry : changedInputs.entrySet()) {
            FileStatus status = entry.getValue();

            switch (status) {
                case NEW:
                    executor.execute(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            File file = entry.getKey();
                            compileSingleFile(getSourceFolder(file), file, importFolders,
                                    processor, processOutputHandler);
                            return null;
                        }
                    });
                    break;
                case CHANGED:
                    Collection<DependencyData> impactedData =
                            inputMap.get(entry.getKey().getAbsolutePath());
                    if (impactedData != null) {
                        for (final DependencyData data: impactedData) {
                            executor.execute(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    File file = new File(data.getMainFile());
                                    compileSingleFile(getSourceFolder(file), file,
                                            importFolders, processor, processOutputHandler);
                                    return null;
                                }
                            });
                        }
                    }
                    break;
                case REMOVED:
                    final DependencyData data2 = mainFileMap.get(entry.getKey().getAbsolutePath());
                    if (data2 != null) {
                        executor.execute(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                cleanUpOutputFrom(data2);
                                return null;
                            }
                        });
                        store.remove(data2);
                    }
                    break;
            }
        }

        try {
            executor.waitForTasksWithQuickFail(true /*cancelRemaining*/);
        } catch (Throwable t) {
            incrementalData.delete();
            throw new RuntimeException(t);
        }

        // get all the update data for the recompiled objects
        store.updateAll(processor.getDependencyDataList());

        try {
            store.saveTo(incrementalData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getSourceFolder(@NonNull File file) {
        File parentDir = file;
        while ((parentDir = parentDir.getParentFile()) != null) {
            for (File folder : getSourceDirs()) {
                if (parentDir.equals(folder)) {
                    return folder;
                }
            }
        }

        throw new IllegalArgumentException(String.format("File '%s' is not in a source dir", file));
    }

    private static void cleanUpOutputFrom(@NonNull DependencyData dependencyData) {
        for (String output : dependencyData.getOutputFiles()) {
            new File(output).delete();
        }
        for (String output : dependencyData.getSecondaryOutputFiles()) {
            new File(output).delete();
        }
    }

    @OutputDirectory
    public File getSourceOutputDir() {
        return sourceOutputDir;
    }

    public void setSourceOutputDir(File sourceOutputDir) {
        this.sourceOutputDir = sourceOutputDir;
    }

    @OutputDirectory @Optional
    public File getAidlParcelableDir() {
        return aidlParcelableDir;
    }

    public void setAidlParcelableDir(File aidlParcelableDir) {
        this.aidlParcelableDir = aidlParcelableDir;
    }

    public List<File> getSourceDirs() {
        return sourceDirs;
    }

    public void setSourceDirs(List<File> sourceDirs) {
        this.sourceDirs = sourceDirs;
    }

    @InputFiles
    public List<File> getImportDirs() {
        return importDirs;
    }

    public void setImportDirs(List<File> importDirs) {
        this.importDirs = importDirs;
    }

    public static class ConfigAction implements TaskConfigAction<AidlCompile> {

        @NonNull
        VariantScope scope;

        public ConfigAction(@NonNull VariantScope scope) {
            this.scope = scope;
        }

        @Override
        @NonNull
        public String getName() {
            return scope.getTaskName("compile", "Aidl");
        }

        @Override
        @NonNull
        public Class<AidlCompile> getType() {
            return AidlCompile.class;
        }

        @Override
        public void execute(AidlCompile compileTask) {
            final VariantConfiguration<?,?,?> variantConfiguration = scope.getVariantConfiguration();

            scope.getVariantData().aidlCompileTask = compileTask;

            compileTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            compileTask.setVariantName(scope.getVariantConfiguration().getFullName());
            compileTask.setIncrementalFolder(scope.getAidlIncrementalDir());

            ConventionMappingHelper.map(compileTask, "sourceDirs", new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    return variantConfiguration.getAidlSourceList();
                }
            });
            ConventionMappingHelper.map(compileTask, "importDirs", new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    return variantConfiguration.getAidlImports();
                }
            });

            ConventionMappingHelper.map(compileTask, "sourceOutputDir", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return scope.getAidlSourceOutputDir();
                }
            });

            if (variantConfiguration.getType() == VariantType.LIBRARY) {
                compileTask.setAidlParcelableDir(scope.getAidlParcelableDir());
            }
        }
    }

}
