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
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Represents a resource output from a variant configuration.
 * <p>
 * Depending on split requirements, there can be more than one output from aapt tool and each
 * output file is represented by an instance of this class.
 */
public class ApkOutputFile implements OutputFile, Serializable {

    @NonNull
    private final Collection<FilterData> filters;
    @NonNull
    private final Collection<String> filterTypes;
    @NonNull
    private final OutputFile.OutputType outputType;
    @NonNull
    private final Callable<File> outputFile;

    public ApkOutputFile(
            @NonNull OutputType outputType,
            @NonNull Collection<FilterData> filters,
            @NonNull Callable<File> outputFile) {
        this.outputType = outputType;
        this.outputFile = outputFile;
        this.filters = filters;
        ImmutableList.Builder<String> filterTypes = ImmutableList.builder();
        for (FilterData filter : filters) {
            filterTypes.add(filter.getFilterType());
        }
        this.filterTypes = filterTypes.build();
    }

    @NonNull
    public OutputFile.OutputType getType() {
        return outputType;
    }

    @NonNull
    @Override
    public File getOutputFile() {
        try {
            return outputFile.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * String identifying the splits within all the filters dimension. For instance, for a {@link
     * com.android.build.OutputFile.FilterType#DENSITY}, a split identifier can be "xxhdpi". Each
     * split identifier will be separated with the passed separator
     *
     * @param separatorChar separator for each filter's value.
     * @return the split identifier (bounded by its split type).
     */
    @NonNull
    public String getSplitIdentifiers(char separatorChar) {

        return Joiner.on(separatorChar).join(Iterables.transform(filters, new Function<FilterData, Object>() {
            @Override
            public Object apply(FilterData filterData) {
                return filterData.getIdentifier();
            }
        }));
    }

    @Override
    @NonNull
    public Collection<FilterData> getFilters() {
        return filters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("OutputType", outputType)
                .add("Filters", Joiner.on(',').join(filters, new Function<FilterData, String>() {

                    @Override
                    public String apply(FilterData splitData) {
                        return '[' + splitData.getFilterType()
                                + ':' + splitData.getIdentifier() + ']';
                    }
                }))
                .add("File", getOutputFile().getAbsolutePath())
                .toString();
    }

    @NonNull
    @Override
    public String getOutputType() {
        return outputType.name();
    }

    @NonNull
    @Override
    public Collection<String> getFilterTypes() {
        return filterTypes;
    }

    @Nullable
    public String getFilterByType(FilterType filterType) {
        for (FilterData filter : filters) {
            if (filter.getFilterType().equals(filterType.name())) {
                return filter.getIdentifier();
            }
        }
        return null;
    }

    /**
     * Returns the split identifier (like "hdpi" for a density split) given the split dimension.
     *
     * @param filterType the string representation of {@see SplitType} split dimension used to
     *                   create the APK.
     * @return the split identifier or null if there was not split of that dimension.
     */
    @Nullable
    public String getFilter(String filterType) {
        return getFilterByType(FilterType.valueOf(filterType));
    }
}
