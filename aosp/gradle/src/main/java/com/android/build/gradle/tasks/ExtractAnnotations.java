package com.android.build.gradle.tasks;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.tasks.AbstractAndroidCompile;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.build.gradle.tasks.annotations.ApiDatabase;
import com.android.build.gradle.tasks.annotations.Extractor;
import com.android.tools.lint.EcjParser;
import com.android.utils.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;
import org.gradle.tooling.BuildException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.android.SdkConstants.DOT_JAVA;

/**
 * Task which extracts annotations from the source files, and writes them to one of
 * two possible destinations:
 * <ul>
 * <li> A "external annotations" file (pointed to by {@link ExtractAnnotations#output})
 * which records the annotations in a zipped XML format for use by the IDE and by
 * lint to associate the (source retention) annotations back with the compiled code</li>
 * <li> For any {@code Keep} annotated elements, a Proguard keep file (pointed to by
 * {@link ExtractAnnotations#proguard}, which lists APIs (classes, methods and fields)
 * that should not be removed even if no references in code are found to those APIs.</li>
 * <p>
 * We typically only extract external annotations when building libraries; ProGuard annotations
 * are extracted when building libraries (to record in the AAR), <b>or</b> when building an
 * app module where ProGuarding is enabled.
 * </ul>
 */
public class ExtractAnnotations extends AbstractAndroidCompile {
    public BaseVariantData variant;
    /**
     * Boot classpath: typically android.jar
     */
    public List<String> bootClasspath;
    /**
     * The output .zip file to write the annotations database to, if any
     */
    public File output;
    /**
     * The output proguard file to write any @Keep rules into, if any
     */
    public File proguard;
    /**
     * An optional pointer to an API file to filter the annotations by (any annotations
     * not found in the API file are considered hidden/not exposed.) This is in the same
     * format as the api-versions.xml file found in the SDK.
     */
    public File apiFilter;
    /**
     * A list of existing annotation zip files (or dirs) to merge in. This can be used to merge in
     * a hardcoded set of annotations that are not present in the source code, such as
     * {@code @Contract} annotations we'd like to record without actually having a dependency
     * on the IDEA annotations library.
     */
    public List<File> mergeJars;
    /**
     * The encoding to use when reading source files. The output file will ignore this and
     * will always be a UTF-8 encoded .xml file inside the annotations zip file.
     */
    public String encoding;
    /**
     * Location of class files. If set, any non-public typedef source retention annotations
     * will be removed prior to .jar packaging.
     */
    public File classDir;
    /**
     * Whether we allow extraction even in the presence of symbol resolution errors
     */
    public boolean allowErrors = true;

    private static long getLanguageLevel(String version) {
        if ("1.6".equals(version)) {
            return EcjParser.getLanguageLevel(1, 6);
        } else if ("1.7".equals(version)) {
            return EcjParser.getLanguageLevel(1, 7);
        } else if ("1.5".endsWith(version)) {
            return EcjParser.getLanguageLevel(1, 5);
        } else {
            return EcjParser.getLanguageLevel(1, 7);
        }

    }

    @Override
    @TaskAction
    protected void compile() {
        if (!hasAndroidAnnotations()) {
            return;

        }


        if (encoding == null) {
            encoding = "UTF_8";
        }


        Pair<Collection<CompilationUnitDeclaration>, INameEnvironment> result = parseSources();
        Collection<CompilationUnitDeclaration> parsedUnits = result.getFirst();
        INameEnvironment environment = result.getSecond();

        try {
            if (!allowErrors) {
                for (CompilationUnitDeclaration unit : parsedUnits) {
                    // so maybe I don't need my map!!
                    CategorizedProblem[] problems = unit.compilationResult().getAllProblems();
                    for (IProblem problem : problems) {
                        if (problem.isError()) {
                            System.out.println("Not extracting annotations (compilation problems encountered)");
                            System.out.println("Error: " + problem.getOriginatingFileName() + ":" + problem.getSourceLineNumber() + ": " + problem.getMessage());
                            // TODO: Consider whether we abort the build at this point!
                            return;

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
                    throw new BuildException("Could not open API database " + apiFilter, e);
                }

            }


            boolean displayInfo = getProject().getLogger().isEnabled(LogLevel.INFO);
            Boolean includeClassRetentionAnnotations = false;
            Boolean sortAnnotations = false;

            Extractor extractor = new Extractor(database, classDir, displayInfo, includeClassRetentionAnnotations, sortAnnotations);
            extractor.extractFromProjectSource(parsedUnits);
            if (mergeJars != null) {
                for (File jar : mergeJars) {
                    extractor.mergeExisting(jar);
                }

            }

            extractor.export(output, proguard);
            extractor.removeTypedefClasses();
        } finally {
            if (environment != null) {
                environment.cleanup();
            }

        }

    }

    public boolean hasAndroidAnnotations() {
        return variant.getVariantDependency().getAnnotationsPresent();
    }

    @NonNull
    private Pair<Collection<CompilationUnitDeclaration>, INameEnvironment> parseSources() {
        final List<ICompilationUnit> sourceUnits = Lists.newArrayListWithExpectedSize(100);

        getSource().visit(new EmptyFileVisitor() {
            @Override
            public void visitFile(FileVisitDetails fileVisitDetails) {
                File file = fileVisitDetails.getFile();
                String path = file.getPath();
                if (path.endsWith(DOT_JAVA) && file.isFile()) {
                    char[] contents = new char[0];
                    try {
                        contents = Util.getFileCharContent(file, encoding);
                        ICompilationUnit unit = new CompilationUnit(contents, path, encoding);
                        sourceUnits.add(unit);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        });

        Map<ICompilationUnit, CompilationUnitDeclaration> outputMap = Maps.newHashMapWithExpectedSize(((ArrayList<ICompilationUnit>) sourceUnits).size());
        List<String> jars = Lists.newArrayList();
        if (bootClasspath != null) {
            ((ArrayList<String>) jars).addAll(bootClasspath);
        }

        if (getClasspath() != null) {
            for (File jar : getClasspath()) {
                ((ArrayList<String>) jars).add(jar.getPath());
            }

        }


        CompilerOptions options = EcjParser.createCompilerOptions();
        options.docCommentSupport = true;// So I can find @hide

        // Note: We can *not* set options.ignoreMethodBodies=true because it disables
        // type attribution!

        long level = getLanguageLevel(getSourceCompatibility());
        options.sourceLevel = level;
        options.complianceLevel = options.sourceLevel;
        // We don't generate code, but just in case the parser consults this flag
        // and makes sure that it's not greater than the source level:
        options.targetJDK = options.sourceLevel;
        options.originalComplianceLevel = options.sourceLevel;
        options.originalSourceLevel = options.sourceLevel;
        options.inlineJsrBytecode = true;// >= 1.5

        INameEnvironment environment = EcjParser.parse(options, sourceUnits, jars, outputMap, null);
        Collection<CompilationUnitDeclaration> parsedUnits = ((HashMap<ICompilationUnit, CompilationUnitDeclaration>) outputMap).values();
        return Pair.of(parsedUnits, environment);
    }

    private boolean addSources(List<ICompilationUnit> sourceUnits, File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File sub : files) {
                    addSources(sourceUnits, sub);
                }

            }

        } else if (file.getPath().endsWith(DOT_JAVA) && file.isFile()) {
            char[] contents = Util.getFileCharContent(file, encoding);
            ICompilationUnit unit = new CompilationUnit(contents, file.getPath(), encoding);
            return sourceUnits.add(unit);
        }
        return false;
    }
}
