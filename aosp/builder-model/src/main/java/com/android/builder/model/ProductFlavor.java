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

import java.util.Collection;

/**
 * a Product Flavor. This is only the configuration of the flavor.
 *
 * It does not include the sources or the dependencies. Those are available on the container
 * or in the artifact info.
 *
 * @see ProductFlavorContainer
 * @see BaseArtifact#getDependencies()
 */
public interface ProductFlavor extends BaseConfig, DimensionAware {

    /**
     * Returns the name of the flavor.
     *
     * @return the name of the flavor.
     */
    @Override
    @NonNull
    String getName();

    /**
     * Returns the name of the product flavor. This is only the value set on this product flavor.
     * To get the final application id name, use {@link AndroidArtifact#getApplicationId()}.
     *
     * @return the application id.
     */
    @Nullable
    String getApplicationId();

    /**
     * Returns the version code associated with this flavor or null if none have been set.
     * This is only the value set on this product flavor, not necessarily the actual
     * version code used.
     *
     * @return the version code, or null if not specified
     */
    @Nullable
    Integer getVersionCode();

    /**
     * Returns the version name. This is only the value set on this product flavor.
     * To get the final value, use {@link Variant#getMergedFlavor()} as well as
     * {@link BuildType#getVersionNameSuffix()}
     *
     * @return the version name.
     */
    @Nullable
    String getVersionName();

    /**
     * Returns the minSdkVersion. This is only the value set on this product flavor.
     *
     * @return the minSdkVersion, or null if not specified
     */
    @Nullable
    ApiVersion getMinSdkVersion();

    /**
     * Returns the targetSdkVersion. This is only the value set on this product flavor.
     *
     * @return the targetSdkVersion, or null if not specified
     */
    @Nullable
    ApiVersion getTargetSdkVersion();

    /**
     * Returns the maxSdkVersion. This is only the value set on this produce flavor.
     *
     * @return the maxSdkVersion, or null if not specified
     */
    @Nullable
    Integer getMaxSdkVersion();


    /**
     * Returns the resource configuration for this variant.
     *
     * This is the list of -c parameters for aapt.
     *
     * @return the resource configuration options.
     */
    @NonNull
    Collection<String> getResourceConfigurations();

    /**
     * Returns the associated signing config or null if none are set on the product flavor.
     */
    @Nullable
    SigningConfig getSigningConfig();
}
