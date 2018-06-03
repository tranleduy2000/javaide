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
import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.build.gradle.internal.PostCompilationData;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.DexOptions;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.utils.FileUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.gradle.api.Action;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@ParallelizableTask
public class PreDex extends BaseTask {

    @Input
    public String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }

    private Collection<File> inputFiles;

    private File outputFolder;

    private com.android.build.gradle.internal.dsl.DexOptions dexOptions;

    private boolean multiDex;

    @TaskAction
    void taskAction(IncrementalTaskInputs taskInputs)
            throws IOException, LoggedErrorException, InterruptedException {

        final boolean multiDexEnabled = isMultiDex();

        final File outFolder = getOutputFolder();

        boolean incremental = taskInputs.isIncremental();
        // if we are not in incremental mode, then outOfDate will contain
        // all the files, but first we need to delete the previous output
        if (!incremental) {
            FileUtils.emptyFolder(outFolder);
        }

        final Set<String> hashs = Sets.newHashSet();
        final WaitableExecutor<Void> executor = new WaitableExecutor<Void>();
        final List<File> inputFileDetails = Lists.newArrayList();

        taskInputs.outOfDate(new Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails change) {
                inputFileDetails.add(change.getFile());
            }
        });

        ProcessOutputHandler outputHandler = new LoggedProcessOutputHandler(getILogger());
        for (final File file : inputFileDetails) {
            Callable<Void> action = new PreDexTask(outFolder, file, hashs,
                    multiDexEnabled, outputHandler);
            executor.execute(action);
        }

        if (incremental) {
            taskInputs.removed(new Action<InputFileDetails>() {
                @Override
                public void execute(InputFileDetails change) {
                    File preDexedFile = getDexFileName(outFolder, change.getFile());

                    try {
                        FileUtils.deleteFolder(preDexedFile);
                    } catch (IOException e) {
                        getLogger().info("Could not delete {}\n{}",
                                preDexedFile, Throwables.getStackTraceAsString(e));
                    }
                }
            });
        }

        executor.waitForTasksWithQuickFail(false);
    }

    private final class PreDexTask implements Callable<Void> {
        private final File outFolder;
        private final File fileToProcess;
        private final Set<String> hashs;
        private final boolean multiDexEnabled;
        private final DexOptions options = getDexOptions();
        private final AndroidBuilder builder = getBuilder();
        private final ProcessOutputHandler mOutputHandler;


        private PreDexTask(
                File outFolder,
                File file,
                Set<String> hashs,
                boolean multiDexEnabled,
                ProcessOutputHandler outputHandler) {
            this.mOutputHandler = outputHandler;
            this.outFolder = outFolder;
            this.fileToProcess = file;
            this.hashs = hashs;
            this.multiDexEnabled = multiDexEnabled;
        }

        @Override
        public Void call() throws Exception {
            // TODO remove once we can properly add a library as a dependency of its test.
            String hash = getFileHash(fileToProcess);

            synchronized (hashs) {
                if (hashs.contains(hash)) {
                    return null;
                }

                hashs.add(hash);
            }

            File preDexedFile = getDexFileName(outFolder, fileToProcess);

            if (multiDexEnabled) {
                preDexedFile.mkdirs();
            }

            builder.preDexLibrary(
                    fileToProcess, preDexedFile, multiDexEnabled, options, mOutputHandler);

            return null;
        }
    }

    // this is used automatically by Gradle, even though nothing
    // in the class uses it.
    @SuppressWarnings("unused")
    @InputFiles
    public Collection<File> getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(Collection<File> inputFiles) {
        this.inputFiles = inputFiles;
    }

    @OutputDirectory
    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Nested
    public com.android.build.gradle.internal.dsl.DexOptions getDexOptions() {
        return dexOptions;
    }

    public void setDexOptions(com.android.build.gradle.internal.dsl.DexOptions dexOptions) {
        this.dexOptions = dexOptions;
    }

    @Input
    public boolean isMultiDex() {
        return multiDex;
    }

    public void setMultiDex(boolean multiDex) {
        this.multiDex = multiDex;
    }

    /**
     * Returns the hash of a file.
     * @param file the file to hash
     */
    private static String getFileHash(@NonNull File file) throws IOException {
        HashCode hashCode = Files.hash(file, Hashing.sha1());
        return hashCode.toString();
    }

    /**
     * Returns a unique File for the pre-dexed library, even
     * if there are 2 libraries with the same file names (but different
     * paths)
     *
     * If multidex is enabled the return File is actually a folder.
     *
     * @param outFolder the output folder.
     * @param inputFile the library.
     */
    @NonNull
    static File getDexFileName(@NonNull File outFolder, @NonNull File inputFile) {
        // get the filename
        String name = inputFile.getName();
        // remove the extension
        int pos = name.lastIndexOf('.');
        if (pos != -1) {
            name = name.substring(0, pos);
        }

        // add a hash of the original file path.
        String input = inputFile.getAbsolutePath();
        HashFunction hashFunction = Hashing.sha1();
        HashCode hashCode = hashFunction.hashString(input, Charsets.UTF_16LE);

        return new File(outFolder, name + "-" + hashCode.toString() + SdkConstants.DOT_JAR);
    }

    public static class ConfigAction implements TaskConfigAction<PreDex> {

        private VariantScope scope;

        private Callable<List<File>> inputLibraries;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            this.inputLibraries = pcData.getInputLibrariesCallable();
        }

        @Override
        public String getName() {
            return scope.getTaskName("preDex");
        }

        @Override
        public Class<PreDex> getType() {
            return PreDex.class;
        }

        @Override
        public void execute(PreDex preDexTask) {
            ApkVariantData variantData = (ApkVariantData) scope.getVariantData();
            VariantConfiguration config = variantData.getVariantConfiguration();

            boolean isTestForApp = config.getType().isForTesting() &&
                    ((TestVariantData) variantData).getTestedVariantData()
                            .getVariantConfiguration().getType() == VariantType.DEFAULT;
            boolean isMultiDexEnabled = config.isMultiDexEnabled() && !isTestForApp;

            variantData.preDexTask = preDexTask;
            preDexTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            preDexTask.setVariantName(config.getFullName());
            preDexTask.dexOptions = scope.getGlobalScope().getExtension().getDexOptions();
            preDexTask.multiDex = isMultiDexEnabled;

            ConventionMappingHelper.map(preDexTask, "inputFiles", inputLibraries);
            ConventionMappingHelper.map(preDexTask, "outputFolder", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return scope.getPreDexOutputDir();
                }
            });
        }
    }
}
