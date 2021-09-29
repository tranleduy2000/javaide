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
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;
import com.google.common.collect.ImmutableSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Implementation of ProductFlavor that is serializable. Objects used in the DSL cannot be
 * serialized.
 **/
class ProductFlavorImpl extends BaseConfigImpl implements ProductFlavor, Serializable {
    private static final long serialVersionUID = 1L;

    private String name = null;
    private String mDimension = null;
    private ApiVersion mMinSdkVersion = null;
    private ApiVersion mTargetSdkVersion = null;
    private Integer mMaxSdkVersion = null;
    private Integer mVersionCode = null;
    private String mVersionName = null;
    private String mApplicationId = null;
    private Set<String> mResourceConfigurations = null;


    private ProductFlavorImpl(@NonNull ProductFlavor productFlavor) {
        super(productFlavor);
    }

    @NonNull
    static ProductFlavorImpl cloneFlavor(
            @NonNull ProductFlavor productFlavor,
            @Nullable ApiVersion minSdkVersionOverride,
            @Nullable ApiVersion targetSdkVersionOverride) {
        ProductFlavorImpl clonedFlavor = new ProductFlavorImpl(productFlavor);
        clonedFlavor.name = productFlavor.getName();
        clonedFlavor.mDimension = productFlavor.getDimension();
        clonedFlavor.mMinSdkVersion = minSdkVersionOverride != null
                ? minSdkVersionOverride
                : ApiVersionImpl.clone(productFlavor.getMinSdkVersion());
        clonedFlavor.mTargetSdkVersion = targetSdkVersionOverride != null
                ? targetSdkVersionOverride
                : ApiVersionImpl.clone(productFlavor.getTargetSdkVersion());
        clonedFlavor.mMaxSdkVersion = targetSdkVersionOverride != null
                ? null /* we remove the maxSdkVersion when dealing with a preview release */
                : productFlavor.getMaxSdkVersion();

        clonedFlavor.mVersionCode = productFlavor.getVersionCode();
        clonedFlavor.mVersionName = productFlavor.getVersionName();

        clonedFlavor.mApplicationId = productFlavor.getApplicationId();


        clonedFlavor.mResourceConfigurations = ImmutableSet.copyOf(
                productFlavor.getResourceConfigurations());


        return clonedFlavor;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public String getApplicationId() {
        return mApplicationId;
    }

    @Override
    @Nullable
    public Integer getVersionCode() {
        return mVersionCode;
    }

    @Override
    @Nullable
    public String getVersionName() {
        return mVersionName;
    }

    @Override
    @Nullable
    public ApiVersion getMinSdkVersion() {
        return mMinSdkVersion;
    }

    @Override
    @Nullable
    public ApiVersion getTargetSdkVersion() {
        return mTargetSdkVersion;
    }

    @Override
    @Nullable
    public Integer getMaxSdkVersion() {
        return mMaxSdkVersion;
    }

    @NonNull
    @Override
    public Collection<String> getResourceConfigurations() {
        return mResourceConfigurations;
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return null;
    }

    @Nullable
    @Override
    public String getDimension() {
        return mDimension;
    }

    @Override
    public String toString() {
        return "ProductFlavorImpl{" +
                "name='" + name + '\'' +
                ", mDimension='" + mDimension + '\'' +
                ", mMinSdkVersion=" + mMinSdkVersion +
                ", mTargetSdkVersion=" + mTargetSdkVersion +
                ", mMaxSdkVersion=" + mMaxSdkVersion +
                ", mVersionCode=" + mVersionCode +
                ", mVersionName='" + mVersionName + '\'' +
                ", mApplicationId='" + mApplicationId + '\'' +
                ", mResourceConfigurations=" + mResourceConfigurations +
                "} " + super.toString();
    }
}
