/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.build.gradle.internal.dsl;

import static com.android.SdkConstants.DOT_XML;
import static com.android.builder.model.AndroidProject.FD_OUTPUTS;
import static com.android.tools.lint.detector.api.Severity.ERROR;
import static com.android.tools.lint.detector.api.Severity.FATAL;
import static com.android.tools.lint.detector.api.Severity.IGNORE;
import static com.android.tools.lint.detector.api.Severity.INFORMATIONAL;
import static com.android.tools.lint.detector.api.Severity.WARNING;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.HtmlReporter;
import com.android.tools.lint.LintCliClient;
import com.android.tools.lint.LintCliFlags;
import com.android.tools.lint.TextReporter;
import com.android.tools.lint.XmlReporter;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

/**
 * DSL object for configuring lint options.
 */
public class LintOptions implements com.android.builder.model.LintOptions, Serializable {
    public static final String STDOUT = "stdout";
    public static final String STDERR = "stderr";
    private static final long serialVersionUID = 1L;

    @NonNull
    private Set<String> disable = Sets.newHashSet();
    @NonNull
    private Set<String> enable = Sets.newHashSet();
    @Nullable
    private Set<String> check = Sets.newHashSet();
    private boolean abortOnError = true;
    private boolean absolutePaths = true;
    private boolean noLines;
    private boolean quiet;
    private boolean checkAllWarnings;
    private boolean ignoreWarnings;
    private boolean warningsAsErrors;
    private boolean showAll;
    private boolean checkReleaseBuilds = true;
    private boolean explainIssues = true;
    @Nullable
    private File lintConfig;
    private boolean textReport;
    @Nullable
    private File textOutput;
    private boolean htmlReport = true;
    @Nullable
    private File htmlOutput;
    private boolean xmlReport = true;
    @Nullable
    private File xmlOutput;

    private Map<String,Severity> severities = Maps.newHashMap();

    public LintOptions() {
    }

    public LintOptions(
            @NonNull Set<String> disable,
            @NonNull Set<String> enable,
            @Nullable Set<String> check,
            @Nullable File lintConfig,
            boolean textReport,
            @Nullable File textOutput,
            boolean htmlReport,
            @Nullable File htmlOutput,
            boolean xmlReport,
            @Nullable File xmlOutput,
            boolean abortOnError,
            boolean absolutePaths,
            boolean noLines,
            boolean quiet,
            boolean checkAllWarnings,
            boolean ignoreWarnings,
            boolean warningsAsErrors,
            boolean showAll,
            boolean explainIssues,
            boolean checkReleaseBuilds,
            @Nullable Map<String,Integer> severityOverrides) {
        this.disable = disable;
        this.enable = enable;
        this.check = check;
        this.lintConfig = lintConfig;
        this.textReport = textReport;
        this.textOutput = textOutput;
        this.htmlReport = htmlReport;
        this.htmlOutput = htmlOutput;
        this.xmlReport = xmlReport;
        this.xmlOutput = xmlOutput;
        this.abortOnError = abortOnError;
        this.absolutePaths = absolutePaths;
        this.noLines = noLines;
        this.quiet = quiet;
        this.checkAllWarnings = checkAllWarnings;
        this.ignoreWarnings = ignoreWarnings;
        this.warningsAsErrors = warningsAsErrors;
        this.showAll = showAll;
        this.explainIssues = explainIssues;
        this.checkReleaseBuilds = checkReleaseBuilds;

        if (severityOverrides != null) {
            for (Map.Entry<String,Integer> entry : severityOverrides.entrySet()) {
                severities.put(entry.getKey(), convert(entry.getValue()));
            }
        }
    }

