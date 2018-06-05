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

package com.android.build.gradle.internal.publishing;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.tasks.FileSupplier;
import com.google.common.base.Supplier;

import org.gradle.api.Task;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.tasks.TaskDependency;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * custom implementation of PublishArtifact for published APKs.
 */
public abstract class BasePublishArtifact implements PublishArtifact {

    @NonNull
    private final String name;

    @Nullable
    private final String classifier;

    @NonNull
    private final Supplier<File> outputFileSupplier;

    @NonNull
    private final TaskDependency taskDependency;

    private static final class DefaultTaskDependency implements TaskDependency {

        @NonNull
        private final Set<Task> tasks;

        DefaultTaskDependency(@NonNull Task task) {
            this.tasks = Collections.singleton(task);
        }

        @Override
        public Set<? extends Task> getDependencies(Task task) {
            return tasks;
        }
    }

    public BasePublishArtifact(
            @NonNull String name,
            @Nullable String classifier,
            @NonNull FileSupplier outputFileSupplier) {
        this.name = name;
        this.classifier = classifier;
        this.outputFileSupplier = outputFileSupplier;
        this.taskDependency = new DefaultTaskDependency(outputFileSupplier.getTask());
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public File getFile() {
        return outputFileSupplier.get();
    }

    @Override
    public Date getDate() {
        // return null to let gradle use the current date during publication.
        return null;
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return taskDependency;
    }
}
