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

package com.android.tools.lint;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Severity;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Flags used by the {@link LintCliClient}
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
public class LintCliFlags {
    private final Set<String> mSuppress = new HashSet<String>();
    private final Set<String> mEnabled = new HashSet<String>();
    private Map<String,Severity> mSeverities;
    private Set<String> mCheck = null;
    private boolean mSetExitCode;
    private boolean mFullPath;
    private boolean mShowLines = true;
    private final List<Reporter> mReporters = Lists.newArrayList();
    private boolean mQuiet;
    private boolean mWarnAll;
    private boolean mNoWarnings;
    private boolean mAllErrors;
    private boolean mFatalOnly;
    private boolean mExplainIssues;
    private List<File> mSources;
    private List<File> mClasses;
    private List<File> mLibraries;
    private List<File> mResources;

    private File mDefaultConfiguration;
    private boolean mShowAll;

    public static final int ERRNO_SUCCESS = 0;
    public static final int ERRNO_ERRORS = 1;
    public static final int ERRNO_USAGE = 2;
    public static final int ERRNO_EXISTS = 3;
    public static final int ERRNO_HELP = 4;
    public static final int ERRNO_INVALID_ARGS = 5;

    /**
     * Returns the set of issue id's to suppress. Callers are allowed to modify this collection.
     * To suppress a given issue, add the {@link Issue#getId()} to the returned set.
     */
    @NonNull
    public Set<String> getSuppressedIds() {
        return mSuppress;
    }

    /**
     * Returns the set of issue id's to enable. Callers are allowed to modify this collection.
     * To enable a given issue, add the {@link Issue#getId()} to the returned set.
     */
    @NonNull
    public Set<String> getEnabledIds() {
        return mEnabled;
    }

    /**
     * Returns a map of manually configured severities to use
     * @return the severity to use for a given issue id
     */
    @NonNull
    public Map<String,Severity> getSeverityOverrides() {
        return mSeverities == null ? Collections.<String,Severity>emptyMap() : mSeverities;
    }

    /**
     * Returns the exact set of issues to check, or null to run the issues that are enabled
     * by default plus any issues enabled via {@link #getEnabledIds} and without issues disabled
     * via {@link #getSuppressedIds}. If non-null, callers are allowed to modify this collection.
     */
    @Nullable
    public Set<String> getExactCheckedIds() {
        return mCheck;
    }

    /**
     * Sets the <b>exact</b> set of issues to check.
     * @param check the set of issue id's to check
     */
    public void setExactCheckedIds(@Nullable Set<String> check) {
        mCheck = check;
    }

    /** Whether lint should set the exit code of the process if errors are found */
    public boolean isSetExitCode() {
        return mSetExitCode;
    }

    /** Sets whether lint should set the exit code of the process if errors are found */
    public void setSetExitCode(boolean setExitCode) {
        mSetExitCode = setExitCode;
    }

    /**
     * Whether lint should display full paths in the error output. By default the paths
     * are relative to the path lint was invoked from.
     */
    public boolean isFullPath() {
        return mFullPath;
    }

    /**
     * Sets whether lint should display full paths in the error output. By default the paths
     * are relative to the path lint was invoked from.
     */
    public void setFullPath(boolean fullPath) {
        mFullPath = fullPath;
    }

    /**
     * Whether lint should include the source lines in the output where errors occurred
     * (true by default)
     */
    public boolean isShowSourceLines() {
        return mShowLines;
    }

    /**
     * Sets whether lint should include the source lines in the output where errors occurred
     * (true by default)
     */
    public void setShowSourceLines(boolean showLines) {
        mShowLines = showLines;
    }

    /**
     * Returns the list of error reports to generate. Clients can modify the returned
     * list and add additional reporters such as {@link XmlReporter} and {@link HtmlReporter}.
     */
    @NonNull
    public List<Reporter> getReporters() {
        return mReporters;
    }

    /**
     * Returns whether lint should be quiet (for example, not show progress dots for each analyzed
     * file)
     */
    public boolean isQuiet() {
        return mQuiet;
    }

    /**
     * Sets whether lint should be quiet (for example, not show progress dots for each analyzed
     * file)
     */
    public void setQuiet(boolean quiet) {
        mQuiet = quiet;
    }

    /** Returns whether lint should check all warnings, including those off by default */
    public boolean isCheckAllWarnings() {
        return mWarnAll;
    }

