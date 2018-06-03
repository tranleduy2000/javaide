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

import java.util.Collections;
import java.util.List;

/**
 * Exposes a read-only view of a variant as well as a flag that can be used to signal that the
 * variant should be ignored.
 */
public class VariantFilter implements com.android.build.gradle.api.VariantFilter {

    @NonNull
    private final ReadOnlyObjectProvider readOnlyObjectProvider;

    private boolean ignore;

    private ProductFlavor defaultConfig;
    private BuildType buildType;
    private List<ProductFlavor> flavors;

    public VariantFilter(@NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        this.readOnlyObjectProvider = readOnlyObjectProvider;
    }

    public void reset(
            @NonNull ProductFlavor defaultConfig,
            @NonNull BuildType buildType,
            @Nullable List<ProductFlavor> flavors) {
        ignore = false;
        this.defaultConfig = defaultConfig;
        this.buildType = buildType;
        this.flavors = flavors;
    }

    /**
     * Whether to ignore this variant.
     */
    public boolean isIgnore() {
        return ignore;
    }

    @Override
    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    /**
     * Returns a read-only ProductFlavor that represents the default config.
     *
     * <p>See {@link com.android.build.gradle.internal.dsl.ProductFlavor} for properties present
     * on the returned object.
     */
    @Override
    @NonNull
    public ProductFlavor getDefaultConfig() {
        return readOnlyObjectProvider.getDefaultConfig(defaultConfig);
    }

    /**
     * Returns a read-only Build Type.
     *
     * <p>See {@link com.android.build.gradle.internal.dsl.BuildType} for properties present
     * on the returned object.
     */
    @Override
    @NonNull
    public BuildType getBuildType() {
        return readOnlyObjectProvider.getBuildType(buildType);
    }

    /**
     * Returns the list of read-only flavors, or an empty list.
     *
     * <p>See {@link com.android.build.gradle.internal.dsl.ProductFlavor} for properties
     * present on the returned objects.
     */
    @NonNull
    @Override
    public List<ProductFlavor> getFlavors() {
        return flavors != null ?
                new ImmutableFlavorList(flavors, readOnlyObjectProvider) :
                Collections.<ProductFlavor>emptyList();
    }
}
