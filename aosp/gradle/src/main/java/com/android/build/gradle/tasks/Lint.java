package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.build.gradle.internal.LintGradleClient;
import com.android.build.gradle.internal.dsl.LintOptions;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.DefaultAndroidTask;
import com.android.builder.model.AndroidProject;
import com.android.builder.model.Variant;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.Reporter;
import com.android.tools.lint.Warning;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.checks.GradleDetector;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Severity;
import com.android.utils.StringHelper;
import com.google.common.collect.Maps;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.ParallelizableTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@ParallelizableTask
public class Lint extends DefaultAndroidTask {
    @NonNull
    private LintOptions mLintOptions;
    @Nullable
    private File mSdkHome;
    private boolean mFatalOnly;
    private ToolingModelBuilderRegistry mToolingRegistry;

    private static void syncOptions(@NonNull LintOptions options, @NonNull LintGradleClient client, @NonNull LintCliFlags flags, @NonNull String variantName, @NonNull Project project, boolean report, boolean fatalOnly) {
        options.syncTo(client, flags, variantName, project, report);

        if (fatalOnly || flags.isQuiet()) {
            for (Reporter reporter : flags.getReporters()) {
                reporter.setDisplayEmpty(false);
            }

        }

    }

    private static BuiltinIssueRegistry createIssueRegistry() {
        return new LintGradleIssueRegistry();
    }

    public void setLintOptions(@NonNull LintOptions lintOptions) {
        mLintOptions = lintOptions;
    }

    public void setSdkHome(@NonNull File sdkHome) {
        mSdkHome = sdkHome;
    }

    public void setToolingRegistry(ToolingModelBuilderRegistry toolingRegistry) {
        mToolingRegistry = toolingRegistry;
    }

