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
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Provides read-only versions of BuildType, ProductFlavor and SigningConfig instances
 * so that they can safely be exposed through the variant API.
 * <p>
 * The class creates them on the fly so that they are only created when a
 * Gradle script/plugin queries for them, and caches them so that we reuse them as needed.
 */
public class ReadOnlyObjectProvider {

    /**
     * Map of read-only build-types. This maps the normal build type to the read-only version.
     */
    @NonNull
    private final Map<BuildType, BuildType> readOnlyBuildTypes = Maps.newIdentityHashMap();
    /**
     * Map of read-only ProductFlavor. This maps the normal flavor to the read-only version.
     */
    @NonNull
    private final Map<ProductFlavor, ProductFlavor> readOnlyFlavors = Maps.newIdentityHashMap();
    /**
     * Map of read-only SigningConfig. This maps the normal config to the read-only version.
     */
    @NonNull
    private final Map<SigningConfig, SigningConfig> readOnlySigningConfig = Maps.newIdentityHashMap();
    private ReadOnlyProductFlavor readOnlyDefaultConfig;

    /**
     * Returns an read-only version of the default config.
     *
     * @param defaultConfig the default config.
     * @return an read-only version.
     */
    @NonNull
    ProductFlavor getDefaultConfig(@NonNull ProductFlavor defaultConfig) {
        if (readOnlyDefaultConfig != null) {
            if (readOnlyDefaultConfig.productFlavor != defaultConfig) {
                throw new IllegalStateException("Different DefaultConfigs passed to ApiObjectProvider");
            }
        } else {
            readOnlyDefaultConfig = new ReadOnlyProductFlavor(defaultConfig, this);
        }

        return readOnlyDefaultConfig;
    }

    /**
     * Returns an read-only version of a build type.
     *
     * @param buildType the build type.
     * @return an read-only version.
     */
    @NonNull
    public BuildType getBuildType(@NonNull BuildType buildType) {
        BuildType readOnlyBuildType = readOnlyBuildTypes.get(buildType);
        if (readOnlyBuildType == null) {
            readOnlyBuildTypes.put(buildType,
                    readOnlyBuildType = new ReadOnlyBuildType(buildType, this));
        }

        return readOnlyBuildType;
    }

    /**
     * Retuens an read-only version of a groupable product flavor.
     *
     * @param productFlavor the product flavor.
     * @return an read-only version.
     */
    @NonNull
    public ProductFlavor getProductFlavor(@NonNull ProductFlavor productFlavor) {
        ProductFlavor readOnlyProductFlavor = readOnlyFlavors.get(productFlavor);
        if (readOnlyProductFlavor == null) {
            readOnlyFlavors.put(productFlavor,
                    readOnlyProductFlavor = new ReadOnlyProductFlavor(
                            productFlavor, this));
        }

        return readOnlyProductFlavor;
    }

    /**
     * Returns an read-only version of a signing config.
     *
     * @param signingConfig the signing config.
     * @return an read-only version.
     */
    @Nullable
    public SigningConfig getSigningConfig(@Nullable SigningConfig signingConfig) {
        if (signingConfig == null) {
            return null;
        }

        SigningConfig readOnlySigningConfig = this.readOnlySigningConfig.get(signingConfig);
        if (readOnlySigningConfig == null) {
            this.readOnlySigningConfig.put(signingConfig,
                    readOnlySigningConfig = new ReadOnlySigningConfig(signingConfig));
        }

        return readOnlySigningConfig;
    }
}
