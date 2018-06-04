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
import com.android.build.gradle.managed.ApiVersion;

/**
 * An adaptor to convert a managed.ApiVersion to an model.ApiVersion.
 */
public class ApiVersionAdaptor implements com.android.builder.model.ApiVersion {

    private final ApiVersion apiVersion;

    public static boolean isEmpty(ApiVersion apiVersion) {
        return apiVersion.getApiLevel() == null &&
                apiVersion.getCodename() == null;
    }

    public ApiVersionAdaptor(ApiVersion apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public int getApiLevel() {
        return apiVersion.getApiLevel() == null ? 0 : apiVersion.getApiLevel();
    }

    @Nullable
    @Override
    public String getCodename() {
        return apiVersion.getCodename();
    }

    @NonNull
    @Override
    public String getApiString() {
        return getCodename() != null ? getCodename() : Integer.toString(getApiLevel());
    }
}
