/*
 * Copyright (C) 2011 The Android Open Source Project
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

import static com.android.SdkConstants.DOT_XML;
import static com.android.SdkConstants.VALUE_NONE;
import static com.android.tools.lint.LintCliFlags.ERRNO_ERRORS;
import static com.android.tools.lint.LintCliFlags.ERRNO_EXISTS;
import static com.android.tools.lint.LintCliFlags.ERRNO_HELP;
import static com.android.tools.lint.LintCliFlags.ERRNO_INVALID_ARGS;
import static com.android.tools.lint.LintCliFlags.ERRNO_SUCCESS;
import static com.android.tools.lint.LintCliFlags.ERRNO_USAGE;
import static com.android.tools.lint.detector.api.LintUtils.endsWith;
import static com.android.tools.lint.detector.api.TextFormat.TEXT;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.checks.BuiltinIssueRegistry;
import com.android.tools.lint.client.api.Configuration;
import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.client.api.LintDriver;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LintUtils;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.TextFormat;
import com.android.utils.SdkUtils;
import com.google.common.annotations.Beta;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Command line driver for the lint framework
 * <p>
 * <b>NOTE: This is not a public or final API; if you rely on this be prepared
 * to adjust your code for the next tools release.</b>
 */
@Beta
public class Main {
    static final int MAX_LINE_WIDTH = 78;
    private static final String ARG_ENABLE     = "--enable";       //$NON-NLS-1$
    private static final String ARG_DISABLE    = "--disable";      //$NON-NLS-1$
    private static final String ARG_CHECK      = "--check";        //$NON-NLS-1$
    private static final String ARG_IGNORE     = "--ignore";       //$NON-NLS-1$
    private static final String ARG_LIST_IDS   = "--list";         //$NON-NLS-1$
    private static final String ARG_SHOW       = "--show";         //$NON-NLS-1$
    private static final String ARG_QUIET      = "--quiet";        //$NON-NLS-1$
    private static final String ARG_FULL_PATH  = "--fullpath";     //$NON-NLS-1$
    private static final String ARG_SHOW_ALL   = "--showall";      //$NON-NLS-1$
    private static final String ARG_HELP       = "--help";         //$NON-NLS-1$
    private static final String ARG_NO_LINES   = "--nolines";      //$NON-NLS-1$
    private static final String ARG_HTML       = "--html";         //$NON-NLS-1$
    private static final String ARG_SIMPLE_HTML= "--simplehtml";   //$NON-NLS-1$
    private static final String ARG_XML        = "--xml";          //$NON-NLS-1$
    private static final String ARG_TEXT       = "--text";         //$NON-NLS-1$
    private static final String ARG_CONFIG     = "--config";       //$NON-NLS-1$
    private static final String ARG_URL        = "--url";          //$NON-NLS-1$
    private static final String ARG_VERSION    = "--version";      //$NON-NLS-1$
    private static final String ARG_EXIT_CODE  = "--exitcode";     //$NON-NLS-1$
    private static final String ARG_CLASSES    = "--classpath";    //$NON-NLS-1$
    private static final String ARG_SOURCES    = "--sources";      //$NON-NLS-1$
    private static final String ARG_RESOURCES  = "--resources";    //$NON-NLS-1$
    private static final String ARG_LIBRARIES  = "--libraries";    //$NON-NLS-1$

    private static final String ARG_NO_WARN_2  = "--nowarn";       //$NON-NLS-1$
    // GCC style flag names for options
    private static final String ARG_NO_WARN_1  = "-w";             //$NON-NLS-1$
    private static final String ARG_WARN_ALL   = "-Wall";          //$NON-NLS-1$
    private static final String ARG_ALL_ERROR  = "-Werror";        //$NON-NLS-1$

    private static final String PROP_WORK_DIR = "com.android.tools.lint.workdir"; //$NON-NLS-1$

    private LintCliFlags mFlags = new LintCliFlags();
    private IssueRegistry mGlobalRegistry;

    /** Creates a CLI driver */
    public Main() {
    }

