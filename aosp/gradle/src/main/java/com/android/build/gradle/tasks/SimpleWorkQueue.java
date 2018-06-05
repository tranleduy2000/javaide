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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.builder.tasks.Job;
import com.android.builder.tasks.JobContext;
import com.android.builder.tasks.QueueThreadContextAdapter;
import com.android.builder.tasks.WorkQueue;
import com.android.utils.StdLogger;

/**
 * Common utilities to use a simple shared instance of {@link WorkQueue}.
 * The context for job will be empty, and it is the responsibility of the
 * {@link com.android.builder.tasks.WorkQueue.QueueTask} to have enough context to run.
 */
public class SimpleWorkQueue {

    /**
     * Simple {@link WorkQueue} context implementation that simply runs the proguard job.
     */
    private static class EmptyThreadContext extends
            QueueThreadContextAdapter<Void> {

        @Override
        public void runTask(@NonNull Job<Void> job) throws Exception {
            job.runTask(new JobContext<Void>(null /* payload */));
            job.finished();
        }
    }

    /**
     * singleton work queue for all proguard invocations.
     */
    private static final WorkQueue<Void> WORK_QUEUE =
            new WorkQueue<Void>(
                    new StdLogger(StdLogger.Level.VERBOSE),
                    new EmptyThreadContext(), "Tasks limiter", 4);


    static void push(Job<Void> job) throws InterruptedException {
        WORK_QUEUE.push(job);
    }
}
