package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.AndroidAppProject;

public class MergeResourceTask extends Task<AndroidAppProject> {

    public MergeResourceTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Merge resource task";
    }

    @Override
    public boolean doFullTaskAction() throws Exception {
        // TODO: 02-Jun-18 impl
        return true;
    }
}