    /**
     * Runs the static analysis command line driver
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        new Main().run(args);
    }

    /**
     * Runs the static analysis command line driver
     *
     * @param args program arguments
     */
    @SuppressWarnings("UnnecessaryLocalVariable")
    public void run(String[] args) {
        if (args.length < 1) {
            printUsage(System.err);
            System.exit(ERRNO_USAGE);
        }


        // When running lint from the command line, warn if the project is a Gradle project
        // since those projects may have custom project configuration that the command line
        // runner won't know about.
        LintCliClient client = new LintCliClient(mFlags) {
            @NonNull
            @Override
            protected Project createProject(@NonNull File dir, @NonNull File referenceDir) {
                Project project = super.createProject(dir, referenceDir);
                if (project.isGradleProject()) {
                    @SuppressWarnings("SpellCheckingInspection")
                    String message = String.format("\"`%1$s`\" is a Gradle project. To correctly "
                            + "analyze Gradle projects, you should run \"`gradlew :lint`\" instead.",
                            project.getName());
                    Location location = Location.create(project.getDir());
                    Context context = new Context(mDriver, project, project, project.getDir());
                    if (context.isEnabled(IssueRegistry.LINT_ERROR) &&
                            !getConfiguration(project, null).isIgnored(context,
                                    IssueRegistry.LINT_ERROR, location, message)) {
                        report(context,
                                IssueRegistry.LINT_ERROR,
                                project.getConfiguration(null).getSeverity(
                                        IssueRegistry.LINT_ERROR), location, message,
                                TextFormat.RAW);
                    }
                }
                return project;
            }

            @NonNull
            @Override
            public Configuration getConfiguration(@NonNull final Project project,
                    @Nullable LintDriver driver) {
                if (project.isGradleProject()) {
                    // Don't report any issues when analyzing a Gradle project from the
                    // non-Gradle runner; they are likely to be false, and will hide the real
                    // problem reported above
                   return new CliConfiguration(getConfiguration(), project, true) {
                       @NonNull
                       @Override
                       public Severity getSeverity(@NonNull Issue issue) {
                           return issue == IssueRegistry.LINT_ERROR
                                   ? Severity.FATAL : Severity.IGNORE;
                       }

                       @Override
                       public boolean isIgnored(@NonNull Context context, @NonNull Issue issue,
                               @Nullable Location location, @NonNull String message) {
                           // If you've deliberately ignored IssueRegistry.LINT_ERROR
                           // don't flag that one either
                           if (issue == IssueRegistry.LINT_ERROR && new LintCliClient(mFlags).isSuppressed(
                                   IssueRegistry.LINT_ERROR)) {
                               return true;
                           }

                           return issue != IssueRegistry.LINT_ERROR;
                       }
                   };
                }
                return super.getConfiguration(project, driver);
            }
        };

        // Mapping from file path prefix to URL. Applies only to HTML reports
        String urlMap = null;

        List<File> files = new ArrayList<File>();
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];

