package com.duy.android.compiler.builder.task.java;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.java.Javac;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.JavaProject;

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
                "-verbose",
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
