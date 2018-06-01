package com.duy.android.compiler.task;

import android.content.Context;
import android.support.annotation.WorkerThread;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.project.JavaProject;

public abstract class ABuildTask<T extends JavaProject> {
    protected final IBuilder builder;
    protected final T project;
    protected final Context context;

    public ABuildTask(IBuilder<? extends T> builder) {
        this.builder = builder;
        this.project = builder.getProject();
        this.context = builder.getContext();
    }

    public abstract String getTaskName();

    @WorkerThread
    public abstract boolean run() throws Exception;
}
