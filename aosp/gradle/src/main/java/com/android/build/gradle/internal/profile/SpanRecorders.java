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

import com.android.annotations.NonNull;
import com.android.annotations.concurrency.Immutable;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;
import com.google.common.collect.ImmutableList;

import org.gradle.api.Project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Helper to record execution spans.
 */
public class SpanRecorders {

    public static final String PROJECT = "project";
    public static final String VARIANT = "variant";


    /**
     * Records an execution span, using a Java {@link Recorder.Block}
     */
    public static <T> T record(@NonNull Project project,
            @NonNull ExecutionType executionType,
            @NonNull Recorder.Block<T> block,
            Recorder.Property... properties) {
        Recorder.Property[] mergedProperties = new Recorder.Property[properties.length + 1];
        mergedProperties[0] = new Recorder.Property(PROJECT, project.getName());
        System.arraycopy(properties, 0, mergedProperties, 1, properties.length);
        return (T) ThreadRecorder.get().record(executionType, block, mergedProperties);
    }
}
