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

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;
import static com.google.common.base.Preconditions.checkState;
import static java.io.File.separator;

import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.build.gradle.internal.ApiObjectFactory;
import com.android.build.gradle.internal.BadPluginException;
import com.android.build.gradle.internal.DependencyManager;
import com.android.build.gradle.internal.ExecutionConfigurationUtil;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.LibraryCache;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.NativeLibraryFactoryImpl;
import com.android.build.gradle.internal.NdkHandler;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.TaskContainerAdaptor;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.coverage.JacocoPlugin;
import com.android.build.gradle.internal.dsl.BuildType;
import com.android.build.gradle.internal.dsl.BuildTypeFactory;
import com.android.build.gradle.internal.dsl.ProductFlavor;
import com.android.build.gradle.internal.dsl.ProductFlavorFactory;
import com.android.build.gradle.internal.dsl.SigningConfig;
import com.android.build.gradle.internal.dsl.SigningConfigFactory;
import com.android.build.gradle.internal.model.ModelBuilder;
import com.android.build.gradle.internal.process.GradleJavaProcessExecutor;
import com.android.build.gradle.internal.process.GradleProcessExecutor;
import com.android.build.gradle.internal.profile.RecordingBuildListener;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.internal.variant.VariantFactory;
import com.android.build.gradle.tasks.JillTask;
import com.android.build.gradle.tasks.PreDex;
import com.android.builder.Version;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.core.BuilderConstants;
import com.android.builder.internal.compiler.JackConversionCache;
import com.android.builder.internal.compiler.PreDexCache;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.ProcessRecorderFactory;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;
import com.android.builder.sdk.TargetInfo;
import com.android.ide.common.internal.ExecutorSingleton;
import com.android.utils.ILogger;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.gradle.BuildListener;
import org.gradle.BuildResult;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.execution.TaskExecutionGraphListener;
import org.gradle.api.initialization.Settings;
import org.gradle.api.invocation.Gradle;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.tooling.BuildException;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Base class for all Android plugins
 */
public abstract class BasePlugin {

    private static final String GRADLE_MIN_VERSION = "2.2";
    public static final Pattern GRADLE_ACCEPTABLE_VERSIONS = Pattern.compile("2\\.[2-9].*");
    private static final String GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY =
            "com.android.build.gradle.overrideVersionCheck";
    private static final String SKIP_PATH_CHECK_PROPERTY =
            "com.android.build.gradle.overridePathCheck";
    /** default retirement age in days since its inception date for RC or beta versions. */
    private static final int DEFAULT_RETIREMENT_AGE_FOR_NON_RELEASE_IN_DAYS = 40;


    protected BaseExtension extension;

    protected VariantManager variantManager;

    protected TaskManager taskManager;

    protected Project project;

    protected SdkHandler sdkHandler;

    private NdkHandler ndkHandler;

    protected AndroidBuilder androidBuilder;

    protected Instantiator instantiator;

    protected VariantFactory variantFactory;

    private ToolingModelBuilderRegistry registry;

    private JacocoPlugin jacocoPlugin;

    private LoggerWrapper loggerWrapper;

    private ExtraModelInfo extraModelInfo;

    private String creator;

    private boolean hasCreatedTasks = false;

    protected BasePlugin(Instantiator instantiator, ToolingModelBuilderRegistry registry) {
        this.instantiator = instantiator;
        this.registry = registry;
        creator = "Android Gradle " + Version.ANDROID_GRADLE_PLUGIN_VERSION;
        verifyRetirementAge();

        ModelBuilder.clearCaches();
    }

