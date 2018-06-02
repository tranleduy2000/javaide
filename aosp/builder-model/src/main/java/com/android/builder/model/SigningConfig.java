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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;

/**
 * A Signing Configuration
 */
public interface SigningConfig {

    /**
     * Returns the name of the Signing config
     *
     * @return the name of the config
     */
    @NonNull
    String getName();

    /**
     * Returns the keystore file.
     *
     * @return the file.
     */
    @Nullable
    File getStoreFile();

    /**
     * Returns the keystore password.
     *
     * @return the password.
     */
    @Nullable
    String getStorePassword();

    /**
     * Returns the key alias name.
     *
     * @return the key alias name.
     */
    @Nullable
    String getKeyAlias();

    /**
     * return the key password.
     *
     * @return the password.
     */
    @Nullable
    String getKeyPassword();

    /**
     * Returns the store type.
     *
     * @return the store type.
     */
    @Nullable
    String getStoreType();

    /**
     * Returns whether the config is fully configured for signing.
     *
     * @return true if all the required information are present.
     */
    boolean isSigningReady();
}
