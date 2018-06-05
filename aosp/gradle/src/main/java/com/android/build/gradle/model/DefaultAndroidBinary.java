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

import com.android.build.gradle.internal.NdkOptionsHelper;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.managed.BuildType;
import com.android.build.gradle.managed.NdkConfig;
import com.android.build.gradle.managed.NdkOptions;
import com.android.build.gradle.managed.ProductFlavor;
import com.google.common.collect.Lists;

import org.gradle.nativeplatform.NativeLibraryBinarySpec;
import org.gradle.platform.base.binary.BaseBinarySpec;

import java.util.List;

/**
 * Binary for Android.
 */
public class DefaultAndroidBinary extends BaseBinarySpec implements AndroidBinary {

    private BuildType buildType;

    private List<ProductFlavor> productFlavors;

    private NdkConfig mergedNdkConfig = new NdkConfigImpl();

    private BaseVariantData variantData;

    private List<NativeLibraryBinarySpec> nativeBinaries = Lists.newArrayList();

    private List<String> targetAbi = Lists.newArrayList();

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

    public NdkConfig getMergedNdkConfig() {
        return mergedNdkConfig;
    }

    public BaseVariantData getVariantData() {
        return variantData;
    }

    public void setVariantData(BaseVariantData variantData) {
        this.variantData = variantData;
    }

    public List<NativeLibraryBinarySpec> getNativeBinaries() {
        return nativeBinaries;
    }

    public List<String> getTargetAbi() {
        return targetAbi;
    }

    public void computeMergedNdk(
            NdkConfig ndkConfig,
            List<com.android.build.gradle.managed.ProductFlavor> flavors,
            com.android.build.gradle.managed.BuildType buildType) {


        if (ndkConfig != null) {
            NdkOptionsHelper.merge(mergedNdkConfig, ndkConfig);
        }

        for (int i = flavors.size() - 1 ; i >= 0 ; i--) {
            NdkOptions ndkOptions = flavors.get(i).getNdk();
            if (ndkOptions != null) {
                NdkOptionsHelper.merge(mergedNdkConfig, ndkOptions);
            }
        }

        if (buildType.getNdk() != null) {
            NdkOptionsHelper.merge(mergedNdkConfig, buildType.getNdk());
        }
    }
}
