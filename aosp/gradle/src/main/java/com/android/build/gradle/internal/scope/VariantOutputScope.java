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

package com.android.build.gradle.internal.scope;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.tasks.CompatibleScreensManifest;
import com.android.build.gradle.tasks.ManifestProcessorTask;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.utils.StringHelper;

import java.io.File;

/**
 * A scope containing data for a specific variant.
 */
public class VariantOutputScope {

    @NonNull
    private VariantScope variantScope;
    @NonNull
    private BaseVariantOutputData variantOutputData;

    // Tasks
    private AndroidTask<CompatibleScreensManifest> compatibleScreensManifestTask;

    private AndroidTask<? extends ManifestProcessorTask> manifestProcessorTask;

    private AndroidTask<ProcessAndroidResources> processResourcesTask;

    public VariantOutputScope(
            @NonNull VariantScope variantScope,
            @NonNull BaseVariantOutputData variantOutputData) {
        this.variantScope = variantScope;
        this.variantOutputData = variantOutputData;
    }

    @NonNull
    public GlobalScope getGlobalScope() {
        return variantScope.getGlobalScope();
    }

    @NonNull
    public VariantScope getVariantScope() {
        return variantScope;
    }

    @NonNull
    public BaseVariantOutputData getVariantOutputData() {
        return variantOutputData;
    }

    @NonNull
    public String getTaskName(@NonNull String prefix) {
        return getTaskName(prefix, "");
    }

    @NonNull
    public String getTaskName(@NonNull String prefix, @NonNull String suffix) {
        return prefix + StringHelper.capitalize(getVariantOutputData().getFullName()) + suffix;
    }

    @NonNull
    public File getPackageApk() {
        ApkVariantData apkVariantData = (ApkVariantData) variantScope.getVariantData();

        boolean signedApk = apkVariantData.isSigned();
        String apkName = signedApk ?
                getGlobalScope().getProjectBaseName() + "-" + variantOutputData.getBaseName() + "-unaligned.apk" :
                getGlobalScope().getProjectBaseName() + "-" + variantOutputData.getBaseName() + "-unsigned.apk";

        // if this is the final task then the location is
        // the potentially overridden one.
        if (!signedApk || !apkVariantData.getZipAlignEnabled()) {
            return getGlobalScope().getProject().file(
                    getGlobalScope().getApkLocation() + "/" + apkName);
        } else {
            // otherwise default one.
            return getGlobalScope().getProject().file(getGlobalScope().getDefaultApkLocation() + "/" + apkName);
        }
    }

    @NonNull
    public File getCompressedResourceFile() {
        return new File(getGlobalScope().getIntermediatesDir(), "/res/" +
                "resources-" + variantOutputData.getBaseName() + "-stripped.ap_");
    }

    @NonNull
    public File getCompatibleScreensManifestFile() {
        return new File(getGlobalScope().getIntermediatesDir(),
                "/manifests/density/" + variantOutputData.getDirName() + "/AndroidManifest.xml");

    }

    @NonNull
    public File getManifestOutputFile() {
        switch(variantScope.getVariantConfiguration().getType()) {
            case DEFAULT:
                return new File(getGlobalScope().getIntermediatesDir(),
                        "/manifests/full/"  + variantOutputData.getDirName()
                                + "/AndroidManifest.xml");
            case LIBRARY:
                return new File(getGlobalScope().getIntermediatesDir(),
                        TaskManager.DIR_BUNDLES + "/"
                                + getVariantScope().getVariantConfiguration().getDirName()
                                + "/AndroidManifest.xml");
            case ANDROID_TEST:
                return new File(getGlobalScope().getIntermediatesDir(),
                        "manifest/" + variantScope.getVariantConfiguration().getDirName()
                                + "/AndroidManifest.xml");
            default:
                throw new RuntimeException(
                        "getManifestOutputFile called for an unexpected variant.");
        }
    }

    @NonNull
    public File getProcessResourcePackageOutputFile() {
        return new File(getGlobalScope().getIntermediatesDir(),
                "res/resources-" + variantOutputData.getBaseName() + ".ap_");
    }

    // Tasks
    @Nullable
    public AndroidTask<CompatibleScreensManifest> getCompatibleScreensManifestTask() {
        return compatibleScreensManifestTask;
    }

    public void setCompatibleScreensManifestTask(
            @Nullable AndroidTask<CompatibleScreensManifest> compatibleScreensManifestTask) {
        this.compatibleScreensManifestTask = compatibleScreensManifestTask;
    }

    public AndroidTask<? extends ManifestProcessorTask> getManifestProcessorTask() {
        return manifestProcessorTask;
    }

    public void setManifestProcessorTask(
            AndroidTask<? extends ManifestProcessorTask> manifestProcessorTask) {
        this.manifestProcessorTask = manifestProcessorTask;
    }

    public AndroidTask<ProcessAndroidResources> getProcessResourcesTask() {
        return processResourcesTask;
    }

    public void setProcessResourcesTask(
            AndroidTask<ProcessAndroidResources> processResourcesTask) {
        this.processResourcesTask = processResourcesTask;
    }
}
