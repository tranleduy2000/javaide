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

package com.android.build.gradle.internal.api;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.TestVariant;
import com.android.build.gradle.api.UnitTestVariant;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.ApplicationVariantData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.core.AndroidBuilder;

/**
 * implementation of the {@link ApplicationVariant} interface around an
 * {@link ApplicationVariantData} object.
 *
 * This is a wrapper around the internal data model, in order to control what is accessible
 * through the external API.
 */
public class ApplicationVariantImpl extends ApkVariantImpl implements ApplicationVariant {

    @NonNull
    private final ApplicationVariantData variantData;

    @Nullable
    private TestVariant testVariant = null;

    @Nullable
    private UnitTestVariant unitTestVariant = null;

    public ApplicationVariantImpl(
            @NonNull ApplicationVariantData variantData,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        super(androidBuilder, readOnlyObjectProvider);
        this.variantData = variantData;
    }

    @Override
    @NonNull
    protected BaseVariantData<?> getVariantData() {
        return variantData;
    }

    @Override
    @NonNull
    protected ApkVariantData getApkVariantData() {
        return variantData;
    }

    @Override
    public void setTestVariant(@Nullable TestVariant testVariant) {
        this.testVariant = testVariant;
    }

    @Override
    @Nullable
    public TestVariant getTestVariant() {
        return testVariant;
    }

    @Override
    @Nullable
    public UnitTestVariant getUnitTestVariant() {
        return unitTestVariant;
    }

    @Override
    public void setUnitTestVariant(@Nullable UnitTestVariant unitTestVariant) {
        this.unitTestVariant = unitTestVariant;
    }
}
