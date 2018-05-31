package com.duy.compile.builder;

import android.content.Context;

import com.duy.compile.task.android.Aapt;
import com.duy.compile.task.android.BuildApkTask;
import com.duy.compile.task.ABuildTask;
import com.duy.compile.task.java.DxTask;
import com.duy.compile.task.java.CompileJavaTask;
import com.duy.compile.task.android.SignApkTask;
import com.duy.project.file.android.AndroidProject;

import java.io.PrintStream;
import java.util.ArrayList;

public class AndroidProjectBuilder implements IBuilder<AndroidProject> {
    private Context mContext;
    private boolean mVerbose;
    private PrintStream mStdout, mStderr;

    private KeyStore mKeyStore;
    private AndroidProject mProject;

    public AndroidProjectBuilder(Context context, AndroidProject project) {
        mContext = context;
        mProject = project;
        mStdout = new PrintStream(System.out);
        mStderr = new PrintStream(System.err);
        mVerbose = true;
    }

    @Override
    public void build(BuildType buildType) {

        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ABuildTask> tasks = new ArrayList<>();
        tasks.add(new Aapt(this));
        tasks.add(new CompileJavaTask(this, mProject));
        tasks.add(new DxTask(this));
        tasks.add(new BuildApkTask(this));
        tasks.add(new SignApkTask(this, buildType));

        for (ABuildTask task : tasks) {
            try {
                stdout("Run " + task.getTaskName() + " task");
                boolean result = task.run();
                if (!result) {
                    stdout(task.getTaskName() + " failed");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                stdout(task.getTaskName() + " failed");
                return;
            }
        }

        cleanAfterBuild();
    }

    private void cleanAfterBuild() {
        mProject.getApkUnsigned().delete();
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

    public AndroidProject getProject() {
        return mProject;
    }

    public PrintStream getStdout() {
        return mStdout;
    }
}
