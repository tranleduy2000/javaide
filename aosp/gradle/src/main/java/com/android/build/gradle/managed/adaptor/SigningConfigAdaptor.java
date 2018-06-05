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
import com.android.build.gradle.internal.dsl.CoreSigningConfig;
import com.android.build.gradle.managed.SigningConfig;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;

import java.io.File;

/**
 * An adaptor to convert a managed.SigningConfig to a model.SigningConfig.
 */
public class SigningConfigAdaptor implements CoreSigningConfig {

    @NonNull
    private final SigningConfig signingConfig;

    public SigningConfigAdaptor(@NonNull SigningConfig signingConfig) {
        this.signingConfig = signingConfig;
    }

    @NonNull
    @Override
    public String getName() {
        return signingConfig.getName();
    }

    @Nullable
    @Override
    @InputFile @Optional
    public File getStoreFile() {
        return signingConfig.getStoreFile() == null ? null : new File(signingConfig.getStoreFile());
    }

    @Nullable
    @Override
    @Input
    public String getStorePassword() {
        return signingConfig.getStorePassword();
    }

    @Nullable
    @Override
    @Input
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
    @Input
    public String getStoreType() {
        return signingConfig.getStoreType();
    }

    @Override
    public boolean isSigningReady() {
        return signingConfig.getStoreFile() != null &&
                signingConfig.getStorePassword() != null &&
                signingConfig.getKeyAlias() != null &&
                signingConfig.getKeyPassword() != null;
    }
}
