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
import com.android.annotations.Nullable;
import com.android.builder.model.ApiVersion;
import com.android.sdklib.AndroidVersion;

import java.io.Serializable;

/**
 * Implementation of ApiVersion that is serializable so that it can be used in the
 * model returned by the Gradle plugin.
 **/
public class ApiVersionImpl implements ApiVersion, Serializable {

    private static final long serialVersionUID = 1L;

    private final int mApiLevel;
    @Nullable
    private final String mCodename;

    @Nullable
    public static ApiVersion clone(@Nullable ApiVersion apiVersion) {
        if (apiVersion == null) {
            return null;
        }

        return new ApiVersionImpl(apiVersion);
    }

    public static ApiVersion clone(@NonNull AndroidVersion androidVersion) {
        return new ApiVersionImpl(androidVersion.getApiLevel(), androidVersion.getCodename());
    }

    private ApiVersionImpl(@NonNull ApiVersion apiVersion) {
        this(apiVersion.getApiLevel(), apiVersion.getCodename());
    }

    private ApiVersionImpl(int apiLevel, @Nullable String codename) {
        mApiLevel = apiLevel;
        mCodename = codename;
    }

    @Override
    public int getApiLevel() {
        return mApiLevel;
    }

    @Nullable
    @Override
    public String getCodename() {
        return mCodename;
    }

    @NonNull
    @Override
    public String getApiString() {
        return mCodename != null ? mCodename : Integer.toString(mApiLevel);
    }
}
