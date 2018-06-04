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

package com.android.build.gradle.model;

import static com.android.build.gradle.model.ModelConstants.IS_APPLICATION;
import static com.android.build.gradle.model.ModelConstants.TASK_MANAGER;

import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.DependencyManager;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.variant.LibraryVariantFactory;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.builder.core.AndroidBuilder;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.model.Model;
import org.gradle.model.RuleSource;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * Gradle component model plugin class for 'application' projects.
 */
public class LibraryComponentModelPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(BaseComponentModelPlugin.class);
        project.getTasks().create("assembleDefault");
        project.getPluginManager().apply(AndroidComponentModelTestPlugin.class);
    }

    @SuppressWarnings("MethodMayBeStatic")
    public static class Rules extends RuleSource {

        @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
        @Model(IS_APPLICATION)
        public Boolean isApplication() {
            return false;
        }

        @Model(TASK_MANAGER)
        public TaskManager createTaskManager(
                AndroidConfig androidExtension,
                Project project,
                AndroidBuilder androidBuilder,
                SdkHandler sdkHandler,
                ExtraModelInfo extraModelInfo,
                ToolingModelBuilderRegistry toolingRegistry) {
            DependencyManager dependencyManager = new DependencyManager(project, extraModelInfo);

            return new LibraryComponentTaskManager(
                    project,
                    androidBuilder,
                    androidExtension,
                    sdkHandler,
                    dependencyManager,
                    toolingRegistry);
        }

        @Model
        public VariantFactory createVariantFactory(
                ServiceRegistry serviceRegistry,
                AndroidBuilder androidBuilder,
                AndroidConfig extension) {
            Instantiator instantiator = serviceRegistry.get(Instantiator.class);
            return new LibraryVariantFactory(instantiator, androidBuilder, extension);
        }
    }
}
