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
import com.android.build.gradle.TestedAndroidConfig;
import com.android.build.gradle.api.BaseVariant;
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider;
import com.android.build.gradle.internal.api.TestVariantImpl;
import com.android.build.gradle.internal.api.TestedVariant;
import com.android.build.gradle.internal.api.UnitTestVariantImpl;
import com.android.build.gradle.internal.variant.ApplicationVariantFactory;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.TestVariantData;
import com.android.build.gradle.internal.variant.TestedVariantData;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.builder.core.AndroidBuilder;

import org.gradle.internal.reflect.Instantiator;

import static com.android.builder.core.VariantType.ANDROID_TEST;
import static com.android.builder.core.VariantType.UNIT_TEST;

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
        if (variantData.getType().isForTesting()) {
            // Testing variants are handled together with their "owners".
            return;
        }

        BaseVariant variantApi =
                variantFactory.createVariantApi(variantData, readOnlyObjectProvider);

        if (variantFactory.hasTestScope()) {
            TestVariantData androidTestVariantData =
                    ((TestedVariantData) variantData).getTestVariantData(ANDROID_TEST);

            if (androidTestVariantData != null) {
                TestVariantImpl androidTestVariant = instantiator.newInstance(
                        TestVariantImpl.class,
                        androidTestVariantData,
                        variantApi,
                        androidBuilder,
                        readOnlyObjectProvider);

                // add the test output.
                ApplicationVariantFactory.createApkOutputApiObjects(
                        instantiator,
                        androidTestVariantData,
                        androidTestVariant);

                ((TestedAndroidConfig) extension).getTestVariants().add(androidTestVariant);
                ((TestedVariant) variantApi).setTestVariant(androidTestVariant);
            }

            TestVariantData unitTestVariantData =
                    ((TestedVariantData) variantData).getTestVariantData(UNIT_TEST);
            if (unitTestVariantData != null) {
                UnitTestVariantImpl unitTestVariant = instantiator.newInstance(
                        UnitTestVariantImpl.class,
                        unitTestVariantData,
                        variantApi,
                        androidBuilder,
                        readOnlyObjectProvider);

                ((TestedAndroidConfig) extension).getUnitTestVariants().add(unitTestVariant);
                ((TestedVariant) variantApi).setUnitTestVariant(unitTestVariant);
            }
        }

        // Only add the variant API object to the domain object set once it's been fully
        // initialized.
        extension.addVariant(variantApi);
    }
}
