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

package com.android.build.gradle;

import com.android.annotations.VisibleForTesting;
import com.android.build.gradle.internal.ApiObjectFactory;
import com.android.build.gradle.internal.DependencyManager;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.NativeLibraryFactoryImpl;
import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.TaskContainerAdaptor;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.BuildTypeFactory;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.ProductFlavorFactory;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.build.gradle.internal.dsl.SigningConfigFactory;
import com.android.build.gradle.internal.model.ModelBuilder;
import com.android.build.gradle.internal.process.GradleJavaProcessExecutor;
import com.android.build.gradle.internal.process.GradleProcessExecutor;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.build.gradle.tasks.PreDex;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.BuilderConstants;
import com.android.builder.internal.compiler.PreDexCache;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;
import com.android.builder.sdk.TargetInfo;
import com.android.utils.ILogger;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;
import static com.google.common.base.Preconditions.checkState;

/**
 * Base class for all Android plugins
 */
public abstract class BasePlugin {

    protected BaseExtension extension;

    protected VariantManager variantManager;

    protected TaskManager taskManager;

    protected Project project;

    protected SdkHandler sdkHandler;
    protected AndroidBuilder androidBuilder;
    protected Instantiator instantiator;
    protected VariantFactory variantFactory;
    private NdkHandler ndkHandler;
    private ToolingModelBuilderRegistry registry;

    private LoggerWrapper loggerWrapper;

    private ExtraModelInfo extraModelInfo;

    private String creator = "Android Gradle Java N-IDE";

    private boolean hasCreatedTasks = false;

