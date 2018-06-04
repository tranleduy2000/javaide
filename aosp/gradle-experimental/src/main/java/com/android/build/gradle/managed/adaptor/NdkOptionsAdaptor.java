/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.build.gradle.managed.adaptor;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.dsl.CoreNdkOptions;
import com.android.build.gradle.managed.NdkOptions;
import com.google.common.base.Joiner;

import java.util.List;
import java.util.Set;

/**
 * An adaptor to convert a NdkConfig to NdkConfig.
 */
public class NdkOptionsAdaptor implements CoreNdkOptions {

    NdkOptions ndkOptions;

    public NdkOptionsAdaptor(@NonNull NdkOptions ndkOptions) {
        this.ndkOptions = ndkOptions;
    }

    @Nullable
    @Override
    public String getModuleName() {
        return ndkOptions.getModuleName();
    }

    @Nullable
    @Override
    public String getcFlags() {
        return Joiner.on(' ').join(ndkOptions.getCFlags());
    }

    @Nullable
    @Override
    public List<String> getLdLibs() {
        return ndkOptions.getLdLibs();
    }

    @Nullable
    @Override
    public Set<String> getAbiFilters() {
        return ndkOptions.getAbiFilters();
    }

    @Nullable
    @Override
    public String getStl() {
        return ndkOptions.getStl();
    }

    @Nullable
    @Override
    public Integer getJobs() {
        return null;
    }
}
