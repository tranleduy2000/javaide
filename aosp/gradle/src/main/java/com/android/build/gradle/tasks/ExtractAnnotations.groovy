/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.build.gradle.tasks

import com.android.annotations.NonNull
import com.android.build.gradle.internal.tasks.AbstractAndroidCompile
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.build.gradle.tasks.annotations.ApiDatabase
import com.android.build.gradle.tasks.annotations.Extractor
import com.android.tools.lint.EcjParser
import com.android.utils.Pair
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit
import org.eclipse.jdt.internal.compiler.env.INameEnvironment
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions
import org.eclipse.jdt.internal.compiler.util.Util
import org.gradle.api.file.EmptyFileVisitor
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException

import static com.android.SdkConstants.DOT_JAVA
import static com.android.SdkConstants.UTF_8

/**
 * Task which extracts annotations from the source files, and writes them to one of
 * two possible destinations:
 * <ul>
 *     <li> A "external annotations" file (pointed to by {@link ExtractAnnotations#output})
 *          which records the annotations in a zipped XML format for use by the IDE and by
 *          lint to associate the (source retention) annotations back with the compiled code</li>
 *     <li> For any {@code Keep} annotated elements, a Proguard keep file (pointed to by
 *          {@link ExtractAnnotations#proguard}, which lists APIs (classes, methods and fields)
 *          that should not be removed even if no references in code are found to those APIs.</li>
 * <p>
 * We typically only extract external annotations when building libraries; ProGuard annotations
 * are extracted when building libraries (to record in the AAR), <b>or</b> when building an
 * app module where ProGuarding is enabled.
 * </ul>
 */
class ExtractAnnotations extends AbstractAndroidCompile {
    public BaseVariantData variant

    /** Boot classpath: typically android.jar */
    @Input
    public List<String> bootClasspath

    /** The output .zip file to write the annotations database to, if any */
    @Optional
    @OutputFile
    public File output

    /** The output proguard file to write any @Keep rules into, if any */
    @Optional
    @OutputFile
    public File proguard

    /**
     * An optional pointer to an API file to filter the annotations by (any annotations
     * not found in the API file are considered hidden/not exposed.) This is in the same
     * format as the api-versions.xml file found in the SDK.
     */
    @Optional
    @InputFile
    public File apiFilter

    /**
     * A list of existing annotation zip files (or dirs) to merge in. This can be used to merge in
     * a hardcoded set of annotations that are not present in the source code, such as
     * {@code @Contract} annotations we'd like to record without actually having a dependency
     * on the IDEA annotations library.
     */
    @Optional
    @InputFile
    public List<File> mergeJars

    /**
     * The encoding to use when reading source files. The output file will ignore this and
     * will always be a UTF-8 encoded .xml file inside the annotations zip file.
     */
    @Optional
    @Input
    public String encoding

    /**
     * Location of class files. If set, any non-public typedef source retention annotations
     * will be removed prior to .jar packaging.
     */
    @Optional
    @InputFile
    public File classDir

    /** Whether we allow extraction even in the presence of symbol resolution errors */
    @InputFile
    public boolean allowErrors = true