    public void setFatalOnly(boolean fatalOnly) {
        mFatalOnly = fatalOnly;
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    public void lint() throws IOException {
        AndroidProject modelProject = createAndroidProject(getProject());
        if (getVariantName() != null) {
            lintSingleVariant(modelProject, getVariantName());
        } else {
            lintAllVariants(modelProject);
        }

    }

    /**
     * Runs lint individually on all the variants, and then compares the results
     * across variants and reports these
     */
    public void lintAllVariants(@NonNull AndroidProject modelProject) throws IOException {
        Map<Variant, List<Warning>> warningMap = Maps.newHashMap();
        for (Variant variant : modelProject.getVariants()) {
            List<Warning> warnings = runLint(modelProject, variant.getName(), false);
            warningMap.put(variant, warnings);
        }


        // Compute error matrix
        boolean quiet = mLintOptions.isQuiet();


        for (Map.Entry<Variant, List<Warning>> entry : warningMap.entrySet()) {
            Variant variant = entry.getKey();
            List<Warning> warnings = entry.getValue();
            if (!mFatalOnly && !quiet) {
                System.out.println("Ran lint on variant " + variant.getName() + ": " + warnings.size() + " issues found");
            }

        }


        List<Warning> mergedWarnings = LintGradleClient.merge(warningMap, modelProject);
        int errorCount = 0;
        int warningCount = 0;
        for (Warning warning : mergedWarnings) {
            if (warning.severity.equals(Severity.ERROR) || warning.severity.equals(Severity.FATAL)) {
                errorCount++;
            } else if (warning.severity.equals(Severity.WARNING)) {
                warningCount++;
            }

        }


        IssueRegistry registry = new BuiltinIssueRegistry();
        LintCliFlags flags = new LintCliFlags();
        LintGradleClient client = new LintGradleClient(registry, flags, getProject(), modelProject, mSdkHome, null);
        syncOptions(mLintOptions, client, flags, null, getProject(), true, mFatalOnly);

        for (Reporter reporter : flags.getReporters()) {
            reporter.write(errorCount, warningCount, mergedWarnings);
        }


        if (flags.isSetExitCode() && errorCount > 0) {
            abort();
        }

    }

    private void abort() {
        Object message;
        if (mFatalOnly) {
            message = ""
                    + "Lint found fatal errors while assembling a release target.\n"
                    + "\n" + "To proceed, either fix the issues identified by lint, or modify your build script as follows:\n"
                    + "...\n" + "android {\n"
                    + "    lintOptions {\n"
                    + "        checkReleaseBuilds false\n"
                    + "        // Or, if you prefer, you can continue to check for errors in release builds,\n"
                    + "        // but continue the build even when errors are found:\n"
                    + "        abortOnError false\n"
                    + "    }\n"
                    + "}\n"
                    + "...";
        } else {
            message = "" + "Lint found errors in the project; aborting build.\n" + "\n" + "Fix the issues identified by lint, or add the following to your build script to proceed with errors:\n" + "...\n" + "android {\n" + "    lintOptions {\n" + "        abortOnError false\n" + "    }\n" + "}\n" + "...";
        }

        throw new GradleException((String) message);
    }

    /**
     * Runs lint on a single specified variant
     */
    public void lintSingleVariant(@NonNull AndroidProject modelProject, String variantName) throws IOException {
        runLint(modelProject, variantName, true);
    }

    /**
     * Runs lint on the given variant and returns the set of warnings
     */
    @Nullable
    private List<Warning> runLint(@NonNull AndroidProject modelProject, @NonNull String variantName, boolean report) {
        IssueRegistry registry = createIssueRegistry();
        LintCliFlags flags = new LintCliFlags();
        LintGradleClient client = new LintGradleClient(registry, flags, getProject(), modelProject, mSdkHome, variantName);
        if (mFatalOnly) {
            if (!mLintOptions.isCheckReleaseBuilds()) {
                return null;

            }

            flags.setFatalOnly(true);
        }

        syncOptions(mLintOptions, client, flags, variantName, getProject(), report, mFatalOnly);
        if (!report || mFatalOnly) {
            flags.setQuiet(true);
        }


        List<Warning> warnings;
        try {
            warnings = client.run(registry);
        } catch (IOException e) {
            throw new GradleException("Invalid arguments.", e);
        }


        if (report && client.haveErrors() && flags.isSetExitCode()) {
            abort();
        }


        return warnings;
    }

    private AndroidProject createAndroidProject(@NonNull Project gradleProject) {
        String modelName = AndroidProject.class.getName();
        ToolingModelBuilder modelBuilder = mToolingRegistry.getBuilder(modelName);
        assert modelBuilder != null;
        return (AndroidProject) modelBuilder.buildAll(modelName, gradleProject);
    }

    public static class LintGradleIssueRegistry extends BuiltinIssueRegistry {
        private boolean mInitialized;

        public LintGradleIssueRegistry() {
        }

        @NonNull
        @Override
        public List<Issue> getIssues() {
            List<Issue> issues = super.getIssues();
            if (!mInitialized) {
                mInitialized = true;
                for (Issue issue : issues) {
                    if (issue.getImplementation().getDetectorClass().equals(GradleDetector.class)) {
                        issue.setImplementation(GroovyGradleDetector.IMPLEMENTATION);
                    }

                }

            }


            return issues;
        }
    }

    public static class ConfigAction implements TaskConfigAction<Lint> {
        @NonNull
        private VariantScope scope;

        public ConfigAction(@NonNull VariantScope scope) {
            this.scope = scope;
        }

        @Override
        @NonNull
        public String getName() {
            return scope.getTaskName("lint");
        }

        @Override
        @NonNull
        public Class<Lint> getType() {
            return Lint.class;
        }

        @Override
        public void execute(Lint lint) {
            lint.setLintOptions(scope.getGlobalScope().getExtension().getLintOptions());
            lint.setSdkHome(scope.getGlobalScope().getSdkHandler().getSdkFolder());
            lint.setVariantName(scope.getVariantConfiguration().getFullName());
            lint.setToolingRegistry(scope.getGlobalScope().getToolingRegistry());
            lint.setDescription(("Runs lint on the " +
                    StringHelper.capitalize(scope.getVariantConfiguration().getFullName()) + " build."));
            lint.setGroup(JavaBasePlugin.VERIFICATION_GROUP);
        }

        public VariantScope getScope() {
            return scope;
        }

        public void setScope(VariantScope scope) {
            this.scope = scope;
        }
    }
}
