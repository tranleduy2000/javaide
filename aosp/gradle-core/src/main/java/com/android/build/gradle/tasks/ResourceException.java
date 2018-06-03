/*
 * Copyright (C) 2012 The Android Open Source Project
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

import com.android.ide.common.res2.MergingException;

/**
 * Exception used for resource merging errors, thrown when
 * a {@link MergingException} is thrown by the resource merging code.
 * We can't just rethrow the {@linkplain MergingException} because
 * gradle 1.8 seems to want a RuntimeException; without it you get
 * the error message
 * {@code
 *     > Could not call IncrementalTask.taskAction() on task ':MyPrj:mergeDebugResources'
 * }
 */
public class ResourceException extends RuntimeException {
    public ResourceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
