package com.duy.compile.builder;

import android.content.Context;

import com.duy.compile.task.ABuildTask;
import com.duy.project.file.java.JavaProject;

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
}
