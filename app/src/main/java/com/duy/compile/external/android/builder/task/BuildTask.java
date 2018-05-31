package com.duy.compile.external.android.builder.task;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.duy.compile.external.android.builder.AndroidBuilder;
import com.duy.project.file.android.AndroidProject;

public abstract class BuildTask {
    protected final AndroidBuilder builder;
    protected final AndroidProject project;
    protected final Context context;

    BuildTask(AndroidBuilder builder) {
        this.builder = builder;
        this.project = builder.getProject();
        this.context = builder.getContext();
    }

    public abstract String getTaskName();

    @WorkerThread
    public abstract boolean run() throws Exception;
}
