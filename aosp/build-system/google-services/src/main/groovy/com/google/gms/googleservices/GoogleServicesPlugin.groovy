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

package com.google.gms.googleservices

import org.gradle.api.Plugin
import org.gradle.api.Project

class GoogleServicesPlugin implements Plugin<Project> {

    public final static String JSON_FILE_NAME = 'google-services.json'

    @Override
    void apply(Project project) {
        // setup this plugin no matter the order.
        if (!checkForKnownPlugins(project)) {
            project.plugins.whenPluginAdded {
                checkForKnownPlugins(project)
            }
        }
    }

    private void setupPlugin(Project project, boolean isLibrary) {
        if (isLibrary) {
            project.android.libraryVariants.all { variant ->
                handleVariant(project, variant)
            }
        } else {
            project.android.applicationVariants.all { variant ->
                handleVariant(project, variant)
            }
        }
    }

    private static void handleVariant(Project project, def variant) {
        File quickstartFile = project.file(JSON_FILE_NAME)
        File outputDir = project.file("$project.buildDir/generated/res/google-services/$variant.dirName")

        GoogleServicesTask task = project.tasks.create("process${variant.name.capitalize()}GoogleServices", GoogleServicesTask)

        task.quickstartFile = quickstartFile
        task.intermediateDir = outputDir
        task.packageName = variant.applicationId

        variant.registerResGeneratingTask(task, outputDir)
    }

    private boolean checkForKnownPlugins(Project project) {
        if (project.plugins.hasPlugin("android") ||
                project.plugins.hasPlugin("com.android.application")) {
            // this is a bit fragile but since this is internal usage this is ok
            // (another plugin could declare itself to be 'android')
            setupPlugin(project, false)
            return true
        } else if (project.plugins.hasPlugin("android-library") ||
                project.plugins.hasPlugin("com.android.library")) {
            // this is a bit fragile but since this is internal usage this is ok
            // (another plugin could declare itself to be 'android-library')
            setupPlugin(project, true)
            return true
        }
        return false
    }
}
