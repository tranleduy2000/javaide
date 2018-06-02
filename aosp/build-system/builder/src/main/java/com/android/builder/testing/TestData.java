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

package com.android.builder.testing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.ApiVersion;
import com.android.builder.testing.api.DeviceConfigProvider;
import com.android.ide.common.process.ProcessException;
import com.android.ide.common.process.ProcessExecutor;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Data representing the test app and the tested application/library.
 */
public interface TestData {

    /**
     * Returns the application id.
     *
     * @return the id
     */
    @NonNull
    String getApplicationId();

    /**
     * Returns the tested application id. This can be empty if the test package is self-contained.
     *
     * @return the id or null.
     */
    @Nullable
    String getTestedApplicationId();

    @NonNull
    String getInstrumentationRunner();

    @NonNull
    Map<String, String> getInstrumentationRunnerArguments();

    /**
     * Returns whether the tested app is enabled for code coverage
     */
    boolean isTestCoverageEnabled();

    /**
     * The min SDK version of the app
     */
    @NonNull
    ApiVersion getMinSdkVersion();

    boolean isLibrary();

    /**
     * Returns an APK file to install based on given density and abis.
     * @param processExecutor an executor for slave processes.
     * @param splitSelectExe path to the split-select native tool.
     * @param deviceConfigProvider provider for the test device characteristics.
     * @return the file to install or null if non is compatible.
     */
    @NonNull
    ImmutableList<File> getTestedApks(
            @NonNull ProcessExecutor processExecutor,
            @Nullable File splitSelectExe,
            @NonNull DeviceConfigProvider deviceConfigProvider,
            ILogger logger) throws ProcessException;
    /**
     * Returns the flavor name being test.
     * @return the tested flavor name.
     */
    @NonNull
    String getFlavorName();

    /**
     * Returns the APK containing the test classes for the application.
     * @return the APK file.
     */
    @NonNull
    File getTestApk();

    /**
     * Returns the list of directories containing test so the build system can check the presence
     * of tests before deploying anything.
     * @return list of folders containing test source files.
     */
    @NonNull
    List<File> getTestDirectories();
}
