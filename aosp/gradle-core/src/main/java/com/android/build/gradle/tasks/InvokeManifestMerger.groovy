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

import com.android.build.gradle.internal.LoggerWrapper
import com.android.build.gradle.internal.tasks.DefaultAndroidTask
import com.android.manifmerger.ManifestMerger2
import com.android.manifmerger.MergingReport
import com.android.utils.ILogger
import com.google.common.base.Supplier
import org.apache.tools.ant.BuildException
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.ParallelizableTask
import org.gradle.api.tasks.TaskAction

/**
 * Simple task to invoke the new Manifest Merger without any injection, features, system properties
 * or overlay manifests
 */
@ParallelizableTask
class InvokeManifestMerger extends DefaultAndroidTask implements Supplier<File> {

    @InputFile
    File mainManifestFile;

    @InputFiles
    List<File> secondaryManifestFiles

    @OutputFile
    File outputFile

    @TaskAction
    protected void doFullTaskAction() {
        ILogger iLogger = new LoggerWrapper(getLogger());
        ManifestMerger2.Invoker mergerInvoker = ManifestMerger2.
                newMerger(getMainManifestFile(), iLogger, ManifestMerger2.MergeType.APPLICATION)
        mergerInvoker.addLibraryManifests(secondaryManifestFiles.toArray(new File[secondaryManifestFiles.size()]))
        MergingReport mergingReport = mergerInvoker.merge();
        if (mergingReport.result.isError()) {
            getLogger().error(mergingReport.reportString);
            mergingReport.log(iLogger);
            throw new BuildException(mergingReport.reportString);
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(getOutputFile())
            fileWriter.append(mergingReport.getMergedDocument().get().prettyPrint())
        } finally {
            if (fileWriter != null) {
                fileWriter.close()
            }
        }
    }

    @Override
    File get() {
        return getOutputFile()
    }
}
