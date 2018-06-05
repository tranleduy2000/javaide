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

package com.android.build.gradle.internal.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.model.SigningConfig;

import java.io.File;

/**
 * Read-only version of the SigningConfig wrapping another SigningConfig.
 *
 * In the variant API, it is important that the objects returned by the variants
 * are read-only.
 *
 * However, even though the API is defined to use the base interfaces as return
 * type (which all contain only getters), the dynamics of Groovy makes it easy to
 * actually use the setters of the implementation classes.
 *
 * This wrapper ensures that the returned instance is actually just a strict implementation
 * of the base interface and is read-only.
 */
public class ReadOnlySigningConfig implements SigningConfig {

    @NonNull
    private final SigningConfig signingConfig;

    ReadOnlySigningConfig(@NonNull SigningConfig signingConfig) {
        this.signingConfig = signingConfig;
    }

    @NonNull
    @Override
    public String getName() {
        return signingConfig.getName();
    }

    @Nullable
    @Override
    public File getStoreFile() {
        return signingConfig.getStoreFile();
    }

    @Nullable
    @Override
    public String getStorePassword() {
        return signingConfig.getStorePassword();
    }

    @Nullable
    @Override
    public String getKeyAlias() {
        return signingConfig.getKeyAlias();
    }

    @Nullable
    @Override
    public String getKeyPassword() {
        return signingConfig.getKeyPassword();
    }

    @Nullable
    @Override
    public String getStoreType() {
        return signingConfig.getStoreType();
    }

    @Override
    public boolean isSigningReady() {
        return signingConfig.isSigningReady();
    }
}
