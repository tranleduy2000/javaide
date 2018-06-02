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
import com.google.common.base.Objects;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * A {@link ExecutionRecord} recorder for a block execution.
 *
 * A block is some code that produces a result and may throw exceptions.
 */
public interface Recorder {

    /**
     * Abstraction of a block of code that produces a result of type T and may throw exceptions. Any
     * exception thrown by {@link Callable#call()} will be passed to the {@link
     * #handleException(Exception)} method. Default implementation of this method is to repackage
     * the exception as {@link RuntimeException} unless it already is one.
     *
     * @param <T> the type of result produced by executing this block of code.
     */
    abstract class Block<T> implements Callable<T> {

        /**
         * Notification that an exception was raised during the {@link #call()} method invocation.
         * Default behavior is to repackage as a {@link RuntimeException}, subclasses can choose
         * differently including swallowing the exception. Swallowing the exception will make the
         * {@link Recorder#record(ExecutionType, Block, Property...)} return null.
         *
         * @param e the exception raised during the {@link #call()} execution.
         */
        public void handleException(@NonNull Exception e) {
            // by default we rethrow as a runtime exception, subclasses should override for more
            // precise handling.
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }
    }

    Block<Void> EmptyBlock = new Block<Void>() {
        @Override
        public Void call() throws Exception {
            return null;
        }
    };

    /**
     * Free formed name/value property pair that will be saved along the execution record for a
     * particular block.
     */
    final class Property {

        @NonNull
        final String name;

        @NonNull
        final String value;

        public Property(@NonNull String name, @NonNull String value) {
            this.name = name;
            this.value = value;
        }

        @NonNull
        public String getName() {
            return name;
        }

        @NonNull
        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("name", name)
                    .add("value", value)
                    .toString();
        }
    }

    /**
     * Records the time elapsed while executing a {@link Block} and saves the resulting {@link
     * ExecutionRecord} to {@link ProcessRecorder}.
     *
     * @param executionType the task type, so aggregation can be performed.
     * @param block         the block of code to execution and measure.
     * @param properties    optional list of free formed properties to save in the {@link
     *                      ExecutionRecord}
     * @param <T>           the type of the returned value from the block.
     * @return the value returned from the block (including null) or null if the block execution
     * raised an exception which was subsequently swallowed by {@link Block#handleException(Exception)}
     */
    @Nullable
    <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
            Property... properties);


    /**
     * Records the time elapsed while executing a {@link Block} and saves the resulting {@link
     * ExecutionRecord} to {@link ProcessRecorder}.
     *
     * @param executionType the task type, so aggregation can be performed.
     * @param block         the block of code to execution and measure.
     * @param properties    optional list of free formed properties to save in the {@link
     *                      ExecutionRecord}
     * @param <T>           the type of the returned value from the block.
     * @return the value returned from the block (including null) or null if the block execution
     * raised an exception which was subsequently swallowed by {@link Block#handleException(Exception)}
     */
    @Nullable
    <T> T record(@NonNull ExecutionType executionType, @NonNull Block<T> block,
            @NonNull List<Property> properties);

    /**
     * Allocate a new recordId that can be used to create a {@link ExecutionRecord} and record
     * an execution span. This method is useful when the code span to measure cannot be expressed
     * as a {@link Block} and therefore cannot directly use the
     * {@link #record(ExecutionType, Block, Property...)} method.
     *
     * @return the unique record id for this process.
     */
    long allocationRecordId();

    /**
     * Closes an execution span measurement using the allocated record id obtained from
     * {@link #allocationRecordId()} method.
     *
     * @param record the span execution record, fully populated.
     */
    void closeRecord(ExecutionRecord record);
}
