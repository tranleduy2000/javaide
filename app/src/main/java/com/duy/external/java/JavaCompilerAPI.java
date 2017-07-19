package com.duy.external.java;

import com.duy.editor.file.FileManager;
import com.duy.project_files.ProjectFile;
import com.spartacusrex.spartacuside.helper.Arrays;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Created by duy on 19/07/2017.
 */

public class JavaCompilerAPI {

    public static void process(ProjectFile pf) throws IOException {
        String projectPath = pf.getProjectDir();
        String projectName = pf.getProjectName();
        String rootPkg = pf.getMainClass().getRootPackage();


        //create build director, delete all file if exists
        File buildDir = new File(projectPath, "build/classes");
        FileManager.deleteFolder(buildDir);
        if (!buildDir.exists()) buildDir.mkdirs();

        File sourcepath = new File(projectPath, "src/main/java");
        if (!sourcepath.exists()) sourcepath.mkdirs();

        String[] args = new String[]{
                "-verbose",
                "-sourcepath", sourcepath.getPath(), //classpath
                "-d", buildDir.getPath(), //output dir
                pf.getMainClass().getPath(pf) //main class
        };

        Collection<File> files = FileUtils.listFiles(sourcepath, new String[]{".java"}, true);

        javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);


        ArrayList<String> options = (ArrayList<String>) Arrays.asList(args);
        JavaCompiler.CompilationTask task
                = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        boolean success = task.call();
        if (!success) {
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                System.out.format("Error on line %d in %s%n",
                        diagnostic.getLineNumber(),
                        diagnostic.getSource());
            }
        }
        fileManager.close();
        System.out.println("Success: " + success);
    }
}
