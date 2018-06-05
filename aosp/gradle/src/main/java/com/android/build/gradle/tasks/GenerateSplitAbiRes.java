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

import com.android.build.gradle.internal.dsl.AaptOptions;
import com.android.build.gradle.internal.tasks.BaseTask;
import com.android.builder.core.AaptPackageProcessBuilder;
import com.android.ide.common.process.LoggedProcessOutputHandler;
import com.android.ide.common.process.ProcessException;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFiles;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generates all metadata (like AndroidManifest.xml) necessary for a ABI dimension split APK.
 */
@ParallelizableTask
public class GenerateSplitAbiRes extends BaseTask {

    private String applicationId;

    private int versionCode;

    private String versionName;

    private String outputBaseName;

    private Set<String> splits;

    private File outputDirectory;

    private boolean debuggable;

    private AaptOptions aaptOptions;

    @OutputFiles
    public List<File> getOutputFiles() {
        List<File> outputFiles = new ArrayList<File>();
        for (String split : getSplits()) {
            outputFiles.add(getOutputFileForSplit(split));
        }

        return outputFiles;
    }

    @TaskAction
    protected void doFullTaskAction() throws IOException, InterruptedException, ProcessException {

        for (String split : getSplits()) {
            String resPackageFileName = getOutputFileForSplit(split).getAbsolutePath();

            File tmpDirectory = new File(getOutputDirectory(), getOutputBaseName());
            tmpDirectory.mkdirs();

            File tmpFile = new File(tmpDirectory, "AndroidManifest.xml");

            String versionNameToUse = getVersionName();
            if (versionNameToUse == null) {
                versionNameToUse = String.valueOf(getVersionCode());
            }

            OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(tmpFile), "UTF-8");
            try {
                fileWriter.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        + "      package=\"" + getApplicationId() + "\"\n"
                        + "      android:versionCode=\"" + getVersionCode() + "\"\n"
                        + "      android:versionName=\"" + versionNameToUse + "\"\n"
                        + "      split=\"lib_" + getOutputBaseName() + "\">\n"
                        + "       <uses-sdk android:minSdkVersion=\"21\"/>\n" + "</manifest> ");
                fileWriter.flush();
            } finally {
                fileWriter.close();
            }

            AaptPackageProcessBuilder aaptPackageCommandBuilder =
                    new AaptPackageProcessBuilder(tmpFile, getAaptOptions())
                        .setDebuggable(isDebuggable())
                        .setResPackageOutput(resPackageFileName);

            getBuilder().processResources(
                    aaptPackageCommandBuilder,
                    false /* enforceUniquePackageName */,
                    new LoggedProcessOutputHandler(getILogger()));
        }
    }

    private File getOutputFileForSplit(final String split) {
        return new File(getOutputDirectory(),
                "resources-" + getOutputBaseName() + "-" + split + ".ap_");
    }

    @Input
    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    @Input
    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    @Input
    @Optional
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Input
    public String getOutputBaseName() {
        return outputBaseName;
    }

    public void setOutputBaseName(String outputBaseName) {
        this.outputBaseName = outputBaseName;
    }

    @Input
    public Set<String> getSplits() {
        return splits;
    }

    public void setSplits(Set<String> splits) {
        this.splits = splits;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Input
    public boolean isDebuggable() {
        return debuggable;
    }

    public void setDebuggable(boolean debuggable) {
        this.debuggable = debuggable;
    }

    @Nested
    public AaptOptions getAaptOptions() {
        return aaptOptions;
    }

    public void setAaptOptions(AaptOptions aaptOptions) {
        this.aaptOptions = aaptOptions;
    }
}
