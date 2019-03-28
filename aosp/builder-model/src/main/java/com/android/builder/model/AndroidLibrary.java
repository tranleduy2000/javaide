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
import java.util.List;

/**
 * Represents an Android Library dependency, its content and its own dependencies
 */
public interface AndroidLibrary extends Library {

    /**
     * Returns an optional project identifier if the library is output
     * by a module.
     *
     * @return the project identifier
     */
    @Nullable
    String getProject();

    /**
     * Returns an optional configuration name if the library is output by a module
     * that publishes more than one variant.
     */
    @Nullable
    String getProjectVariant();

    /**
     * Returns the location of the library aar bundle.
     */
    @NonNull
    File getBundle();

    /**
     * Returns the location of the unzipped bundle folder.
     */
    @NonNull
    File getFolder();

    /**
     * Returns the direct dependency of this dependency. The order is important.
     */
    @NonNull
    List<? extends AndroidLibrary> getLibraryDependencies();

    /**
     * Returns the location of the manifest.
     */
    @NonNull
    File getManifest();

    /**
     * Returns the location of the jar file to use for packaging.
     *
     * @return a File for the jar file. The file may not point to an existing file.
     */
    @NonNull
    File getJarFile();

    /**
     * Returns the list of local Jar files that are included in the dependency.
     *
     * @return a list of File. May be empty but not null.
     */
    @NonNull
    Collection<File> getLocalJars();

    /**
     * Returns the location of the res folder.
     *
     * @return a File for the res folder. The file may not point to an existing folder.
     */
    @NonNull
    File getResFolder();

    /**
     * Returns the location of the assets folder.
     *
     * @return a File for the assets folder. The file may not point to an existing folder.
     */
    @NonNull
    File getAssetsFolder();

    /**
     * Returns the location of the jni libraries folder.
     *
     * @return a File for the folder. The file may not point to an existing folder.
     */
    @NonNull
    File getJniFolder();

    /**
     * Returns the location of the aidl import folder.
     *
     * @return a File for the folder. The file may not point to an existing folder.
     */
    @NonNull
    File getAidlFolder();

    /**
     * Returns the location of the renderscript import folder.
     *
     * @return a File for the folder. The file may not point to an existing folder.
     */
    @NonNull
    File getRenderscriptFolder();

    /**
     * Returns the location of the proguard files.
     *
     * @return a File for the file. The file may not point to an existing file.
     */
    @NonNull
    File getProguardRules();

    /**
     * Returns the location of the lint jar.
     *
     * @return a File for the jar file. The file may not point to an existing file.
     */
    @NonNull
    File getLintJar();

    /**
     * Returns the location of the external annotations zip file (which may not exist)
     *
     * @return a File for the zip file. The file may not point to an existing file.
     */
    @NonNull
    File getExternalAnnotations();

    /**
     * Returns the location of an optional file that lists the only
     * resources that should be considered public.
     *
     * @return a File for the file. The file may not point to an existing file.
     */
    @NonNull
    File getPublicResources();

    /**
     * Returns whether the library is considered optional, meaning that it may or may not
     * be present in the final APK.
     *
     * If the library is optional, then:
     * - if the consumer is a library, it'll get skipped from resource merging and won't show up
     *   in the consumer R.txt
     * - if the consumer is a separate test project, all the resources gets skipped from merging.
     */
    boolean isOptional();
}
