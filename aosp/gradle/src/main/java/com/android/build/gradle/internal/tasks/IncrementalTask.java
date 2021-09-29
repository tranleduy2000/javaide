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
package com.android.build.gradle.internal.tasks;

import com.android.ide.common.res2.SourceSet;
import com.google.common.collect.Lists;

import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class IncrementalTask extends BaseTask {

    private File incrementalFolder;

    public static List<File> flattenSourceSets(List<? extends SourceSet> resourceSets) {
        List<File> list = Lists.newArrayList();

        for (SourceSet sourceSet : resourceSets) {
            list.addAll(sourceSet.getSourceFiles());
        }

        return list;
    }

    @OutputDirectory
    @Optional
    public File getIncrementalFolder() {
        return incrementalFolder;
    }

    public void setIncrementalFolder(File incrementalFolder) {
        this.incrementalFolder = incrementalFolder;
    }

    /**
     * Actual task action. This is called when a full run is needed, which is always the case if
     */
    protected abstract void doFullTaskAction() throws IOException;

    public void taskAction(IncrementalTaskInputs inputs) {

    }
}
