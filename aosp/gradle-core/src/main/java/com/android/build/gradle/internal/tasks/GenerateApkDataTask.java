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

package com.android.build.gradle.internal.tasks;

import static com.android.SdkConstants.DOT_ANDROID_PACKAGE;
import static com.android.SdkConstants.FD_RES_RAW;
import static com.android.SdkConstants.FN_ANDROID_MANIFEST_XML;
import static com.android.builder.core.BuilderConstants.ANDROID_WEAR_MICRO_APK;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.ApkVariantData;
import com.android.builder.core.AndroidBuilder;
import com.android.ide.common.internal.LoggedErrorException;
import com.android.ide.common.process.ProcessException;
import com.android.utils.FileUtils;
import com.google.common.io.Files;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Task to generate micro app data res file.
 */
public class GenerateApkDataTask extends BaseTask {

    private File apkFile;

    private File resOutputDir;

    private File manifestFile;

    private String mainPkgName;

    private int minSdkVersion;

    private int targetSdkVersion;

    @Input
    String getBuildToolsVersion() {
        return getBuildTools().getRevision().toString();
    }

    @TaskAction
    void generate() throws IOException, ProcessException, LoggedErrorException,
            InterruptedException {
        // always empty output dir.
        File outDir = getResOutputDir();
        FileUtils.emptyFolder(outDir);

        File apk = getApkFile();
        // copy the file into the destination, by sanitizing the name first.
        File rawDir = new File(outDir, FD_RES_RAW);
        rawDir.mkdirs();

        File to = new File(rawDir, ANDROID_WEAR_MICRO_APK + DOT_ANDROID_PACKAGE);
        Files.copy(apk, to);

        // now create the matching XML and the manifest entry.
        AndroidBuilder builder = getBuilder();

        builder.generateApkData(apk, outDir, getMainPkgName(), ANDROID_WEAR_MICRO_APK);
        AndroidBuilder.generateApkDataEntryInManifest(getMinSdkVersion(),
                getTargetSdkVersion(),
                getManifestFile());
    }

    @OutputDirectory
    public File getResOutputDir() {
        return resOutputDir;
    }

    public void setResOutputDir(File resOutputDir) {
        this.resOutputDir = resOutputDir;
    }

    @InputFile
    public File getApkFile() {
        return apkFile;
    }

    public void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

    @Input
    public String getMainPkgName() {
        return mainPkgName;
    }

    public void setMainPkgName(String mainPkgName) {
        this.mainPkgName = mainPkgName;
    }

    @Input
    public int getMinSdkVersion() {
        return minSdkVersion;
    }

    public void setMinSdkVersion(int minSdkVersion) {
        this.minSdkVersion = minSdkVersion;
    }

    @Input
    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public void setTargetSdkVersion(int targetSdkVersion) {
        this.targetSdkVersion = targetSdkVersion;
    }

    @OutputFile
    public File getManifestFile() {
        return manifestFile;
    }

    public void setManifestFile(File manifestFile) {
        this.manifestFile = manifestFile;
    }

    public static class ConfigAction implements TaskConfigAction<GenerateApkDataTask> {

        @NonNull
        VariantScope scope;

        @NonNull
        Configuration config;

        public ConfigAction(@NonNull VariantScope scope, @NonNull Configuration config) {
            this.scope = scope;
            this.config = config;
        }

        @Override
        @NonNull
        public String getName() {
            return scope.getTaskName("handle", "MicroApk");
        }

        @Override
        @NonNull
        public Class<GenerateApkDataTask> getType() {
            return GenerateApkDataTask.class;
        }

        @Override
        public void execute(GenerateApkDataTask task) {
            final ApkVariantData variantData = (ApkVariantData) scope.getVariantData();
            variantData.generateApkDataTask = task;

            task.setAndroidBuilder(scope.getGlobalScope().getAndroidBuilder());
            task.setVariantName(scope.getVariantConfiguration().getFullName());
            ConventionMappingHelper.map(task, "resOutputDir", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return new File(
                            scope.getGlobalScope().getGeneratedDir(),
                            "/res/microapk/"
                                    + variantData.getVariantConfiguration().getDirName());
                }
            });
            ConventionMappingHelper.map(task, "apkFile", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    // only care about the first one. There shouldn't be more anyway.
                    return config.getFiles().iterator().next();
                }
            });
            ConventionMappingHelper.map(task, "manifestFile", new Callable<File>() {
                @Override
                public File call() throws Exception {
                    return new File(
                            scope.getGlobalScope().getGeneratedDir(),
                            "/manifests/microapk/"
                                    + scope.getVariantData().getVariantConfiguration().getDirName()
                                    + "/" + FN_ANDROID_MANIFEST_XML);
                }
            });
            ConventionMappingHelper.map(task, "mainPkgName", new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return variantData.getVariantConfiguration().getApplicationId();
                }
            });

            ConventionMappingHelper.map(task, "minSdkVersion", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return variantData.getVariantConfiguration().getMinSdkVersion().getApiLevel();
                }
            });

            ConventionMappingHelper.map(task, "targetSdkVersion", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    return variantData.getVariantConfiguration().getTargetSdkVersion().getApiLevel();
                }
            });
        }
    }
}
