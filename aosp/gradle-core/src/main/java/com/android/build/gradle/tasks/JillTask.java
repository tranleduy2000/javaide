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
import com.android.build.gradle.internal.dsl.DexOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.builder.core.AndroidBuilder;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.internal.WaitableExecutor;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.FileUtils;
import com.google.common.base.Charsets;
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
public class JillTask extends BaseTask {

    private Collection<File> inputLibs;

    private File outputFolder;

    private DexOptions dexOptions;

    @TaskAction
    public void taskAction(IncrementalTaskInputs taskInputs)
            throws LoggedErrorException, InterruptedException, IOException {
        FullRevision revision = getBuilder().getTargetInfo().getBuildTools().getRevision();
        if (revision.compareTo(JackTask.JACK_MIN_REV) < 0) {
            throw new RuntimeException(
                    "Jack requires Build Tools " + JackTask.JACK_MIN_REV.toString()
                            + " or later");
        }

        final File outFolder = getOutputFolder();

        // if we are not in incremental mode, then outOfDate will contain
        // all th files, but first we need to delete the previous output
        if (!taskInputs.isIncremental()) {
            FileUtils.emptyFolder(outFolder);
        }

        final Set<String> hashs = Sets.newHashSet();
        final WaitableExecutor<Void> executor = new WaitableExecutor<Void>();
        final List<File> inputFileDetails = Lists.newArrayList();

        final AndroidBuilder builder = getBuilder();

        taskInputs.outOfDate(new Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails change) {
                inputFileDetails.add(change.getFile());
            }
        });

        for (final File file : inputFileDetails) {
            Callable<Void> action = new JillCallable(this, file, hashs, outFolder, builder);
            executor.execute(action);
        }

        taskInputs.removed(new Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails change) {
                File jackFile = getJackFileName(outFolder, ((InputFileDetails) change).getFile());
                //noinspection ResultOfMethodCallIgnored
                jackFile.delete();
            }
        });

        executor.waitForTasksWithQuickFail(false);
    }

    @Input
    public String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }

    @InputFiles
    public Collection<File> getInputLibs() {
        return inputLibs;
    }

    public void setInputLibs(Collection<File> inputLibs) {
        this.inputLibs = inputLibs;
    }

    @OutputDirectory
    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Nested
    public DexOptions getDexOptions() {
        return dexOptions;
    }

    public void setDexOptions(DexOptions dexOptions) {
        this.dexOptions = dexOptions;
    }

    private final class JillCallable implements Callable<Void> {

        @NonNull
        private final File fileToProcess;

        @NonNull
        private final Set<String> hashs;

        @NonNull
        private final com.android.builder.core.DexOptions options = getDexOptions();

        @NonNull
        private final File outFolder;

        @NonNull
        private final AndroidBuilder builder;

        private JillCallable(JillTask enclosing, @NonNull File file, @NonNull Set<String> hashs,
                @NonNull File outFolder, @NonNull AndroidBuilder builder) {
            this.fileToProcess = file;
            this.hashs = hashs;
            this.outFolder = outFolder;
            this.builder = builder;
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

            //noinspection GroovyAssignabilityCheck
            File jackFile = getJackFileName(outFolder, fileToProcess);
            //noinspection GroovyAssignabilityCheck
            builder.convertLibraryToJack(fileToProcess, jackFile, options,
                    new LoggedProcessOutputHandler(builder.getLogger()));

            return null;
        }

        @NonNull
        public final File getOutFolder() {
            return outFolder;
        }
    }

    /**
     * Returns the hash of a file.
     *
     * @param file the file to hash
     */
    private static String getFileHash(@NonNull File file) throws IOException {
        HashCode hashCode = Files.hash(file, Hashing.sha1());
        return hashCode.toString();
    }

    /**
     * Returns a unique File for the converted library, even if there are 2 libraries with the same
     * file names (but different paths)
     *
     * @param outFolder the output folder.
     * @param inputFile the library
     */
    @NonNull
    public static File getJackFileName(@NonNull File outFolder, @NonNull File inputFile) {
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

    public static class RuntimeTaskConfigAction implements TaskConfigAction<JillTask> {

        private final VariantScope variantScope;

        // TODO: If task can be shared between variants, change to GlobalScope.
        public RuntimeTaskConfigAction(VariantScope scope) {
            this.variantScope = scope;
        }

        @Override
        public String getName() {
            return variantScope.getTaskName("jill", "RuntimeLibraries");
        }

        @Override
        public Class<JillTask> getType() {
            return JillTask.class;
        }

        @Override
        public void execute(JillTask jillTask) {
            final GlobalScope globalScope = variantScope.getGlobalScope();
            final AndroidBuilder androidBuilder = globalScope.getAndroidBuilder();

            jillTask.setAndroidBuilder(androidBuilder);
            jillTask.setVariantName(variantScope.getVariantConfiguration().getFullName());
            jillTask.setDexOptions(globalScope.getExtension().getDexOptions());

            ConventionMappingHelper.map(jillTask, "inputLibs", new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    return androidBuilder.getBootClasspath();
                }
            });

            jillTask.setOutputFolder(variantScope.getJillRuntimeLibrariesDir());
        }
    }

    public static class PackagedConfigAction implements TaskConfigAction<JillTask> {

        private final VariantScope variantScope;

        public PackagedConfigAction(VariantScope scope) {
            this.variantScope = scope;
        }

        @Override
        public String getName() {
            return variantScope.getTaskName("jill", "PackagedLibraries");
        }

        @Override
        public Class<JillTask> getType() {
            return JillTask.class;
        }

        @Override
        public void execute(JillTask jillTask) {
            final GlobalScope globalScope = variantScope.getGlobalScope();
            final AndroidBuilder androidBuilder = globalScope.getAndroidBuilder();

            jillTask.setAndroidBuilder(androidBuilder);
            jillTask.setVariantName(variantScope.getVariantConfiguration().getFullName());
            jillTask.setDexOptions(globalScope.getExtension().getDexOptions());

            ConventionMappingHelper.map(jillTask, "inputLibs", new Callable<Set<File>>() {
                @Override
                public Set<File> call() throws Exception {
                    return androidBuilder.getPackagedJars(variantScope.getVariantConfiguration());
                }
            });

            jillTask.setOutputFolder(variantScope.getJillPackagedLibrariesDir());
        }
    }
}
