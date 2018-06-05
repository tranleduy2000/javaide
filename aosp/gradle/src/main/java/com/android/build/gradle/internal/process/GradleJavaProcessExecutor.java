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

package com.android.build.gradle.internal.process;

import com.android.annotations.NonNull;
import com.android.ide.common.process.JavaProcessExecutor;
import com.android.ide.common.process.JavaProcessInfo;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessOutput;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.process.ProcessResult;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;
import org.gradle.process.JavaExecSpec;

import java.io.File;

/**
 * Implementation of JavaProcessExecutor that uses Gradle's mechanism to execute external java
 * processes.
 */
public class GradleJavaProcessExecutor implements JavaProcessExecutor {

    @NonNull
    private final Project project;

    public GradleJavaProcessExecutor(@NonNull Project project) {
        this.project = project;
    }

    @NonNull
    @Override
    public ProcessResult execute(
            @NonNull JavaProcessInfo javaProcessInfo,
            @NonNull ProcessOutputHandler processOutputHandler) {
        ProcessOutput output = processOutputHandler.createOutput();

        ExecResult result = project.javaexec(new ExecAction(javaProcessInfo, output));

        try {
            processOutputHandler.handleOutput(output);
        } catch (ProcessException e) {
            return new OutputHandlerFailedGradleProcessResult(e);
        }

        return new GradleProcessResult(result);
    }

    private static class ExecAction implements Action<JavaExecSpec> {

        @NonNull
        private final JavaProcessInfo javaProcessInfo;

        @NonNull
        private final ProcessOutput processOutput;

        private ExecAction(@NonNull JavaProcessInfo javaProcessInfo,
                @NonNull ProcessOutput processOutput) {
            this.javaProcessInfo = javaProcessInfo;
            this.processOutput = processOutput;
        }

        @Override
        public void execute(JavaExecSpec javaExecSpec) {
            javaExecSpec.classpath(new File(javaProcessInfo.getClasspath()));
            javaExecSpec.setMain(javaProcessInfo.getMainClass());
            javaExecSpec.args(javaProcessInfo.getArgs());
            javaExecSpec.jvmArgs(javaProcessInfo.getJvmArgs());
            javaExecSpec.environment(javaProcessInfo.getEnvironment());
            javaExecSpec.setStandardOutput(processOutput.getStandardOutput());
            javaExecSpec.setErrorOutput(processOutput.getErrorOutput());

            // we want the caller to be able to do its own thing.
            javaExecSpec.setIgnoreExitValue(true);
        }
    }
}
