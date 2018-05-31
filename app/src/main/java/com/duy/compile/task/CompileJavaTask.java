package com.duy.compile.task;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.java.Javac;
import com.duy.project.file.java.JavaProject;

import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class CompileJavaTask extends ABuildTask {

    private final JavaProject project;

    public CompileJavaTask(IBuilder builder, JavaProject project) {
        super(builder);
        this.project = project;
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
        int compileStatus;
        compileStatus = Javac.compile(args, new DiagnosticListener() {
            @Override
            public void report(Diagnostic diagnostic) {
                System.out.println("diagnostic = " + diagnostic);
            }
        });
        System.out.println("compileStatus = " + compileStatus);
        return compileStatus == 0;
    }
}
