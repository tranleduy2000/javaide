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

package com.android.build.gradle.internal.dsl;

import com.android.annotations.Nullable;

import java.io.File;
import com.android.builder.model.SigningConfig;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

/**
 * A SigningConfig with added annotations to be used with @Nested in a Gradle task.
 */
public interface CoreSigningConfig extends SigningConfig {

    @Override
    @Nullable
    @InputFile @Optional
    File getStoreFile();

    @Override
    @Nullable
    @Input
    String getStorePassword();

    @Override
    @Nullable
    @Input
    String getKeyAlias();

    @Override
    @Nullable
    @Input
    String getStoreType();
}
