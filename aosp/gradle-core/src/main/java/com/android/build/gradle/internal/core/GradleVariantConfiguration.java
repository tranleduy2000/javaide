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

import static com.google.common.base.Preconditions.checkNotNull;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.dsl.CoreBuildType;
import com.android.build.gradle.internal.dsl.CoreNdkOptions;
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.builder.core.VariantConfiguration;
import com.android.builder.core.VariantType;
import com.android.builder.model.SigningConfig;
import com.android.builder.model.SourceProvider;

import java.util.List;
import java.util.Set;

/**
 * Version of {@link com.android.builder.core.VariantConfiguration} that uses the specific
 * types used in the Gradle plugins.
 *
 * It also adds support for Ndk support that is not ready to go in the builder library.
 */
public class GradleVariantConfiguration extends VariantConfiguration<CoreBuildType, CoreProductFlavor, CoreProductFlavor> {

    private final MergedNdkConfig mMergedNdkConfig = new MergedNdkConfig();

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
        computeNdkConfig();
    }

    /**
     * Creates a {@link GradleVariantConfiguration} for a testing variant.
     */
    public GradleVariantConfiguration(
            @Nullable VariantConfiguration testedConfig,
            @NonNull CoreProductFlavor defaultConfig,
            @NonNull SourceProvider defaultSourceProvider,
            @NonNull CoreBuildType buildType,
            @Nullable SourceProvider buildTypeSourceProvider,
            @NonNull VariantType type,
            @Nullable SigningConfig signingConfigOverride) {
        super(defaultConfig, defaultSourceProvider, buildType, buildTypeSourceProvider, type,
                testedConfig, signingConfigOverride);
        computeNdkConfig();
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
        computeNdkConfig();
        return this;
    }

    @NonNull
    public CoreNdkOptions getNdkConfig() {
        return mMergedNdkConfig;
    }

    /**
     * Returns the ABI filters associated with the artifact, or null if there are no filters.
     *
     * If the list contains values, then the artifact only contains these ABIs and excludes
     * others.
     */
    @Nullable
    public Set<String> getSupportedAbis() {
        return mMergedNdkConfig.getAbiFilters();
    }

    /**
     * Returns whether the configuration has minification enabled.
     */
    public boolean isMinifyEnabled() {
        VariantType type = getType();
        // if type == test then getTestedConfig always returns non-null
        //noinspection ConstantConditions
        return getBuildType().isMinifyEnabled() &&
                (!type.isForTesting() || (getTestedConfig().getType() != VariantType.LIBRARY));
    }

    public boolean getUseJack() {
        Boolean value = getBuildType().getUseJack();
        if (value != null) {
            return value;
        }

        // cant use merge flavor as useJack is not a prop on the base class.
        for (CoreProductFlavor productFlavor : getProductFlavors()) {
            value = productFlavor.getUseJack();
            if (value != null) {
                return value;
            }
        }

        value = getDefaultConfig().getUseJack();
        if (value != null) {
            return value;
        }

        return false;
    }

    private void computeNdkConfig() {
        mMergedNdkConfig.reset();

        if (getDefaultConfig().getNdkConfig() != null) {
            mMergedNdkConfig.append(getDefaultConfig().getNdkConfig());
        }

        final List<CoreProductFlavor> flavors = getProductFlavors();
        for (int i = flavors.size() - 1 ; i >= 0 ; i--) {
            CoreNdkOptions ndkConfig = flavors.get(i).getNdkConfig();
            if (ndkConfig != null) {
                mMergedNdkConfig.append(ndkConfig);
            }
        }

        if (getBuildType().getNdkConfig() != null && !getType().isForTesting()) {
            mMergedNdkConfig.append(getBuildType().getNdkConfig());
        }
    }
}
