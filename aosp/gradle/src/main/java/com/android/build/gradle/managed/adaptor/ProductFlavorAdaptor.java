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
import com.android.build.gradle.internal.dsl.CoreProductFlavor;
import com.android.build.gradle.managed.ProductFlavor;
import com.android.builder.core.BuilderConstants;
import com.android.builder.internal.ClassFieldImpl;
import com.android.builder.model.ApiVersion;
import com.android.builder.model.ClassField;
import com.android.builder.model.SigningConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * An adaptor to convert a ProductFlavor to CoreProductFlavor.
 */
public class ProductFlavorAdaptor implements CoreProductFlavor {

    @NonNull
    protected final ProductFlavor productFlavor;

    public ProductFlavorAdaptor(@NonNull ProductFlavor productFlavor) {
        this.productFlavor = productFlavor;
    }

    @NonNull
    @Override
    public String getName() {
        return productFlavor.getName().equals("defaultConfig") ? BuilderConstants.MAIN : productFlavor.getName();
    }

    @Nullable
    @Override
    public String getDimension() {
        return productFlavor.getDimension();
    }

    @NonNull
    @Override
    public Map<String, ClassField> getBuildConfigFields() {
        ImmutableMap.Builder<String, ClassField> builder = ImmutableMap.builder();
        for (com.android.build.gradle.managed.ClassField cf : productFlavor.getBuildConfigFields()) {
            builder.put(
                    cf.getName(),
                    new ClassFieldImpl(
                            cf.getType(),
                            cf.getName(),
                            cf.getValue(),
                            MoreObjects.firstNonNull(cf.getAnnotations(), ImmutableSet.<String>of()),
                            MoreObjects.firstNonNull(cf.getDocumentation(), "")));
        }
        return builder.build();
    }

    @NonNull
    @Override
    public Map<String, ClassField> getResValues() {
        ImmutableMap.Builder<String, ClassField> builder = ImmutableMap.builder();
        for (com.android.build.gradle.managed.ClassField cf : productFlavor.getResValues()) {
            builder.put(
                    cf.getName(),
                    new ClassFieldImpl(
                            cf.getType(),
                            cf.getName(),
                            cf.getValue(),
                            MoreObjects.firstNonNull(cf.getAnnotations(), ImmutableSet.<String>of()),
                            MoreObjects.firstNonNull(cf.getDocumentation(), "")));
        }
        return builder.build();
    }

    @NonNull
    @Override
    public Collection<File> getProguardFiles() {
        return productFlavor.getProguardFiles();
    }

    @NonNull
    @Override
    public Collection<File> getConsumerProguardFiles() {
        return productFlavor.getConsumerProguardFiles();
    }

    @NonNull
    @Override
    public Collection<File> getTestProguardFiles() {
        return productFlavor.getTestProguardFiles();
    }

    @NonNull
    @Override
    public Map<String, Object> getManifestPlaceholders() {
        // TODO: To be implemented
        return Maps.newHashMap();
    }

    @Nullable
    @Override
    public Boolean getMultiDexEnabled() {
        return productFlavor.getMultiDexEnabled();
    }

    @Nullable
    @Override
    public File getMultiDexKeepFile() {
        return productFlavor.getMultiDexKeepFile();
    }

    @Nullable
    @Override
    public File getMultiDexKeepProguard() {
        return productFlavor.getMultiDexKeepProguard();
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
        return ApiVersionAdaptor.isEmpty(productFlavor.getMinSdkVersion()) ?
                null :
                new ApiVersionAdaptor(productFlavor.getMinSdkVersion());
    }

    @Nullable
    @Override
    public ApiVersion getTargetSdkVersion() {
        return ApiVersionAdaptor.isEmpty(productFlavor.getTargetSdkVersion()) ?
                null :
                new ApiVersionAdaptor(productFlavor.getTargetSdkVersion());
    }

    @Nullable
    @Override
    public Integer getMaxSdkVersion() {
        return productFlavor.getMaxSdkVersion();
    }

    @NonNull
    private Map<String, String> getTestInstrumentationRunnerArguments() {
        // TODO: To be implemented.
        return Maps.newHashMap();
    }

    @NonNull
    @Override
    public Collection<String> getResourceConfigurations() {
        return productFlavor.getResourceConfigurations();
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return productFlavor.getSigningConfig() == null ?
                null :
                new SigningConfigAdaptor(productFlavor.getSigningConfig());
    }

    @NonNull
    @Override
    public List<File> getJarJarRuleFiles() {
        return productFlavor.getJarJarRuleFiles();
    }
}
