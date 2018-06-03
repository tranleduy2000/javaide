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
package com.android.build.gradle.tasks
import com.android.annotations.NonNull
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.builder.compiling.ResValueGenerator
import com.android.builder.core.VariantConfiguration
import com.android.builder.model.ClassField
import com.google.common.collect.Lists
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

@ParallelizableTask
public class GenerateResValues extends BaseTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File resOutputDir

    // ----- PRIVATE TASK API -----

    List<Object> items

    @Input
    List<String> getItemValues() {
        List<Object> resolvedItems = getItems()
        List<String> list = Lists.newArrayListWithCapacity(resolvedItems.size() * 3)

        for (Object object : resolvedItems) {
            if (object instanceof String) {
                list.add((String) object)
            } else if (object instanceof ClassField) {
                ClassField field = (ClassField) object
                list.add(field.type)
                list.add(field.name)
                list.add(field.value)
            }
        }

        return list
    }

    @TaskAction
    void generate() {
        File folder = getResOutputDir()
        List<Object> resolvedItems = getItems()

        if (resolvedItems.isEmpty()) {
            folder.deleteDir()
        } else {
            ResValueGenerator generator = new ResValueGenerator(folder)
            generator.addItems(getItems())

            generator.generate()
        }
    }


    public static class ConfigAction implements TaskConfigAction<GenerateResValues> {

        @NonNull
        VariantScope scope

        ConfigAction(@NonNull VariantScope scope) {
            this.scope = scope
        }

        @Override
        String getName() {
            return scope.getTaskName("generate", "ResValues");
        }

        @Override
        Class getType() {
            return GenerateResValues.class
        }

        @Override
        void execute(GenerateResValues generateResValuesTask) {
            scope.variantData.generateResValuesTask = generateResValuesTask

            VariantConfiguration variantConfiguration = scope.variantData.variantConfiguration

            generateResValuesTask.androidBuilder = scope.globalScope.androidBuilder
            generateResValuesTask.setVariantName(variantConfiguration.getFullName())

            ConventionMappingHelper.map(generateResValuesTask, "items") {
                variantConfiguration.resValues
            }

            ConventionMappingHelper.map(generateResValuesTask, "resOutputDir") {
                new File(scope.globalScope.generatedDir,
                        "res/resValues/${scope.variantData.variantConfiguration.dirName}")
            }
        }
    }
}
