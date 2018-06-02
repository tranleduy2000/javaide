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

/**
 * The result of executing an external process.
 */
public interface ProcessResult {

    /**
     * Throws an exception if the process exited with a non-zero exit value.
     * @return this
     * @throws ProcessException if the process exited with a non-zero exit value
     */
    ProcessResult assertNormalExitValue() throws ProcessException;

    /**
     * Returns the exit value of the process.
     */
    int getExitValue();

    /**
     * Re-throws any failure executing this process.
     * @return this
     * @throws ProcessException the execution failure wrapped in a ProcessExecution
     */
    ProcessResult rethrowFailure() throws ProcessException;
}
