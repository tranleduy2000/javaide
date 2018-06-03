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

package com.android.build.gradle.internal.coverage

import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.PostCompilationData
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES

/**
 * Simple Jacoco instrument task that calls the Ant version.
 */
public class JacocoInstrumentTask extends DefaultTask {

    @InputDirectory
    File inputDir

    @OutputDirectory
    File outputDir

    /**
     * Classpath containing Jacoco classes for use by the task.
     */
    @InputFiles
    FileCollection jacocoClasspath

    @TaskAction
    void instrument() {
        File outDir = getOutputDir()
        outDir.deleteDir()
        outDir.mkdirs()

        getAnt().taskdef(name: 'instrumentWithJacoco',
                         classname: 'org.jacoco.ant.InstrumentTask',
                         classpath: getJacocoClasspath().asPath)
        getAnt().instrumentWithJacoco(destdir: outDir) {
            fileset(dir: getInputDir())
        }
    }

    public static class ConfigAction implements TaskConfigAction<JacocoInstrumentTask> {

        VariantScope scope;

        PostCompilationData pcData;

        ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope
            this.pcData = pcData
        }

        @Override
        String getName() {
            return scope.getTaskName("instrument");
        }

        @Override
        Class<JacocoInstrumentTask> getType() {
            return JacocoInstrumentTask.class
        }

        @Override
        void execute(JacocoInstrumentTask jacocoTask) {

            ConventionMappingHelper.map(jacocoTask, "jacocoClasspath") {
                scope.globalScope.project.configurations[JacocoPlugin.ANT_CONFIGURATION_NAME]
            }
            // can't directly use the existing inputFiles closure as we need the dir instead :\
            ConventionMappingHelper.map(jacocoTask, "inputDir", pcData.inputDirCallable)
            ConventionMappingHelper.map(jacocoTask, "outputDir") {
                new File("${scope.globalScope.buildDir}/${FD_INTERMEDIATES}/coverage-instrumented-classes/${scope.variantConfiguration.dirName}")
            }
            scope.variantData.jacocoInstrumentTask = jacocoTask
        }
    }
}
