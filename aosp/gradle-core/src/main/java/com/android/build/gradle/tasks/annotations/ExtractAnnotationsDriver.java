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

package com.android.build.gradle.tasks.annotations;

import static com.android.SdkConstants.DOT_JAVA;
import static java.io.File.pathSeparator;
import static java.io.File.pathSeparatorChar;

import com.android.annotations.NonNull;
import com.android.tools.lint.EcjParser;
import com.android.utils.Pair;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The extract annotations driver is a command line interface to extracting annotations
 * from a source tree. It's similar to the gradle
 * {@link com.android.build.gradle.tasks.ExtractAnnotations} task,
 * but usable from the command line and outside Gradle, for example
 * to extract annotations from the Android framework itself (which is not built with
 * Gradle). It also allows other options only interesting for extracting
 * platform annotations, such as filtering all APIs and constants through an
 * API white-list (such that we for example can pull annotations from the master
 * branch which has the latest metadata, but only expose APIs that are actually in
 * a released platform), as well as translating android.annotation annotations into
 * android.support.annotations.
 */
public class ExtractAnnotationsDriver {
    public static void main(String[] args) {
        new ExtractAnnotationsDriver().run(args);
    }

    private static void usage(PrintStream output) {
        output.println("Usage: " + ExtractAnnotationsDriver.class.getSimpleName() + " <flags>");
        output.println(" --sources <paths>       : Source directories to extract annotations from. ");
        output.println("                           Separate paths with " + pathSeparator + ", and you can use @ ");
        output.println("                           as a filename prefix to have the filenames fed from a file");
        output.println("--classpath <paths>      : Directories and .jar files to resolve symbols from");
        output.println("--output <zip path>      : The .zip file to write the extracted annotations to, if any");
        output.println("--proguard <path>        : The proguard.cfg file to write the keep rules to, if any");
        output.println();
        output.println("Optional flags:");
        output.println("--merge-zips <paths>     : Existing external annotation files to merge in");
        output.println("--quiet                  : Don't print summary information");
        output.println("--rmtypedefs <folder>    : Remove typedef classes found in the given folder");
        output.println("--allow-missing-types    : Don't fail even if some types can't be resolved");
        output.println("--allow-errors           : Don't fail even if there are some compiler errors");
        output.println("--encoding <encoding>    : Encoding (defaults to utf-8)");
        output.println("--language-level <level> : Java source language level, typically 1.6 (default) or 1.7");
        output.println("--api-filter <api.txt>   : A framework API definition to restrict included APIs to");
        output.println("--hide-filtered          : If filtering out non-APIs, supply this flag to hide listing matches");
        output.println("--skip-class-retention   : Don't extract annotations that have class retention");
        System.exit(-1);
    }

