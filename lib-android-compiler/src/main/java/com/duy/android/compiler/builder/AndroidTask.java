package com.duy.android.compiler.builder;

import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.AndroidAppProject;

public abstract class AndroidTask extends Task<AndroidAppProject> {
    public AndroidTask(AndroidAppBuilder builder) {
        super(builder);
    }
}
