package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.utils.ILogger;
import com.duy.android.compiler.builder.task.Task;
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

    protected boolean runTasks(ArrayList<Task> tasks) {
        for (Task task : tasks) {
            try {
                stdout("Run " + task.getTaskName() + " task");
                boolean result = task.doFullTaskAction();
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

    @Override
    public PrintStream getStdout() {
        return mStdout;
    }

    @Override
    public ILogger getLogger() {
        return mLogger;
    }

    public void setLogger(ILogger logger) {
        this.mLogger = logger;
    }

    @Override
    public void setStdErr(PrintStream stdErr) {
        mStderr = stdErr;
    }

    @Override
    public void stdout(String message) {
        if (mVerbose) {
            mStdout.println(message);
        }
    }

    @Override
    public void stderr(String stderr) {
        if (mVerbose) {
            mStderr.println(stderr);
        }
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public boolean isVerbose() {
        return mVerbose;
    }

}
