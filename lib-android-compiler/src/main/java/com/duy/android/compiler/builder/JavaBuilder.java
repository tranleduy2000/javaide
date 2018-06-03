package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.builder.task.java.JarTask;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.project.JavaProject;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.tools.DiagnosticListener;

public class JavaBuilder extends BuilderImpl<JavaProject> {


    private JavaProject mProject;

    public JavaBuilder(Context context, JavaProject project, DiagnosticListener listener) {
        super(context, listener);
        mProject = project;

    }

    @Override
    public boolean build(BuildType buildType) {
        if (mVerbose) {
            mStdout.println("Starting build java project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ATask> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new CompileJavaTask(this));

        tasks.add(new JarTask(this));

        tasks.add(new DexTask(this));

        return runTasks(tasks);
    }


    public void stdout(String message) {
        if (mVerbose) {
            mStdout.println(message);
        }
    }

    public void stderr(String stderr) {
        if (mVerbose) {
            mStderr.println(stderr);
        }
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public Context getContext() {
        return mContext;
    }

    public JavaProject getProject() {
        return mProject;
    }

    public PrintStream getStdout() {
        return mStdout;
    }
}
