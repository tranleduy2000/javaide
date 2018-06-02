package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.AndroidAppProject;

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
        // TODO: 02-Jun-18 impl
        return true;
    }
}
