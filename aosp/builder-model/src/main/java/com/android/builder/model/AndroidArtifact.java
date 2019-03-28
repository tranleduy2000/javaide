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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * An Android Artifact.
 *
 * This is the entry point for the output of a {@link Variant}. This can be more than one
 * output in the case of multi-apk where more than one APKs are generated from the same set
 * of sources.
 *
 */
public interface AndroidArtifact extends BaseArtifact {

    @NonNull
    Collection<AndroidArtifactOutput> getOutputs();

    /**
     * Returns whether the output file is signed. This is always false for the main artifact
     * of a library project.
     *
     * @return true if the app is signed.
     */
    boolean isSigned();

    /**
     * Returns the name of the {@link SigningConfig} used for the signing. If none are setup or
     * if this is the main artifact of a library project, then this is null.
     *
     * @return the name of the setup signing config.
     */
    @Nullable
    String getSigningConfigName();

    /**
     * Returns the application id of this artifact.
     *
     * @return the application id.
     */
    @NonNull
    String getApplicationId();

    /**
     * Returns the name of the task used to generate the source code. The actual value might
     * depend on the build system front end.
     *
     * @return the name of the code generating task.
     */
    @NonNull
    String getSourceGenTaskName();

    /**
     * Returns all the source folders that are generated. This is typically folders for the R,
     * the aidl classes, and the renderscript classes.
     *
     * Deprecated, as of 1.2, present in super interface.
     *
     * @return a list of folders.
     */
    @NonNull
    @Override
    Collection<File> getGeneratedSourceFolders();

    /**
     * Returns all the resource folders that are generated. This is typically the renderscript
     * output and the merged resources.
     *
     * @return a list of folder.
     */
    @NonNull
    Collection<File> getGeneratedResourceFolders();

    /**
     * Returns the ABI filters associated with the artifact, or null if there are no filters.
     *
     * If the list contains values, then the artifact only contains these ABIs and excludes
     * others.
     */
    @Nullable
    Set<String> getAbiFilters();

    /**
     * Returns the native libraries associated with the artifact.
     */
    @Nullable
    Collection<NativeLibrary> getNativeLibraries();

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
}