    @NonNull
    public static com.android.builder.model.LintOptions create(@NonNull com.android.builder.model.LintOptions source) {
        return new LintOptions(
                source.getDisable(),
                source.getEnable(),
                source.getCheck(),
                source.getLintConfig(),
                source.getTextReport(),
                source.getTextOutput(),
                source.getHtmlReport(),
                source.getHtmlOutput(),
                source.getXmlReport(),
                source.getXmlOutput(),
                source.isAbortOnError(),
                source.isAbsolutePaths(),
                source.isNoLines(),
                source.isQuiet(),
                source.isCheckAllWarnings(),
                source.isIgnoreWarnings(),
                source.isWarningsAsErrors(),
                source.isShowAll(),
                source.isExplainIssues(),
                source.isCheckReleaseBuilds(),
                source.getSeverityOverrides()
        );
    }

    /**
     * Returns the set of issue id's to suppress. Callers are allowed to modify this collection.
     */
    @Override
    @NonNull
    @Input
    public Set<String> getDisable() {
        return disable;
    }

    /**
     * Sets the set of issue id's to suppress. Callers are allowed to modify this collection.
     * Note that these ids add to rather than replace the given set of ids.
     */
    public void setDisable(@Nullable Set<String> ids) {
        disable.addAll(ids);
    }

    /**
     * Returns the set of issue id's to enable. Callers are allowed to modify this collection.
     * To enable a given issue, add the issue ID to the returned set.
     */
    @Override
    @NonNull
    @Input
    public Set<String> getEnable() {
        return enable;
    }

    /**
     * Sets the set of issue id's to enable. Callers are allowed to modify this collection.
     * Note that these ids add to rather than replace the given set of ids.
     */
    public void setEnable(@Nullable Set<String> ids) {
        enable.addAll(ids);
    }

    /**
     * Returns the exact set of issues to check, or null to run the issues that are enabled
     * by default plus any issues enabled via {@link #getEnable} and without issues disabled
     * via {@link #getDisable}. If non-null, callers are allowed to modify this collection.
     */
    @Override
    @Nullable
    @Optional
    @Input
    public Set<String> getCheck() {
        return check;
    }

    /**
     * Sets the <b>exact</b> set of issues to check.
     * @param ids the set of issue id's to check
     */
    public void setCheck(@NonNull Set<String> ids) {
        check.addAll(ids);
    }

    /** Whether lint should set the exit code of the process if errors are found */
    @Override
    @Input
    public boolean isAbortOnError() {
        return abortOnError;
    }

    /** Sets whether lint should set the exit code of the process if errors are found */
    public void setAbortOnError(boolean abortOnError) {
        this.abortOnError = abortOnError;
    }

    /**
     * Whether lint should display full paths in the error output. By default the paths
     * are relative to the path lint was invoked from.
     */
    @Override
    @Input
    public boolean isAbsolutePaths() {
        return absolutePaths;
    }

    /**
     * Sets whether lint should display full paths in the error output. By default the paths
     * are relative to the path lint was invoked from.
     */
    public void setAbsolutePaths(boolean absolutePaths) {
        this.absolutePaths = absolutePaths;
    }

    /**
     * Whether lint should include the source lines in the output where errors occurred
     * (true by default)
     */
    @Override
    @Input
    public boolean isNoLines() {
        return this.noLines;
    }

    /**
     * Sets whether lint should include the source lines in the output where errors occurred
     * (true by default)
     */
    public void setNoLines(boolean noLines) {
        this.noLines = noLines;
    }

    /**
     * Returns whether lint should be quiet (for example, not write informational messages
     * such as paths to report files written)
     */
    @Override
    @Input
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Sets whether lint should be quiet (for example, not write informational messages
     * such as paths to report files written)
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /** Returns whether lint should check all warnings, including those off by default */
    @Override
    @Input
    public boolean isCheckAllWarnings() {
        return checkAllWarnings;
    }

    /** Sets whether lint should check all warnings, including those off by default */
    public void setCheckAllWarnings(boolean warnAll) {
        this.checkAllWarnings = warnAll;
    }

    /** Returns whether lint will only check for errors (ignoring warnings) */
    @Override
    @Input
    public boolean isIgnoreWarnings() {
        return ignoreWarnings;
    }

    /** Sets whether lint will only check for errors (ignoring warnings) */
    public void setIgnoreWarnings(boolean noWarnings) {
        this.ignoreWarnings = noWarnings;
    }