    /** Sets whether lint should check all warnings, including those off by default */
    public void setCheckAllWarnings(boolean warnAll) {
        mWarnAll = warnAll;
    }

    /** Returns whether lint will only check for errors (ignoring warnings) */
    public boolean isIgnoreWarnings() {
        return mNoWarnings;
    }

    /** Sets whether lint will only check for errors (ignoring warnings) */
    public void setIgnoreWarnings(boolean noWarnings) {
        mNoWarnings = noWarnings;
    }

    /** Returns whether lint should treat all warnings as errors */
    public boolean isWarningsAsErrors() {
        return mAllErrors;
    }

    /** Sets whether lint should treat all warnings as errors */
    public void setWarningsAsErrors(boolean allErrors) {
        mAllErrors = allErrors;
    }

    /**
     * Returns whether lint should include all output (e.g. include all alternate
     * locations, not truncating long messages, etc.)
     */
    public boolean isShowEverything() {
        return mShowAll;
    }

    /**
     * Sets whether lint should include all output (e.g. include all alternate
     * locations, not truncating long messages, etc.)
     */
    public void setShowEverything(boolean showAll) {
        mShowAll = showAll;
    }

    /**
     * Returns the default configuration file to use as a fallback
     */
    @Nullable
    public File getDefaultConfiguration() {
        return mDefaultConfiguration;
    }

    /**
     * Sets the default config file to use as a fallback. This corresponds to a {@code lint.xml}
     * file with severities etc to use when a project does not have more specific information.
     * To construct a configuration from a {@link java.io.File}, use
     * {@link LintCliClient#createConfigurationFromFile(java.io.File)}.
     */
    public void setDefaultConfiguration(@Nullable File defaultConfiguration) {
        mDefaultConfiguration = defaultConfiguration;
    }

    /**
     * Gets the optional <b>manual override</b> of the source directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    @Nullable
    public List<File> getSourcesOverride() {
        return mSources;
    }

    /**
     * Sets the optional <b>manual override</b> of the source directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    public void setSourcesOverride(@Nullable List<File> sources) {
        mSources = sources;
    }

    /**
     * Gets the optional <b>manual override</b> of the class file directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    @Nullable
    public List<File> getClassesOverride() {
        return mClasses;
    }

    /**
     * Sets the optional <b>manual override</b> of the class file directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    public void setClassesOverride(@Nullable List<File> classes) {
        mClasses = classes;
    }

    /**
     * Gets the optional <b>manual override</b> of the library directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    @Nullable
    public List<File> getLibrariesOverride() {
        return mLibraries;
    }

    /**
     * Sets the optional <b>manual override</b> of the library directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    public void setLibrariesOverride(@Nullable List<File> libraries) {
        mLibraries = libraries;
    }

    /**
     * Gets the optional <b>manual override</b> of the resources directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    @Nullable
    public List<File> getResourcesOverride() {
        return mResources;
    }

    /**
     * Gets the optional <b>manual override</b> of the resource directories. Normally null.
     * <p>
     * Normally, the source, library and resource paths for a project should be computed
     * by the {@link LintClient} itself, using available project metadata.
     * However, the user can set the source paths explicitly. This is normally done
     * when running lint on raw source code without proper metadata (or when using a
     * build system unknown to lint, such as say {@code make}.
     */
    public void setResourcesOverride(@Nullable List<File> resources) {
        mResources = resources;
    }

    /**
     * Returns true if we should only check fatal issues
     * @return true if we should only check fatal issues
     */
    public boolean isFatalOnly() {
        return mFatalOnly;
    }

    /**
     * Sets whether we should only check fatal issues
     * @param fatalOnly if true, only check fatal issues
     */
    public void setFatalOnly(boolean fatalOnly) {
        mFatalOnly = fatalOnly;
    }

    /**
     * Sets a map of severities to use
     * @param severities map from issue id to severity
     */
    public void setSeverityOverrides(@NonNull Map<String, Severity> severities) {
        mSeverities = severities;
    }

    /**
     * Whether text reports should include full explanation texts. (HTML and XML reports always
     * do, unconditionally.)
     *
     * @return true if text reports should include explanation text
     */
    public boolean isExplainIssues() {
        return mExplainIssues;
    }

    /**
     * Sets whether text reports should include full explanation texts. (HTML and XML reports
     * always do, unconditionally.)
     *
     * @param explainText true if text reports should include explanation text
     */
    public void setExplainIssues(boolean explainText) {
        mExplainIssues = explainText;
    }
}
