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
package com.android.build.gradle.tasks

import com.android.SdkConstants
import com.android.annotations.Nullable
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.internal.dsl.DexOptions
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.variant.ApkVariantData
import com.android.build.gradle.internal.variant.TestVariantData
import com.android.ide.common.process.LoggedProcessOutputHandler
import com.android.utils.FileUtils
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import com.android.build.gradle.internal.PostCompilationData
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean

import static com.android.builder.core.VariantType.DEFAULT
import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES

public class Dex extends BaseTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File outputFolder

    @Input @Optional
    List<String> additionalParameters

    boolean enableIncremental = true

    // ----- PRIVATE TASK API -----
    @Input
    String getBuildToolsVersion() {
        getBuildTools().getRevision()
    }

    @InputFiles @Optional
    Collection<File> inputFiles
    @InputDirectory @Optional
    File inputDir

    @InputFiles
    Collection<File> libraries

    @Nested
    DexOptions dexOptions

    @Input
    boolean multiDexEnabled = false

    @Input
    boolean optimize = true

    @InputFile @Optional
    File mainDexListFile

    File tmpFolder

    /**
     * Actual entry point for the action.
     * Calls out to the doTaskAction as needed.
     */
    @TaskAction
    void taskAction(IncrementalTaskInputs inputs) {
        Collection<File> _inputFiles = getInputFiles()
        File _inputDir = getInputDir()
        if (_inputFiles == null && _inputDir == null) {
            throw new RuntimeException("Dex task '${getName()}: inputDir and inputFiles cannot both be null");
        }

        if (!dexOptions.incremental || !enableIncremental) {
            doTaskAction(_inputFiles, _inputDir, false /*incremental*/)
            return
        }

        if (!inputs.isIncremental()) {
            project.logger.info("Unable to do incremental execution: full task run.")
            doTaskAction(_inputFiles, _inputDir, false /*incremental*/)
            return
        }

        AtomicBoolean forceFullRun = new AtomicBoolean()

        //noinspection GroovyAssignabilityCheck
        inputs.outOfDate { change ->
            // force full dx run if existing jar file is modified
            // New jar files are fine.
            if (change.isModified() && change.file.path.endsWith(SdkConstants.DOT_JAR)) {
                project.logger.info("Force full dx run: Found updated ${change.file}")
                forceFullRun.set(true)
            }
        }

        //noinspection GroovyAssignabilityCheck
        inputs.removed { change ->
            // force full dx run if existing jar file is removed
            if (change.file.path.endsWith(SdkConstants.DOT_JAR)) {
                project.logger.info("Force full dx run: Found removed ${change.file}")
                forceFullRun.set(true)
            }
        }

        doTaskAction(_inputFiles, _inputDir, !forceFullRun.get())
    }

    private void doTaskAction(
            @Nullable Collection<File> inputFiles,
            @Nullable File inputDir,
            boolean incremental) {
        File outFolder = getOutputFolder()
        if (!incremental) {
            FileUtils.emptyFolder(outFolder)
        }

        File tmpFolder = getTmpFolder()
        tmpFolder.mkdirs()

        // if some of our .jar input files exist, just reset the inputDir to null
        for (File inputFile : inputFiles) {
            if (inputFile.exists()) {
                inputDir = null;
            }
        }
        if (inputDir != null) {
            inputFiles = project.files(inputDir).files
        }

        getBuilder().convertByteCode(
                inputFiles,
                getLibraries(),
                outFolder,
                getMultiDexEnabled(),
                getMainDexListFile(),
                getDexOptions(),
                getAdditionalParameters(),
                tmpFolder,
                incremental,
                getOptimize(),
                new LoggedProcessOutputHandler(getILogger()))
    }


    public static class ConfigAction implements TaskConfigAction<Dex> {

        private final VariantScope scope;

        private final PostCompilationData pcData;

        public ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope;
            this.pcData = pcData;
        }

        @Override
        public String getName() {
            return scope.getTaskName("dex")
        }

        @Override
        public Class<Dex> getType() {
            return Dex.class;
        }

        @Override
        public void execute(Dex dexTask) {
            ApkVariantData variantData = (ApkVariantData) scope.getVariantData();
            final GradleVariantConfiguration config = variantData.getVariantConfiguration();

            boolean isTestForApp = config.getType().isForTesting() && (DefaultGroovyMethods
                    .asType(variantData, TestVariantData.class)).getTestedVariantData()
                    .getVariantConfiguration().getType().equals(DEFAULT);

            boolean isMultiDexEnabled = config.isMultiDexEnabled() && !isTestForApp;
            boolean isLegacyMultiDexMode = config.isLegacyMultiDexMode();

            variantData.dexTask = dexTask;
            dexTask.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder())
            dexTask.setVariantName(config.getFullName())
            ConventionMappingHelper.map(dexTask, "outputFolder", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return scope.getDexOutputFolder();
                }
            });
            dexTask.setTmpFolder(new File(
                    String.valueOf(scope.getGlobalScope().getBuildDir()) + "/" + FD_INTERMEDIATES
                            + "/tmp/dex/" + config.getDirName()));
            dexTask.setDexOptions(scope.getGlobalScope().getExtension().getDexOptions());
            dexTask.setMultiDexEnabled(isMultiDexEnabled);
            // dx doesn't work with receving --no-optimize in debug so we disable it for now.
            dexTask.setOptimize(true);//!variantData.variantConfiguration.buildType.debuggable

            // inputs
            if (pcData.getInputDirCallable() != null) {
                ConventionMappingHelper.map(dexTask, "inputDir", pcData.getInputDirCallable());
            }
            ConventionMappingHelper.map(dexTask, "inputFiles", pcData.getInputFilesCallable());
            ConventionMappingHelper.map(dexTask, "libraries", pcData.getInputLibrariesCallable());

            if (isMultiDexEnabled && isLegacyMultiDexMode) {
                // configure the dex task to receive the generated class list.
                ConventionMappingHelper.map(dexTask, "mainDexListFile", new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        return scope.getMainDexListFile();
                    }
                });
            }
        }
    }
}
