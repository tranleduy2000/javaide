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

package com.android.build.gradle.internal.scope;

import com.android.build.gradle.internal.TaskFactory;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.internal.ClosureBackedAction;

import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;

/**
 * Registry for creating and storing AndroidTask.
 */
public class AndroidTaskRegistry {

    private final Map<String, AndroidTask> tasks = new HashMap<String, AndroidTask>();

    public synchronized < T extends Task> AndroidTask<T> create(
            TaskFactory taskFactory,
            String taskName,
            Class<T> taskClass,
            Action<T> configAction) {

        taskFactory.create(taskName, taskClass, configAction);
        final AndroidTask<T> newTask = new AndroidTask<T>(taskName, taskClass);
        tasks.put(taskName, newTask);

        return newTask;
    }

    public synchronized AndroidTask<Task> create(
            TaskFactory taskFactory,
            String taskName,
            Closure configAction) {

        taskFactory.create(taskName, DefaultTask.class, new ClosureBackedAction<Task>(configAction));
        final AndroidTask<Task> newTask = new AndroidTask<Task>(taskName, Task.class);
        tasks.put(taskName, newTask);

        return newTask;
    }

    public synchronized <T extends Task> AndroidTask<T> create(
            TaskFactory taskFactory,
            String taskName,
            Class<T> taskClass,
            Closure configAction) {

        taskFactory.create(taskName, taskClass, new ClosureBackedAction<T>(configAction));
        final AndroidTask<T> newTask = new AndroidTask<T>(taskName, taskClass);
        tasks.put(taskName, newTask);

        return newTask;
    }

    public <T extends Task> AndroidTask<T> create(
            TaskFactory taskFactory,
            TaskConfigAction<T> configAction) {
        return create(taskFactory, configAction.getName(), configAction.getType(), configAction);
    }
}
