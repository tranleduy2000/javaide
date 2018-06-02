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

package com.android.builder.sdk;

import com.android.annotations.NonNull;
import com.android.sdklib.repository.FullRevision;
import com.android.utils.ILogger;
import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * A loader for the SDK. It's able to provide general SDK information
 * ({@link #getSdkInfo(com.android.utils.ILogger)}, or {@link #getRepositories()}), or
 * target-specific information
 * ({@link #getTargetInfo(String, com.android.sdklib.repository.FullRevision, com.android.utils.ILogger)}).
 */
public interface SdkLoader {

    /**
     * Returns information about a build target.
     *
     * This requires loading/parsing the SDK.
     *
     * @param targetHash the compilation target hash string.
     * @param buildToolRevision the build tools revision.
     * @param logger a logger to output messages.
     * @return the target info.
     */
    @NonNull
    TargetInfo getTargetInfo(
            @NonNull String targetHash,
            @NonNull FullRevision buildToolRevision,
            @NonNull ILogger logger);

    /**
     * Returns generic SDK information.
     *
     * This requires loading/parsing the SDK.
     *
     * @param logger a logger to output messages.
     * @return the sdk info.
     */
    @NonNull
    SdkInfo getSdkInfo(@NonNull ILogger logger);

    /**
     * Returns the location of artifact repositories built-in the SDK.
     * @return a non null list of repository folders.
     */
    @NonNull
    ImmutableList<File> getRepositories();
}
