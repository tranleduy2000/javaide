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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.internal.process.GradleProcessExecutor;
import com.android.build.gradle.ndk.internal.NdkNamingScheme;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessInfoBuilder;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;

import java.io.File;

/**
 * Task to remove debug symbols from a native library.
 */
public class StripDebugSymbolTask extends DefaultTask {

    private File stripCommand;

    private File inputFile;

    private File outputFile;

    // ----- PUBLIC API -----

    @Input
    public File getStripCommand() {
        return stripCommand;
    }

    public void setStripCommand(File stripCommand) {
        this.stripCommand = stripCommand;
    }

    @Optional
    @InputFile
    public File getInputFile() {
        // If source set is empty, the file debuggable library is not generated.
        return inputFile.exists() ? inputFile : null;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    @OutputFile
    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    // ----- PRIVATE API -----

    @TaskAction
    void taskAction() throws ProcessException {
        if (getInputFile() == null) {
            return;
        }

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        ProcessInfoBuilder builder = new ProcessInfoBuilder();
        builder.setExecutable(stripCommand);
        builder.addArgs("--strip-unneeded");
        builder.addArgs("-o");
        builder.addArgs(outputFile.toString());
        builder.addArgs(inputFile.toString());
        new GradleProcessExecutor(getProject()).execute(
                builder.createProcess(),
                new LoggedProcessOutputHandler(new LoggerWrapper(getLogger())));
    }

    // ----- ConfigAction -----

    public static class ConfigAction implements Action<StripDebugSymbolTask> {
        @NonNull
        private final SharedLibraryBinarySpec binary;
        @NonNull
        private final File buildDir;
        @NonNull
        private final NdkHandler handler;

        public ConfigAction(
                @NonNull SharedLibraryBinarySpec binary,
                @NonNull File buildDir,
                @NonNull NdkHandler handler) {
            this.binary = binary;
            this.buildDir = buildDir;
            this.handler = handler;
        }

        @Override
        public void execute(@NonNull StripDebugSymbolTask task) {
            File debugLib = binary.getSharedLibraryFile();
            task.setInputFile(debugLib);
            task.setOutputFile(new File(
                    buildDir,
                    NdkNamingScheme.getOutputDirectoryName(binary) + "/"
                            + debugLib.getName()));
            task.setStripCommand(handler.getStripCommand(
                    Abi.getByName(binary.getTargetPlatform().getName())));
            task.dependsOn(binary);
        }
    }
}
