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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base config object for Build Type and Product flavor.
 */
public interface BaseConfig {

    @NonNull
    String getName();

    /**
     * Map of Build Config Fields where the key is the field name.
     *
     * @return a non-null map of class fields (possibly empty).
     */
    @NonNull
    Map<String, ClassField> getBuildConfigFields();

    /**
     * Map of generated res values where the key is the res name.
     *
     * @return a non-null map of class fields (possibly empty).
     */
    @NonNull
    Map<String, ClassField> getResValues();

    /**
     * Returns the collection of proguard rule files.
     *
     * <p>These files are only applied to the production code.
     *
     * @return a non-null collection of files.
     * @see #getTestProguardFiles()
     */
    @NonNull
    Collection<File> getProguardFiles();

    /**
     * Returns the collection of proguard rule files for consumers of the library to use.
     *
     * @return a non-null collection of files.
     */
    @NonNull
    Collection<File> getConsumerProguardFiles();

    /**
     * Returns the collection of proguard rule files to use for the test APK.
     *
     * @return a non-null collection of files.
     */
    @NonNull
    Collection<File> getTestProguardFiles();

    /**
     * Returns the map of key value pairs for placeholder substitution in the android manifest file.
     *
     * This map will be used by the manifest merger.
     * @return the map of key value pairs.
     */
    @NonNull
    Map<String, Object> getManifestPlaceholders();

    /**
     * Returns whether multi-dex is enabled.
     *
     * This can be null if the flag is not set, in which case the default value is used.
     */
    @Nullable
    Boolean getMultiDexEnabled();

    @Nullable
    File getMultiDexKeepFile();

    @Nullable
    File getMultiDexKeepProguard();

    /**
     * Returns the optional jarjar rule files, or empty if jarjar should be skipped.
     * If more than one file is provided, the rule files will be merged in order with last one
     * win in case of rule redefinition.
     * Can only be used with Jack toolchain.
     * @return the optional jarjar rule file.
     */
    @NonNull
    List<File> getJarJarRuleFiles();
}
