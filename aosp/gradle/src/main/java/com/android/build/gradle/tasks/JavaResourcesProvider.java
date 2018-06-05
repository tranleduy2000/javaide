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

package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.TaskFactory;
import com.android.build.gradle.internal.scope.AndroidTask;
import com.google.common.collect.ImmutableList;

import java.io.File;

/**
 * Denotes a provider for the java resources ready to be packaged in the final variant APK.
 *
 * These java resources can be the merged results of all the packaged libraries resources as well
 * as obfuscated resources (that match obfuscated java code loading such resources).
 *
 * Depending on the configuration of the variant, the actual provider can be the obfuscation task
 * or the resource merging task or something else.
 */
public interface JavaResourcesProvider {

    /**
     * Denotes how the java resources are provided (as a jar or as folder).
     */
    enum Type { JAR, FOLDER }

    final class JavaResourcesLocation {
        final Type type;
        final File location;

        public JavaResourcesLocation(Type type, File location) {
            this.type = type;
            this.location = location;
        }
    }

    @NonNull
    ImmutableList<JavaResourcesLocation> getJavaResourcesLocations();

    /**
     * Adapter for tasks that are not created yet.
     */
    class Adapter {
        @NonNull
        public static JavaResourcesProvider build(@NonNull final TaskFactory tasks,
                @NonNull final AndroidTask<? extends JavaResourcesProvider> androidTask) {
            return new JavaResourcesProvider() {
                @NonNull
                @Override
                public ImmutableList<JavaResourcesLocation> getJavaResourcesLocations() {
                    return androidTask.get(tasks).getJavaResourcesLocations();
                }
            };
        }
    }
}
