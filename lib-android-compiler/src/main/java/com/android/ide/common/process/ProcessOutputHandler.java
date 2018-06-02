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
 * Handler for the Process output.
 */
public interface ProcessOutputHandler {

    /**
     * Creates a ProcessOutput to be used by the process executor.
     */
    @NonNull
    ProcessOutput createOutput();

    /**
     * Handles the output after the process has run. This is called by the process executor
     * before {@link ProcessExecutor#execute(ProcessInfo, ProcessOutputHandler)} returns.
     * @param processOutput the process output to handle
     * @throws ProcessException
     */
    void handleOutput(@NonNull ProcessOutput processOutput) throws ProcessException;
}
