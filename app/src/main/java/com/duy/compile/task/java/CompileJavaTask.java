package com.duy.compile.task.java;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.java.Javac;
import com.duy.compile.task.ABuildTask;
import com.duy.project.file.java.JavaProject;

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
        String[] args = new String[]{
//                "-verbose",
                "-cp", project.getJavaClassPath(context),
                "-sourcepath", project.getSourcePath(), //sourcepath
                "-d", project.getDirBuildClasses().getPath(), //output dir
                project.getMainClass().getPath(project) //main class
        };
        System.out.println("args = " + Arrays.toString(args));
        int resultCode = Javac.compile(args, listener);
        return resultCode == 0;
    }
}
