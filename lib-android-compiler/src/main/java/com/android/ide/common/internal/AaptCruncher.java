/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.ide.common.process.ProcessInfo;
import com.android.ide.common.process.ProcessInfoBuilder;
import com.android.ide.common.process.ProcessOutputHandler;
import com.android.ide.common.process.ProcessResult;

import java.io.File;

/**
 * Implementation of the PngCruncher using aapt underneath.
 */
public class AaptCruncher implements PngCruncher {

    @NonNull
    private final String mAaptLocation;
    @NonNull
    private final ProcessExecutor mProcessExecutor;
    @NonNull
    private final ProcessOutputHandler mProcessOutputHandler;

    public AaptCruncher(
            @NonNull String aaptLocation,
            @NonNull ProcessExecutor processExecutor,
            @NonNull ProcessOutputHandler processOutputHandler) {
        mAaptLocation = aaptLocation;
        mProcessExecutor = processExecutor;
        mProcessOutputHandler = processOutputHandler;
    }

    @Override
    public int start() {
        return 0;
    }

    /**
     * Runs the aapt crunch command on a single file
     *
     * @param key the request key.
     * @param from the file to crunch
     * @param to the output file
     * @throws PngException
     */
    @Override
    public void crunchPng(int key, @NonNull File from, @NonNull File to) throws PngException {

        try {
            ProcessInfo processInfo = new ProcessInfoBuilder()
                    .setExecutable(mAaptLocation)
                    .addArgs("s",
                            "-i",
                            from.getAbsolutePath(),
                            "-o",
                            to.getAbsolutePath()).createProcess();

            ProcessResult result = mProcessExecutor.execute(processInfo, mProcessOutputHandler);

            result.rethrowFailure().assertNormalExitValue();
        } catch (ProcessException e) {
            throw new PngException(e);
        }
    }

    @Override
    public void end(int key) throws InterruptedException {
        // nothing to do, it's all synchronous.
    }
}