    /** Returns whether lint should treat all warnings as errors */
    @Override
    @Input
    public boolean isWarningsAsErrors() {
        return warningsAsErrors;
    }

    /** Sets whether lint should treat all warnings as errors */
    public void setWarningsAsErrors(boolean allErrors) {
        this.warningsAsErrors = allErrors;
    }

    /** Returns whether lint should include explanations for issue errors. (Note that
     * HTML and XML reports intentionally do this unconditionally, ignoring this setting.) */
    @Override
    @Input
    public boolean isExplainIssues() {
        return explainIssues;
    }

    public void setExplainIssues(boolean explainIssues) {
        this.explainIssues = explainIssues;
    }

    /**
     * Returns whether lint should include all output (e.g. include all alternate
     * locations, not truncating long messages, etc.)
     */
    @Override
    @Input
    public boolean isShowAll() {
        return showAll;
    }

    /**
     * Sets whether lint should include all output (e.g. include all alternate
     * locations, not truncating long messages, etc.)
     */
    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    /**
     * Returns whether lint should check for fatal errors during release builds. Default is true.
     * If issues with severity "fatal" are found, the release build is aborted.
     */
    @Override
    @Input
    public boolean isCheckReleaseBuilds() {
        return checkReleaseBuilds;
    }

    public void setCheckReleaseBuilds(boolean checkReleaseBuilds) {
        this.checkReleaseBuilds = checkReleaseBuilds;
    }

    /**
     * Returns the default configuration file to use as a fallback
     */
    @Override
    @Optional @InputFile
    public File getLintConfig() {
        return lintConfig;
    }

    /** Whether we should write an text report. Default false. The location can be
     * controlled by {@link #getTextOutput()}. */
    @Override
    @Input
    public boolean getTextReport() {
        return textReport;
    }

    public void setTextReport(boolean textReport) {
        this.textReport = textReport;
    }

    public void setHtmlReport(boolean htmlReport) {
        this.htmlReport = htmlReport;
    }

    public void setHtmlOutput(@NonNull File htmlOutput) {
        this.htmlOutput = htmlOutput;
    }

    public void setXmlReport(boolean xmlReport) {
        this.xmlReport = xmlReport;
    }

    public void setXmlOutput(@NonNull File xmlOutput) {
        this.xmlOutput = xmlOutput;
    }

    /**
     * The optional path to where a text report should be written. The special value
     * "stdout" can be used to point to standard output.
     */
    @Override
    @Nullable
    @Optional
    @Input
    public File getTextOutput() {
        return textOutput;
    }

    /** Whether we should write an HTML report. Default true. The location can be
     * controlled by {@link #getHtmlOutput()}. */
    @Override
    @Input
    public boolean getHtmlReport() {
        return htmlReport;
    }

    /** The optional path to where an HTML report should be written */
    @Override
    @Nullable
    @Optional
    @OutputFile
    public File getHtmlOutput() {
        return htmlOutput;
    }

    /** Whether we should write an XML report. Default true. The location can be
     * controlled by {@link #getXmlOutput()}. */
    @Override
    @Input
    public boolean getXmlReport() {
        return xmlReport;
    }

    /** The optional path to where an XML report should be written */
    @Override
    @Nullable
    @Optional
    @OutputFile
    public File getXmlOutput() {
        return xmlOutput;
    }

    /**
     * Sets the default config file to use as a fallback. This corresponds to a {@code lint.xml}
     * file with severities etc to use when a project does not have more specific information.
     */
    public void setLintConfig(@NonNull File lintConfig) {
        this.lintConfig = lintConfig;
    }

