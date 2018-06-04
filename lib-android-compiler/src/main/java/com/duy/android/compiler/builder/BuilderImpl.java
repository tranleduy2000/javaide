package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.utils.ILogger;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;

import java.io.PrintStream;
import java.util.ArrayList;

public abstract class BuilderImpl<T extends JavaProject> implements IBuilder<T> {
    protected Context mContext;
    protected boolean mVerbose;
    protected PrintStream mStdout;
    protected PrintStream mStderr;
    private ILogger mLogger;

    public BuilderImpl(Context context) {
        mContext = context;
        mStdout = new PrintStream(System.out);
        mStderr = new PrintStream(System.err);
        mVerbose = true;
    }

    protected boolean runTasks(ArrayList<ATask> tasks) {
        for (ATask task : tasks) {
            try {
                stdout("Run " + task.getTaskName() + " task");
                boolean result = task.run();
                if (!result) {
                    stdout(task.getTaskName() + " failed");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                stdout(task.getTaskName() + " failed");
                return false;
            }
        }
        return true;
    }

    @Override
    public PrintStream getStderr() {
        return mStderr;
    }

    @Override
    public String getBootClassPath() {
        return Environment.getClasspathFile(mContext).getAbsolutePath();
    }

    @Override
    public void setStdOut(PrintStream stdOut) {
        mStdout = stdOut;
    }

    public void setLogger(ILogger logger) {
        this.mLogger = logger;
    }

    @Override
    public ILogger getLogger() {
        return mLogger;
    }

    @Override
    public void setStdErr(PrintStream stdErr) {
        mStderr = stdErr;
    }
}
