package com.duy.compile.builder;

import android.content.Context;

import com.duy.project.file.java.JavaProject;

import java.io.PrintStream;

public interface IBuilder<T extends JavaProject> {
    void build(BuildType buildType);

    T getProject();

    void stdout(String message);

    void stderr(String message);

    Context getContext();

    PrintStream getStdout();
}
