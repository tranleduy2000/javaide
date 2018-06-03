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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.build.gradle.api.ApkOutputFile;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.android.build.gradle.tasks.PackageApplication;
import com.android.build.gradle.tasks.SplitZipAlign;
import com.android.build.gradle.tasks.ZipAlign;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;

import org.gradle.api.Task;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Base output data for a variant that generates an APK file.
 */
public class ApkVariantOutputData extends BaseVariantOutputData {

    public PackageApplication packageApplicationTask;
    public ZipAlign zipAlignTask;
    public SplitZipAlign splitZipAlign;

    private TaskManager taskManager;
    private int versionCodeOverride = -1;
    private String versionNameOverride = null;

    public ApkVariantOutputData(
            @NonNull OutputFile.OutputType outputType,
            @NonNull Collection<FilterData> filters,
            @NonNull BaseVariantData variantData,
            @NonNull TaskManager taskManager) {
        super(outputType, filters, variantData);
        this.taskManager = taskManager;
    }

    @Override
    public void setOutputFile(@NonNull File file) {
        if (zipAlignTask != null) {
            zipAlignTask.setOutputFile(file);
        } else {
            packageApplicationTask.setOutputFile(file);
        }
    }

    @Nullable
    @Override
    public File getOutputFile() {
        if (zipAlignTask != null) {
            return zipAlignTask.getOutputFile();
        }

        return packageApplicationTask == null ? null : packageApplicationTask.getOutputFile();
    }

    @NonNull
    @Override
    public ImmutableList<ApkOutputFile> getOutputs() {
        ImmutableList.Builder<ApkOutputFile> outputs = ImmutableList.builder();
        outputs.add(getMainOutputFile());
        if (splitZipAlign != null) {
            outputs.addAll(splitZipAlign.getOutputSplitFiles());
        } else {
            if (packageSplitResourcesTask != null) {
                outputs.addAll(packageSplitResourcesTask.getOutputSplitFiles());
            }
        }
        return outputs.build();
    }

    @NonNull
    public ZipAlign createZipAlignTask(@NonNull String taskName, @NonNull File inputFile,
            @NonNull File outputFile) {
        //noinspection VariableNotUsedInsideIf
        if (zipAlignTask != null) {
            throw new RuntimeException(String.format(
                    "ZipAlign task for variant '%s' already exists.", variantData.getName()));
        }

        zipAlignTask = taskManager.createZipAlignTask(taskName, inputFile, outputFile);

        // setup dependencies
        assembleTask.dependsOn(zipAlignTask);

        return zipAlignTask;
    }

    @Override
    public int getVersionCode() {
        if (versionCodeOverride > 0) {
            return versionCodeOverride;
        }

        return variantData.getVariantConfiguration().getVersionCode();
    }

    @NonNull
    @Override
    public File getSplitFolder() {
        return getOutputFile().getParentFile();
    }

    public String getVersionName() {
        if (versionNameOverride != null) {
            return versionNameOverride;
        }

        return variantData.getVariantConfiguration().getVersionName();
    }

    public void setVersionCodeOverride(int versionCodeOverride) {
        this.versionCodeOverride = versionCodeOverride;
    }

    public int getVersionCodeOverride() {
        return versionCodeOverride;
    }

    public void setVersionNameOverride(String versionNameOverride) {
        this.versionNameOverride = versionNameOverride;
    }

    public String getVersionNameOverride() {
        return versionNameOverride;
    }

    /**
     * Returns the list of {@link Supplier} for this variant. Some variant can produce more
     * than one file when dealing with pure splits.
     * @return the complete list of tasks producing an APK for this variant.
     */
    public List<FileSupplier> getSplitOutputFileSuppliers() {
        ImmutableList.Builder<FileSupplier> tasks = ImmutableList.builder();
        if (splitZipAlign != null || packageSplitResourcesTask != null) {
            tasks.addAll(splitZipAlign == null ? packageSplitResourcesTask.getOutputFileSuppliers()
                : splitZipAlign.getOutputFileSuppliers());
        }
        // ABI splits zip are aligned together with the other densities in the splitZipAlign task
        // so only add the ABI splits from the package task if there was no splitZipAlign task.
        if (packageSplitAbiTask != null && splitZipAlign == null) {
            tasks.addAll(packageSplitAbiTask.getOutputFileSuppliers());
        }
        return tasks.build();
    }

    @Nullable
    public FileSupplier getMetadataFile() throws IOException {

        if (splitZipAlign == null) {
            return null;
        }
        return new FileSupplier() {
            @NonNull
            @Override
            public Task getTask() {
                return splitZipAlign;
            }

            @Override
            public File get() {
                return splitZipAlign.getApkMetadataFile();
            }
        };
    }
}
