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
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.NdkTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.builder.model.ApiVersion
import com.android.builder.model.ProductFlavor
import com.android.ide.common.process.LoggedProcessOutputHandler
import com.android.sdklib.SdkVersionInfo
import com.android.utils.FileUtils
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static com.android.builder.model.AndroidProject.FD_GENERATED
import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES
/**
 * Task to compile Renderscript files. Supports incremental update.
 */
public class RenderscriptCompile extends NdkTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File sourceOutputDir

    @OutputDirectory
    File resOutputDir

    @OutputDirectory
    File objOutputDir

    @OutputDirectory
    File libOutputDir


    // ----- PRIVATE TASK API -----
    @Input
    String getBuildToolsVersion() {
        getBuildTools().getRevision()
    }

    @InputFiles
    List<File> sourceDirs

    @InputFiles
    List<File> importDirs

    @Input
    Integer targetApi

    @Input
    boolean supportMode

    @Input
    int optimLevel

    @Input
    boolean debugBuild

    @Input
    boolean ndkMode

    @TaskAction
    void taskAction() throws IOException {
        // this is full run (always), clean the previous outputs
        File sourceDestDir = getSourceOutputDir()
        FileUtils.emptyFolder(sourceDestDir)

        File resDestDir = getResOutputDir()
        FileUtils.emptyFolder(resDestDir)

        File objDestDir = getObjOutputDir()
        FileUtils.emptyFolder(objDestDir)

        File libDestDir = getLibOutputDir()
        FileUtils.emptyFolder(libDestDir)

        // get the import folders. If the .rsh files are not directly under the import folders,
        // we need to get the leaf folders, as this is what llvm-rs-cc expects.
        List<File> importFolders = getBuilder().getLeafFolders("rsh",
                getImportDirs(), getSourceDirs())

        getBuilder().compileAllRenderscriptFiles(
                getSourceDirs(),
                importFolders,
                sourceDestDir,
                resDestDir,
                objDestDir,
                libDestDir,
                getTargetApi(),
                getDebugBuild(),
                getOptimLevel(),
                getNdkMode(),
                getSupportMode(),
                getNdkConfig()?.abiFilters,
                new LoggedProcessOutputHandler(getILogger()))
    }

    // ----- ConfigAction -----

    public static class ConfigAction implements TaskConfigAction<RenderscriptCompile> {

        @NonNull
        VariantScope scope

        ConfigAction(VariantScope scope) {
            this.scope = scope
        }

        @Override
        String getName() {
            return scope.getTaskName("compile", "Renderscript");
        }

        @Override
        Class<RenderscriptCompile> getType() {
            return RenderscriptCompile
        }

        @Override
        void execute(RenderscriptCompile renderscriptTask) {
            BaseVariantData<? extends BaseVariantOutputData> variantData = scope.variantData
            GradleVariantConfiguration config = variantData.variantConfiguration

            variantData.renderscriptCompileTask = renderscriptTask
            ProductFlavor mergedFlavor = config.mergedFlavor
            boolean ndkMode = config.renderscriptNdkModeEnabled
            renderscriptTask.androidBuilder = scope.globalScope.androidBuilder
            renderscriptTask.setVariantName(config.getFullName())

            ConventionMappingHelper.map(renderscriptTask, "targetApi") {
                int targetApi = mergedFlavor.renderscriptTargetApi != null ?
                        mergedFlavor.renderscriptTargetApi : -1
                ApiVersion apiVersion = config.getMinSdkVersion()
                if (apiVersion != null) {
                    int minSdk = apiVersion.apiLevel
                    if (apiVersion.codename != null) {
                        minSdk = SdkVersionInfo.getApiByBuildCode(apiVersion.codename, true)
                    }

                    return targetApi > minSdk ? targetApi : minSdk
                }

                return targetApi
            }

            renderscriptTask.supportMode = config.renderscriptSupportModeEnabled
            renderscriptTask.ndkMode = ndkMode
            renderscriptTask.debugBuild = config.buildType.renderscriptDebuggable
            renderscriptTask.optimLevel = config.buildType.renderscriptOptimLevel

            ConventionMappingHelper.map(renderscriptTask, "sourceDirs") { config.renderscriptSourceList }
            ConventionMappingHelper.map(renderscriptTask, "importDirs") { config.renderscriptImports }

            ConventionMappingHelper.map(renderscriptTask, "sourceOutputDir") {
                new File(
                        "$scope.globalScope.buildDir/${FD_GENERATED}/source/rs/${variantData.variantConfiguration.dirName}")
            }
            ConventionMappingHelper.map(renderscriptTask, "resOutputDir") {
                scope.getRenderscriptResOutputDir()
            }
            ConventionMappingHelper.map(renderscriptTask, "objOutputDir") {
                new File(
                        "$scope.globalScope.buildDir/${FD_INTERMEDIATES}/rs/${variantData.variantConfiguration.dirName}/obj")
            }
            ConventionMappingHelper.map(renderscriptTask, "libOutputDir") {
                new File(
                        "$scope.globalScope.buildDir/${FD_INTERMEDIATES}/rs/${variantData.variantConfiguration.dirName}/lib")
            }
            ConventionMappingHelper.map(renderscriptTask, "ndkConfig") { config.ndkConfig }
        }
    }
}
