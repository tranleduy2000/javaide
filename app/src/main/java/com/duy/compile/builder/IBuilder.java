package com.duy.compile.builder;

import android.content.Context;

import com.duy.compile.builder.model.BuildType;
import com.duy.project.file.java.JavaProject;

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
