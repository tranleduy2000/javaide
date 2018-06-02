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
 * Task that can be created asynchronously.
 */
public interface Task<T> {
    /**
     * Executes the task with the context object to retrieve
     * and store inter-tasks information.
     *
     * @param context the task contextual object
     * @throws java.io.IOException an exception occured while processing
     * the task.
     */
    void run(@NonNull Job<T> job, @NonNull JobContext<T> context) throws IOException;
}
