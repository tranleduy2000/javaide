/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.build.gradle.tasks

import com.android.annotations.NonNull
import com.android.build.gradle.internal.dependency.ManifestDependencyImpl
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantOutputScope
import com.android.build.gradle.internal.variant.ApkVariantOutputData
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.builder.core.VariantConfiguration
import com.android.builder.dependency.LibraryDependency
import com.android.manifmerger.ManifestMerger2
import com.google.common.collect.Lists
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.ParallelizableTask

import static com.android.builder.model.AndroidProject.FD_OUTPUTS

/**
 * A task that processes the manifest
 */
@ParallelizableTask
public class MergeManifests extends ManifestProcessorTask {

    // ----- PRIVATE TASK API -----
    @InputFile
    File getMainManifest() {
        return variantConfiguration.getMainManifest();
    }

    @InputFiles
    List<File> getManifestOverlays() {
        return variantConfiguration.getManifestOverlays();
    }

    @Input @Optional
    String getPackageOverride() {
        return variantConfiguration.getIdOverride();
    }

    @Input
    int getVersionCode() {
        if (variantOutputData!= null) {
            return variantOutputData.versionCode
        }

        return variantConfiguration.versionCode;
    }

    @Input @Optional
    String getVersionName() {
        if (variantOutputData!= null) {
            return variantOutputData.versionName
        }
        return variantConfiguration.getVersionName();
    }

    @Input @Optional
    String minSdkVersion

    @Input @Optional
    String targetSdkVersion

    @Input @Optional
    Integer maxSdkVersion

    @Input @Optional
    File reportFile

    /**
     * Return a serializable version of our map of key value pairs for placeholder substitution.
     * This serialized form is only used by gradle to compare past and present tasks to determine
     * whether a task need to be re-run or not.
     */
    @Input @Optional
    String getManifestPlaceholders() {
        return serializeMap(variantConfiguration.getManifestPlaceholders());
    }

    VariantConfiguration variantConfiguration
    ApkVariantOutputData variantOutputData
    List<ManifestDependencyImpl> libraries

    /**
     * since libraries above can't return it's input files (@Nested doesn't
     * work on lists), so do a method that will gather them and return them.
     */
    @InputFiles
    List<File> getLibraryManifests() {
        List<ManifestDependencyImpl> libs = getLibraries()
        if (libs == null || libs.isEmpty()) {
            return Collections.emptyList();
        }

        List<File> files = Lists.newArrayListWithCapacity(libs.size() * 2)
        for (ManifestDependencyImpl mdi : libs) {
            files.addAll(mdi.getAllManifests())
        }

        return files;
    }

    @Override
    protected void doFullTaskAction() {

        getBuilder().mergeManifests(
                getMainManifest(),
                getManifestOverlays(),
                getLibraries(),
                getPackageOverride(),
                getVersionCode(),
                getVersionName(),
                getMinSdkVersion(),
                getTargetSdkVersion(),
                getMaxSdkVersion(),
                getManifestOutputFile().absolutePath,
                // no appt friendly merged manifest file necessary for applications.
                null /* aaptFriendlyManifestOutputFile */ ,
                ManifestMerger2.MergeType.APPLICATION,
                variantConfiguration.getManifestPlaceholders(),
                getReportFile())
    }

    // ----- ConfigAction -----

    public static class ConfigAction implements TaskConfigAction<MergeManifests> {

        VariantOutputScope scope

        ConfigAction(VariantOutputScope scope) {
            this.scope = scope
        }

        @Override
        String getName() {
            return scope.getTaskName("process", "Manifest")
        }

        @Override
        Class<MergeManifests> getType() {
            return MergeManifests
        }

        @Override
        void execute(MergeManifests processManifestTask) {
            BaseVariantOutputData variantOutputData = scope.variantOutputData

            BaseVariantData<? extends BaseVariantOutputData> variantData =
                    scope.variantScope.variantData
            VariantConfiguration config = variantData.getVariantConfiguration()

            variantOutputData.manifestProcessorTask = processManifestTask

            processManifestTask.androidBuilder = scope.globalScope.androidBuilder
            processManifestTask.setVariantName(config.getFullName())

            processManifestTask.dependsOn variantData.prepareDependenciesTask
            if (variantData.generateApkDataTask != null) {
                processManifestTask.dependsOn variantData.generateApkDataTask
            }
            if (scope.compatibleScreensManifestTask != null) {
                processManifestTask.dependsOn scope.compatibleScreensManifestTask.name
            }

            processManifestTask.variantConfiguration = config
            if (variantOutputData instanceof ApkVariantOutputData) {
                processManifestTask.variantOutputData =
                        variantOutputData as ApkVariantOutputData
            }

            ConventionMappingHelper.map(processManifestTask, "libraries") {
                List<ManifestDependencyImpl> manifests =
                        getManifestDependencies(config.directLibraries)

                if (variantData.generateApkDataTask != null &&
                        variantData.getVariantConfiguration().getBuildType().
                                isEmbedMicroApp()) {
                    manifests.add(new ManifestDependencyImpl(
                            variantData.generateApkDataTask.getManifestFile(), []))
                }

                if (scope.compatibleScreensManifestTask != null) {
                    manifests.add(new ManifestDependencyImpl(
                            scope.getCompatibleScreensManifestFile(), []))
                }

                return manifests
            }

            ConventionMappingHelper.map(processManifestTask, "minSdkVersion") {
                if (scope.globalScope.androidBuilder.isPreviewTarget()) {
                    return scope.globalScope.androidBuilder.getTargetCodename()
                }

                config.mergedFlavor.minSdkVersion?.apiString
            }

            ConventionMappingHelper.map(processManifestTask, "targetSdkVersion") {
                if (scope.globalScope.androidBuilder.isPreviewTarget()) {
                    return scope.globalScope.androidBuilder.getTargetCodename()
                }

                return config.mergedFlavor.targetSdkVersion?.apiString
            }

            ConventionMappingHelper.map(processManifestTask, "maxSdkVersion") {
                if (scope.globalScope.androidBuilder.isPreviewTarget()) {
                    return null
                }

                return config.mergedFlavor.maxSdkVersion
            }

            ConventionMappingHelper.map(processManifestTask, "manifestOutputFile") {
                scope.getManifestOutputFile()
            }

            ConventionMappingHelper.map(processManifestTask, "reportFile") {
                new File(
                        "${scope.getGlobalScope().getBuildDir()}/${FD_OUTPUTS}/logs/manifest-merger-${config.baseName}-report.txt")
            }

        }

        @NonNull
        private static List<ManifestDependencyImpl> getManifestDependencies(
                List<LibraryDependency> libraries) {

            List<ManifestDependencyImpl> list = Lists.newArrayListWithCapacity(libraries.size())

            for (LibraryDependency lib : libraries) {
                // get the dependencies
                List<ManifestDependencyImpl> children = getManifestDependencies(lib.dependencies)
                list.add(new ManifestDependencyImpl(lib.getName(), lib.manifest, children))
            }

            return list
        }
    }
}
