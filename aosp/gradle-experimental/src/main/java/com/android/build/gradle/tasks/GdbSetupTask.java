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
package com.android.build.gradle.tasks;

import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.core.Abi;
import com.android.build.gradle.managed.NdkConfig;
import com.android.build.gradle.ndk.internal.StlConfiguration;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.language.c.CSourceSet;
import org.gradle.language.cpp.CppSourceSet;
import org.gradle.nativeplatform.NativeBinarySpec;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Task to create gdb.setup for native code debugging.
 */
public class GdbSetupTask extends DefaultTask {

    private NdkHandler ndkHandler;

    private NdkConfig extension;

    private NativeBinarySpec binary;

    private File outputDir;


    // ----- PUBLIC TASK API -----

    @Input
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    // ----- PRIVATE TASK API -----

    @Input
    public NdkHandler getNdkHandler() {
        return ndkHandler;
    }

    public void setNdkHandler(NdkHandler ndkHandler) {
        this.ndkHandler = ndkHandler;
    }

    @Input
    public NdkConfig getExtension() {
        return extension;
    }

    public void setExtension(NdkConfig extension) {
        this.extension = extension;
    }

    @Input
    public NativeBinarySpec getBinary() {
        return binary;
    }

    public void setBinary(NativeBinarySpec binary) {
        this.binary = binary;
    }

    @TaskAction
    public void taskAction() {
        File gdbSetupFile = new File(outputDir, "gdb.setup");

        StringBuilder sb = new StringBuilder();

        sb.append("set solib-search-path ")
                .append(outputDir.toString())
                .append("\n")
                .append("directory ")
                .append(ndkHandler.getSysroot(Abi.getByName(binary.getTargetPlatform().getName())))
                .append("/usr/include ");

        final Set<String> sources = Sets.newHashSet();
        binary.getSource().withType(CSourceSet.class, new Action<CSourceSet>() {
            @Override
            public void execute(CSourceSet sourceSet) {
                for (File src : sourceSet.getSource().getSrcDirs()) {
                    sources.add(src.toString());
                }
            }
        });
        binary.getSource().withType(CppSourceSet.class, new Action<CppSourceSet>() {
            @Override
            public void execute(CppSourceSet sourceSet) {
                for (File src : sourceSet.getSource().getSrcDirs()) {
                    sources.add(src.toString());
                }
            }
        });
        sources.addAll(StlConfiguration.getStlSources(ndkHandler, extension.getStl()));
        sb.append(Joiner.on(' ').join(sources));

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        try {
            Files.write(sb.toString(), gdbSetupFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
