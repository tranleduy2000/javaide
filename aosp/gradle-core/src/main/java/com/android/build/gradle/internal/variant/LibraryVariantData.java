/*
 * Copyright (C) 2013 The Android Open Source Project
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
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.tasks.ExtractAnnotations;
import com.android.builder.core.VariantType;
import com.google.common.collect.Maps;

import org.gradle.api.Task;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Data about a variant that produce a Library bundle (.aar)
 */
public class LibraryVariantData extends BaseVariantData<LibVariantOutputData> implements TestedVariantData {

    private final Map<VariantType, TestVariantData> testVariants;

    @Nullable
    public ExtractAnnotations generateAnnotationsTask = null;

    public LibraryVariantData(
            @NonNull AndroidConfig androidConfig,
            @NonNull TaskManager taskManager,
            @NonNull GradleVariantConfiguration config) {
        super(androidConfig, taskManager, config);
        testVariants = Maps.newEnumMap(VariantType.class);

        // create default output
        createOutput(OutputFile.OutputType.MAIN,
                Collections.<FilterData>emptyList());
    }

    @NonNull
    @Override
    protected LibVariantOutputData doCreateOutput(
            OutputFile.OutputType splitOutput,
            Collection<FilterData> filters) {
        return new LibVariantOutputData(splitOutput, filters, this);
    }

    @Override
    @NonNull
    public String getDescription() {
        if (getVariantConfiguration().hasFlavors()) {
            return String.format("%s build for flavor %s",
                    getCapitalizedBuildTypeName(),
                    getCapitalizedFlavorName());
        } else {
            return String.format("%s build", getCapitalizedBuildTypeName());
        }
    }

    @Nullable
    @Override
    public TestVariantData getTestVariantData(@NonNull VariantType type) {
        return testVariants.get(type);
    }

    @Override
    public void setTestVariantData(
            @NonNull TestVariantData testVariantData,
            VariantType type) {
        testVariants.put(type, testVariantData);
    }

    // Overridden to add source folders to a generateAnnotationsTask, if it exists.
    @Override
    public void registerJavaGeneratingTask(
            @NonNull Task task, @NonNull File... generatedSourceFolders) {
        super.registerJavaGeneratingTask(task, generatedSourceFolders);
        if (generateAnnotationsTask != null) {
            for (File f : generatedSourceFolders) {
                generateAnnotationsTask.source(f);
            }
        }
    }

    // Overridden to add source folders to a generateAnnotationsTask, if it exists.
    @Override
    public void registerJavaGeneratingTask(
            @NonNull Task task, @NonNull Collection<File> generatedSourceFolders) {
        super.registerJavaGeneratingTask(task, generatedSourceFolders);
        if (generateAnnotationsTask != null) {
            for (File f : generatedSourceFolders) {
                generateAnnotationsTask.source(f);
            }
        }
    }
}
