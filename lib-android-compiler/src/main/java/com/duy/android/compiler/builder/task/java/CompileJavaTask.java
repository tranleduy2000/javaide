package com.duy.android.compiler.builder.task.java;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.builder.util.Argument;
import com.duy.android.compiler.java.Javac;
import com.duy.android.compiler.project.JavaProject;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.tools.DiagnosticListener;

public class CompileJavaTask extends ABuildTask<JavaProject> {

    private static final String TAG = "CompileJavaTask";
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
//        return runAnt();
    }


    private boolean runEcj() {
        PrintWriter outWriter = new PrintWriter(builder.getStdout());
        PrintWriter errWriter = new PrintWriter(builder.getStderr());
        org.eclipse.jdt.internal.compiler.batch.Main main =
                new org.eclipse.jdt.internal.compiler.batch.Main(outWriter, errWriter,
                        false, null, null);

        Argument argument = new Argument();
        argument.add(builder.isVerbose() ? "-verbose" : "-warn:");
        argument.add("-bootclasspath", project.getBootClassPath(context));
        argument.add("-classpath", project.getClasspath());
        argument.add("-sourcepath", project.getSourcePath());
        argument.add("-" + CompilerOptions.VERSION_1_7); //host
        argument.add("-target", CompilerOptions.VERSION_1_7); //target
        argument.add("-proc:none"); // Disable annotation processors...
        argument.add("-d", project.getDirBuildClasses().getAbsolutePath()); // The location of the output folder
        argument.add(getAllSourceFile(project));

        System.err.println("argument = " + argument);
        return main.compile(argument.toArray());
    }

    private String getAllSourceFile(JavaProject project) {
        ArrayList<File> javaFiles = new ArrayList<>();
        String[] sourcePaths = project.getSourcePath().split(File.pathSeparator);
        for (String sourcePath : sourcePaths) {
            getAllSourceFile(javaFiles, new File(sourcePath));
        }
        StringBuilder srcs = new StringBuilder();
        for (File javaFile : javaFiles) {
            srcs.append(" ").append(javaFile.getAbsolutePath());
        }
        return srcs.toString();
    }

    public void getAllSourceFile(ArrayList<File> toAdd, File parent) {
        if (!parent.exists()) {
            return;
        }
        for (File child : parent.listFiles()) {
            if (child.isDirectory()) {
                getAllSourceFile(toAdd, child);
            } else {
                if (child.isFile()) {
                    if (parent.getName().endsWith(".java")) {
                        toAdd.add(parent);
                    }
                    return;
                }
            }
        }
    }

    private boolean runOldJavaCompiler() {
        ArrayList<File> javaLibraries = project.getJavaLibraries();
        StringBuilder classpath = new StringBuilder(".");
        for (File javaLibrary : javaLibraries) {
            classpath.append(File.pathSeparator).append(javaLibrary.getParent());
        }
        String[] args = new String[]{
                "-verbose",
                "-cp", classpath.toString() + File.pathSeparator + project.getBootClassPath(context),
                "-sourcepath", project.getSourcePath(), //sourcepath
                "-d", project.getDirBuildClasses().getPath(), //output dir
                project.getMainClass().getPath(project) //main class
        };
        System.out.println("args = " + Arrays.toString(args));
        int resultCode = Javac.compile(args, listener);
        return resultCode == 0;
    }

}
