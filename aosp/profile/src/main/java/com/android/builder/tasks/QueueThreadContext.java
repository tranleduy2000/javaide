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

package com.android.builder.tasks;

import com.android.annotations.NonNull;

import java.io.IOException;

/**
 * Notification of queue events, creation, task running and destruction
 */
public interface QueueThreadContext<T> {

    /**
     * Notification of a new worker thread association with the queue
     * @param t the thread being associated.
     * @throws IOException
     */
    void creation(@NonNull Thread t) throws IOException;

    /**
     * Notification of a scheduled task execution.
     * @param job the job that should be executed on the current thread.
     * @throws Exception
     */
    void runTask(@NonNull Job<T> job) throws Exception;

    /**
     * Notification of the removal of the passed thread as a queue worker thread.
     * @param t the removed thread.
     * @throws IOException
     * @throws InterruptedException
     */
    void destruction(@NonNull Thread t) throws IOException, InterruptedException;

    /**
     * Notification of the queue temporary shutdown. All native resources must be released.
     * Once shutdown is called, at least one {@link #creation} must be made before any call to
     * {@link #runTask}.
     */
    void shutdown();
}
