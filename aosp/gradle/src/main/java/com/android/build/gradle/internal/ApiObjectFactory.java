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

package com.android.build.gradle.internal;

import com.android.annotations.NonNull;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.builder.core.AndroidBuilder;

import org.gradle.internal.reflect.Instantiator;

/**
 * Factory to create ApiObject from VariantData.
 */
public class ApiObjectFactory {
    @NonNull
    private final AndroidBuilder androidBuilder;
    @NonNull
    private final BaseExtension extension;
    @NonNull
    private final VariantFactory variantFactory;
    @NonNull
    private final Instantiator instantiator;
    @NonNull
    private final ReadOnlyObjectProvider readOnlyObjectProvider = new ReadOnlyObjectProvider();

    public ApiObjectFactory(
            @NonNull AndroidBuilder androidBuilder,
            @NonNull BaseExtension extension,
            @NonNull VariantFactory variantFactory,
            @NonNull Instantiator instantiator) {
        this.androidBuilder = androidBuilder;
        this.extension = extension;
        this.variantFactory = variantFactory;
        this.instantiator = instantiator;
    }

    public void create(BaseVariantData<?> variantData) {
        // Testing variants are handled together with their "owners".

        BaseVariant variantApi =
                variantFactory.createVariantApi(variantData, readOnlyObjectProvider);

        // Only add the variant API object to the domain object set once it's been fully
        // initialized.
        extension.addVariant(variantApi);
    }
}
