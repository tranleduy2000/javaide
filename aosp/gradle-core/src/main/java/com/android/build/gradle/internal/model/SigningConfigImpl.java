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
import com.android.annotations.Nullable;
import com.android.builder.model.SigningConfig;

import java.io.File;
import java.io.Serializable;

/**
 * Implementation of SigningConfig that is serializable. Objects used in the DSL cannot be
 * serialized.
 */
class SigningConfigImpl implements SigningConfig, Serializable {
    private static final long serialVersionUID = 1L;

    @NonNull
    private final String name;
    @Nullable
    private final File storeFile;
    @Nullable
    private final String storePassword;
    @Nullable
    private final String keyAlias;
    @Nullable
    private final String keyPassword;
    @Nullable
    private final String storeType;
    private final boolean signingReady;

    @NonNull
    static SigningConfig createSigningConfig(@NonNull SigningConfig signingConfig) {
        return new SigningConfigImpl(
                signingConfig.getName(),
                signingConfig.getStoreFile(),
                signingConfig.getStorePassword(),
                signingConfig.getKeyAlias(),
                signingConfig.getKeyPassword(),
                signingConfig.getStoreType(),
                signingConfig.isSigningReady());
    }

    private SigningConfigImpl(
            @NonNull  String name,
            @Nullable File storeFile,
            @Nullable String storePassword,
            @Nullable String keyAlias,
            @Nullable String keyPassword,
            @Nullable String storeType,
            boolean signingReady) {
        this.name = name;
        this.storeFile = storeFile;
        this.storePassword = storePassword;
        this.keyAlias = keyAlias;
        this.keyPassword = keyPassword;
        this.storeType = storeType;
        this.signingReady = signingReady;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public File getStoreFile() {
        return storeFile;
    }

    @Nullable
    @Override
    public String getStorePassword() {
        return storePassword;
    }

    @Nullable
    @Override
    public String getKeyAlias() {
        return keyAlias;
    }

    @Nullable
    @Override
    public String getKeyPassword() {
        return keyPassword;
    }

    @Nullable
    @Override
    public String getStoreType() {
        return storeType;
    }

    @Override
    public boolean isSigningReady() {
        return signingReady;
    }
}
