
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

package com.android.build.gradle.internal.profile;

import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.android.builder.profile.ExecutionRecord;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.Recorder;
import com.google.common.base.CaseFormat;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionListener;
import org.gradle.api.tasks.TaskState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link TaskExecutionListener} that records the execution span of
 * tasks execution and records such spans using the {@link Recorder} facilities.
 */
public class RecordingBuildListener implements TaskExecutionListener {

    private static final class TaskRecord {

        private final long recordId;
        private final long startTime;

        TaskRecord(long recordId, long startTime) {
            this.startTime = startTime;
            this.recordId = recordId;
        }
    }

    private final Recorder mRecorder;

    public RecordingBuildListener(Recorder recorder) {
        mRecorder = recorder;
    }

    // map of outstanding tasks executing, keyed by their name.
    final Map<String, TaskRecord> taskRecords = new ConcurrentHashMap<String, TaskRecord>();

    @Override
    public void beforeExecute(Task task) {
        taskRecords.put(task.getName(), new TaskRecord(
                mRecorder.allocationRecordId(), System.currentTimeMillis()));
    }

    @Override
    public void afterExecute(Task task, TaskState taskState) {

        // find the right ExecutionType.
        String taskImpl = task.getClass().getSimpleName();
        if (taskImpl.endsWith("_Decorated")) {
            taskImpl = taskImpl.substring(0, taskImpl.length() - "_Decorated".length());
        }
        String potentialExecutionTypeName = "TASK_" +
                CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE).
                        convert(taskImpl);
        ExecutionType executionType;
        try {
            executionType = ExecutionType.valueOf(potentialExecutionTypeName);
        } catch (IllegalArgumentException ignored) {
            executionType = ExecutionType.GENERIC_TASK_EXECUTION;
        }

        List<Recorder.Property> properties = new ArrayList<Recorder.Property>();
        properties.add(new Recorder.Property("project", task.getProject().getName()));
        properties.add(new Recorder.Property("task", task.getName()));

        if (task instanceof DefaultAndroidTask) {
            String variantName = ((DefaultAndroidTask) task).getVariantName();
            if (variantName == null) {
                throw new IllegalStateException("Task with type " + task.getClass().getName() +
                        " does not include a variantName");
            }
            if (!variantName.isEmpty()) {
                properties.add(new Recorder.Property("variant", variantName));
            }
        }

        TaskRecord taskRecord = taskRecords.get(task.getName());
        mRecorder.closeRecord(new ExecutionRecord(
                taskRecord.recordId,
                0 /* parentId */,
                taskRecord.startTime,
                System.currentTimeMillis() - taskRecord.startTime,
                executionType,
                properties));
    }
}