    @SuppressWarnings("MethodMayBeStatic")
    public void run(@NonNull String[] args) {
        List<String> classpath = Lists.newArrayList();
        List<File> sources = Lists.newArrayList();
        List<File> mergePaths = Lists.newArrayList();
        List<File> apiFilters = null;
        File rmTypeDefs = null;
        boolean verbose = true;
        boolean allowMissingTypes = false;
        boolean allowErrors = false;
        boolean listFiltered = true;
        boolean skipClassRetention = false;

        String encoding = Charsets.UTF_8.name();
        File output = null;
        File proguard = null;
        long languageLevel = EcjParser.getLanguageLevel(1, 7);
        if (args.length == 1 && "--help".equals(args[0])) {
            usage(System.out);
        }
        if (args.length < 2) {
            usage(System.err);
        }
        for (int i = 0, n = args.length; i < n; i++) {
            String flag = args[i];

            if (flag.equals("--quiet")) {
                verbose = false;
                continue;
            } else if (flag.equals("--allow-missing-types")) {
                allowMissingTypes = true;
                continue;
            } else if (flag.equals("--allow-errors")) {
                allowErrors = true;
                continue;
            } else if (flag.equals("--hide-filtered")) {
                listFiltered = false;
                continue;
            } else if (flag.equals("--skip-class-retention")) {
                skipClassRetention = true;
                continue;
            }
            if (i == n - 1) {
                usage(System.err);
            }
            String value = args[i + 1];
            i++;

            if (flag.equals("--sources")) {
                sources = getFiles(value);
            } else if (flag.equals("--classpath")) {
                classpath = getPaths(value);
            } else if (flag.equals("--merge-zips")) {
                mergePaths = getFiles(value);
            } else if (flag.equals("--output")) {
                output = new File(value);
                if (output.exists()) {
                    if (output.isDirectory()) {
                        abort(output + " is a directory");
                    }
                    boolean deleted = output.delete();
                    if (!deleted) {
                        abort("Could not delete previous version of " + output);
                    }
                } else if (output.getParentFile() != null && !output.getParentFile().exists()) {
                    abort(output.getParentFile() + " does not exist");
                }
            } else if (flag.equals("--proguard")) {
                proguard = new File(value);
                if (proguard.exists()) {
                    if (proguard.isDirectory()) {
                        abort(proguard + " is a directory");
                    }
                    boolean deleted = proguard.delete();
                    if (!deleted) {
                        abort("Could not delete previous version of " + proguard);
                    }
                } else if (proguard.getParentFile() != null && !proguard.getParentFile().exists()) {
                    abort(proguard.getParentFile() + " does not exist");
                }
            } else if (flag.equals("--encoding")) {
                encoding = value;
            } else if (flag.equals("--api-filter")) {
                if (apiFilters == null) {
                    apiFilters = Lists.newArrayList();
                }
                for (String path : Splitter.on(",").omitEmptyStrings().split(value)) {
                    File apiFilter = new File(path);
                    if (!apiFilter.isFile()) {
                        String message = apiFilter + " does not exist or is not a file";
                        abort(message);
                    }
                    apiFilters.add(apiFilter);
                }
            } else if (flag.equals("--language-level")) {
                if ("1.6".equals(value)) {
                    languageLevel = EcjParser.getLanguageLevel(1, 6);
                } else if ("1.7".equals(value)) {
                    languageLevel = EcjParser.getLanguageLevel(1, 7);
                } else {
                    abort("Unsupported language level " + value);
                }
            } else if (flag.equals("--rmtypedefs")) {
                rmTypeDefs = new File(value);
                if (!rmTypeDefs.isDirectory()) {
                    abort(rmTypeDefs + " is not a directory");
                }
            } else {
                System.err.println("Unknown flag " + flag + ": Use --help for usage information");
            }
        }

        if (sources.isEmpty()) {
            abort("Must specify at least one source path");
        }
        if (classpath.isEmpty()) {
            abort("Must specify classpath pointing to at least android.jar or the framework");
        }
        if (output == null && proguard == null) {
            abort("Must specify output path with --output or a proguard path with --proguard");
        }

        // API definition files
        ApiDatabase database = null;
        if (apiFilters != null && !apiFilters.isEmpty()) {
            try {
                List<String> lines = Lists.newArrayList();
                for (File file : apiFilters) {
                    lines.addAll(Files.readLines(file, Charsets.UTF_8));
                }
                database = new ApiDatabase(lines);
            } catch (IOException e) {
                abort("Could not open API database " + apiFilters + ": " + e.getLocalizedMessage());
            }
        }

        Extractor extractor = new Extractor(database, rmTypeDefs, verbose, !skipClassRetention,
                true);
        extractor.setListIgnored(listFiltered);

        try {
            Pair<Collection<CompilationUnitDeclaration>, INameEnvironment>
                    pair = parseSources(sources, classpath, encoding, languageLevel);
            Collection<CompilationUnitDeclaration> units = pair.getFirst();

            boolean abort = false;
            int errorCount = 0;
            for (CompilationUnitDeclaration unit : units) {
                // so maybe I don't need my map!!
                IProblem[] problems = unit.compilationResult().getAllProblems();
                if (problems != null) {
                    for (IProblem problem : problems) {
                        if (problem.isError()) {
                            errorCount++;
                            String message = problem.getMessage();
                            if (allowMissingTypes) {
                                if (message.contains("cannot be resolved")) {
                                    continue;
                                }
                            }

                            System.out.println("Error: " +
                                    new String(problem.getOriginatingFileName()) + ":" +
                                    problem.getSourceLineNumber() + ": " + message);
                            abort = !allowErrors;
                        }
                    }
                }
            }
            if (errorCount > 0) {
                System.err.println("Found " + errorCount + " errors");
            }
            if (abort) {
                abort("Not extracting annotations (compilation problems encountered)");
            }

            INameEnvironment environment = pair.getSecond();
            extractor.extractFromProjectSource(units);

            if (mergePaths != null) {
                for (File jar : mergePaths) {
                    extractor.mergeExisting(jar);
                }
            }

            extractor.export(output, proguard);

            // Remove typedefs?
            //noinspection VariableNotUsedInsideIf
            if (rmTypeDefs != null) {
                extractor.removeTypedefClasses();
            }

            environment.cleanup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void abort(@NonNull String message) {
        System.err.println(message);
        System.exit(-1);
    }

    private static List<File> getFiles(String value) {
        List<File> files = Lists.newArrayList();
        Splitter splitter = Splitter.on(pathSeparatorChar).omitEmptyStrings().trimResults();
        for (String path : splitter.split(value)) {
            if (path.startsWith("@")) {
                // Special syntax for providing files in a list
                File sourcePath = new File(path.substring(1));
                if (!sourcePath.exists()) {
                    abort(sourcePath + " does not exist");
                }
                try {
                    for (String line : Files.readLines(sourcePath, Charsets.UTF_8)) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            File file = new File(line);
                            if (!file.exists()) {
                                System.err.println("Warning: Could not find file " + line +
                                        " listed in " + sourcePath);
                            }
                            files.add(file);
                        }
                    }
                    continue;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
            File file = new File(path);
            if (!file.exists()) {
                abort(file + " does not exist");
            }
            files.add(file);
        }

        return files;
    }

    private static List<String> getPaths(String value) {
        List<File> files = getFiles(value);
        List<String> paths = Lists.newArrayListWithExpectedSize(files.size());
        for (File file : files) {
            paths.add(file.getPath());
        }
        return paths;
    }

    private static void addJavaSources(List<File> list, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    addJavaSources(list, child);
                }
            }
        } else {
            if (file.isFile() && file.getName().endsWith(DOT_JAVA)) {
                list.add(file);
            }
        }
    }

    private static List<File> gatherJavaSources(List<File> sourcePath) {
        List<File> sources = Lists.newArrayList();
        for (File file : sourcePath) {
            addJavaSources(sources, file);
        }
        return sources;
    }

    @NonNull
    private static Pair<Collection<CompilationUnitDeclaration>,INameEnvironment> parseSources(
            @NonNull List<File> sourcePaths,
            @NonNull List<String> classpath,
            @NonNull String encoding,
            long languageLevel)
            throws IOException {
        List<ICompilationUnit> sourceUnits = Lists.newArrayListWithExpectedSize(100);

        for (File source : gatherJavaSources(sourcePaths)) {
            char[] contents = Util.getFileCharContent(source, encoding);
            ICompilationUnit unit = new CompilationUnit(contents, source.getPath(), encoding);
            sourceUnits.add(unit);
        }

        Map<ICompilationUnit, CompilationUnitDeclaration> outputMap = Maps.newHashMapWithExpectedSize(
                sourceUnits.size());

        CompilerOptions options = EcjParser.createCompilerOptions();
        options.docCommentSupport = true; // So I can find @hide

        // Note: We can *not* set options.ignoreMethodBodies=true because it disables
        // type attribution!

        options.sourceLevel = languageLevel;
        options.complianceLevel = options.sourceLevel;
        // We don't generate code, but just in case the parser consults this flag
        // and makes sure that it's not greater than the source level:
        options.targetJDK = options.sourceLevel;
        options.originalComplianceLevel = options.sourceLevel;
        options.originalSourceLevel = options.sourceLevel;
        options.inlineJsrBytecode = true; // >= 1.5

        INameEnvironment environment = EcjParser.parse(options, sourceUnits, classpath,
                outputMap, null);
        Collection<CompilationUnitDeclaration> parsedUnits = outputMap.values();
        return Pair.of(parsedUnits, environment);
    }
}