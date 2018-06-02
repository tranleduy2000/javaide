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

package com.android.ide.common.process;

import com.android.annotations.NonNull;

/**
 * Implementation of ProcessOutputHandler that allows getting access to the output after
 * the call to {@link ProcessExecutor#execute(ProcessInfo, ProcessOutputHandler)}.
 *
 * This can only be used once since only a single process output is kept for query after
 * the process is run.
 */
public class CachedProcessOutputHandler extends BaseProcessOutputHandler {

    private BaseProcessOutput mProcessOutput = null;

    public CachedProcessOutputHandler() {
    }

    public BaseProcessOutput getProcessOutput() {
        return mProcessOutput;
    }

    @NonNull
    @Override
    public ProcessOutput createOutput() {
        //noinspection VariableNotUsedInsideIf
        if (mProcessOutput != null) {
            throw new IllegalStateException("CachedProcessOutputHandler cannot be reused");
        }

        return mProcessOutput = (BaseProcessOutput) super.createOutput();
    }

    @Override
    public void handleOutput(@NonNull ProcessOutput processOutput) throws ProcessException {
        // do nothing
    }
}
