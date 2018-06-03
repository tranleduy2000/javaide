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



package com.android.build.gradle.internal.tasks.multidex

import com.android.build.gradle.internal.PostCompilationData
import com.android.build.gradle.internal.scope.ConventionMappingHelper
import com.android.build.gradle.internal.scope.TaskConfigAction
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.tasks.DefaultAndroidTask
import com.google.common.base.Charsets
import com.google.common.base.Joiner
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import com.google.common.io.Files
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES

/**
 * Take a list of classes for the main dex (that was computed before obfuscation),
 * a proguard-generated mapping file and create a new list of classes with the new
 * obfuscated names.
 */
class RetraceMainDexList extends DefaultAndroidTask {

    @InputFile
    File mainDexListFile

    @OutputFile
    File outputFile

    /**
     * Gradle doesn't really handle optional inputs as being a file that
     * doesn't exist. Optional means the task field is null. So we do some
     * custom logic to return null if the file doesn't exist since we cannot
     * know ahead of time without parsing the proguard config rule files.
     */
    @InputFile @Optional
    File getMappingFileInput() {
        File file = getMappingFile()
        if (file != null && file.isFile()) {
            return file
        }

        return null
    }

    File mappingFile

    @TaskAction
    void retrace() {

        File mapping = getMappingFile()
        // if there is no mapping file or if it doesn't exist, then we just copy from the main
        // dex list ot the output.
        if (mapping == null || !mapping.isFile()) {
            Files.copy(getMainDexListFile(), getOutputFile())
            return
        }

        // load the main class names
        List<String> classes = Files.readLines(getMainDexListFile(), Charsets.UTF_8)

        // load the mapping file and create a dictionary
        List<String> mappingLines = Files.readLines(mapping, Charsets.UTF_8)
        Map<String, String> map = createDict(mappingLines)

        // create the deobfuscated class list. This is a set to detect dups
        // You can have the same class coming from the non-obfuscated list and from
        // the shrinked jar that have different names until we remap them.
        Set<String> deobfuscatedClasses = Sets.newHashSetWithExpectedSize(classes.size())

        for (String clazz : classes) {
            String fullName = map.get(clazz)

            deobfuscatedClasses.add(fullName != null ? fullName : clazz)
        }

        String fileContent =
                Joiner.on(System.getProperty("line.separator")).join(deobfuscatedClasses)
        Files.write(fileContent, getOutputFile(), Charsets.UTF_8)
    }

    static Map<String, String> createDict(List<String> lines) {
        Map<String, String> map = Maps.newHashMap()

        for (String line : lines) {
            if (line.startsWith(" ")) {
                continue
            }

            int pos = line.indexOf(" -> ")
            if (pos == -1) {
                throw new RuntimeException("unable to read mapping file.")
            }

            String fullName = line.substring(0, pos)
            String obfuscatedName = line.substring(pos + 4, line.size() - 1)

            map.put(obfuscatedName.replace(".", "/") + ".class",
                    fullName.replace(".", "/") + ".class")
        }

        return map
    }


    public static class ConfigAction implements TaskConfigAction<RetraceMainDexList> {

        VariantScope scope;

        PostCompilationData pcData;

        ConfigAction(VariantScope scope, PostCompilationData pcData) {
            this.scope = scope
            this.pcData = pcData
        }

        @Override
        String getName() {
            return scope.getTaskName("retrace", "MainDexClassList");
        }

        @Override
        Class<RetraceMainDexList> getType() {
            return RetraceMainDexList.class
        }

        @Override
        void execute(RetraceMainDexList retraceTask) {
            retraceTask.setVariantName(scope.getVariantConfiguration().getFullName())
            ConventionMappingHelper.map(retraceTask, "mainDexListFile") { scope.getMainDexListFile() }
            ConventionMappingHelper.map(retraceTask, "mappingFile") {
                scope.variantData.getMappingFile()
            }
            retraceTask.outputFile = new File(
                    "${scope.globalScope.buildDir}/${FD_INTERMEDIATES}/multi-dex/" +
                            "${scope.variantConfiguration.dirName}/maindexlist_deobfuscated.txt")

        }
    }
}