    @Override
    @TaskAction
    protected void compile() {
        if (!hasAndroidAnnotations()) {
            return
        }

        if (encoding == null) {
            encoding = UTF_8
        }

        Pair<Collection<CompilationUnitDeclaration>, INameEnvironment> result = parseSources()
        def parsedUnits = result.first
        def environment = result.second

        try {
            if (!allowErrors) {
                for (CompilationUnitDeclaration unit : parsedUnits) {
                    // so maybe I don't need my map!!
                    def problems = unit.compilationResult().allProblems
                    for (IProblem problem : problems) {
                        if (problem.error) {
                            println "Not extracting annotations (compilation problems encountered)";
                            println "Error: " + problem.getOriginatingFileName() + ":" +
                                    problem.getSourceLineNumber() + ": " + problem.getMessage()
                            // TODO: Consider whether we abort the build at this point!
                            return
                        }
                    }
                }
            }

            // API definition file
            ApiDatabase database = null;
            if (apiFilter != null && apiFilter.exists()) {
                try {
                    database = new ApiDatabase(apiFilter);
                } catch (IOException e) {
                    throw new BuildException("Could not open API database " + apiFilter, e)
                }
            }


            def displayInfo = project.logger.isEnabled(LogLevel.INFO)
            def includeClassRetentionAnnotations = false
            def sortAnnotations = false

            Extractor extractor = new Extractor(database, classDir, displayInfo,
                    includeClassRetentionAnnotations, sortAnnotations);
            extractor.extractFromProjectSource(parsedUnits)
            if (mergeJars != null) {
                for (File jar : mergeJars) {
                    extractor.mergeExisting(jar);
                }
            }
            extractor.export(output, proguard)
            extractor.removeTypedefClasses();
        } finally {
            if (environment != null) {
                environment.cleanup()
            }
        }
    }

    @Input
    public boolean hasAndroidAnnotations() {
        return variant.variantDependency.annotationsPresent
    }

    @NonNull
    private Pair<Collection<CompilationUnitDeclaration>,INameEnvironment> parseSources() {
        List<ICompilationUnit> sourceUnits = Lists.newArrayListWithExpectedSize(100);

        source.visit(new EmptyFileVisitor() {
            @Override
            void visitFile(FileVisitDetails fileVisitDetails) {
                def file = fileVisitDetails.file;
                def path = file.getPath()
                if (path.endsWith(DOT_JAVA) && file.isFile()) {
                    char[] contents = Util.getFileCharContent(file, encoding);
                    ICompilationUnit unit = new CompilationUnit(contents, path, encoding);
                    sourceUnits.add(unit);
                }
            }
        })

        Map<ICompilationUnit, CompilationUnitDeclaration> outputMap = Maps.
                newHashMapWithExpectedSize(sourceUnits.size())
        List<String> jars = Lists.newArrayList();
        if (bootClasspath != null) {
            jars.addAll(bootClasspath)
        }
        if (classpath != null) {
            for (File jar : classpath) {
                jars.add(jar.getPath());
            }
        }

        CompilerOptions options = EcjParser.createCompilerOptions();
        options.docCommentSupport = true; // So I can find @hide

        // Note: We can *not* set options.ignoreMethodBodies=true because it disables
        // type attribution!

        def level = getLanguageLevel(sourceCompatibility)
        options.sourceLevel = level
        options.complianceLevel = options.sourceLevel
        // We don't generate code, but just in case the parser consults this flag
        // and makes sure that it's not greater than the source level:
        options.targetJDK = options.sourceLevel
        options.originalComplianceLevel = options.sourceLevel;
        options.originalSourceLevel = options.sourceLevel;
        options.inlineJsrBytecode = true; // >= 1.5

        def environment = EcjParser.parse(options, sourceUnits, jars, outputMap, null);
        Collection<CompilationUnitDeclaration> parsedUnits = outputMap.values()
        Pair.of(parsedUnits, environment);
    }

    private static long getLanguageLevel(String version) {
        if ("1.6".equals(version)) {
            return EcjParser.getLanguageLevel(1, 6);
        } else if ("1.7".equals(version)) {
            return EcjParser.getLanguageLevel(1, 7);
        } else if ("1.5") {
            return EcjParser.getLanguageLevel(1, 5);
        } else {
            return EcjParser.getLanguageLevel(1, 7);
        }
    }

    private def addSources(List<ICompilationUnit> sourceUnits, File file) {
        if (file.isDirectory()) {
            def files = file.listFiles();
            if (files != null) {
                for (File sub : files) {
                    addSources(sourceUnits, sub);
                }
            }
        } else if (file.getPath().endsWith(DOT_JAVA) && file.isFile()) {
            char[] contents = Util.getFileCharContent(file, encoding);
            ICompilationUnit unit = new CompilationUnit(contents, file.getPath(), encoding);
            sourceUnits.add(unit);
        }
    }
}
