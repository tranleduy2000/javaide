package com.android.build.gradle;

import com.android.annotations.Nullable;
import com.android.build.gradle.internal.ExtraModelInfo;
import com.android.build.gradle.internal.LoggerWrapper;
import com.android.build.gradle.internal.SdkHandler;
import com.android.build.gradle.internal.process.GradleProcessExecutor;
import com.android.builder.core.AndroidBuilder;
import com.android.builder.internal.compiler.JackConversionCache;
import com.android.builder.internal.compiler.PreDexCache;
import com.android.builder.profile.ExecutionType;
import com.android.builder.profile.ProcessRecorderFactory;
import com.android.builder.profile.Recorder;
import com.android.builder.profile.ThreadRecorder;
import com.android.builder.tasks.Task;
import com.android.ide.common.internal.ExecutorSingleton;
import com.android.utils.ILogger;
import com.duy.android.compiler.library.LibraryCache;
import com.google.common.base.CharMatcher;

import org.gradle.api.Project;
import org.gradle.api.tasks.StopExecutionException;
import org.gradle.internal.reflect.Instantiator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

public class BasePlugin {
    public static final Pattern GRADLE_ACCEPTABLE_VERSIONS = Pattern.compile("2\\.[2-9].*");
    private static final String GRADLE_MIN_VERSION = "2.2";
    private static final String GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY =
            "com.android.build.gradle.overrideVersionCheck";
    private static final String SKIP_PATH_CHECK_PROPERTY =
            "com.android.build.gradle.overridePathCheck";
    /**
     * default retirement age in days since its inception date for RC or beta versions.
     */
    private static final int DEFAULT_RETIREMENT_AGE_FOR_NON_RELEASE_IN_DAYS = 40;

    protected Project project;

    protected SdkHandler sdkHandler;
    protected AndroidBuilder androidBuilder;
    protected Instantiator instantiator;
    protected VariantFactory variantFactory;

    private LoggerWrapper loggerWrapper;
    private ExtraModelInfo extraModelInfo;
    private String creator;

    public BasePlugin() {
        creator = "Android Gradle for Java N-IDE";
    }

    private static int getRetirementAgeInDays(@Nullable String version) {
        if (version == null || version.contains("rc") || version.contains("beta")
                || version.contains("alpha")) {
            return DEFAULT_RETIREMENT_AGE_FOR_NON_RELEASE_IN_DAYS;
        }
        return -1;
    }

    /**
     * Return whether this plugin creates Android library.  Should be overridden if true.
     */
    protected boolean isLibrary() {
        return false;
    }

    protected ILogger getLogger() {
        if (loggerWrapper == null) {
            loggerWrapper = new LoggerWrapper(project.getLogger());
        }

        return loggerWrapper;
    }


    protected void apply(Project project) throws IOException {
        this.project = project;

        checkPathForErrors();
        checkModulesForErrors();

        configureProject();
        createExtension();
        createTasks();

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

        project.getTasks().getByName("assemble").setDescription(
                "Assembles all variants of all applications and secondary packages.");

        // call back on execution. This is called after the whole build is done (not
        // after the current project is done).
        // This is will be called for each (android) projects though, so this should support
        // being called 2+ times.
        project.getGradle().addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(Gradle gradle) {
            }

            @Override
            public void settingsEvaluated(Settings settings) {
            }

            @Override
            public void projectsLoaded(Gradle gradle) {
            }

            @Override
            public void projectsEvaluated(Gradle gradle) {
            }

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

    private void checkGradleVersion() {
//        if (!GRADLE_ACCEPTABLE_VERSIONS.matcher(project.getGradle().getGradleVersion()).matches()) {
//            boolean allowNonMatching = Boolean.getBoolean(GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
//            File file = new File("gradle" + separator + "wrapper" + separator +
//                    "gradle-wrapper.properties");
//            String errorMessage = String.format(
//                    "Gradle version %s is required. Current version is %s. " +
//                            "If using the gradle wrapper, try editing the distributionUrl in %s " +
//                            "to gradle-%s-all.zip",
//                    GRADLE_MIN_VERSION, project.getGradle().getGradleVersion(), file.getAbsolutePath(),
//                    GRADLE_MIN_VERSION);
//            if (allowNonMatching) {
//                getLogger().warning(errorMessage);
//                getLogger().warning("As %s is set, continuing anyways.",
//                        GRADLE_VERSION_CHECK_OVERRIDE_PROPERTY);
//            } else {
//                throw new BuildException(errorMessage, null);
//            }
//        }
    }
}
