package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.ide.common.process.ProcessExecutor;
import com.android.utils.ILogger;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.project.JavaProject;

import java.io.PrintStream;

public interface IBuilder<T extends JavaProject> {

    T getProject();

    boolean build(BuildType buildType);

    Context getContext();

    boolean isVerbose();

    String getBootClassPath();

    PrintStream getStderr();

    PrintStream getStdout();

    void setStdErr(PrintStream stdErr);

    void setStdOut(PrintStream stdOut);

    void stdout(String message);

    void stderr(String message);

    ILogger getLogger();

}