    /**
     * Verify that this plugin execution is within its public time range.
     */
    private void verifyRetirementAge() {

        Manifest manifest;
        URLClassLoader cl = (URLClassLoader) getClass().getClassLoader();
        try {
            URL url = cl.findResource("META-INF/MANIFEST.MF");
            manifest = new Manifest(url.openStream());
        } catch (IOException ignore) {
            return;
        }

        String inceptionDateAttr = manifest.getMainAttributes().getValue("Inception-Date");
        // when running in unit tests, etc... the manifest entries are absent.
        if (inceptionDateAttr == null) {
            return;
        }
        List<String> items = ImmutableList.copyOf(Splitter.on(':').split(inceptionDateAttr));
        GregorianCalendar inceptionDate = new GregorianCalendar(Integer.parseInt(items.get(0)),
                Integer.parseInt(items.get(1)), Integer.parseInt(items.get(2)));

        int retirementAgeInDays =
                getRetirementAgeInDays(manifest.getMainAttributes().getValue("Plugin-Version"));

        if (retirementAgeInDays == -1) {
            return;
        }
        Calendar now = GregorianCalendar.getInstance();
        long nowTimestamp = now.getTimeInMillis();
        long inceptionTimestamp = inceptionDate.getTimeInMillis();
        long days = TimeUnit.DAYS.convert(nowTimestamp - inceptionTimestamp, TimeUnit.MILLISECONDS);
        if (days > retirementAgeInDays) {
            // this plugin is too old.
            String dailyOverride = System.getenv("ANDROID_DAILY_OVERRIDE");
            final MessageDigest crypt;
            try {
                crypt = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                return;
            }
            crypt.reset();
            // encode the day, not the current time.
            try {
                crypt.update(String.format("%1$s:%2$s:%3$s",
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH),
                        now.get(Calendar.DATE)).getBytes("utf8"));
            } catch (UnsupportedEncodingException e) {
                return;
            }
            String overrideValue = new BigInteger(1, crypt.digest()).toString(16);
            if (dailyOverride == null) {
                String message = "Plugin is too old, please update to a more recent version, or " +
                    "set ANDROID_DAILY_OVERRIDE environment variable to \"" + overrideValue + '"';
                System.err.println(message);
                throw new RuntimeException(message);
            } else {
                if (!dailyOverride.equals(overrideValue)) {
                    String message = "Plugin is too old and ANDROID_DAILY_OVERRIDE value is " +
                    "also outdated, please use new value :\"" + overrideValue + '"';
                    System.err.println(message);
                    throw new RuntimeException(message);
                }
            }
        }
    }

    private static int getRetirementAgeInDays(@Nullable String version) {
        if (version == null || version.contains("rc") || version.contains("beta")
                || version.contains("alpha")) {
            return DEFAULT_RETIREMENT_AGE_FOR_NON_RELEASE_IN_DAYS;
        }
        return -1;
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


    protected void apply(Project project) throws IOException {
        this.project = project;

        ExecutionConfigurationUtil.setThreadPoolSize(project);
        checkPathForErrors();
        checkModulesForErrors();

        List<Recorder.Property> propertyList = Lists.newArrayList(
                new Recorder.Property("plugin_version", Version.ANDROID_GRADLE_PLUGIN_VERSION),
                new Recorder.Property("next_gen_plugin", "false"),
                new Recorder.Property("gradle_version", project.getGradle().getGradleVersion())
        );
        String benchmarkName = AndroidGradleOptions.getBenchmarkName(project);
        if (benchmarkName != null) {
            propertyList.add(new Recorder.Property("benchmark_name", benchmarkName));
        }
        String benchmarkMode = AndroidGradleOptions.getBenchmarkMode(project);
        if (benchmarkMode != null) {
            propertyList.add(new Recorder.Property("benchmark_mode", benchmarkMode));
        }

        ProcessRecorderFactory.initialize(
                getLogger(),
                project.getRootProject().file("profiler" + System.currentTimeMillis() + ".json"),
                propertyList);
        project.getGradle().addListener(new RecordingBuildListener(ThreadRecorder.get()));


        ThreadRecorder.get().record(ExecutionType.BASE_PLUGIN_PROJECT_CONFIGURE,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        configureProject();
                        return null;
                    }
                }, new Recorder.Property("project", project.getName()));

        ThreadRecorder.get().record(ExecutionType.BASE_PLUGIN_PROJECT_BASE_EXTENSTION_CREATION,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createExtension();
                        return null;
                    }
                }, new Recorder.Property("project", project.getName()));

        ThreadRecorder.get().record(ExecutionType.BASE_PLUGIN_PROJECT_TASKS_CREATION,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        createTasks();
                        return null;
                    }
                }, new Recorder.Property("project", project.getName()));
    }

    protected void configureProject() {
        checkGradleVersion();
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

        project.getPlugins().apply(JavaBasePlugin.class);

        jacocoPlugin = project.getPlugins().apply(JacocoPlugin.class);

        project.getTasks().getByName("assemble").setDescription(
                "Assembles all variants of all applications and secondary packages.");

        // call back on execution. This is called after the whole build is done (not
        // after the current project is done).
        // This is will be called for each (android) projects though, so this should support
        // being called 2+ times.
        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(Gradle gradle) { }

            @Override
            public void settingsEvaluated(Settings settings) { }

            @Override
            public void projectsLoaded(Gradle gradle) { }

            @Override
            public void projectsEvaluated(Gradle gradle) { }

            @Override
            public void buildFinished(BuildResult buildResult) {
                ExecutorSingleton.shutdown();
                sdkHandler.unload();
                ThreadRecorder.get().record(ExecutionType.BASE_PLUGIN_BUILD_FINISHED,
                        new Recorder.Block() {
                            @Override
                            public Void call() throws Exception {
                                PreDexCache.getCache().clear(
                                        new File(project.getRootProject().getBuildDir(),
                                                FD_INTERMEDIATES + "/dex-cache/cache.xml"),
                                        getLogger());
                                JackConversionCache.getCache().clear(
                                        new File(project.getRootProject().getBuildDir(),
                                                FD_INTERMEDIATES + "/jack-cache/cache.xml"),
                                        getLogger());
                                LibraryCache.getCache().unload();
                                return null;
                            }
                        }, new Recorder.Property("project", project.getName()));

                try {
                    ProcessRecorderFactory.shutdown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
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
                            } else if (task instanceof JillTask) {
                                JackConversionCache.getCache().load(
                                        new File(project.getRootProject().getBuildDir(),
                                                FD_INTERMEDIATES + "/jack-cache/cache.xml"));
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
        final NamedDomainObjectContainer<SigningConfig>  signingConfigContainer = project.container(
                SigningConfig.class,
                new SigningConfigFactory(instantiator));

        extension = project.getExtensions().create("android", getExtensionClass(),
                project, instantiator, androidBuilder, sdkHandler,
                buildTypeContainer, productFlavorContainer, signingConfigContainer,
                extraModelInfo, isLibrary());

        // create the default mapping configuration.
        project.getConfigurations().create("default-mapping")
                .setDescription("Configuration for default mapping artifacts.");
        project.getConfigurations().create("default-metadata")
                .setDescription("Metadata for the produced APKs.");

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

    private void createTasks() {
        ThreadRecorder.get().record(ExecutionType.TASK_MANAGER_CREATE_TASKS,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        taskManager.createTasksBeforeEvaluate(
                                new TaskContainerAdaptor(project.getTasks()));
                        return null;
                    }
                },
                new Recorder.Property("project", project.getName()));

        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                ThreadRecorder.get().record(ExecutionType.BASE_PLUGIN_CREATE_ANDROID_TASKS,
                        new Recorder.Block<Void>() {
                            @Override
                            public Void call() throws Exception {
                                createAndroidTasks(false);
                                return null;
                            }
                        },
                        new Recorder.Property("project", project.getName()));
            }
        });
    }

    private void checkGradleVersion() {
        if (!GRADLE_ACCEPTABLE_VERSIONS.matcher(project.getGradle().getGradleVersion()).matches()) {
            boolean allowNonMatching = Boolean.getBoolean(GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
            File file = new File("gradle" + separator + "wrapper" + separator +
                    "gradle-wrapper.properties");
            String errorMessage = String.format(
                "Gradle version %s is required. Current version is %s. " +
                "If using the gradle wrapper, try editing the distributionUrl in %s " +
                "to gradle-%s-all.zip",
                GRADLE_MIN_VERSION, project.getGradle().getGradleVersion(), file.getAbsolutePath(),
                GRADLE_MIN_VERSION);
            if (allowNonMatching) {
                getLogger().warning(errorMessage);
                getLogger().warning("As %s is set, continuing anyways.",
                        GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
            } else {
                throw new BuildException(errorMessage, null);
            }
        }
    }

    @VisibleForTesting
    final void createAndroidTasks(boolean force) {
        // Make sure unit tests set the required fields.
        checkState(extension.getBuildToolsRevision() != null, "buildToolsVersion is not specified.");
        checkState(extension.getCompileSdkVersion() != null, "compileSdkVersion is not specified.");

        ndkHandler.setCompileSdkVersion(extension.getCompileSdkVersion());

        // get current plugins and look for the default Java plugin.
        if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            throw new BadPluginException(
                    "The 'java' plugin has been applied, but it is not compatible with the Android plugins.");
        }

        ensureTargetSetup();

        // don't do anything if the project was not initialized.
        // Unless TEST_SDK_DIR is set in which case this is unit tests and we don't return.
        // This is because project don't get evaluated in the unit test setup.
        // See AppPluginDslTest
        if (!force
                && (!project.getState().getExecuted() ||  project.getState().getFailure()!= null)
                && SdkHandler.sTestSdkFolder == null) {
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

        taskManager.createMockableJarTask();
        ThreadRecorder.get().record(ExecutionType.VARIANT_MANAGER_CREATE_ANDROID_TASKS,
                new Recorder.Block<Void>() {
                    @Override
                    public Void call() throws Exception {
                        variantManager.createAndroidTasks();
                        ApiObjectFactory apiObjectFactory = new ApiObjectFactory(
                                androidBuilder, extension, variantFactory, instantiator);
                        for (BaseVariantData variantData : variantManager.getVariantDataList())  {
                            apiObjectFactory.create(variantData);
                        }
                        return null;
                    }
                }, new Recorder.Property("project", project.getName()));
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
                        subProject.getPath() );
                throw new StopExecutionException(message);
            } else {
                subProjectsById.put(id, subProject);
            }
        }
    }

    private void checkPathForErrors() {
        // See if the user disabled the check:
        if (Boolean.getBoolean(SKIP_PATH_CHECK_PROPERTY)) {
            return;
        }

        if (project.hasProperty(SKIP_PATH_CHECK_PROPERTY)
                && project.property(SKIP_PATH_CHECK_PROPERTY) instanceof String
                && Boolean.valueOf((String) project.property(SKIP_PATH_CHECK_PROPERTY))) {
            return;
        }

        // See if we're on Windows:
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }

        // See if the path contains non-ASCII characters.
        if (CharMatcher.ASCII.matchesAllOf(project.getRootDir().getAbsolutePath())) {
            return;
        }

        String message = "Your project path contains non-ASCII characters. This will most likely " +
                "cause the build to fail on Windows. Please move your project to a different " +
                "directory. See http://b.android.com/95744 for details. " +
                "This warning can be disabled by using the command line flag -D" +
                SKIP_PATH_CHECK_PROPERTY + "=true, or adding the line " +
                SKIP_PATH_CHECK_PROPERTY + "=true' to gradle.properties file " +
                "in the project directory.";

        throw new StopExecutionException(message);
    }
}
