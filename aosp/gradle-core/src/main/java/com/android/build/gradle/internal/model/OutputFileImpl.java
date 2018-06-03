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

package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

/**
 * Implementation of the {@link OutputFile} interface for the model.
 */
public class OutputFileImpl implements OutputFile, Serializable {

    private final Collection<FilterData> filters;
    private final Collection<String> filterTypes;
    private final String type;
    private final File outputFile;

    public OutputFileImpl(Collection<FilterData> filters, String type, File file) {
        this.filters = filters;
        this.type = type;
        ImmutableList.Builder<String> filterTypes = ImmutableList.builder();
        for (FilterData filter : filters) {
            filterTypes.add(filter.getFilterType());
        }
        this.filterTypes = filterTypes.build();
        this.outputFile = file;
    }

    @NonNull
    @Override
    public String getOutputType() {
        return type;
    }

    @NonNull
    @Override
    public Collection<String> getFilterTypes() {
        return filterTypes;
    }

    @NonNull
    @Override
    public Collection<FilterData> getFilters() {
        return filters;
    }

    @NonNull
    @Override
    public File getOutputFile() {
        return outputFile;
    }
}
