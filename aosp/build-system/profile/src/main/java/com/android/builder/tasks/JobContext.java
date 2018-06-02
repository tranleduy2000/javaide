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

import com.android.annotations.Nullable;

/**
 * Contains contextual (and usually mutable) job data.
 */
public class JobContext<T> {

    private final T payload;
    private final long creationTime = System.currentTimeMillis();

    public JobContext(@Nullable T payload) {
        this.payload = payload;
    }

    @Nullable
    public T getPayload() {
        return payload;
    }

    /**
     * Returns time spent processing this job so far.
     * @return the real time spent.
     */
    public long elapsed() {
        return System.currentTimeMillis() - creationTime;
    }
}