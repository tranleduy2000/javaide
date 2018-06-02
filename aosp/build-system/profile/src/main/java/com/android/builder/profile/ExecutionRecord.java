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
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Record task execution.
 */
public class ExecutionRecord {

    public final long id;
    public final long parentId;
    public final long startTimeInMs;
    public final long durationInMs;

    @NonNull public final ExecutionType type;
    @NonNull public final ImmutableList<Recorder.Property> attributes;

    public ExecutionRecord(long id, long parentId, long startTimeInMs, long durationInMs,
           @NonNull ExecutionType type, @Nullable List<Recorder.Property> attributes) {
        this.id = id;
        this.parentId = parentId;
        this.startTimeInMs = startTimeInMs;
        this.durationInMs = durationInMs;
        this.type = type;
        this.attributes = attributes == null || attributes.isEmpty()
                ? ImmutableList.<Recorder.Property>of() : ImmutableList.copyOf(attributes);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("type", type)
                .add("attributes", attributes)
                .toString();
    }
}
