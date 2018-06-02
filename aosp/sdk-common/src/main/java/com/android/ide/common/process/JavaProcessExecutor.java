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

package com.android.ide.common.process;

import com.android.annotations.NonNull;

/**
 * An executor for external Java-based processes.
 */
public interface JavaProcessExecutor {

    /**
     * Executes an external process as specified by the ProcessInfo.
     *
     * The process always returns, even when the execution failed. The various possible outcomes
     * of the execution can be queried through the ProcessResult instance.
     *
     * @param javaProcessInfo   the specification of what to run.
     * @param processOutputHandler the output handler
     * @return the ProcessResult
     */
    @NonNull
    ProcessResult execute(
            @NonNull JavaProcessInfo javaProcessInfo,
            @NonNull ProcessOutputHandler processOutputHandler);
}
