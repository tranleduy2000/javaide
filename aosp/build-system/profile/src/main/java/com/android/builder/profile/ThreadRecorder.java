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
import com.android.annotations.Nullable;
import com.google.common.collect.ImmutableList;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Facility to record block execution time on a single thread. Threads should not be spawned during
 * the block execution as its processing will not be recorded as of the parent's execution time.
 *
 * // TODO : provide facilities to create a new ThreadRecorder using a parent so the slave threads
 * can be connected to the parent's task.
 */
public class ThreadRecorder implements Recorder {

    private static final Logger logger = Logger.getLogger(ThreadRecorder.class.getName());

    // Dummy implementation that records nothing but comply to the overall recording contracts.
    private static final Recorder dummyRecorder = new Recorder() {
        @Nullable
        @Override
        public <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
                Property... properties) {
            return record(executionType, block, Collections.<Property>emptyList());
        }

        @Nullable
        @Override
        public <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
                @NonNull List<Property> properties) {
            try {
                return block.call();
            } catch (Exception e) {
                block.handleException(e);
            }
            return null;
        }

        @Override
        public long allocationRecordId() {
            return 0;
        }

        @Override
        public void closeRecord(ExecutionRecord record) {
        }
    };

    private static final Recorder recorder = new ThreadRecorder();


    public static Recorder get() {
        return ProcessRecorderFactory.getFactory().isInitialized() ? recorder : dummyRecorder;
    }

    private static  class PartialRecord {
        final ExecutionType executionType;
        final long recordId;
        final long parentRecordId;
        final long startTimeInMs;

        final List<Recorder.Property> extraArgs;

        PartialRecord(ExecutionType executionType,
                long recordId,
                long parentId,
                long startTimeInMs,
                List<Recorder.Property> extraArgs) {
            this.executionType = executionType;
            this.recordId = recordId;
            this.parentRecordId = parentId;
            this.startTimeInMs = startTimeInMs;
            this.extraArgs = extraArgs;
        }
    }

    /**
     * Do not put anything else than JDK classes in the ThreadLocal as it prevents that class
     * and therefore the plugin classloader to be gc'ed leading to OOM or PermGen issues.
     */
    private static final ThreadLocal<Deque<Long>> recordStacks =
            new ThreadLocal<Deque<Long>>() {
        @Override
        protected Deque<Long> initialValue() {
            return  new ArrayDeque<Long>();
        }
    };


    @Override
    public long allocationRecordId() {
        long recordId = ProcessRecorder.allocateRecordId();
        recordStacks.get().push(recordId);
        return recordId;
    }

    @Override
    public void closeRecord(ExecutionRecord executionRecord) {
        if (recordStacks.get().pop() != executionRecord.id) {
            logger.severe("Internal Error : mixed records in profiling stack");
        }
        ProcessRecorder.get().writeRecord(executionRecord);
    }

    @Nullable
    @Override
    public <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
            Property... properties) {

        List<Recorder.Property> propertyList = properties == null
                ? ImmutableList.<Recorder.Property>of()
                : ImmutableList.copyOf(properties);

        return record(executionType, block, propertyList);
    }

    @Nullable
    @Override
    public <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
            @NonNull List<Property> properties) {

        long thisRecordId = ProcessRecorder.allocateRecordId();

        // am I a child ?
        Long parentId = recordStacks.get().peek();

        long startTimeInMs = System.currentTimeMillis();

        final PartialRecord currentRecord = new PartialRecord(executionType,
                thisRecordId, parentId == null ? 0 : parentId,
                startTimeInMs, properties);

        recordStacks.get().push(thisRecordId);
        try {
            return block.call();
        } catch (Exception e) {
            block.handleException(e);
        } finally {
            // pop this record from the stack.
            if (recordStacks.get().pop() != currentRecord.recordId) {
                logger.log(Level.SEVERE, "Profiler stack corrupted");
            }
            ProcessRecorder.get().writeRecord(
                    new ExecutionRecord(currentRecord.recordId,
                            currentRecord.parentRecordId,
                            currentRecord.startTimeInMs,
                            System.currentTimeMillis() - currentRecord.startTimeInMs,
                            currentRecord.executionType,
                            currentRecord.extraArgs));
        }
        // we always return null when an exception occurred and was not rethrown.
        return null;
    }
}
