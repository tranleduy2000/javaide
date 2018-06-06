package com.android.build.gradle.tasks.factory;

import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.scope.ConventionMappingHelper;
import com.android.build.gradle.internal.scope.TaskConfigAction;
import com.android.build.gradle.internal.scope.VariantScope;
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
        scope.getVariantData().javacTask = javacTask;

        javacTask.setSource(scope.getVariantData().getJavaSources());
        ConventionMappingHelper.map(javacTask, "classpath", new Callable<FileCollection>() {
            @Override
            public FileCollection call() {
                FileCollection classpath = scope.getJavaClasspath();
                Project project = scope.getGlobalScope().getProject();

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
