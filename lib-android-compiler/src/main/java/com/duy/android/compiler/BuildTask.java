package com.duy.android.compiler;

import android.os.AsyncTask;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.project.AndroidApplicationProject;
import com.duy.android.compiler.project.JavaProject;

public class BuildTask<T extends JavaProject> extends AsyncTask<AndroidApplicationProject, Object, Boolean> {
    private IBuilder<T> builder;
    private BuildTask.CompileListener<T> listener;
    private Exception exception;

    public BuildTask(IBuilder<T> builder, BuildTask.CompileListener<T> listener) {
        this.builder = builder;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onStart();
    }

    @Override
    protected Boolean doInBackground(AndroidApplicationProject... params) {
        try {
            return builder.build(BuildType.DEBUG);
        } catch (Exception e) {
            exception = e;
            return false;
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        super.onPostExecute(result);
        if (!result) {
            listener.onError(exception);
        } else {
            listener.onComplete();
        }
    }

    public interface CompileListener<T extends JavaProject> {
        void onStart();

        void onError(Exception project);

        void onComplete();
    }
}
