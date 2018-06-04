package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.annotations.NonNull;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.android.GenerateConfigTask;
import com.duy.android.compiler.builder.task.android.PackageApplicationTask;
import com.duy.android.compiler.builder.task.android.ProcessAndroidResourceTask;
import com.duy.android.compiler.builder.task.android.SignApkTask;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.PrintStream;
import java.util.ArrayList;

public class AndroidAppBuilder extends BuilderImpl<AndroidAppProject> {


    private AndroidAppProject mProject;
    public AndroidAppBuilder(@NonNull Context context,
                             @NonNull AndroidAppProject project) {
        super(context);
        mProject = project;
        setDefaultValues();
    }

    private void setDefaultValues() {

    }

    @Override
    public boolean build(BuildType buildType) {

        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ATask> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new GenerateConfigTask(this));

        tasks.add(new ProcessAndroidResourceTask(this));

        tasks.add(new CompileJavaTask(this));

        tasks.add(new DexTask(this));

        tasks.add(new PackageApplicationTask(this));

        tasks.add(new SignApkTask(this, buildType));

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

    public AndroidAppProject getProject() {
        return mProject;
    }

    public PrintStream getStdout() {
        return mStdout;
    }

}
