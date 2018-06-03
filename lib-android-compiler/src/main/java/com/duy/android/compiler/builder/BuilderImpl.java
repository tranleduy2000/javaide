package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.tools.DiagnosticListener;

public abstract class BuilderImpl<T extends JavaProject> implements IBuilder<T> {
    protected Context mContext;
    protected boolean mVerbose;
    protected PrintStream mStdout;
    protected PrintStream mStderr;
    protected DiagnosticListener mDiagnosticListener;

    public BuilderImpl(Context context, DiagnosticListener listener) {
        mContext = context;
        mDiagnosticListener = listener;
        mStdout = new PrintStream(System.out);
        mStderr = new PrintStream(System.err);
        mVerbose = true;
    }

    protected boolean runTasks(ArrayList<ABuildTask> tasks) {
        for (ABuildTask task : tasks) {
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

    @Override
    public void setStdErr(PrintStream stdErr) {
        mStderr = stdErr;
    }
}
