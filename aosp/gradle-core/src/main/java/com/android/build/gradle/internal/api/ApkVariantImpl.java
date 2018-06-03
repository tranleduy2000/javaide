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
import com.android.build.gradle.api.ApkVariant;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.tasks.Dex;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.SigningConfig;

import org.gradle.api.DefaultTask;

import java.io.File;
import java.util.Collection;

/**
 * Implementation of the apk-generating variant.
 *
 * This is a wrapper around the internal data model, in order to control what is accessible
 * through the external API.
 */
public abstract class ApkVariantImpl extends BaseVariantImpl implements ApkVariant {

    protected ApkVariantImpl(@NonNull AndroidBuilder androidBuilder,
            @NonNull ReadOnlyObjectProvider immutableObjectProvider) {
        super(androidBuilder, immutableObjectProvider);
    }

    @NonNull
    protected abstract ApkVariantData getApkVariantData();

    @Override
    @Nullable
    public String getVersionName() {
        return getApkVariantData().getVariantConfiguration().getVersionName();
    }

    @Override
    public int getVersionCode() {
        return getApkVariantData().getVariantConfiguration().getVersionCode();
    }

    @Override
    public Dex getDex() {
        return getApkVariantData().dexTask;
    }

    @Override
    public DefaultTask getUninstall() {
        return getApkVariantData().uninstallTask;
    }

    @Override
    public SigningConfig getSigningConfig() {
        return readOnlyObjectProvider.getSigningConfig(
                getApkVariantData().getVariantConfiguration().getSigningConfig());
    }

    @Override
    public boolean isSigningReady() {
        return getApkVariantData().isSigned();
    }

    @Override
    @NonNull
    public Collection<File> getCompileLibraries() {
        return androidBuilder.getCompileClasspath(
                getVariantData().getVariantConfiguration());
    }

    @Override
    @NonNull
    public Collection<File> getApkLibraries() {
        return androidBuilder.getPackagedJars(getVariantData().getVariantConfiguration());
    }

    @Override
    public DefaultTask getInstall() {
        return getApkVariantData().installTask;
    }
}
