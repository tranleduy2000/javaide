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
import com.android.build.gradle.internal.tasks.BaseTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.internal.variant.BaseVariantOutputData
import com.android.builder.compiling.BuildConfigGenerator
import com.android.builder.core.VariantConfiguration
import com.android.builder.model.ClassField
import com.android.utils.FileUtils
import com.google.common.base.Strings
import com.google.common.collect.Lists
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

@ParallelizableTask
public class GenerateBuildConfig extends BaseTask {

    // ----- PUBLIC TASK API -----

    @OutputDirectory
    File sourceOutputDir

    // ----- PRIVATE TASK API -----

    @Input
    String buildConfigPackageName

    @Input
    String appPackageName

    @Input
    boolean debuggable

    @Input
    String flavorName

    @Input
    List<String> flavorNamesWithDimensionNames

    @Input
    String buildTypeName

    @Input
    @Optional
    String versionName

    @Input
    int versionCode

    List<Object> items;

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
    void generate() throws IOException {
        // must clear the folder in case the packagename changed, otherwise,
        // there'll be two classes.
        File destinationDir = getSourceOutputDir()
        FileUtils.emptyFolder(destinationDir)

        BuildConfigGenerator generator = new BuildConfigGenerator(
                getSourceOutputDir(),
                getBuildConfigPackageName());

        // Hack (see IDEA-100046): We want to avoid reporting "condition is always true"
        // from the data flow inspection, so use a non-constant value. However, that defeats
        // the purpose of this flag (when not in debug mode, if (BuildConfig.DEBUG && ...) will
        // be completely removed by the compiler), so as a hack we do it only for the case
        // where debug is true, which is the most likely scenario while the user is looking
        // at source code.
        //map.put(PH_DEBUG, Boolean.toString(mDebug));
        generator.addField("boolean", "DEBUG",
                getDebuggable() ? "Boolean.parseBoolean(\"true\")" : "false")
                .addField("String", "APPLICATION_ID", "\"${getAppPackageName()}\"")
                .addField("String", "BUILD_TYPE", "\"${getBuildTypeName()}\"")
                .addField("String", "FLAVOR", "\"${getFlavorName()}\"")
                .addField("int", "VERSION_CODE", Integer.toString(getVersionCode()))
                .addField("String", "VERSION_NAME", "\"${Strings.nullToEmpty(getVersionName())}\"")
                .addItems(getItems())

        List<String> flavors = getFlavorNamesWithDimensionNames()
        int count = flavors.size()
        if (count > 1) {
            for (int i = 0; i < count; i += 2) {
                generator.
                        addField("String", "FLAVOR_${flavors.get(i + 1)}", "\"${flavors.get(i)}\"")
            }
        }

        generator.generate()
    }

    // ----- Config Action -----

    public static class ConfigAction implements TaskConfigAction<GenerateBuildConfig> {

        @NonNull
        VariantScope scope

        ConfigAction(@NonNull VariantScope scope) {
            this.scope = scope
        }

        @Override
        @NonNull
        String getName() {
            return scope.getTaskName("generate", "BuildConfig");
        }

        @Override
        @NonNull
        Class<GenerateBuildConfig> getType() {
            return GenerateBuildConfig
        }


        @Override
        void execute(GenerateBuildConfig generateBuildConfigTask) {
            BaseVariantData<? extends BaseVariantOutputData> variantData = scope.variantData

            variantData.generateBuildConfigTask = generateBuildConfigTask

            VariantConfiguration variantConfiguration = variantData.variantConfiguration

            generateBuildConfigTask.androidBuilder = scope.globalScope.androidBuilder
            generateBuildConfigTask.setVariantName(scope.getVariantConfiguration().getFullName())

            ConventionMappingHelper.map(generateBuildConfigTask, "buildConfigPackageName") {
                variantConfiguration.originalApplicationId
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "appPackageName") {
                variantConfiguration.applicationId
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "versionName") {
                variantConfiguration.versionName
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "versionCode") {
                variantConfiguration.versionCode
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "debuggable") {
                variantConfiguration.buildType.isDebuggable()
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "buildTypeName") {
                variantConfiguration.buildType.name
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "flavorName") {
                variantConfiguration.flavorName
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "flavorNamesWithDimensionNames") {
                variantConfiguration.flavorNamesWithDimensionNames
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "items") {
                variantConfiguration.buildConfigItems
            }

            ConventionMappingHelper.map(generateBuildConfigTask, "sourceOutputDir") {
                scope.getBuildConfigSourceOutputDir()
            }
        }
    }
}
