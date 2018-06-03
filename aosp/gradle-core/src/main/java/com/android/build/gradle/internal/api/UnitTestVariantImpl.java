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

package com.android.build.gradle.internal.api;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.UnitTestVariant;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.builder.core.AndroidBuilder;

/**
 * External API wrapper around the {@link TestVariantData}, for unit testing variants.
 */
public class UnitTestVariantImpl extends BaseVariantImpl implements UnitTestVariant {

    @NonNull
    private final TestVariantData variantData;
    @NonNull
    private final TestedVariant testedVariant;

    public UnitTestVariantImpl(
            @NonNull TestVariantData variantData,
            @NonNull TestedVariant testedVariant,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        super(androidBuilder, readOnlyObjectProvider);

        this.variantData = variantData;
        this.testedVariant = testedVariant;
    }

    @NonNull
    @Override
    protected BaseVariantData<?> getVariantData() {
        return variantData;
    }

    @NonNull
    @Override
    public TestedVariant getTestedVariant() {
        return testedVariant;
    }
}
