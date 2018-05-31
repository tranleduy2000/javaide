package com.duy.compile.external.android.builder.task;

import com.duy.compile.external.android.builder.AndroidBuilder2;
import com.duy.compile.external.java.Javac;
import com.duy.project.file.android.AndroidProject;

import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class JavaCompiler extends BuildTask {

    public JavaCompiler(AndroidBuilder2 builder, AndroidProject project) {
        super(builder, project);
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
