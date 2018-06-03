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
import com.android.build.gradle.internal.scope.VariantOutputScope
import com.android.build.gradle.internal.tasks.DefaultAndroidTask
import com.android.resources.Density
import com.google.common.base.Charsets
import com.google.common.io.Files
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Task to generate a manifest snippet that just contains a compatible-screens
 * node with the given density and the given list of screen sizes.

 */
class CompatibleScreensManifest extends DefaultAndroidTask {

    @Input
    String screenDensity

    @Input
    Set<String> screenSizes

    @OutputFile
    File manifestFile

    @TaskAction
    void generate() {
        StringBuilder content = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                "    package=\"\">\n" +
                "\n" +
                "    <compatible-screens>\n")

        String density = getScreenDensity()

        // convert unsupported values to numbers.
        density = convert(density, Density.XXHIGH, Density.XXXHIGH);

        for (String size : getScreenSizes()) {
            content.append(
                "        <screen android:screenSize=\"$size\" android:screenDensity=\"$density\" />\n")
        }

        content.append(
                "    </compatible-screens>\n" +
                "</manifest>")

        Files.write(content.toString(), getManifestFile(), Charsets.UTF_8);
    }

    private static String convert(@NonNull String density, @NonNull Density... densitiesToConvert) {
        for (Density densityToConvert : densitiesToConvert) {
            if (densityToConvert.getResourceValue().equals(density)) {
                return Integer.toString(densityToConvert.dpiValue);
            }
        }

        return density;
    }

    public static class ConfigAction implements TaskConfigAction<CompatibleScreensManifest> {

        @NonNull
        VariantOutputScope scope
        @NonNull
        Set<String> screenSizes

        ConfigAction(
                @NonNull VariantOutputScope scope,
                @NonNull Set<String> screenSizes) {
            this.scope = scope
            this.screenSizes = screenSizes
        }

        @Override
        String getName() {
            return scope.getTaskName("create", "CompatibleScreenManifest")
        }

        @Override
        Class<CompatibleScreensManifest> getType() {
            return CompatibleScreensManifest.class
        }

        @Override
        void execute(CompatibleScreensManifest csmTask) {
            csmTask.setVariantName(scope.getVariantScope().getVariantConfiguration().getFullName())

            csmTask.screenDensity = scope.variantOutputData.getMainOutputFile().getFilter(
                    com.android.build.OutputFile.DENSITY)
            csmTask.screenSizes = screenSizes

            ConventionMappingHelper.map(csmTask, "manifestFile") {
                scope.getCompatibleScreensManifestFile()
            }
        }
    }
}
