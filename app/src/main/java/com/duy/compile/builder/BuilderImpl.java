package com.duy.compile.builder;

import android.content.Context;

import com.duy.project.file.java.JavaProject;

import java.io.PrintStream;

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
}
