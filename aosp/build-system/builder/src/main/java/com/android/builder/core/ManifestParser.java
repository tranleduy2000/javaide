/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.builder.core;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;

/**
 * A Manifest parser
 */
interface ManifestParser {

    /**
     * Returns the package name parsed from the given manifest file.
     *
     * @param manifestFile the manifest file to parse
     *
     * @return the package name or null if not found.
     */
    @Nullable
    String getPackage(@NonNull File manifestFile);

    /**
     * Returns the minSdkVersion parsed from the given manifest file.
     * The returned value can be an Integer or a String
     *
     * @param manifestFile the manifest file to parse
     *
     * @return the minSdkVersion or null if not found.
     */
    Object getMinSdkVersion(@NonNull File manifestFile);

    /**
     * Returns the targetSdkVersion parsed from the given manifest file.
     * The returned value can be an Integer or a String
     *
     * @param manifestFile the manifest file to parse
     *
     * @return the targetSdkVersion or null if not found
     */
    Object getTargetSdkVersion(@NonNull File manifestFile);

    /**
     * Returns the version name parsed from the given manifest file.
     *
     * @param manifestFile the manifest file to parse
     *
     * @return the version name or null if not found.
     */
    @Nullable
    String getVersionName(@NonNull File manifestFile);

    /**
     * Returns the version code parsed from the given manifest file.
     *
     * @param manifestFile the manifest file to parse
     *
     * @return the version code or -1 if not found.
     */
    int getVersionCode(@NonNull File manifestFile);

}
