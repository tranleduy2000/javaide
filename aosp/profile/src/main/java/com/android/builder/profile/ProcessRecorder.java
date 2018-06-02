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

package com.android.builder.profile;

import com.android.annotations.NonNull;
import com.android.builder.tasks.Job;
import com.android.builder.tasks.JobContext;
import com.android.builder.tasks.QueueThreadContextAdapter;
import com.android.builder.tasks.Task;
import com.android.builder.tasks.WorkQueue;
import com.android.utils.ILogger;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Records all the {@link ExecutionRecord} for a process, in order it was received and sends then
 * synchronously to a {@link JsonRecordWriter}.
 */
public class ProcessRecorder {

    private static final AtomicLong lastRecordId = new AtomicLong(0);

    static long allocateRecordId() {
        return lastRecordId.incrementAndGet();
    }

    static void resetForTests() {
        lastRecordId.set(0);
    }

    @NonNull
    static ProcessRecorder get() {
        return ProcessRecorderFactory.sINSTANCE.get();
    }

    /**
     * Abstraction for a {@link ExecutionRecord} writer.
     */
    public interface ExecutionRecordWriter {

        void write(@NonNull ExecutionRecord executionRecord) throws IOException;

        void close() throws IOException;
    }



    private class WorkQueueContext extends QueueThreadContextAdapter<ExecutionRecordWriter> {
        @Override
        public void runTask(@NonNull Job<ExecutionRecordWriter> job) throws Exception {
            job.runTask(singletonJobContext);
        }

        @Override
        public void shutdown() {
            try {
                singletonJobContext.getPayload().close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    private final JobContext<ExecutionRecordWriter> singletonJobContext;
    @NonNull
    private final WorkQueue<ExecutionRecordWriter> workQueue;

    ProcessRecorder(@NonNull ExecutionRecordWriter outWriter, @NonNull ILogger iLogger) {
        this.singletonJobContext = new JobContext<ExecutionRecordWriter>(outWriter);
        workQueue = new WorkQueue<ExecutionRecordWriter>(
                iLogger, new WorkQueueContext(), "execRecordWriter", 1);
    }

    void writeRecord(@NonNull final ExecutionRecord executionRecord) {

        try {
            workQueue.push(new Job<ExecutionRecordWriter>("recordWriter", new Task<ExecutionRecordWriter>() {
                @Override
                public void run(@NonNull Job<ExecutionRecordWriter> job,
                        @NonNull JobContext<ExecutionRecordWriter> context) throws IOException {
                    context.getPayload().write(executionRecord);
                    job.finished();
                }
            }));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Done with the recording processing, finish processing the outstanding {@link ExecutionRecord}
     * publication and shutdowns the processing queue.
     *
     * @throws InterruptedException
     */
    void finish() throws InterruptedException {
        workQueue.shutdown();
    }

    /**
     * Implementation of {@link ExecutionRecordWriter} that persist in json format.
     */
    static class JsonRecordWriter implements ExecutionRecordWriter {

        @NonNull
        private final Gson gson;

        @NonNull
        private final Writer writer;

        @NonNull
        private final AtomicBoolean closed = new AtomicBoolean(false);

        public JsonRecordWriter(@NonNull Writer writer) {
            this.gson = new Gson();
            this.writer = writer;
        }

        @Override
        public synchronized void write(@NonNull ExecutionRecord executionRecord)
                throws IOException {

            if (closed.get()) {
                return;
            }
            String json = gson.toJson(executionRecord);
            writer.append(json);
            writer.append("\n");
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                if (closed.get()) {
                    return;
                }
                closed.set(true);
            }
            writer.flush();
            writer.close();
        }
    }
}
