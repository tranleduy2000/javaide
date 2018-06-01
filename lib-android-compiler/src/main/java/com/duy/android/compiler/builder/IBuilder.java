package com.duy.android.compiler.builder;

import android.content.Context;

import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.file.JavaProject;

import java.io.PrintStream;

public interface IBuilder<T extends JavaProject> {

    T getProject();

    boolean build(BuildType buildType);

    void stdout(String message);

    PrintStream getStdout();

    void stderr(String message);

    Context getContext();

    boolean isVerbose();
}
