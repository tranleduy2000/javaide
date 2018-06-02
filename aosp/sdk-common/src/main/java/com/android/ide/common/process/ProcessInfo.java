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

import java.util.List;
import java.util.Map;

/**
 * Information to run an external process.
 */
public interface ProcessInfo {

    /**
     * The executable to run.
     */
    @NonNull
    String getExecutable();

    /**
     * The command line arguments.
     */
    @NonNull
    List<String> getArgs();

    /**
     * The environment variables to set when running the process.
     *
     * The objects in the map are used through their <code>toString()</code> representation.
     */
    @NonNull
    Map<String, Object> getEnvironment();
}
