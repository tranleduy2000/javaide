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

package com.android.build.gradle.model;

import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.managed.BuildType;
import com.android.build.gradle.managed.ProductFlavor;

import java.util.List;

/**
 * Binary for Android.
 */
public class DefaultAndroidBinary  implements AndroidBinary {

    private BuildType buildType;

    private List<ProductFlavor> productFlavors;

    private BaseVariantData variantData;

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    @Override
    public List<ProductFlavor> getProductFlavors() {
        return productFlavors;
    }

    public void setProductFlavors(List<ProductFlavor> productFlavors) {
        this.productFlavors = productFlavors;
    }

    public BaseVariantData getVariantData() {
        return variantData;
    }

    public void setVariantData(BaseVariantData variantData) {
        this.variantData = variantData;
    }

}
