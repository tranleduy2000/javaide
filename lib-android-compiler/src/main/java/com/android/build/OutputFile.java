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
 * An output with an associated set of filters.
 */
public interface OutputFile {

    /**
     * An object representing the lack of filter.
     */
    String NO_FILTER = null;

    /**
     * Type of package file, either the main APK or a pure split APK file containing resources for
     * a particular split dimension.
     */
    enum OutputType {
        MAIN, FULL_SPLIT, SPLIT
    }

    /**
     * String representation of the OutputType enum which can be used for remote-able interfaces.
     */
    String MAIN = OutputType.MAIN.name();
    String FULL_SPLIT = OutputType.FULL_SPLIT.name();
    String SPLIT = OutputType.SPLIT.name();

    /**
     * Split dimension type
     */
    enum FilterType {
        DENSITY, ABI, LANGUAGE
    }

    /**
     * String representations of the FilterType enum which can be used for remote-able interfaces.
     */
    String DENSITY = FilterType.DENSITY.name();
    String ABI = FilterType.ABI.name();
    String LANGUAGE = FilterType.LANGUAGE.name();

    /**
     * Returns the output type of the referenced APK.
     */
    @NonNull
    String getOutputType();

    /**
     * Returns the split dimensions the referenced APK was created with. Each collection's value
     * is the string representation of an element of the {@see FilterType} enum.
     */
    @NonNull
    Collection<String> getFilterTypes();

    /**
     * Returns all the split information used to create the APK.
     */
    @NonNull
    Collection<FilterData> getFilters();

    /**
     * Returns the output file for this artifact's output.
     * Depending on whether the project is an app or a library project, this could be an apk or
     * an aar file. If this {@link OutputFile} has filters, this is a split
     * APK.
     *
     * For test artifact for a library project, this would also be an apk.
     *
     * @return the output file.
     */
    @NonNull
    File getOutputFile();
}
