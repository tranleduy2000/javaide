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

package com.android.build.gradle.internal;

import com.android.annotations.Nullable;

import org.gradle.api.Action;
import org.gradle.api.Task;

/**
 * Interface for a container that can create Task.
 */
public interface TaskFactory {

    /**
     * Returns true if this collection contains an item with the given name.
     */
    boolean containsKey(String name);

    /**
     * Creates a task with the given name.
     */
    void create(String name);

    /**
     * Creates a task and initialize it with the given configAction.
     */
    void create(String name, Action<? super Task> configAction);

    /**
     * Creates a task with the given name and type.
     */
    <S extends Task> void create(String name, Class<S> type);

    /**
     * Creates a task the given name and type, and initialize it with the given configAction.
     */
    <S extends Task> void create(String name, Class<S> type, Action<? super S> configAction);

    /**
     * Applies the given configAction to the task with given name.
     */
    void named(String name, Action<? super Task> configAction);

    /**
     * Returns the {@link Task} named name from the current set of defined tasks.
     * @param name the name of the requested {@link Task}
     * @return the {@link Task} instance or null if not found.
     */
    @Nullable
    Task named(String name);
}
