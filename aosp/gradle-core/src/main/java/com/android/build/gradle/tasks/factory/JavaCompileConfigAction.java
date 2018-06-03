package com.android.build.gradle.tasks.factory;

import static com.android.builder.core.VariantType.LIBRARY;
import static com.android.builder.core.VariantType.UNIT_TEST;

import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.android.builder.dependency.LibraryDependency;
import com.google.common.base.Joiner;

import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Configuration Action for a JavaCompile task.
 */
public class JavaCompileConfigAction implements TaskConfigAction<JavaCompile> {

    private VariantScope scope;

    public JavaCompileConfigAction(VariantScope scope) {
        this.scope = scope;
    }

    @Override
    public String getName() {
        return scope.getTaskName("compile", "JavaWithJavac");
    }

    @Override
    public Class<JavaCompile> getType() {
        return JavaCompile.class;
    }

    @Override
    public void execute(final JavaCompile javacTask) {
        final BaseVariantData testedVariantData = scope.getTestedVariantData();
        scope.getVariantData().javacTask = javacTask;

        javacTask.setSource(scope.getVariantData().getJavaSources());

        ConventionMappingHelper.map(javacTask, "classpath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() {
                FileCollection classpath = scope.getJavaClasspath();
                Project project = scope.getGlobalScope().getProject();

                if (testedVariantData != null) {
                    // For libraries, the classpath from androidBuilder includes the library
                    // output (bundle/classes.jar) as a normal dependency. In unit tests we
                    // don't want to package the jar at every run, so we use the *.class
                    // files instead.
                    if (!testedVariantData.getType().equals(LIBRARY)
                            || scope.getVariantData().getType().equals(UNIT_TEST)) {
                        classpath = classpath.plus(project.files(
                                        testedVariantData.getScope().getJavaClasspath(),
                                        testedVariantData.getScope().getJavaOutputDir(),
                                        testedVariantData.getScope().getJavaDependencyCache()));
                    }

                    if (scope.getVariantData().getType().equals(UNIT_TEST)
                            && testedVariantData.getType().equals(LIBRARY)) {
                        // The bundled classes.jar may exist, but it's probably old. Don't
                        // use it, we already have the *.class files in the classpath.
                        LibraryDependency libraryDependency =
                                testedVariantData.getVariantConfiguration().getOutput();
                        if (libraryDependency != null) {
                            File jarFile = libraryDependency.getJarFile();
                            classpath = classpath.minus(project.files(jarFile));
                        }
                    }
                }

                return classpath;
            }
        });

        javacTask.setDestinationDir(scope.getJavaOutputDir());

        javacTask.setDependencyCacheDir(scope.getJavaDependencyCache());

        CompileOptions compileOptions = scope.getGlobalScope().getExtension().getCompileOptions();

        AbstractCompilesUtil.configureLanguageLevel(
                javacTask,
                compileOptions,
                scope.getGlobalScope().getExtension().getCompileSdkVersion()
        );

        javacTask.getOptions().setEncoding(compileOptions.getEncoding());

        javacTask.getOptions().setBootClasspath(
                Joiner.on(File.pathSeparator).join(
                        scope.getGlobalScope().getAndroidBuilder().getBootClasspathAsStrings()));
    }
}
