package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.project.AndroidAppProject;

public class ExtractProguardFilesTask extends ATask<AndroidAppProject> {
    public ExtractProguardFilesTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return null;
    }

    @Override
    public boolean run() throws Exception {
        return false;
    }
}
