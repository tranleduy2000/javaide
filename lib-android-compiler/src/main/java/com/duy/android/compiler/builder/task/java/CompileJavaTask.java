package com.duy.android.compiler.builder.task.java;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.JavaProject;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.tools.DiagnosticListener;

public class CompileJavaTask extends ABuildTask<JavaProject> {

    private DiagnosticListener listener;

    public CompileJavaTask(IBuilder<? extends JavaProject> builder, DiagnosticListener listener) {
        super(builder);
        this.listener = listener;
    }

    @Override
    public String getTaskName() {
        return "Compile java source";
    }

    public boolean run() {
//        return runOldJavaCompiler();
        return runEcj();
    }

    //    private boolean runOldJavaCompiler() {
//        ArrayList<File> javaLibraries = project.getJavaLibraries(context);
//        StringBuilder classpath = new StringBuilder(".");
//        for (File javaLibrary : javaLibraries) {
//            classpath.append(File.pathSeparator).append(javaLibrary.getParent());
//        }
//        String[] args = new String[]{
//                "-verbose",
//                "-cp", classpath.toString() + File.pathSeparator + project.getBootClassPath(context),
//                "-sourcepath", project.getSourcePath(), //sourcepath
//                "-d", project.getDirBuildClasses().getPath(), //output dir
//                project.getMainClass().getPath(project) //main class
//        };
//        System.out.println("args = " + Arrays.toString(args));
//        int resultCode = Javac.compile(args, listener);
//        return resultCode == 0;
//    }

    private boolean runEcj() {
        org.eclipse.jdt.internal.compiler.batch.Main main =
                new org.eclipse.jdt.internal.compiler.batch.Main(new PrintWriter(builder.getStdout()), new PrintWriter(builder.getStderr()),
                        false, null, null);
        ArrayList<File> javaLibraries = project.getJavaLibraries(context);
        StringBuilder extDir = new StringBuilder(".");
        for (File javaLibrary : javaLibraries) {
            extDir.append(File.pathSeparator).append(javaLibrary.getParent());
        }

        String[] args = {
                (builder.isVerbose() ? "-verbose" : "-warn:-unusedImport"), // Disable warning for unused imports (the preprocessor gives us a lot of them, so this is just a lot of noise)
                "-extdirs", extDir.toString(),
                "-bootclasspath", project.getBootClassPath(context),
                "-classpath", project.getSourcePath(),
                "-1.7",
                "-target", "1.7", // Target Java level
                "-proc:none", // Disable annotation processors...
                "-d", project.getDirBuildClasses().getAbsolutePath(), // The location of the output folder
                project.getMainClass().getPath(project), // The location of the main activity
        };
        System.out.println("args = " + Arrays.toString(args));
        return main.compile(args);
    }
}