            if (arg.equals(ARG_HELP)
                    || arg.equals("-h") || arg.equals("-?")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (index < args.length - 1) {
                    String topic = args[index + 1];
                    if (topic.equals("suppress") || topic.equals("ignore")) {
                        printHelpTopicSuppress();
                        System.exit(ERRNO_HELP);
                    } else {
                        System.err.println(String.format("Unknown help topic \"%1$s\"", topic));
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                }
                printUsage(System.out);
                System.exit(ERRNO_HELP);
            } else if (arg.equals(ARG_LIST_IDS)) {
                IssueRegistry registry = getGlobalRegistry(client);
                // Did the user provide a category list?
                if (index < args.length - 1 && !args[index + 1].startsWith("-")) { //$NON-NLS-1$
                    String[] ids = args[++index].split(",");
                    for (String id : ids) {
                        if (registry.isCategoryName(id)) {
                            // List all issues with the given category
                            String category = id;
                            for (Issue issue : registry.getIssues()) {
                                // Check prefix such that filtering on the "Usability" category
                                // will match issue category "Usability:Icons" etc.
                                if (issue.getCategory().getName().startsWith(category) ||
                                        issue.getCategory().getFullName().startsWith(category)) {
                                    listIssue(System.out, issue);
                                }
                            }
                        } else {
                            System.err.println("Invalid category \"" + id + "\".\n");
                            displayValidIds(registry, System.err);
                            System.exit(ERRNO_INVALID_ARGS);
                        }
                    }
                } else {
                    displayValidIds(registry, System.out);
                }
                System.exit(ERRNO_SUCCESS);
            } else if (arg.equals(ARG_SHOW)) {
                IssueRegistry registry = getGlobalRegistry(client);
                // Show specific issues?
                if (index < args.length - 1 && !args[index + 1].startsWith("-")) { //$NON-NLS-1$
                    String[] ids = args[++index].split(",");
                    for (String id : ids) {
                        if (registry.isCategoryName(id)) {
                            // Show all issues in the given category
                            String category = id;
                            for (Issue issue : registry.getIssues()) {
                                // Check prefix such that filtering on the "Usability" category
                                // will match issue category "Usability:Icons" etc.
                                if (issue.getCategory().getName().startsWith(category) ||
                                        issue.getCategory().getFullName().startsWith(category)) {
                                    describeIssue(issue);
                                    System.out.println();
                                }
                            }
                        } else if (registry.isIssueId(id)) {
                            describeIssue(registry.getIssue(id));
                            System.out.println();
                        } else {
                            System.err.println("Invalid id or category \"" + id + "\".\n");
                            displayValidIds(registry, System.err);
                            System.exit(ERRNO_INVALID_ARGS);
                        }
                    }
                } else {
                    showIssues(registry);
                }
                System.exit(ERRNO_SUCCESS);
            } else if (arg.equals(ARG_FULL_PATH)
                    || arg.equals(ARG_FULL_PATH + "s")) { // allow "--fullpaths" too
                mFlags.setFullPath(true);
            } else if (arg.equals(ARG_SHOW_ALL)) {
                mFlags.setShowEverything(true);
            } else if (arg.equals(ARG_QUIET) || arg.equals("-q")) {
                mFlags.setQuiet(true);
            } else if (arg.equals(ARG_NO_LINES)) {
                mFlags.setShowSourceLines(false);
            } else if (arg.equals(ARG_EXIT_CODE)) {
                mFlags.setSetExitCode(true);
            } else if (arg.equals(ARG_VERSION)) {
                printVersion(client);
                System.exit(ERRNO_SUCCESS);
            } else if (arg.equals(ARG_URL)) {
                if (index == args.length - 1) {
                    System.err.println("Missing URL mapping string");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                String map = args[++index];
                // Allow repeated usage of the argument instead of just comma list
                if (urlMap != null) {
                    urlMap = urlMap + ',' + map;
                } else {
                    urlMap = map;
                }
            } else if (arg.equals(ARG_CONFIG)) {
                if (index == args.length - 1 || !endsWith(args[index + 1], DOT_XML)) {
                    System.err.println("Missing XML configuration file argument");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                File file = getInArgumentPath(args[++index]);
                if (!file.exists()) {
                    System.err.println(file.getAbsolutePath() + " does not exist");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                mFlags.setDefaultConfiguration(file);
            } else if (arg.equals(ARG_HTML) || arg.equals(ARG_SIMPLE_HTML)) {
                if (index == args.length - 1) {
                    System.err.println("Missing HTML output file name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                File output = getOutArgumentPath(args[++index]);
                // Get an absolute path such that we can ask its parent directory for
                // write permission etc.
                output = output.getAbsoluteFile();
                if (output.isDirectory() ||
                        (!output.exists() && output.getName().indexOf('.') == -1)) {
                    if (!output.exists()) {
                        boolean mkdirs = output.mkdirs();
                        if (!mkdirs) {
                            log(null, "Could not create output directory %1$s", output);
                            System.exit(ERRNO_EXISTS);
                        }
                    }
                    try {
                        MultiProjectHtmlReporter reporter =
                                new MultiProjectHtmlReporter(client, output);
                        if (arg.equals(ARG_SIMPLE_HTML)) {
                            reporter.setSimpleFormat(true);
                        }
                        mFlags.getReporters().add(reporter);
                    } catch (IOException e) {
                        log(e, null);
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    continue;
                }
                if (output.exists()) {
                    boolean delete = output.delete();
                    if (!delete) {
                        System.err.println("Could not delete old " + output);
                        System.exit(ERRNO_EXISTS);
                    }
                }
                if (output.getParentFile() != null && !output.getParentFile().canWrite()) {
                    System.err.println("Cannot write HTML output file " + output);
                    System.exit(ERRNO_EXISTS);
                }
                try {
                    HtmlReporter htmlReporter = new HtmlReporter(client, output);
                    if (arg.equals(ARG_SIMPLE_HTML)) {
                        htmlReporter.setSimpleFormat(true);
                    }
                    mFlags.getReporters().add(htmlReporter);
                } catch (IOException e) {
                    log(e, null);
                    System.exit(ERRNO_INVALID_ARGS);
                }
            } else if (arg.equals(ARG_XML)) {
                if (index == args.length - 1) {
                    System.err.println("Missing XML output file name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                File output = getOutArgumentPath(args[++index]);
                // Get an absolute path such that we can ask its parent directory for
                // write permission etc.
                output = output.getAbsoluteFile();

                if (output.exists()) {
                    boolean delete = output.delete();
                    if (!delete) {
                        System.err.println("Could not delete old " + output);
                        System.exit(ERRNO_EXISTS);
                    }
                }
                if (output.getParentFile() != null && !output.getParentFile().canWrite()) {
                    System.err.println("Cannot write XML output file " + output);
                    System.exit(ERRNO_EXISTS);
                }
                try {
                    mFlags.getReporters().add(new XmlReporter(client, output));
                } catch (IOException e) {
                    log(e, null);
                    System.exit(ERRNO_INVALID_ARGS);
                }
            } else if (arg.equals(ARG_TEXT)) {
                if (index == args.length - 1) {
                    System.err.println("Missing text output file name");
                    System.exit(ERRNO_INVALID_ARGS);
                }

                Writer writer = null;
                boolean closeWriter;
                String outputName = args[++index];
                if (outputName.equals("stdout")) { //$NON-NLS-1$
                    //noinspection IOResourceOpenedButNotSafelyClosed
                    writer = new PrintWriter(System.out, true);
                    closeWriter = false;
                } else {
                    File output = getOutArgumentPath(outputName);

                    // Get an absolute path such that we can ask its parent directory for
                    // write permission etc.
                    output = output.getAbsoluteFile();

                    if (output.exists()) {
                        boolean delete = output.delete();
                        if (!delete) {
                            System.err.println("Could not delete old " + output);
                            System.exit(ERRNO_EXISTS);
                        }
                    }
                    if (output.getParentFile() != null && !output.getParentFile().canWrite()) {
                        System.err.println("Cannot write text output file " + output);
                        System.exit(ERRNO_EXISTS);
                    }
                    try {
                        //noinspection IOResourceOpenedButNotSafelyClosed
                        writer = new BufferedWriter(new FileWriter(output));
                    } catch (IOException e) {
                        log(e, null);
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    closeWriter = true;
                }
                mFlags.getReporters().add(new TextReporter(client, mFlags, writer, closeWriter));
            } else if (arg.equals(ARG_DISABLE) || arg.equals(ARG_IGNORE)) {
                if (index == args.length - 1) {
                    System.err.println("Missing categories or id's to disable");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                IssueRegistry registry = getGlobalRegistry(client);
                String[] ids = args[++index].split(",");
                for (String id : ids) {
                    if (registry.isCategoryName(id)) {
                        // Suppress all issues with the given category
                        String category = id;
                        for (Issue issue : registry.getIssues()) {
                            // Check prefix such that filtering on the "Usability" category
                            // will match issue category "Usability:Icons" etc.
                            if (issue.getCategory().getName().startsWith(category) ||
                                    issue.getCategory().getFullName().startsWith(category)) {
                                mFlags.getSuppressedIds().add(issue.getId());
                            }
                        }
                    } else if (!registry.isIssueId(id)) {
                        System.err.println("Invalid id or category \"" + id + "\".\n");
                        displayValidIds(registry, System.err);
                        System.exit(ERRNO_INVALID_ARGS);
                    } else {
                        mFlags.getSuppressedIds().add(id);
                    }
                }
            } else if (arg.equals(ARG_ENABLE)) {
                if (index == args.length - 1) {
                    System.err.println("Missing categories or id's to enable");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                IssueRegistry registry = getGlobalRegistry(client);
                String[] ids = args[++index].split(",");
                for (String id : ids) {
                    if (registry.isCategoryName(id)) {
                        // Enable all issues with the given category
                        String category = id;
                        for (Issue issue : registry.getIssues()) {
                            if (issue.getCategory().getName().startsWith(category) ||
                                    issue.getCategory().getFullName().startsWith(category)) {
                                mFlags.getEnabledIds().add(issue.getId());
                            }
                        }
                    } else if (!registry.isIssueId(id)) {
                        System.err.println("Invalid id or category \"" + id + "\".\n");
                        displayValidIds(registry, System.err);
                        System.exit(ERRNO_INVALID_ARGS);
                    } else {
                        mFlags.getEnabledIds().add(id);
                    }
                }
            } else if (arg.equals(ARG_CHECK)) {
                if (index == args.length - 1) {
                    System.err.println("Missing categories or id's to check");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                Set<String> checkedIds = mFlags.getExactCheckedIds();
                if (checkedIds == null) {
                    checkedIds = new HashSet<String>();
                    mFlags.setExactCheckedIds(checkedIds);
                }
                IssueRegistry registry = getGlobalRegistry(client);
                String[] ids = args[++index].split(",");
                for (String id : ids) {
                    if (registry.isCategoryName(id)) {
                        // Check all issues with the given category
                        String category = id;
                        for (Issue issue : registry.getIssues()) {
                            // Check prefix such that filtering on the "Usability" category
                            // will match issue category "Usability:Icons" etc.
                            if (issue.getCategory().getName().startsWith(category) ||
                                    issue.getCategory().getFullName().startsWith(category)) {
                                checkedIds.add(issue.getId());
                            }
                        }
                    } else if (!registry.isIssueId(id)) {
                        System.err.println("Invalid id or category \"" + id + "\".\n");
                        displayValidIds(registry, System.err);
                        System.exit(ERRNO_INVALID_ARGS);
                    } else {
                        checkedIds.add(id);
                    }
                }
            } else if (arg.equals(ARG_NO_WARN_1) || arg.equals(ARG_NO_WARN_2)) {
                mFlags.setIgnoreWarnings(true);
            } else if (arg.equals(ARG_WARN_ALL)) {
                mFlags.setCheckAllWarnings(true);
            } else if (arg.equals(ARG_ALL_ERROR)) {
                mFlags.setWarningsAsErrors(true);
            } else if (arg.equals(ARG_CLASSES)) {
                if (index == args.length - 1) {
                    System.err.println("Missing class folder name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                String paths = args[++index];
                for (String path : LintUtils.splitPath(paths)) {
                    File input = getInArgumentPath(path);
                    if (!input.exists()) {
                        System.err.println("Class path entry " + input + " does not exist.");
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    List<File> classes = mFlags.getClassesOverride();
                    if (classes == null) {
                        classes = new ArrayList<File>();
                        mFlags.setClassesOverride(classes);
                    }
                    classes.add(input);
                }
            } else if (arg.equals(ARG_SOURCES)) {
                if (index == args.length - 1) {
                    System.err.println("Missing source folder name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                String paths = args[++index];
                for (String path : LintUtils.splitPath(paths)) {
                    File input = getInArgumentPath(path);
                    if (!input.exists()) {
                        System.err.println("Source folder " + input + " does not exist.");
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    List<File> sources = mFlags.getSourcesOverride();
                    if (sources == null) {
                        sources = new ArrayList<File>();
                        mFlags.setSourcesOverride(sources);
                    }
                    sources.add(input);
                }
            } else if (arg.equals(ARG_RESOURCES)) {
                if (index == args.length - 1) {
                    System.err.println("Missing resource folder name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                String paths = args[++index];
                for (String path : LintUtils.splitPath(paths)) {
                    File input = getInArgumentPath(path);
                    if (!input.exists()) {
                        System.err.println("Resource folder " + input + " does not exist.");
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    List<File> resources = mFlags.getResourcesOverride();
                    if (resources == null) {
                        resources = new ArrayList<File>();
                        mFlags.setResourcesOverride(resources);
                    }
                    resources.add(input);
                }
            } else if (arg.equals(ARG_LIBRARIES)) {
                if (index == args.length - 1) {
                    System.err.println("Missing library folder name");
                    System.exit(ERRNO_INVALID_ARGS);
                }
                String paths = args[++index];
                for (String path : LintUtils.splitPath(paths)) {
                    File input = getInArgumentPath(path);
                    if (!input.exists()) {
                        System.err.println("Library " + input + " does not exist.");
                        System.exit(ERRNO_INVALID_ARGS);
                    }
                    List<File> libraries = mFlags.getLibrariesOverride();
                    if (libraries == null) {
                        libraries = new ArrayList<File>();
                        mFlags.setLibrariesOverride(libraries);
                    }
                    libraries.add(input);
                }
            } else if (arg.startsWith("--")) {
                System.err.println("Invalid argument " + arg + "\n");
                printUsage(System.err);
                System.exit(ERRNO_INVALID_ARGS);
            } else {
                String filename = arg;
                File file = getInArgumentPath(filename);

                if (!file.exists()) {
                    System.err.println(String.format("%1$s does not exist.", filename));
                    System.exit(ERRNO_EXISTS);
                }
                files.add(file);
            }
        }

        if (files.isEmpty()) {
            System.err.println("No files to analyze.");
            System.exit(ERRNO_INVALID_ARGS);
        } else if (files.size() > 1
                && (mFlags.getClassesOverride() != null
                    || mFlags.getSourcesOverride() != null
                    || mFlags.getLibrariesOverride() != null
                    || mFlags.getResourcesOverride() != null)) {
            System.err.println(String.format(
                  "The %1$s, %2$s, %3$s and %4$s arguments can only be used with a single project",
                  ARG_SOURCES, ARG_CLASSES, ARG_LIBRARIES, ARG_RESOURCES));
            System.exit(ERRNO_INVALID_ARGS);
        }

        List<Reporter> reporters = mFlags.getReporters();
        if (reporters.isEmpty()) {
            //noinspection VariableNotUsedInsideIf
            if (urlMap != null) {
                System.err.println(String.format(
                        "Warning: The %1$s option only applies to HTML reports (%2$s)",
                            ARG_URL, ARG_HTML));
            }

            reporters.add(new TextReporter(client, mFlags,
                    new PrintWriter(System.out, true), false));
        } else {
            //noinspection VariableNotUsedInsideIf
            if (urlMap != null) {
                for (Reporter reporter : reporters) {
                    if (!reporter.isSimpleFormat()) {
                        reporter.setBundleResources(true);
                    }
                }

                if (!urlMap.equals(VALUE_NONE)) {
                    Map<String, String> map = new HashMap<String, String>();
                    String[] replace = urlMap.split(","); //$NON-NLS-1$
                    for (String s : replace) {
                        // Allow ='s in the suffix part
                        int index = s.indexOf('=');
                        if (index == -1) {
                            System.err.println(
                              "The URL map argument must be of the form 'path_prefix=url_prefix'");
                            System.exit(ERRNO_INVALID_ARGS);
                        }
                        String key = s.substring(0, index);
                        String value = s.substring(index + 1);
                        map.put(key, value);
                    }
                    for (Reporter reporter : reporters) {
                        reporter.setUrlMap(map);
                    }
                }
            }
        }

        try {
            // Not using mGlobalRegistry; LintClient will do its own registry merging
            // also including project rules.
            int exitCode = client.run(new BuiltinIssueRegistry(), files);
            System.exit(exitCode);
        } catch (IOException e) {
            log(e, null);
            System.exit(ERRNO_INVALID_ARGS);
        }
    }

    private IssueRegistry getGlobalRegistry(LintCliClient client) {
        if (mGlobalRegistry == null) {
            mGlobalRegistry = client.addCustomLintRules(new BuiltinIssueRegistry());
        }

        return mGlobalRegistry;
    }

    /**
     * Converts a relative or absolute command-line argument into an input file.
     *
     * @param filename The filename given as a command-line argument.
     * @return A File matching filename, either absolute or relative to lint.workdir if defined.
     */
    private static File getInArgumentPath(String filename) {
        File file = new File(filename);

        if (!file.isAbsolute()) {
            File workDir = getLintWorkDir();
            if (workDir != null) {
                File file2 = new File(workDir, filename);
                if (file2.exists()) {
                    try {
                        file = file2.getCanonicalFile();
                    } catch (IOException e) {
                        file = file2;
                    }
                }
            }
        }
        return file;
    }

    /**
     * Converts a relative or absolute command-line argument into an output file.
     * <p/>
     * The difference with {@code getInArgumentPath} is that we can't check whether the
     * a relative path turned into an absolute compared to lint.workdir actually exists.
     *
     * @param filename The filename given as a command-line argument.
     * @return A File matching filename, either absolute or relative to lint.workdir if defined.
     */
    private static File getOutArgumentPath(String filename) {
        File file = new File(filename);

        if (!file.isAbsolute()) {
            File workDir = getLintWorkDir();
            if (workDir != null) {
                File file2 = new File(workDir, filename);
                try {
                    file = file2.getCanonicalFile();
                } catch (IOException e) {
                    file = file2;
                }
            }
        }
        return file;
    }

    /**
     * Returns the File corresponding to the system property or the environment variable
     * for {@link #PROP_WORK_DIR}.
     * This property is typically set by the SDK/tools/lint[.bat] wrapper.
     * It denotes the path where the command-line client was originally invoked from
     * and can be used to convert relative input/output paths.
     *
     * @return A new File corresponding to {@link #PROP_WORK_DIR} or null.
     */
    @Nullable
    private static File getLintWorkDir() {
        // First check the Java properties (e.g. set using "java -jar ... -Dname=value")
        String path = System.getProperty(PROP_WORK_DIR);
        if (path == null || path.isEmpty()) {
            // If not found, check environment variables.
            path = System.getenv(PROP_WORK_DIR);
        }
        if (path != null && !path.isEmpty()) {
            return new File(path);
        }
        return null;
    }

    private static void printHelpTopicSuppress() {
        System.out.println(wrap(TextFormat.RAW.convertTo(getSuppressHelp(), TextFormat.TEXT)));
    }

    static String getSuppressHelp() {
        // \\u00a0 is a non-breaking space
        final String NBSP = "\u00a0\u00a0\u00a0\u00a0";

        return
            "Lint errors can be suppressed in a variety of ways:\n" +
            "\n" +
            "1. With a `@SuppressLint` annotation in the Java code\n" +
            "2. With a `tools:ignore` attribute in the XML file\n" +
            "3. With ignore flags specified in the `build.gradle` file, " +
                "as explained below\n" +
            "4. With a `lint.xml` configuration file in the project\n" +
            "5. With a `lint.xml` configuration file passed to lint " +
                "via the " + ARG_CONFIG + " flag\n" +
            "6. With the " + ARG_IGNORE + " flag passed to lint.\n" +
            "\n" +
            "To suppress a lint warning with an annotation, add " +
            "a `@SuppressLint(\"id\")` annotation on the class, method " +
            "or variable declaration closest to the warning instance " +
            "you want to disable. The id can be one or more issue " +
            "id's, such as `\"UnusedResources\"` or `{\"UnusedResources\"," +
            "\"UnusedIds\"}`, or it can be `\"all\"` to suppress all lint " +
            "warnings in the given scope.\n" +
            "\n" +
            "To suppress a lint warning in an XML file, add a " +
            "`tools:ignore=\"id\"` attribute on the element containing " +
            "the error, or one of its surrounding elements. You also " +
            "need to define the namespace for the tools prefix on the " +
            "root element in your document, next to the `xmlns:android` " +
            "declaration:\n" +
            "`xmlns:tools=\"http://schemas.android.com/tools\"`\n" +
            "\n" +
            "To suppress a lint warning in a `build.gradle` file, add a " +
            "section like this:\n" +
            "\n" +
            "android {\n" +
            NBSP + "lintOptions {\n" +
            NBSP + NBSP + "disable 'TypographyFractions','TypographyQuotes'\n" +
            NBSP + "}\n" +
            "}\n" +
            "\n" +
            "Here we specify a comma separated list of issue id's after the " +
            "disable command. You can also use `warning` or `error` instead " +
            "of `disable` to change the severity of issues.\n" +
            "\n" +
            "To suppress lint warnings with a configuration XML file, " +
            "create a file named `lint.xml` and place it at the root " +
            "directory of the project in which it applies.\n" +
            "\n" +
            "The format of the `lint.xml` file is something like the " +
            "following:\n" +
            "\n" +
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<lint>\n" +
            NBSP + "<!-- Disable this given check in this project -->\n" +
            NBSP + "<issue id=\"IconMissingDensityFolder\" severity=\"ignore\" />\n" +
            "\n" +
            NBSP + "<!-- Ignore the ObsoleteLayoutParam issue in the given files -->\n" +
            NBSP + "<issue id=\"ObsoleteLayoutParam\">\n" +
            NBSP + NBSP + "<ignore path=\"res/layout/activation.xml\" />\n" +
            NBSP + NBSP + "<ignore path=\"res/layout-xlarge/activation.xml\" />\n" +
            NBSP + "</issue>\n" +
            "\n" +
            NBSP + "<!-- Ignore the UselessLeaf issue in the given file -->\n" +
            NBSP + "<issue id=\"UselessLeaf\">\n" +
            NBSP + NBSP + "<ignore path=\"res/layout/main.xml\" />\n" +
            NBSP + "</issue>\n" +
            "\n" +
            NBSP + "<!-- Change the severity of hardcoded strings to \"error\" -->\n" +
            NBSP + "<issue id=\"HardcodedText\" severity=\"error\" />\n" +
            "</lint>\n" +
            "\n" +
            "To suppress lint checks from the command line, pass the " + ARG_IGNORE +  " " +
            "flag with a comma separated list of ids to be suppressed, such as:\n" +
            "`$ lint --ignore UnusedResources,UselessLeaf /my/project/path`\n" +
            "\n" +
            "For more information, see " +
            "http://g.co/androidstudio/suppressing-lint-warnings\n";
    }

    private static void printVersion(LintCliClient client) {
        String revision = client.getRevision();
        if (revision != null) {
            System.out.println(String.format("lint: version %1$s", revision));
        } else {
            System.out.println("lint: unknown version");
        }
    }

    private static void displayValidIds(IssueRegistry registry, PrintStream out) {
        List<Category> categories = registry.getCategories();
        out.println("Valid issue categories:");
        for (Category category : categories) {
            out.println("    " + category.getFullName());
        }
        out.println();
        List<Issue> issues = registry.getIssues();
        out.println("Valid issue id's:");
        for (Issue issue : issues) {
            listIssue(out, issue);
        }
    }

    private static void listIssue(PrintStream out, Issue issue) {
        out.print(wrapArg("\"" + issue.getId() + "\": " + issue.getBriefDescription(TEXT)));
    }

    private static void showIssues(IssueRegistry registry) {
        List<Issue> issues = registry.getIssues();
        List<Issue> sorted = new ArrayList<Issue>(issues);
        Collections.sort(sorted, new Comparator<Issue>() {
            @Override
            public int compare(Issue issue1, Issue issue2) {
                int d = issue1.getCategory().compareTo(issue2.getCategory());
                if (d != 0) {
                    return d;
                }
                d = issue2.getPriority() - issue1.getPriority();
                if (d != 0) {
                    return d;
                }

                return issue1.getId().compareTo(issue2.getId());
            }
        });

        System.out.println("Available issues:\n");
        Category previousCategory = null;
        for (Issue issue : sorted) {
            Category category = issue.getCategory();
            if (!category.equals(previousCategory)) {
                String name = category.getFullName();
                System.out.println(name);
                for (int i = 0, n = name.length(); i < n; i++) {
                    System.out.print('=');
                }
                System.out.println('\n');
                previousCategory = category;
            }

            describeIssue(issue);
            System.out.println();
        }
    }

    private static void describeIssue(Issue issue) {
        System.out.println(issue.getId());
        for (int i = 0; i < issue.getId().length(); i++) {
            System.out.print('-');
        }
        System.out.println();
        System.out.println(wrap("Summary: " + issue.getBriefDescription(TEXT)));
        System.out.println("Priority: " + issue.getPriority() + " / 10");
        System.out.println("Severity: " + issue.getDefaultSeverity().getDescription());
        System.out.println("Category: " + issue.getCategory().getFullName());

        if (!issue.isEnabledByDefault()) {
            System.out.println("NOTE: This issue is disabled by default!");
            System.out.println(String.format("You can enable it by adding %1$s %2$s", ARG_ENABLE,
                    issue.getId()));
        }

        System.out.println();
        System.out.println(wrap(issue.getExplanation(TEXT)));
        List<String> moreInfo = issue.getMoreInfo();
        if (!moreInfo.isEmpty()) {
            System.out.println("More information: ");
            for (String uri : moreInfo) {
                System.out.println(uri);
            }
        }
    }

    static String wrapArg(String explanation) {
        // Wrap arguments such that the wrapped lines are not showing up in the left column
        return wrap(explanation, MAX_LINE_WIDTH, "      ");
    }

    static String wrap(String explanation) {
        return wrap(explanation, MAX_LINE_WIDTH, "");
    }

    static String wrap(String explanation, int lineWidth, String hangingIndent) {
        return SdkUtils.wrap(explanation, lineWidth, hangingIndent);
    }

    private static void printUsage(PrintStream out) {
        // TODO: Look up launcher script name!
        String command = "lint"; //$NON-NLS-1$

        out.println("Usage: " + command + " [flags] <project directories>\n");
        out.println("Flags:\n");

        printUsage(out, new String[] {
            ARG_HELP, "This message.",
            ARG_HELP + " <topic>", "Help on the given topic, such as \"suppress\".",
            ARG_LIST_IDS, "List the available issue id's and exit.",
            ARG_VERSION, "Output version information and exit.",
            ARG_EXIT_CODE, "Set the exit code to " + ERRNO_ERRORS + " if errors are found.",
            ARG_SHOW, "List available issues along with full explanations.",
            ARG_SHOW + " <ids>", "Show full explanations for the given list of issue id's.",

            "", "\nEnabled Checks:",
            ARG_DISABLE + " <list>", "Disable the list of categories or " +
                "specific issue id's. The list should be a comma-separated list of issue " +
                "id's or categories.",
            ARG_ENABLE + " <list>", "Enable the specific list of issues. " +
                "This checks all the default issues plus the specifically enabled issues. The " +
                "list should be a comma-separated list of issue id's or categories.",
            ARG_CHECK + " <list>", "Only check the specific list of issues. " +
                "This will disable everything and re-enable the given list of issues. " +
                "The list should be a comma-separated list of issue id's or categories.",
            ARG_NO_WARN_1 + ", " + ARG_NO_WARN_2, "Only check for errors (ignore warnings)",
            ARG_WARN_ALL, "Check all warnings, including those off by default",
            ARG_ALL_ERROR, "Treat all warnings as errors",
            ARG_CONFIG + " <filename>", "Use the given configuration file to " +
                    "determine whether issues are enabled or disabled. If a project contains " +
                    "a lint.xml file, then this config file will be used as a fallback.",


            "", "\nOutput Options:",
            ARG_QUIET, "Don't show progress.",
            ARG_FULL_PATH, "Use full paths in the error output.",
            ARG_SHOW_ALL, "Do not truncate long messages, lists of alternate locations, etc.",
            ARG_NO_LINES, "Do not include the source file lines with errors " +
                "in the output. By default, the error output includes snippets of source code " +
                "on the line containing the error, but this flag turns it off.",
            ARG_HTML + " <filename>", "Create an HTML report instead. If the filename is a " +
                "directory (or a new filename without an extension), lint will create a " +
                "separate report for each scanned project.",
            ARG_URL + " filepath=url", "Add links to HTML report, replacing local " +
                "path prefixes with url prefix. The mapping can be a comma-separated list of " +
                "path prefixes to corresponding URL prefixes, such as " +
                "C:\\temp\\Proj1=http://buildserver/sources/temp/Proj1.  To turn off linking " +
                "to files, use " + ARG_URL + " " + VALUE_NONE,
            ARG_SIMPLE_HTML + " <filename>", "Create a simple HTML report",
            ARG_XML + " <filename>", "Create an XML report instead.",

            "", "\nProject Options:",
            ARG_RESOURCES + " <dir>", "Add the given folder (or path) as a resource directory " +
                "for the project. Only valid when running lint on a single project.",
            ARG_SOURCES + " <dir>", "Add the given folder (or path) as a source directory for " +
                "the project. Only valid when running lint on a single project.",
            ARG_CLASSES + " <dir>", "Add the given folder (or jar file, or path) as a class " +
                "directory for the project. Only valid when running lint on a single project.",
            ARG_LIBRARIES + " <dir>", "Add the given folder (or jar file, or path) as a class " +
                    "library for the project. Only valid when running lint on a single project.",

            "", "\nExit Status:",
            "0",                                 "Success.",
            Integer.toString(ERRNO_ERRORS),      "Lint errors detected.",
            Integer.toString(ERRNO_USAGE),       "Lint usage.",
            Integer.toString(ERRNO_EXISTS),      "Cannot clobber existing file.",
            Integer.toString(ERRNO_HELP),        "Lint help.",
            Integer.toString(ERRNO_INVALID_ARGS), "Invalid command-line argument.",
        });
    }

    private static void printUsage(PrintStream out, String[] args) {
        int argWidth = 0;
        for (int i = 0; i < args.length; i += 2) {
            String arg = args[i];
            argWidth = Math.max(argWidth, arg.length());
        }
        argWidth += 2;
        StringBuilder sb = new StringBuilder(20);
        for (int i = 0; i < argWidth; i++) {
            sb.append(' ');
        }
        String indent = sb.toString();
        String formatString = "%1$-" + argWidth + "s%2$s"; //$NON-NLS-1$

        for (int i = 0; i < args.length; i += 2) {
            String arg = args[i];
            String description = args[i + 1];
            if (arg.isEmpty()) {
                out.println(description);
            } else {
                out.print(wrap(String.format(formatString, arg, description),
                        MAX_LINE_WIDTH, indent));
            }
        }
    }

    public void log(
            @Nullable Throwable exception,
            @Nullable String format,
            @Nullable Object... args) {
        System.out.flush();
        if (!mFlags.isQuiet()) {
            // Place the error message on a line of its own since we're printing '.' etc
            // with newlines during analysis
            System.err.println();
        }
        if (format != null) {
            System.err.println(String.format(format, args));
        }
        if (exception != null) {
            exception.printStackTrace();
        }
    }
}
