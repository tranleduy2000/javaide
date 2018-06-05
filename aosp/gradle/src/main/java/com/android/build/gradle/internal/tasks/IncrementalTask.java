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
import com.android.ide.common.res2.FileStatus;
import com.android.ide.common.res2.SourceSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.gradle.api.Action;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.incremental.IncrementalTaskInputs;
import org.gradle.api.tasks.incremental.InputFileDetails;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class IncrementalTask extends BaseTask {

    private File incrementalFolder;

    public void setIncrementalFolder(File incrementalFolder) {
        this.incrementalFolder = incrementalFolder;
    }

    @OutputDirectory @Optional
    public File getIncrementalFolder() {
        return incrementalFolder;
    }

    /**
     * Whether this task can support incremental update.
     *
     * @return whether this task can support incremental update.
     */
    protected boolean isIncremental() {
        return false;
    }

    /**
     * Actual task action. This is called when a full run is needed, which is always the case if
     * {@link #isIncremental()} returns false.
     *
     */
    protected abstract void doFullTaskAction() throws IOException;

    /**
     * Optional incremental task action.
     * Only used if {@link #isIncremental()} returns true.
     *
     * @param changedInputs the changed input files.
     */
    protected void doIncrementalTaskAction(Map<File, FileStatus> changedInputs) throws IOException {
        // do nothing.
    }

    /**
     * Actual entry point for the action.
     * Calls out to the doTaskAction as needed.
     */
    @TaskAction
    void taskAction(IncrementalTaskInputs inputs) throws IOException {
        if (!isIncremental()) {
            doFullTaskAction();
            return;
        }

        if (!inputs.isIncremental()) {
            getProject().getLogger().info("Unable do incremental execution: full task run");
            doFullTaskAction();
            return;
        }

        final Map<File, FileStatus> changedInputs = Maps.newHashMap();
        inputs.outOfDate(new Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails change) {
                changedInputs.put(change.getFile(), change.isAdded() ? FileStatus.NEW : FileStatus.CHANGED);
            }
        });

        inputs.removed(new Action<InputFileDetails>() {
            @Override
            public void execute(InputFileDetails change) {

                changedInputs.put(change.getFile(), FileStatus.REMOVED);
            }
        });

        doIncrementalTaskAction(changedInputs);
    }

    public static List<File> flattenSourceSets(List<? extends SourceSet> resourceSets) {
        List<File> list = Lists.newArrayList();

        for (SourceSet sourceSet : resourceSets) {
            list.addAll(sourceSet.getSourceFiles());
        }

        return list;
    }
}