    protected BasePlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        this.instantiator = instantiator;
        this.registry = registry;
    }

    protected abstract Class<? extends BaseExtension> getExtensionClass();

    protected abstract VariantFactory createVariantFactory();

    protected abstract TaskManager createTaskManager(
            Project project,
            AndroidBuilder androidBuilder,
            AndroidConfig extension,
            SdkHandler sdkHandler,
            DependencyManager dependencyManager,
            ToolingModelBuilderRegistry toolingRegistry);

    /**
     * Return whether this plugin creates Android library.  Should be overridden if true.
     */
    protected boolean isLibrary() {
        return false;
    }

    @VisibleForTesting
    VariantManager getVariantManager() {
        return variantManager;
    }

    protected ILogger getLogger() {
        if (loggerWrapper == null) {
            loggerWrapper = new LoggerWrapper(project.getLogger());
        }

        return loggerWrapper;
    }


    protected void apply(Project project) {
        this.project = project;
        checkModulesForErrors();
        configureProject();
        createExtension();
        createTasks();
    }

    protected void configureProject() {
        extraModelInfo = new ExtraModelInfo(project, isLibrary());
        sdkHandler = new SdkHandler(project, getLogger());
        androidBuilder = new AndroidBuilder(
                project == project.getRootProject() ? project.getName() : project.getPath(),
                creator,
                new GradleProcessExecutor(project),
                new GradleJavaProcessExecutor(project),
                extraModelInfo,
                getLogger(),
                isVerbose());

//        project.getPlugins().apply(JavaBasePlugin.class);

        // call back on execution. This is called after the whole build is done (not
        // after the current project is done).
        // This is will be called for each (android) projects though, so this should support
        // being called 2+ times.
        project.getGradle().getTaskGraph().addTaskExecutionGraphListener(
                new TaskExecutionGraphListener() {
                    @Override
                    public void graphPopulated(TaskExecutionGraph taskGraph) {
                        for (Task task : taskGraph.getAllTasks()) {
                            if (task instanceof PreDex) {
                                PreDexCache.getCache().load(
                                        new File(project.getRootProject().getBuildDir(),
                                                FD_INTERMEDIATES + "/dex-cache/cache.xml"));
                                break;
                            }
                        }
                    }
                });
    }


    private void createExtension() {
        final NamedDomainObjectContainer<BuildType> buildTypeContainer = project.container(
                BuildType.class,
                new BuildTypeFactory(instantiator, project, project.getLogger()));
        final NamedDomainObjectContainer<ProductFlavor> productFlavorContainer = project.container(
                ProductFlavor.class,
                new ProductFlavorFactory(instantiator, project, project.getLogger()));
        final NamedDomainObjectContainer<SigningConfig> signingConfigContainer = project.container(
                SigningConfig.class,
                new SigningConfigFactory(instantiator));

        extension = project.getExtensions().create("android", getExtensionClass(),
                project, instantiator, androidBuilder, sdkHandler,
                buildTypeContainer, productFlavorContainer, signingConfigContainer,
                extraModelInfo, isLibrary());

        DependencyManager dependencyManager = new DependencyManager(project, extraModelInfo);
        taskManager = createTaskManager(
                project,
                androidBuilder,
                extension,
                sdkHandler,
                dependencyManager,
                registry);

        variantFactory = createVariantFactory();
        variantManager = new VariantManager(
                project,
                androidBuilder,
                extension,
                variantFactory,
                taskManager,
                instantiator);

        ndkHandler = new NdkHandler(
                project.getRootDir(),
                null, /* compileSkdVersion, this will be set in afterEvaluate */
                "gcc",
                "" /*toolchainVersion*/);

        // Register a builder for the custom tooling model
        ModelBuilder modelBuilder = new ModelBuilder(
                androidBuilder,
                variantManager,
                taskManager,
                extension,
                extraModelInfo,
                ndkHandler,
                new NativeLibraryFactoryImpl(ndkHandler),
                isLibrary());
        registry.register(modelBuilder);

        // map the whenObjectAdded callbacks on the containers.
        signingConfigContainer.whenObjectAdded(new Action<SigningConfig>() {
            @Override
            public void execute(SigningConfig signingConfig) {
                variantManager.addSigningConfig(signingConfig);
            }
        });


        buildTypeContainer.whenObjectAdded(new Action<BuildType>() {
            @Override
            public void execute(BuildType buildType) {
                SigningConfig signingConfig = signingConfigContainer.findByName(BuilderConstants.DEBUG);
                buildType.init(signingConfig);
                variantManager.addBuildType(buildType);
            }
        });

        productFlavorContainer.whenObjectAdded(new Action<ProductFlavor>() {
            @Override
            public void execute(ProductFlavor productFlavor) {
                variantManager.addProductFlavor(productFlavor);
            }
        });

        // map whenObjectRemoved on the containers to throw an exception.
        signingConfigContainer.whenObjectRemoved(
                new UnsupportedAction("Removing signingConfigs is not supported."));
        buildTypeContainer.whenObjectRemoved(
                new UnsupportedAction("Removing build types is not supported."));
        productFlavorContainer.whenObjectRemoved(
                new UnsupportedAction("Removing product flavors is not supported."));

        // create default Objects, signingConfig first as its used by the BuildTypes.
        variantFactory.createDefaultComponents(
                buildTypeContainer, productFlavorContainer, signingConfigContainer);
    }

    private void createTasks() {
        taskManager.createTasksBeforeEvaluate(new TaskContainerAdaptor(project.getTasks()));
        createAndroidTasks(false);
    }

    final void createAndroidTasks(boolean force) {
        // Make sure unit tests set the required fields.
        checkState(extension.getBuildToolsRevision() != null, "buildToolsVersion is not specified.");
        checkState(extension.getCompileSdkVersion() != null, "compileSdkVersion is not specified.");

        ndkHandler.setCompileSdkVersion(extension.getCompileSdkVersion());


        // don't do anything if the project was not initialized.
        // Unless TEST_SDK_DIR is set in which case this is unit tests and we don't return.
        // This is because project don't get evaluated in the unit test setup.
        // See AppPluginDslTest
        if (!force) {
            return;
        }

        if (hasCreatedTasks) {
            return;
        }
        hasCreatedTasks = true;

        extension.disableWrite();

        ThreadRecorder.get().record(
                ExecutionType.GENERAL_CONFIG,
                Recorder.EmptyBlock,
                new Recorder.Property("build_tools_version",
                        extension.getBuildToolsRevision().toString()));

        // setup SDK repositories.
        for (final File file : sdkHandler.getSdkLoader().getRepositories()) {
            project.getRepositories().maven(new Action<MavenArtifactRepository>() {
                @Override
                public void execute(MavenArtifactRepository mavenArtifactRepository) {
                    mavenArtifactRepository.setUrl(file.toURI());
                }
            });
        }

        variantManager.createAndroidTasks();
        ApiObjectFactory apiObjectFactory = new ApiObjectFactory(
                androidBuilder, extension, variantFactory, instantiator);
        for (BaseVariantData variantData : variantManager.getVariantDataList()) {
            apiObjectFactory.create(variantData);
        }

    }

    private boolean isVerbose() {
        return project.getLogger().isEnabled(LogLevel.INFO);
    }

    private void ensureTargetSetup() {
        // check if the target has been set.
        TargetInfo targetInfo = androidBuilder.getTargetInfo();
        if (targetInfo == null) {
            if (extension.getCompileOptions() == null) {
                throw new GradleException("Calling getBootClasspath before compileSdkVersion");
            }

            sdkHandler.initTarget(
                    extension.getCompileSdkVersion(),
                    extension.getBuildToolsRevision(),
                    extension.getLibraryRequests(),
                    androidBuilder);
        }
    }

    /**
     * Check the sub-projects structure :
     * So far, checks that 2 modules do not have the same identification (group+name).
     */
    private void checkModulesForErrors() {
        Project rootProject = project.getRootProject();
        Map<String, Project> subProjectsById = new HashMap<String, Project>();
        for (Project subProject : rootProject.getAllprojects()) {
            String id = subProject.getGroup().toString() + ":" + subProject.getName();
            if (subProjectsById.containsKey(id)) {
                String message = String.format(
                        "Your project contains 2 or more modules with the same " +
                                "identification %1$s\n" +
                                "at \"%2$s\" and \"%3$s\".\n" +
                                "You must use different identification (either name or group) for " +
                                "each modules.",
                        id,
                        subProjectsById.get(id).getPath(),
                        subProject.getPath());
                throw new StopExecutionException(message);
            } else {
                subProjectsById.put(id, subProject);
            }
        }
    }

    private static class UnsupportedAction implements Action<Object> {

        private final String message;

        UnsupportedAction(String message) {
            this.message = message;
        }

        @Override
        public void execute(Object o) {
            throw new UnsupportedOperationException(message);
        }
    }
}
