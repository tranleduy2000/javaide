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
import com.android.build.OutputFile;
import com.android.build.gradle.api.ApkVariantOutput;
import com.android.build.gradle.internal.variant.ApkVariantOutputData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.tasks.PackageApplication;
import com.android.build.gradle.tasks.ZipAlign;

import java.io.File;

/**
 * Implementation of variant output for apk-generating variants.
 *
 * This is a wrapper around the internal data model, in order to control what is accessible
 * through the external API.
 */
public class ApkVariantOutputImpl extends BaseVariantOutputImpl implements ApkVariantOutput {

    private final ApkVariantOutputData variantOutputData;

    public ApkVariantOutputImpl(@NonNull ApkVariantOutputData variantOutputData) {
        this.variantOutputData = variantOutputData;
    }

    @Override
    @NonNull
    protected BaseVariantOutputData getVariantOutputData() {
        return variantOutputData;
    }

    @Nullable
    @Override
    public PackageApplication getPackageApplication() {
        return variantOutputData.packageApplicationTask;
    }

    @Nullable
    @Override
    public ZipAlign getZipAlign() {
        return variantOutputData.zipAlignTask;
    }

    @NonNull
    @Override
    public ZipAlign createZipAlignTask(@NonNull String taskName, @NonNull File inputFile,
            @NonNull File outputFile) {
        return variantOutputData.createZipAlignTask(taskName, inputFile, outputFile);
    }

    @Override
    public void setVersionCodeOverride(int versionCodeOverride) {
        variantOutputData.setVersionCodeOverride(versionCodeOverride);
    }

    @Override
    public int getVersionCodeOverride() {
        return variantOutputData.getVersionCodeOverride();
    }

    @Override
    public void setVersionNameOverride(String versionNameOverride) {
        variantOutputData.setVersionNameOverride(versionNameOverride);
    }

    @Override
    public String getVersionNameOverride() {
        return variantOutputData.getVersionNameOverride();
    }

    @Override
    public int getVersionCode() {
        return variantOutputData.getVersionCode();
    }

    @Override
    public String getFilter(OutputFile.FilterType filterType) {
        return variantOutputData.getMainOutputFile().getFilter(filterType.name());
    }
}
