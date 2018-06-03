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
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ProductFlavor;
import com.android.builder.model.SigningConfig;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * read-only version of the ProductFlavor wrapping another ProductFlavor.
 *
 * In the variant API, it is important that the objects returned by the variants
 * are read-only.
 *
 * However, even though the API is defined to use the base interfaces as return
 * type (which all contain only getters), the dynamics of Groovy makes it easy to
 * actually use the setters of the implementation classes.
 *
 * This wrapper ensures that the returned instance is actually just a strict implementation
 * of the base interface and is read-only.
 */
public class ReadOnlyProductFlavor extends ReadOnlyBaseConfig implements ProductFlavor {

    @NonNull
    /*package*/ final ProductFlavor productFlavor;

    @NonNull
    private final ReadOnlyObjectProvider readOnlyObjectProvider;

    ReadOnlyProductFlavor(
            @NonNull ProductFlavor productFlavor,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        super(productFlavor);
        this.productFlavor = productFlavor;
        this.readOnlyObjectProvider = readOnlyObjectProvider;
    }

    @Nullable
    @Override
    public String getApplicationId() {
        return productFlavor.getApplicationId();
    }

    @Nullable
    @Override
    public Integer getVersionCode() {
        return productFlavor.getVersionCode();
    }

    @Nullable
    @Override
    public String getVersionName() {
        return productFlavor.getVersionName();
    }

    @Nullable
    @Override
    public ApiVersion getMinSdkVersion() {
        return productFlavor.getMinSdkVersion();
    }

    @Nullable
    @Override
    public ApiVersion getTargetSdkVersion() {
        return productFlavor.getTargetSdkVersion();
    }

    @Nullable
    @Override
    public Integer getMaxSdkVersion() {
        return productFlavor.getMaxSdkVersion();
    }

    @Nullable
    @Override
    public Integer getRenderscriptTargetApi() {
        return productFlavor.getRenderscriptTargetApi();
    }

    @Nullable
    @Override
    public Boolean getRenderscriptSupportModeEnabled() {
        return productFlavor.getRenderscriptSupportModeEnabled();
    }

    @Nullable
    @Override
    public Boolean getRenderscriptNdkModeEnabled() {
        return productFlavor.getRenderscriptNdkModeEnabled();
    }

    @Nullable
    @Override
    public String getTestApplicationId() {
        return productFlavor.getTestApplicationId();
    }

    @Nullable
    @Override
    public String getTestInstrumentationRunner() {
        return productFlavor.getTestInstrumentationRunner();
    }

    @NonNull
    @Override
    public Map<String, String> getTestInstrumentationRunnerArguments() {
        return productFlavor.getTestInstrumentationRunnerArguments();
    }

    @Nullable
    @Override
    public Boolean getTestHandleProfiling() {
        return productFlavor.getTestHandleProfiling();
    }

    @Nullable
    @Override
    public Boolean getTestFunctionalTest() {
        return productFlavor.getTestFunctionalTest();
    }

    @NonNull
    @Override
    public Collection<String> getResourceConfigurations() {
        return productFlavor.getResourceConfigurations();
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return readOnlyObjectProvider.getSigningConfig(productFlavor.getSigningConfig());
    }

    @Nullable
    @Override
    public String getDimension() {
        return productFlavor.getDimension();
    }

    @NonNull
    @Override
    public List<File> getJarJarRuleFiles() {
        return productFlavor.getJarJarRuleFiles();
    }

    @Nullable
    @Deprecated
    public String getFlavorDimension() {
        return productFlavor.getDimension();
    }
}
