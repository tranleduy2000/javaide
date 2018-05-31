package com.duy.compile.builder;

import android.content.Context;

import com.duy.project.file.android.AndroidProject;

import java.io.PrintStream;

public interface IBuilder {
    void build(BuildType buildType);

    AndroidProject getProject();

    void stdout(String message);

    void stderr(String message);

    Context getContext();

    PrintStream getStdout();
}
