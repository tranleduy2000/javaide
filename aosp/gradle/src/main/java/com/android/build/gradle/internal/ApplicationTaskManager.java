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

import com.android.annotations.NonNull;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.internal.scope.AndroidTask;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.builder.core.AndroidBuilder;

import org.gradle.api.Project;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

/**
 * TaskManager for creating tasks in an Android application project.
 */
public class ApplicationTaskManager extends TaskManager {

    public ApplicationTaskManager(
            Project project,
            AndroidBuilder androidBuilder,
            AndroidConfig extension,
            SdkHandler sdkHandler,
            DependencyManager dependencyManager,
            ToolingModelBuilderRegistry toolingRegistry) {
        super(project, androidBuilder, extension, sdkHandler, dependencyManager, toolingRegistry);
    }

    @Override
    public void createTasksForVariantData(
            @NonNull final TaskFactory tasks,
            @NonNull final BaseVariantData<? extends BaseVariantOutputData> variantData) {
        final VariantScope variantScope = variantData.getScope();

        createAnchorTasks(tasks, variantScope);
        createCheckManifestTask(tasks, variantScope);

        // Add a task to process the manifest(s)
        createMergeAppManifestsTask(tasks, variantScope);

        // Add a task to create the res values
        createGenerateResValuesTask(tasks, variantScope);

        // Add a task to merge the resource folders
        createMergeResourcesTask(tasks, variantScope);

        // Add a task to merge the asset folders

        createMergeAssetsTask(tasks, variantScope);

        // Add a task to create the BuildConfig class
        createBuildConfigTask(tasks, variantScope);

        // Add a task to process the Android Resources and generate source files
        createProcessResTask(tasks, variantScope, true);

        // Add a task to process the java resources
        createProcessJavaResTasks(tasks, variantScope);

        createAidlTask(tasks, variantScope);

        // Add a compile task
        AndroidTask<JavaCompile> javacTask = createJavacTask(tasks, variantScope);
        setJavaCompilerTask(javacTask, tasks, variantScope);
        createJarTask(tasks, variantScope);
        createPostCompilationTasks(tasks, variantScope);


        if (variantData.compileTask != null) {
            variantData.compileTask.dependsOn();
        } else {
            variantScope.getCompileTask().dependsOn(tasks);
        }

        if (variantData.getSplitHandlingPolicy()
                .equals(BaseVariantData.SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY)) {
            createSplitResourcesTasks(variantScope);
        }

        createPackagingTask(tasks, variantScope, true);

        // create the lint tasks.
        createLintTasks(tasks, variantScope);
    }

}
