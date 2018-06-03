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
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;
import com.android.builder.core.VariantType;

import org.gradle.api.Project;

/**
 * Common parts of build type and product flavor data objects.
 */
public class VariantDimensionData {

    private final DefaultAndroidSourceSet sourceSet;
    private final DefaultAndroidSourceSet androidTestSourceSet;
    private final DefaultAndroidSourceSet unitTestSourceSet;

    private final ConfigurationProvider mainProvider;
    private final ConfigurationProvider androidTestProvider;
    private final ConfigurationProvider unitTestProvider;

    public VariantDimensionData(
            @NonNull DefaultAndroidSourceSet sourceSet,
            @Nullable DefaultAndroidSourceSet androidTestSourceSet,
            @Nullable DefaultAndroidSourceSet unitTestSourceSet,
            @NonNull Project project) {
        this.sourceSet = sourceSet;
        this.androidTestSourceSet = androidTestSourceSet;
        this.unitTestSourceSet = unitTestSourceSet;

        mainProvider = new ConfigurationProviderImpl(project, sourceSet);

        androidTestProvider = androidTestSourceSet != null ?
                new ConfigurationProviderImpl(project, androidTestSourceSet) : null;
        unitTestProvider = unitTestSourceSet != null ?
                new ConfigurationProviderImpl(project, unitTestSourceSet) : null;
    }

    @NonNull
    public ConfigurationProvider getMainProvider() {
        return mainProvider;
    }

    @NonNull
    public DefaultAndroidSourceSet getSourceSet() {
        return sourceSet;
    }

    @Nullable
    public DefaultAndroidSourceSet getTestSourceSet(@NonNull VariantType type) {
        switch (type) {
            case ANDROID_TEST:
                return androidTestSourceSet;
            case UNIT_TEST:
                return unitTestSourceSet;
            default:
                throw unknownTestType(type);
        }
    }

    @Nullable
    public ConfigurationProvider getTestConfigurationProvider(@NonNull VariantType type) {
        switch (type) {
            case ANDROID_TEST:
                return androidTestProvider;
            case UNIT_TEST:
                return unitTestProvider;
            default:
                throw unknownTestType(type);
        }
    }

    private static RuntimeException unknownTestType(VariantType type) {
        throw new IllegalArgumentException(
                String.format("Unknown test variant type %s", type));
    }
}
