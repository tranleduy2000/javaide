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

package com.android.build.gradle.api;

import com.android.annotations.NonNull;
import com.android.builder.model.BuildType;
import com.android.builder.model.ProductFlavor;

import java.util.List;

/**
 * Interface for variant control, allowing to query a variant for some base
 * data and allowing to disable some variants.
 */
public interface VariantFilter {

    /**
     * Sets whether or not to ignore this particular variant. Default is false.
     * @param ignore whether to ignore the variant
     */
    void setIgnore(boolean ignore);

    /**
     * Returns the ProductFlavor that represents the default config.
     */
    @NonNull
    ProductFlavor getDefaultConfig();

    /**
     * Returns the Build Type.
     */
    @NonNull
    BuildType getBuildType();

    /**
     * Returns the list of flavors, or an empty list.
     */
    @NonNull
    List<ProductFlavor> getFlavors();
}
