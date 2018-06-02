package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.model.KeyStore;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.builder.task.android.AAPTTask;
import com.duy.android.compiler.builder.task.android.BuildApkTask;
import com.duy.android.compiler.builder.task.android.MergeManifestTask;
import com.duy.android.compiler.builder.task.android.SignApkTask;
import com.duy.android.compiler.builder.task.java.CleanTask;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DxTask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.tools.DiagnosticListener;

public class AndroidAppBuilder extends BuilderImpl<AndroidAppProject> {

    private KeyStore mKeyStore;
    private AndroidAppProject mProject;

    public AndroidAppBuilder(Context context, AndroidAppProject project, DiagnosticListener diagnosticCollector) {
        super(context, diagnosticCollector);
        mProject = project;
    }

    @Override
    public boolean build(BuildType buildType) {

        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ABuildTask> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new MergeManifestTask(this));

        tasks.add(new AAPTTask(this));

        tasks.add(new CompileJavaTask(this, mDiagnosticListener));

        tasks.add(new DxTask(this));

        tasks.add(new BuildApkTask(this));

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