    public void syncTo(
            @NonNull LintCliClient client,
            @NonNull LintCliFlags flags,
            @Nullable String variantName,
            @Nullable org.gradle.api.Project project,
            boolean report) {
        if (disable != null) {
            flags.getSuppressedIds().addAll(disable);
        }
        if (enable != null) {
            flags.getEnabledIds().addAll(enable);
        }
        if (check != null && !check.isEmpty()) {
            flags.setExactCheckedIds(check);
        }
        flags.setSetExitCode(this.abortOnError);
        flags.setFullPath(absolutePaths);
        flags.setShowSourceLines(!noLines);
        flags.setQuiet(quiet);
        flags.setCheckAllWarnings(checkAllWarnings);
        flags.setIgnoreWarnings(ignoreWarnings);
        flags.setWarningsAsErrors(warningsAsErrors);
        flags.setShowEverything(showAll);
        flags.setDefaultConfiguration(lintConfig);
        flags.setSeverityOverrides(severities);
        flags.setExplainIssues(explainIssues);

        if (report || flags.isFatalOnly() && this.abortOnError) {
            if (textReport || flags.isFatalOnly()) {
                File output = textOutput;
                if (output == null) {
                    output = new File(flags.isFatalOnly() ? STDERR: STDOUT);
                } else if (!output.isAbsolute() && !isStdOut(output) && !isStdErr(output)) {
                    output = project.file(output.getPath());
                }
                output = validateOutputFile(output);

                Writer writer;
                File file = null;
                boolean closeWriter;
                if (isStdOut(output)) {
                    writer = new PrintWriter(System.out, true);
                    closeWriter = false;
                } else if (isStdErr(output)) {
                    writer = new PrintWriter(System.err, true);
                    closeWriter = false;
                } else {
                    file = output;
                    try {
                        writer = new BufferedWriter(new FileWriter(output));
                    } catch (IOException e) {
                        throw new org.gradle.api.GradleException("Text invalid argument.", e);
                    }
                    closeWriter = true;
                }
                flags.getReporters().add(new TextReporter(client, flags, file, writer,
                        closeWriter));
            }
            if (htmlReport) {
                File output = htmlOutput;
                if (output == null || flags.isFatalOnly()) {
                    output = createOutputPath(project, variantName, ".html", flags.isFatalOnly());
                } else if (!output.isAbsolute()) {
                    output = project.file(output.getPath());
                }
                output = validateOutputFile(output);
                try {
                    flags.getReporters().add(new HtmlReporter(client, output));
                } catch (IOException e) {
                    throw new GradleException("HTML invalid argument.", e);
                }
            }
            if (xmlReport) {
                File output = xmlOutput;
                if (output == null || flags.isFatalOnly()) {
                    output = createOutputPath(project, variantName, DOT_XML, flags.isFatalOnly());
                } else if (!output.isAbsolute()) {
                    output = project.file(output.getPath());
                }
                output = validateOutputFile(output);
                try {
                    flags.getReporters().add(new XmlReporter(client, output));
                } catch (IOException e) {
                    throw new org.gradle.api.GradleException("XML invalid argument.", e);
                }
            }
        }
    }

    private static boolean isStdOut(@NonNull File output) {
        return STDOUT.equals(output.getPath());
    }

    private static boolean isStdErr(@NonNull File output) {
        return STDERR.equals(output.getPath());
    }

    @NonNull
    private static File validateOutputFile(@NonNull File output) {
        if (isStdOut(output) || isStdErr(output)) {
            return output;
        }

        File parent = output.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        output = output.getAbsoluteFile();
        if (output.exists()) {
            boolean delete = output.delete();
            if (!delete) {
                throw new org.gradle.api.GradleException("Could not delete old " + output);
            }
        }
        if (output.getParentFile() != null && !output.getParentFile().canWrite()) {
            throw new org.gradle.api.GradleException("Cannot write output file " + output);
        }

        return output;
    }

    private static File createOutputPath(
            @NonNull org.gradle.api.Project project,
            @NonNull String variantName,
            @NonNull String extension,
            boolean fatalOnly) {
        StringBuilder base = new StringBuilder();
        base.append(FD_OUTPUTS);
        base.append(File.separator);
        base.append("lint-results");
        if (variantName != null) {
            base.append("-");
            base.append(variantName);
        }
        if (fatalOnly) {
            base.append("-fatal");
        }
        base.append(extension);
        return new File(project.getBuildDir(), base.toString());
    }

