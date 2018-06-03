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
package com.android.build.gradle.internal.model;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.CompileOptions;
import com.android.builder.model.JavaCompileOptions;

import java.io.Serializable;

/**
 * Implementation of {@link JavaCompileOptions}.
 */
class DefaultJavaCompileOptions implements JavaCompileOptions, Serializable {
    @NonNull
    private final String sourceCompatibility;
    @NonNull
    private final String targetCompatibility;
    @NonNull
    private final String encoding;

    DefaultJavaCompileOptions(@NonNull CompileOptions options) {
      sourceCompatibility = options.getSourceCompatibility().toString();
      targetCompatibility = options.getTargetCompatibility().toString();
      encoding = options.getEncoding();
    }

    @NonNull
    @Override
    public String getSourceCompatibility() {
        return sourceCompatibility;
    }

    @NonNull
    @Override
    public String getTargetCompatibility() {
        return targetCompatibility;
    }

    @NonNull
    @Override
    public String getEncoding() {
        return encoding;
    }
}
