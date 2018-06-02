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

package com.android.build;

import com.android.annotations.NonNull;

import java.io.File;
import java.util.Collection;

/**
 * basic variant output information
 */
public interface VariantOutput {

    /**
     * Returns the main file for this artifact which can be either the
     * {@link OutputFile.OutputType#MAIN} or
     * {@link OutputFile.OutputType#FULL_SPLIT}
     */
    @NonNull
    OutputFile getMainOutputFile();

    /**
     * All the output files for this artifacts, contains the main APK and optionally a list of
     * split APKs.
     */
    @NonNull
    Collection<? extends OutputFile> getOutputs();


    /**
     * Returns the folder containing all the split APK files.
     */
    @NonNull
    File getSplitFolder();

    /**
     * Returns the version code for this output.
     *
     * This is convenient method that returns the final version code whether it's coming
     * from the override set in the output or from the variant's merged flavor.
     *
     * @return the version code.
     */
    int getVersionCode();
}
