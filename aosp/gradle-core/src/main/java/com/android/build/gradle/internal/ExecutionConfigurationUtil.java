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

import com.android.ide.common.internal.ExecutorSingleton;

import org.gradle.api.Project;

public class ExecutionConfigurationUtil {

    private static final String THREAD_POOL_SIZE_PROPERTY = "com.android.build.threadPoolSize";

    public static void setThreadPoolSize(Project project) {
        if (!project.hasProperty(THREAD_POOL_SIZE_PROPERTY)) {
            return;
        }

        String threadPoolSizeProperty = project.property(THREAD_POOL_SIZE_PROPERTY).toString();

        try {
            ExecutorSingleton.setThreadPoolSize(Integer.parseInt(threadPoolSizeProperty));
        } catch (NumberFormatException e) {
            project.getLogger().error("com.android.threadPoolSize should be an integer.");
        }
    }
}
