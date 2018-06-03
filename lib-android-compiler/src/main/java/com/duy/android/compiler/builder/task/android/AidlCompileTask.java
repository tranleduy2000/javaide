package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.project.AndroidAppProject;

public class AidlCompileTask extends ATask<AndroidAppProject> {

    public AidlCompileTask(IBuilder<? extends AndroidAppProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Compile aidl";
    }

    @Override
    public boolean run() throws Exception {
        // TODO: 02-Jun-18 impl
        return true;
    }
}