    /**
     * An optional map of severity overrides. The map maps from issue id's to the corresponding
     * severity to use, which must be "fatal", "error", "warning", or "ignore".
     *
     * @return a map of severity overrides, or null. The severities are one of the constants
     *  {@link #SEVERITY_FATAL}, {@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING},
     *  {@link #SEVERITY_INFORMATIONAL}, {@link #SEVERITY_IGNORE}
     */
    @Override
    @Nullable
    public Map<String, Integer> getSeverityOverrides() {
        if (severities == null || severities.isEmpty()) {
            return null;
        }

        Map<String, Integer> map =
                Maps.newHashMapWithExpectedSize(severities.size());
        for (Map.Entry<String,Severity> entry : severities.entrySet()) {
            map.put(entry.getKey(), convert(entry.getValue()));
        }

        return map;
    }

    // -- DSL Methods.

    /**
     * Adds the id to the set of issues to check.
     */
    public void check(String id) {
        check.add(id);
    }

    /**
     * Adds the ids to the set of issues to check.
     */
    public void check(String... ids) {
        for (String id : ids) {
            check(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void enable(String id) {
        enable.add(id);
        Issue issue = new BuiltinIssueRegistry().getIssue(id);
        severities.put(id, issue != null ? issue.getDefaultSeverity() : WARNING);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void enable(String... ids) {
        for (String id : ids) {
            enable(id);
        }
    }

    /**
     * Adds the id to the set of issues to enable.
     */
    public void disable(String id) {
        disable.add(id);
        severities.put(id, IGNORE);
    }

    /**
     * Adds the ids to the set of issues to enable.
     */
    public void disable(String... ids) {
        for (String id : ids) {
            disable(id);
        }
    }

    // For textOutput 'stdout' or 'stderr' (normally a file)
    public void textOutput(String textOutput) {
        this.textOutput = new File(textOutput);
    }

    // For textOutput file()
    public void textOutput(File textOutput) {
        this.textOutput = textOutput;
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void fatal(String id) {
        severities.put(id, FATAL);
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void fatal(String... ids) {
        for (String id : ids) {
            fatal(id);
        }
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void error(String id) {
        severities.put(id, ERROR);
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void error(String... ids) {
        for (String id : ids) {
            error(id);
        }
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void warning(String id) {
        severities.put(id, WARNING);
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void warning(String... ids) {
        for (String id : ids) {
            warning(id);
        }
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void ignore(String id) {
        severities.put(id, IGNORE);
    }

    /**
     * Adds a severity override for the given issues.
     */
    public void ignore(String... ids) {
        for (String id : ids) {
            ignore(id);
        }
    }

    // Without these qualifiers, Groovy compilation will fail with "Apparent variable
    // 'SEVERITY_FATAL' was found in a static scope but doesn't refer to a local variable,
    // static field or class"
    //@SuppressWarnings("UnnecessaryQualifiedReference")
    private static int convert(Severity s) {
        switch (s) {
            case FATAL:
                return com.android.builder.model.LintOptions.SEVERITY_FATAL;
            case ERROR:
                return com.android.builder.model.LintOptions.SEVERITY_ERROR;
            case WARNING:
                return com.android.builder.model.LintOptions.SEVERITY_WARNING;
            case INFORMATIONAL:
                return com.android.builder.model.LintOptions.SEVERITY_INFORMATIONAL;
            case IGNORE:
            default:
                return com.android.builder.model.LintOptions.SEVERITY_IGNORE;
        }
    }

    //@SuppressWarnings("UnnecessaryQualifiedReference")
    private static Severity convert(int s) {
        switch (s) {
            case com.android.builder.model.LintOptions.SEVERITY_FATAL:
                return FATAL;
            case com.android.builder.model.LintOptions.SEVERITY_ERROR:
                return ERROR;
            case com.android.builder.model.LintOptions.SEVERITY_WARNING:
                return WARNING;
            case com.android.builder.model.LintOptions.SEVERITY_INFORMATIONAL:
                return INFORMATIONAL;
            case com.android.builder.model.LintOptions.SEVERITY_IGNORE:
            default:
                return IGNORE;
        }
    }
}
