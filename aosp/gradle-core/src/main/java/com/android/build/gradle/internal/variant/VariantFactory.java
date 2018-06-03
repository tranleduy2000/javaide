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

package com.android.build.gradle.internal.variant;

import com.android.annotations.NonNull;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.VariantModel;
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.builder.core.VariantType;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;

/**
 * Interface for Variant Factory.
 *
 * While VariantManager is the general variant management, implementation of this interface
 * provides variant type (app, lib) specific implementation.
 */
public interface VariantFactory {

    @NonNull
    BaseVariantData createVariantData(
            @NonNull GradleVariantConfiguration variantConfiguration,
            @NonNull TaskManager taskManager);

    @NonNull
    BaseVariant createVariantApi(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider);

    @NonNull
    VariantType getVariantConfigurationType();

    boolean isLibrary();

    boolean hasTestScope();

    /**
     * Fail if the model is configured incorrectly.
     * @param model the non-null model to validate, as implemented by the VariantManager.
     * @throws org.gradle.api.GradleException when the model does not validate.
     */
    void validateModel(@NonNull VariantModel model);

    void preVariantWork(Project project);

    void createDefaultComponents(
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs);
}
