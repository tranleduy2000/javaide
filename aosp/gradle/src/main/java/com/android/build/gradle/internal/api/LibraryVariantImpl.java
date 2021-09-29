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
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.builder.core.AndroidBuilder;

/**
 * implementation of the {@link LibraryVariant} interface around a
 * {@link LibraryVariantData} object.
 * <p>
 * This is a wrapper around the internal data model, in order to control what is accessible
 * through the external API.
 */
public class LibraryVariantImpl extends BaseVariantImpl implements LibraryVariant {

    @NonNull
    private final LibraryVariantData variantData;

    public LibraryVariantImpl(
            @NonNull LibraryVariantData variantData,
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

}
