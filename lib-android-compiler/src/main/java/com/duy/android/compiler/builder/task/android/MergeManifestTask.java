package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.project.AndroidAppProject;

public class MergeManifestTask extends ABuildTask<AndroidAppProject> {
    private static final String TAG = "MergeManifestTask";

    public MergeManifestTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Merge manifest";
    }

    @Override
    public boolean run() throws Exception {
        builder.stderr(TAG + ": Not impalement yet");
        return true;
    }
}
