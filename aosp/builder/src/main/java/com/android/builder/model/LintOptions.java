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

package com.android.builder.model;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Options for lint.
 * Example:
 *
 * <pre>
 *
 * android {
 *    lintOptions {
 *          // set to true to turn off analysis progress reporting by lint
 *          quiet true
 *          // if true, stop the gradle build if errors are found
 *          abortOnError false
 *          // set to true to have all release builds run lint on issues with severity=fatal
 *          // and abort the build (controlled by abortOnError above) if fatal issues are found
 *          checkReleaseBuilds true
 *          // if true, only report errors
 *          ignoreWarnings true
 *          // if true, emit full/absolute paths to files with errors (true by default)
 *          //absolutePaths true
 *          // if true, check all issues, including those that are off by default
 *          checkAllWarnings true
 *          // if true, treat all warnings as errors
 *          warningsAsErrors true
 *          // turn off checking the given issue id's
 *          disable 'TypographyFractions','TypographyQuotes'
 *          // turn on the given issue id's
 *          enable 'RtlHardcoded','RtlCompat', 'RtlEnabled'
 *          // check *only* the given issue id's
 *          check 'NewApi', 'InlinedApi'
 *          // if true, don't include source code lines in the error output
 *          noLines true
 *          // if true, show all locations for an error, do not truncate lists, etc.
 *          showAll true
 *          // whether lint should include full issue explanations in the text error output
 *          explainIssues false
 *          // Fallback lint configuration (default severities, etc.)
 *          lintConfig file("default-lint.xml")
 *          // if true, generate a text report of issues (false by default)
 *          textReport true
 *          // location to write the output; can be a file or 'stdout' or 'stderr'
 *          //textOutput 'stdout'
 *          textOutput file("lint-results.txt")
 *          // if true, generate an XML report for use by for example Jenkins
 *          xmlReport true
 *          // file to write report to (if not specified, defaults to lint-results.xml)
 *          xmlOutput file("lint-report.xml")
 *          // if true, generate an HTML report (with issue explanations, sourcecode, etc)
 *          htmlReport true
 *          // optional path to report (default will be lint-results.html in the builddir)
 *          htmlOutput file("lint-report.html")
 *          // Set the severity of the given issues to fatal (which means they will be
 *          // checked during release builds (even if the lint target is not included)
 *          fatal 'NewApi', 'InlineApi'
 *          // Set the severity of the given issues to error
 *          error 'Wakelock', 'TextViewEdits'
 *          // Set the severity of the given issues to warning
 *          warning 'ResourceAsColor'
 *          // Set the severity of the given issues to ignore (same as disabling the check)
 *          ignore 'TypographyQuotes'
 *     }
 * }
 * </pre>
 */
public interface LintOptions {
    /**
     * Returns the set of issue id's to suppress. Callers are allowed to modify this collection.
     * To suppress a given issue, add the lint issue id to the returned set.
     */
    @NonNull
    Set<String> getDisable();

    /**
     * Returns the set of issue id's to enable. Callers are allowed to modify this collection.
     * To enable a given issue, add the lint issue id to the returned set.
     */
    @NonNull
    Set<String> getEnable();

    /**
     * Returns the exact set of issues to check, or null to run the issues that are enabled
     * by default plus any issues enabled via {@link #getEnable} and without issues disabled
     * via {@link #getDisable}. If non-null, callers are allowed to modify this collection.
     */
    @Nullable
    Set<String> getCheck();

    /** Whether lint should abort the build if errors are found */
    boolean isAbortOnError();

    /**
     * Whether lint should display full paths in the error output. By default the paths
     * are relative to the path lint was invoked from.
     */
    boolean isAbsolutePaths();

    /**
     * Whether lint should include the source lines in the output where errors occurred
     * (true by default)
     */
    boolean isNoLines();

    /**
     * Returns whether lint should be quiet (for example, not write informational messages
     * such as paths to report files written)
     */
    boolean isQuiet();

    /** Returns whether lint should check all warnings, including those off by default */
    boolean isCheckAllWarnings();

    /** Returns whether lint will only check for errors (ignoring warnings) */
    boolean isIgnoreWarnings();

    /** Returns whether lint should treat all warnings as errors */
    boolean isWarningsAsErrors();

    /** Returns whether lint should include explanations for issue errors. (Note that
     * HTML and XML reports intentionally do this unconditionally, ignoring this setting.) */
    boolean isExplainIssues();

    /**
     * Returns whether lint should include all output (e.g. include all alternate
     * locations, not truncating long messages, etc.)
     */
    boolean isShowAll();

    /**
     * Returns an optional path to a lint.xml configuration file
     */
    @Nullable
    File getLintConfig();

    /** Whether we should write an text report. Default false. The location can be
     * controlled by {@link #getTextOutput()}. */
    boolean getTextReport();

    /**
     * The optional path to where a text report should be written. The special value
     * "stdout" can be used to point to standard output.
     */
    @Nullable
    File getTextOutput();

    /** Whether we should write an HTML report. Default true. The location can be
     * controlled by {@link #getHtmlOutput()}. */
    boolean getHtmlReport();

    /** The optional path to where an HTML report should be written */
    @Nullable
    File getHtmlOutput();

    /** Whether we should write an XML report. Default true. The location can be
     * controlled by {@link #getXmlOutput()}. */
    boolean getXmlReport();

    /** The optional path to where an XML report should be written */
    @Nullable
    File getXmlOutput();

    /**
     * Returns whether lint should check for fatal errors during release builds. Default is true.
     * If issues with severity "fatal" are found, the release build is aborted.
     */
    boolean isCheckReleaseBuilds();

    /**
     * An optional map of severity overrides. The map maps from issue id's to the corresponding
     * severity to use, which must be "fatal", "error", "warning", or "ignore".
     *
     * @return a map of severity overrides, or null. The severities are one of the constants
     *  {@link #SEVERITY_FATAL}, {@link #SEVERITY_ERROR}, {@link #SEVERITY_WARNING},
     *  {@link #SEVERITY_INFORMATIONAL}, {@link #SEVERITY_IGNORE}
     */
    @Nullable
    Map<String, Integer> getSeverityOverrides();

    /** A severity for Lint. Corresponds to com.android.tools.lint.detector.api.Severity#FATAL */
    int SEVERITY_FATAL         = 1;
    /** A severity for Lint. Corresponds to com.android.tools.lint.detector.api.Severity#ERROR */
    int SEVERITY_ERROR         = 2;
    /** A severity for Lint. Corresponds to com.android.tools.lint.detector.api.Severity#WARNING */
    int SEVERITY_WARNING       = 3;
    /** A severity for Lint. Corresponds to com.android.tools.lint.detector.api.Severity#INFORMATIONAL */
    int SEVERITY_INFORMATIONAL = 4;
    /** A severity for Lint. Corresponds to com.android.tools.lint.detector.api.Severity#IGNORE */
    int SEVERITY_IGNORE        = 5;
}
