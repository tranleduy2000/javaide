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
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.IncrementalTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.builder.core.VariantConfiguration
import com.android.builder.core.VariantType
import com.android.builder.model.AndroidProject
import com.android.ide.common.res2.AssetMerger
import com.android.ide.common.res2.AssetSet
import com.android.ide.common.res2.FileValidity
import com.android.ide.common.res2.MergedAssetWriter
import com.android.ide.common.res2.MergingException
import com.android.utils.FileUtils
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask

@ParallelizableTask
public class MergeAssets extends IncrementalTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File outputDir

    // ----- PRIVATE TASK API -----

    // fake input to detect changes. Not actually used by the task
    @InputFiles
    Iterable<File> getRawInputFolders() {
        return flattenSourceSets(getInputAssetSets())
    }

    // actual inputs
    List<AssetSet> inputAssetSets

    private final FileValidity<AssetSet> fileValidity = new FileValidity<AssetSet>();


    @Override
    protected void doFullTaskAction() {
        // this is full run, clean the previous output
        File destinationDir = getOutputDir()
        FileUtils.emptyFolder(destinationDir)

        List<AssetSet> assetSets = getInputAssetSets()

        // create a new merger and populate it with the sets.
        AssetMerger merger = new AssetMerger()

        try {
            for (AssetSet assetSet : assetSets) {
                // set needs to be loaded.
                assetSet.loadFromFiles(getILogger())
                merger.addDataSet(assetSet)
            }

            // get the merged set and write it down.
            MergedAssetWriter writer = new MergedAssetWriter(destinationDir)

            merger.mergeData(writer, false /*doCleanUp*/)

            // No exception? Write the known state.
            merger.writeBlobTo(getIncrementalFolder(), writer)
        } catch (MergingException e) {
            println e.getMessage()
            merger.cleanBlob(getIncrementalFolder())
            throw new ResourceException(e.getMessage(), e)
        }
    }


    public static class ConfigAction implements TaskConfigAction<MergeAssets> {

        @NonNull
        VariantScope scope

        ConfigAction(VariantScope scope) {
            this.scope = scope
        }

        @Override
        String getName() {
            return scope.getTaskName("merge", "Assets");
        }

        @Override
        Class<MergeAssets> getType() {
            return MergeAssets
        }

        @Override
        void execute(MergeAssets mergeAssetsTask) {
            BaseVariantData<? extends BaseVariantOutputData> variantData = scope.variantData
            VariantConfiguration variantConfig = variantData.variantConfiguration
            boolean includeDependencies = variantConfig.type != VariantType.LIBRARY

            variantData.mergeAssetsTask = mergeAssetsTask

            mergeAssetsTask.androidBuilder = scope.globalScope.androidBuilder
            mergeAssetsTask.setVariantName(variantConfig.getFullName())
            mergeAssetsTask.incrementalFolder =
                    new File(
                            "$scope.globalScope.buildDir/${AndroidProject.FD_INTERMEDIATES}/incremental/mergeAssets/${variantConfig.dirName}")

            ConventionMappingHelper.map(mergeAssetsTask, "inputAssetSets") {
                def generatedAssets = []
                if (variantData.copyApkTask != null) {
                    generatedAssets.add(variantData.copyApkTask.destinationDir)
                }
                variantConfig.getAssetSets(generatedAssets, includeDependencies)
            }
            ConventionMappingHelper.map(mergeAssetsTask, "outputDir") {
                scope.getMergeAssetsOutputDir()
            }
        }
    }
}
