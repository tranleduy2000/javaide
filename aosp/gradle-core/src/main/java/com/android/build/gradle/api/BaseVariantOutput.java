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

package com.android.build.gradle.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.VariantOutput;
import com.android.build.gradle.tasks.ManifestProcessorTask;
import com.android.build.gradle.tasks.ProcessAndroidResources;

import org.gradle.api.Task;

import java.io.File;

/**
 * A Build variant output and all its public data. This is the base class for items common to apps,
 * test apps, and libraries
 */
public interface BaseVariantOutput extends VariantOutput {

    /**
     * Returns the output file for this build variants. Depending on the configuration, this could
     * be an apk (regular and test project) or a bundled library (library project).
     *
     * If it's an apk, it could be signed, or not; zip-aligned, or not.
     */
    @NonNull
    File getOutputFile();

    void setOutputFile(@NonNull File outputFile);

    /**
     * Returns the Android Resources processing task.
     */
    @NonNull
    ProcessAndroidResources getProcessResources();

    /**
     * Returns the Manifest processing task.
     */
    @NonNull
    ManifestProcessorTask getProcessManifest();

    /**
     * Returns the assemble task for this particular output
     */
    @Nullable
    Task getAssemble();

    /**
     * Returns the name of the variant. Guaranteed to be unique.
     */
    @NonNull
    String getName();

    /**
     * Returns the base name for the output of the variant. Guaranteed to be unique.
     */
    @NonNull
    String getBaseName();

    /**
     * Returns a subfolder name for the variant output. Guaranteed to be unique.
     *
     * This is usually a mix of build type and flavor(s) (if applicable).
     * For instance this could be:
     * "debug"
     * "debug/myflavor"
     * "release/Flavor1Flavor2"
     */
    @NonNull
    String getDirName();

}
