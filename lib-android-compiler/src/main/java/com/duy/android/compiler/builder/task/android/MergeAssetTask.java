package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.AndroidAppProject;

public class MergeAssetTask extends ABuildTask<AndroidAppProject> {

    public MergeAssetTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Merge Asset";
    }

    @Override
    public boolean run() throws Exception {
        // TODO: 02-Jun-18 impl
        return true;
    }
}
