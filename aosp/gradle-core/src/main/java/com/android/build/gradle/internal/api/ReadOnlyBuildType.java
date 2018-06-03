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
import com.android.builder.model.SigningConfig;

import java.io.File;
import java.util.List;

/**
 * Read-only version of the BuildType wrapping another BuildType.
 *
 * In the variant API, it is important that the objects returned by the variants
 * are read-only.
 *
 * However, even though the API is defined to use the base interfaces as return
 * type (which all contain only getters), the dynamics of Groovy makes it easy to
 * actually use the setters of the implementation classes.
 *
 * This wrapper ensures that the returned instance is actually just a strict implementation
 * of the base interface and is read-only.
 */
public class ReadOnlyBuildType extends ReadOnlyBaseConfig implements BuildType {

    @NonNull
    private final BuildType buildType;

    @NonNull
    private final ReadOnlyObjectProvider readOnlyObjectProvider;

    public ReadOnlyBuildType(
            @NonNull BuildType buildType,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        super(buildType);
        this.buildType = buildType;
        this.readOnlyObjectProvider = readOnlyObjectProvider;
    }

    @Override
    public boolean isDebuggable() {
        return buildType.isDebuggable();
    }

    @Override
    public boolean isTestCoverageEnabled() {
        return buildType.isTestCoverageEnabled();
    }

    @Override
    public boolean isJniDebuggable() {
        return buildType.isJniDebuggable();
    }

    @Override
    public boolean isPseudoLocalesEnabled() {
        return buildType.isPseudoLocalesEnabled();
    }

    @Override
    public boolean isRenderscriptDebuggable() {
        return buildType.isRenderscriptDebuggable();
    }

    @Override
    public int getRenderscriptOptimLevel() {
        return buildType.getRenderscriptOptimLevel();
    }

    @Nullable
    @Override
    public String getApplicationIdSuffix() {
        return buildType.getApplicationIdSuffix();
    }

    @Nullable
    @Override
    public String getVersionNameSuffix() {
        return buildType.getVersionNameSuffix();
    }

    @Override
    public boolean isMinifyEnabled() {
        return buildType.isMinifyEnabled();
    }

    @Override
    public boolean isZipAlignEnabled() {
        return buildType.isZipAlignEnabled();
    }

    @Override
    public boolean isEmbedMicroApp() {
        return buildType.isEmbedMicroApp();
    }

    @Nullable
    @Override
    public SigningConfig getSigningConfig() {
        return readOnlyObjectProvider.getSigningConfig(buildType.getSigningConfig());
    }

    @NonNull
    @Override
    public List<File> getJarJarRuleFiles() {
        return buildType.getJarJarRuleFiles();
    }
}
