package com.duy.compile.external.android.builder.task;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.duy.compile.external.android.builder.AndroidBuilder2;
import com.duy.project.file.android.AndroidProject;

abstract class BuildTask {
    protected final AndroidBuilder2 builder;
    protected final AndroidProject project;
    protected final Context context;

    public BuildTask(AndroidBuilder2 builder, AndroidProject project) {
        this.builder = builder;
        this.project = project;
        this.context = builder.getContext();
    }

    @WorkerThread
    public abstract boolean run() throws Throwable;
}
