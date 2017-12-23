/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.ide.common.internal;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.utils.GrabProcessOutput;
import com.android.utils.ILogger;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CommandLineRunner {

    private final ILogger mLogger;

    public abstract static class CommandLineOutput implements GrabProcessOutput.IProcessOutput {
        private final List<String> mErrors = Lists.newArrayList();

        @Override
        public void err(@Nullable String line) {
            if (line != null) {
                mErrors.add(line);
            }
        }

        @NonNull
        public final List<String> getErrors() {
            return mErrors;
        }
    }

    private class OutputGrabber extends CommandLineOutput {
        @Override
        public void out(@Nullable String line) {
            if (line != null) {
                mLogger.info(line);
            }
        }

        @Override
        public void err(@Nullable String line) {
            super.err(line);
            if (line != null) {
                mLogger.error(null /*throwable*/, line);
            }
        }
    }

    public CommandLineRunner(ILogger logger) {
        mLogger = logger;
    }

    public void runCmdLine(
            @NonNull List<String> command,
            @Nullable Map<String, String> envVariableMap)
            throws IOException, InterruptedException, LoggedErrorException {
        String[] cmdArray = command.toArray(new String[command.size()]);
        runCmdLine(cmdArray, envVariableMap);
    }

    public void runCmdLine(
            @NonNull List<String> command,
            @NonNull CommandLineOutput commandLineOutput,
            @Nullable Map<String, String> envVariableMap)
            throws IOException, InterruptedException, LoggedErrorException {
        String[] cmdArray = command.toArray(new String[command.size()]);
        runCmdLine(cmdArray, commandLineOutput, envVariableMap);
    }

    public void runCmdLine(
            @NonNull String[] command,
            @Nullable Map<String, String> envVariableMap)
            throws IOException, InterruptedException, LoggedErrorException {

        // create a default CommandLineOutput
        OutputGrabber grabber = new OutputGrabber();

        runCmdLine(command, grabber, envVariableMap);
    }

    public void runCmdLine(
            @NonNull String[] command,
            @NonNull CommandLineOutput commandLineOutput,
            @Nullable Map<String, String> envVariableMap)
            throws IOException, InterruptedException, LoggedErrorException {
        printCommand(command);

        // launch the command line process
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        if (envVariableMap != null) {
            Map<String, String> env = processBuilder.environment();
            for (Map.Entry<String, String> entry : envVariableMap.entrySet()) {
                env.put(entry.getKey(), entry.getValue());
            }
        }

        Process process = processBuilder.start();

        int returnCode = GrabProcessOutput.grabProcessOutput(
                process,
                GrabProcessOutput.Wait.WAIT_FOR_READERS, // we really want to make sure we get all the output!
                commandLineOutput);

        if (returnCode != 0) {
            throw new LoggedErrorException(
                    returnCode,
                    commandLineOutput.getErrors(),
                    Joiner.on(' ').join(command));
        }
    }

    private void printCommand(String[] command) {
        mLogger.info("command: " + Joiner.on(' ').join(command));
    }
}
