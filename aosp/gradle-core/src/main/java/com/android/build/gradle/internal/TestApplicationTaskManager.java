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

import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.AndroidConfig;
import com.android.build.gradle.TestAndroidConfig;
import com.android.build.gradle.internal.scope.AndroidTask;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.DeviceProviderInstrumentTestTask;
import com.android.build.gradle.internal.test.TestApplicationTestData;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.BaseVariantOutputData;
import com.android.build.gradle.internal.variant.LibraryVariantData;
import com.android.build.gradle.tasks.TestModuleProGuardTask;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.model.AndroidProject;
import com.android.builder.testing.ConnectedDeviceProvider;
import com.android.builder.testing.TestData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import proguard.ParseException;

/**
 * TaskManager for standalone test application that lives in a separate module from the tested
 * application.
 */
public class TestApplicationTaskManager extends ApplicationTaskManager {


    public TestApplicationTaskManager(Project project,
            AndroidBuilder androidBuilder,
            AndroidConfig extension,
            SdkHandler sdkHandler,
            DependencyManager dependencyManager,
            ToolingModelBuilderRegistry toolingRegistry) {
        super(project, androidBuilder, extension, sdkHandler, dependencyManager, toolingRegistry);
    }

    @Override
    public void createTasksForVariantData(@NonNull TaskFactory tasks,
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData) {

        super.createTasksForVariantData(tasks, variantData);

        // create a new configuration with the target application coordinates.
        final Configuration testTarget = project.getConfigurations().create("testTarget");

        DependencyHandler dependencyHandler = project.getDependencies();
        TestAndroidConfig testExtension = (TestAndroidConfig) extension;
        dependencyHandler.add("testTarget",
                dependencyHandler.project(
                        ImmutableMap.of(
                                "path", testExtension.getTargetProjectPath(),
                                "configuration", testExtension.getTargetVariant())));

        // and create the configuration for the project's metadata.
        final Configuration testTargetMetadata = project.getConfigurations().create("testTargetMetadata");

        dependencyHandler.add("testTargetMetadata", dependencyHandler.project(
                        ImmutableMap.of(
                                "path", testExtension.getTargetProjectPath(),
                                "configuration", testExtension.getTargetVariant() + "-metadata"
                        )));

        TestData testData = new TestApplicationTestData(
                variantData, testTarget, testTargetMetadata, androidBuilder);

        // create the test connected check task.
        AndroidTask<DeviceProviderInstrumentTestTask> testConnectedCheck =
                getAndroidTasks().create(
                        tasks,
                        new DeviceProviderInstrumentTestTask.ConfigAction(
                                variantData.getScope(),
                                new ConnectedDeviceProvider(
                                        sdkHandler.getSdkInfo().getAdb(),
                                        new LoggerWrapper(getLogger())),
                                testData));

        // make the test application connectedCheck depends on the configuration added above so
        // we can retrieve its artifacts

        testConnectedCheck.dependsOn(tasks,
                testTarget,
                testTargetMetadata,
                variantData.assembleVariantTask);

        // make the main ConnectedCheck task depends on this test connectedCheck
        Task connectedCheck = tasks.named(CONNECTED_CHECK);
        if (connectedCheck != null) {
            connectedCheck.dependsOn(testConnectedCheck.getName());
        }
    }

    @Override
    @Nullable
    public File maybeCreateProguardTasks(
            @NonNull final TaskFactory tasks,
            @NonNull final VariantScope scope,
            @NonNull final PostCompilationData pcData) {
        DependencyHandler dependencyHandler = project.getDependencies();
        TestAndroidConfig testExtension = (TestAndroidConfig) extension;
        Configuration testTargetMapping = project.getConfigurations().create("testTargetMapping");

        dependencyHandler.add("testTargetMapping", dependencyHandler.project(
                ImmutableMap.of(
                        "path", testExtension.getTargetProjectPath(),
                        "configuration", testExtension.getTargetVariant() + "-mapping"
                )));

        if (testTargetMapping.getFiles().isEmpty()
                || scope.getVariantConfiguration().getProvidedOnlyJars().isEmpty()) {
            return null;
        }

        BaseVariantData<? extends BaseVariantOutputData> variantData = scope.getVariantData();

        final TestModuleProGuardTask proguardTask = project.getTasks().create(
                scope.getTaskName("proguard"),
                TestModuleProGuardTask.class);

        variantData.obfuscationTask = proguardTask;
        proguardTask.setLogger(getLogger());

        // and create the configuration for the project's mapping file.
        // Input the mapping from the tested app so that we can deal with obfuscated code.
        proguardTask.setMappingConfiguration(testTargetMapping);
        proguardTask.setVariantConfiguration(scope.getVariantConfiguration());

        // --- Proguard Config ---

        // Don't remove any code in tested app.
        proguardTask.dontshrink();
        proguardTask.dontoptimize();

        // We can't call dontobfuscate, since that would make ProGuard ignore the mapping file.
        try {
            proguardTask.keep("class * {*;}");
            proguardTask.keep("interface * {*;}");
            proguardTask.keep("enum * {*;}");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        // libraryJars: the runtime jars. Do this in doFirst since the boot classpath isn't
        // available until the SDK is loaded in the prebuild task
        proguardTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                for (String runtimeJar : androidBuilder.getBootClasspathAsStrings()) {
                    try {
                        proguardTask.libraryjars(runtimeJar);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        try {

            // injar: the compilation output
            proguardTask.injars(pcData.getInputDirCallable());
            if (pcData.getJavaResourcesInputDirCallable() != null) {
                proguardTask.injars(pcData.getJavaResourcesInputDirCallable());
            }

            // injar: the packaged dependencies
            LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(1);
            map.put("filter", "!META-INF/MANIFEST.MF");
            proguardTask.injars(map, new Callable<Set<File>>() {
                @Override
                public Set<File> call() throws Exception {
                    return scope.getVariantConfiguration().getPackagedJars();
                }
            });

            proguardTask.libraryjars(new Callable<List<File>>() {
                @Override
                public List<File> call() throws Exception {
                    return scope.getVariantConfiguration().getProvidedOnlyJars();
                }
            });


            // All -dontwarn rules for test dependencies should go in here:
            proguardTask.configuration(
                    variantData.getVariantConfiguration().getTestProguardFiles());



            // --- Out files ---

            proguardTask.outjars(scope.getProguardOutputFile());

            final File proguardOut = project.file(
                    new File(project.getBuildDir(),
                            FD_OUTPUTS
                                    + File.separatorChar
                                    + "mapping"
                                    + File.separatorChar
                                    + variantData.getVariantConfiguration().getDirName()));

            proguardTask.dump(new File(proguardOut, "dump.txt"));
            proguardTask.printseeds(new File(proguardOut, "seeds.txt"));
            proguardTask.printusage(new File(proguardOut, "usage.txt"));
            proguardTask.printmapping(new File(proguardOut, "mapping.txt"));

            // proguard doesn't verify that the seed/mapping/usage folders exist and will fail
            // if they don't so create them.
            proguardTask.doFirst(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    if (!proguardOut.mkdirs()) {
                        throw new RuntimeException(
                                "Cannot create proguard output folder " + proguardOut);
                    }
                }
            });

            // update dependency.
            optionalDependsOn(proguardTask, pcData.getClassGeneratingTasks());
            optionalDependsOn(proguardTask, pcData.getLibraryGeneratingTasks());
            pcData.setLibraryGeneratingTasks(ImmutableList.of(proguardTask));
            pcData.setClassGeneratingTasks(ImmutableList.of(proguardTask));

            // Update the inputs
            return scope.getProguardOutputFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
