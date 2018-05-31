package com.duy.compile.external.android.builder;

import android.content.Context;

import com.duy.compile.external.android.builder.task.Aapt;
import com.duy.compile.external.android.builder.task.BuildApkTask;
import com.duy.compile.external.android.builder.task.BuildTask;
import com.duy.compile.external.android.builder.task.Dexer;
import com.duy.compile.external.android.builder.task.JavaCompiler;
import com.duy.compile.external.android.builder.task.SignApkTask;
import com.duy.project.file.android.AndroidProject;

import java.io.PrintStream;
import java.util.ArrayList;

public class AndroidBuilder2 implements IAndroidBuilder {
    private Context mContext;
    private boolean mVerbose;
    private PrintStream mStdout, mStderr;

    private KeyStore mKeyStore;
    private AndroidProject mProject;

    public AndroidBuilder2(Context context, AndroidProject project) {
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

        ArrayList<BuildTask> tasks = new ArrayList<>();
        tasks.add(new Aapt(this));
        tasks.add(new JavaCompiler(this));
        tasks.add(new Dexer(this));
        tasks.add(new BuildApkTask(this));
        tasks.add(new SignApkTask(this, buildType));

        for (BuildTask task : tasks) {
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
