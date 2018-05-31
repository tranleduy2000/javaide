package com.duy.compile.task;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.duy.compile.builder.IBuilder;
import com.duy.project.file.android.AndroidProject;

public abstract class ABuildTask {
    protected final IBuilder builder;
    protected final AndroidProject project;
    protected final Context context;

    ABuildTask(IBuilder builder) {
        this.builder = builder;
        this.project = builder.getProject();
        this.context = builder.getContext();
    }

    public abstract String getTaskName();

    @WorkerThread
    public abstract boolean run() throws Exception;
}
