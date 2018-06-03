package com.duy.android.compiler.builder.task.android;

import com.android.builder.compiling.BuildConfigGenerator;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.File;

public class GenerateBuildConfigTask extends ABuildTask<AndroidAppProject> {

    public GenerateBuildConfigTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Generate BuildConfig.java";
    }

    @Override
    public boolean run() throws Exception {
        String packageName = project.getPackageName();
        File genFolder = project.getDirGeneratedSource();
        BuildConfigGenerator buildConfigGenerator = new BuildConfigGenerator(genFolder, packageName);
        buildConfigGenerator.generate();
        return true;
    }
}
