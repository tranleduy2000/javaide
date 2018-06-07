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

package com.android.build.gradle.internal.core;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.model.SigningConfig;
import com.android.builder.model.SourceProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Version of {@link com.android.builder.core.VariantConfiguration} that uses the specific
 * types used in the Gradle plugins.
 */
public class GradleVariantConfiguration extends VariantConfiguration<CoreBuildType, CoreProductFlavor, CoreProductFlavor> {

    /**
     * Creates a {@link GradleVariantConfiguration} for a normal (non-test) variant.
     */
    public GradleVariantConfiguration(
            @NonNull CoreProductFlavor defaultConfig,
            @NonNull SourceProvider defaultSourceProvider,
            @NonNull CoreBuildType buildType,
            @Nullable SourceProvider buildTypeSourceProvider,
            @NonNull VariantType type,
            @Nullable SigningConfig signingConfigOverride) {
        super(defaultConfig, defaultSourceProvider, buildType, buildTypeSourceProvider, type,
                signingConfigOverride);
    }


    @NonNull
    @Override
    public VariantConfiguration addProductFlavor(
            @NonNull CoreProductFlavor productFlavor,
            @NonNull SourceProvider sourceProvider,
            @NonNull String dimensionName) {
        checkNotNull(productFlavor);
        checkNotNull(sourceProvider);
        checkNotNull(dimensionName);
        super.addProductFlavor(productFlavor, sourceProvider, dimensionName);
        return this;
    }

    /**
     * Returns whether the configuration has minification enabled.
     */
    public boolean isMinifyEnabled() {
        VariantType type = getType();
        // if type == test then getTestedConfig always returns non-null
        //noinspection ConstantConditions
        return getBuildType().isMinifyEnabled();
    }

}
