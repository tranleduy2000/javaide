package com.duy.android.compiler.builder.task;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.project.JavaProject;

public abstract class Task<T extends JavaProject> {
    protected final IBuilder mBuilder;
    protected final T mProject;
    protected final Context context;

    public Task(IBuilder<? extends T> builder) {
        this.mBuilder = builder;
        this.mProject = builder.getProject();
        this.context = builder.getContext();
    }

    public abstract String getTaskName();

    @WorkerThread
    public abstract boolean doFullTaskAction() throws Exception;

    public IBuilder getBuilder() {
        return mBuilder;
    }
}
