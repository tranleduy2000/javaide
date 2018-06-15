package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.builder.task.java.JarTask;
import com.duy.android.compiler.project.JavaProject;

import java.io.PrintStream;
import java.util.ArrayList;

public class JavaBuilder extends BuilderImpl<JavaProject> {


    private JavaProject mProject;

    public JavaBuilder(Context context, JavaProject project) {
        super(context);
        mProject = project;

    }

    @Override
    public boolean build(BuildType buildType) {
        if (mVerbose) {
            mStdout.println("Starting build java project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<Task> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new CompileJavaTask(this));

        tasks.add(new JarTask(this));

        tasks.add(new DexTask(this));

        return runTasks(tasks);
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
