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

import static com.android.build.OutputFile.NO_FILTER;
import static com.android.builder.core.BuilderConstants.DEBUG;
import static com.android.builder.core.BuilderConstants.RELEASE;

import com.android.annotations.NonNull;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.BaseVariantOutput;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.VariantModel;
import com.android.build.gradle.internal.api.ApkVariantImpl;
import com.android.build.gradle.internal.api.ApkVariantOutputImpl;
import com.android.build.gradle.internal.api.ApplicationVariantImpl;
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.build.gradle.internal.model.FilterDataImpl;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.VariantType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An implementation of VariantFactory for a project that generates APKs.
 *
 * This can be an app project, or a test-only project, though the default
 * behavior is app.
 */
public class ApplicationVariantFactory implements VariantFactory {

    Instantiator instantiator;
    @NonNull
    protected final AndroidConfig extension;
    @NonNull
    private final AndroidBuilder androidBuilder;

    public ApplicationVariantFactory(
            @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull AndroidConfig extension) {
        this.instantiator = instantiator;
        this.androidBuilder = androidBuilder;
        this.extension = extension;
    }

    @Override
    @NonNull
    public BaseVariantData createVariantData(
            @NonNull GradleVariantConfiguration variantConfiguration,
            @NonNull TaskManager taskManager) {
        ApplicationVariantData variant =
                new ApplicationVariantData(extension, variantConfiguration, taskManager);

        variant.calculateFilters(extension.getSplits());

        Set<String> densities = variant.getFilters(OutputFile.FilterType.DENSITY);
        Set<String> abis = variant.getFilters(OutputFile.FilterType.ABI);

        if (!densities.isEmpty()) {
            variant.setCompatibleScreens(extension.getSplits().getDensity()
                    .getCompatibleScreens());
        }

        // create its outputs
        if (variant.getSplitHandlingPolicy() ==
                BaseVariantData.SplitHandlingPolicy.PRE_21_POLICY) {

            // Always dd an entry with no filter for universal and add it FIRST,
            // since code assume that the first variant output will be the universal one.
            List<String> orderedDensities = new ArrayList<String>();
            orderedDensities.add(NO_FILTER);
            orderedDensities.addAll(densities);

            List<String> orderedAbis = new ArrayList<String>();
            // if the abi list is empty or we must generate a universal apk, add a NO_FILTER
            if (abis.isEmpty() || (extension.getSplits().getAbi().isEnable() &&
                    extension.getSplits().getAbi().isUniversalApk())) {
                orderedAbis.add(NO_FILTER);
            }
            orderedAbis.addAll(abis);

            // create its outputs
            for (String density : orderedDensities) {
                for (String abi : orderedAbis) {
                    ImmutableList.Builder<FilterData> builder = ImmutableList.builder();
                    if (density != null) {
                        builder.add(FilterDataImpl.build(OutputFile.DENSITY, density));
                    }
                    if (abi != null) {
                        builder.add(FilterDataImpl.build(OutputFile.ABI, abi));
                    }
                    variant.createOutput(
                            OutputFile.OutputType.FULL_SPLIT,
                            builder.build());
                }
            }
        } else {
            variant.createOutput(OutputFile.OutputType.MAIN,
                    Collections.<FilterData>emptyList());
        }

        return variant;
    }

    @Override
    @NonNull
    public ApplicationVariant createVariantApi(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        // create the base variant object.
        ApplicationVariantImpl variant = instantiator.newInstance(
                ApplicationVariantImpl.class,
                variantData,
                androidBuilder,
                readOnlyObjectProvider);

        // now create the output objects
        createApkOutputApiObjects(instantiator, variantData, variant);

        return variant;
    }

    public static void createApkOutputApiObjects(
            @NonNull Instantiator instantiator,
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData,
            @NonNull ApkVariantImpl variant) {
        List<? extends BaseVariantOutputData> outputList = variantData.getOutputs();
        List<BaseVariantOutput> apiOutputList = Lists.newArrayListWithCapacity(outputList.size());

        for (BaseVariantOutputData variantOutputData : outputList) {
            ApkVariantOutputData apkOutput = (ApkVariantOutputData) variantOutputData;

            ApkVariantOutputImpl output = instantiator.newInstance(
                    ApkVariantOutputImpl.class, apkOutput);

            apiOutputList.add(output);
        }

        variant.addOutputs(apiOutputList);
    }

    @NonNull
    @Override
    public VariantType getVariantConfigurationType() {
        return VariantType.DEFAULT;
    }

    @Override
    public boolean isLibrary() {
        return false;
    }

    @Override
    public boolean hasTestScope() {
        return true;
    }

    @Override
    public void validateModel(@NonNull VariantModel model){
        // No additional checks for ApplicationVariantFactory, so just return.
    }

    @Override
    public void preVariantWork(Project project) {
        // nothing to be done here.
    }

    @Override
    public void createDefaultComponents(
            @NonNull NamedDomainObjectContainer<BuildType> buildTypes,
            @NonNull NamedDomainObjectContainer<ProductFlavor> productFlavors,
            @NonNull NamedDomainObjectContainer<SigningConfig> signingConfigs) {
        // must create signing config first so that build type 'debug' can be initialized
        // with the debug signing config.
        signingConfigs.create(DEBUG);
        buildTypes.create(DEBUG);
        buildTypes.create(RELEASE);
    }
}
