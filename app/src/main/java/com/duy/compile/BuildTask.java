package com.duy.compile;

import android.os.AsyncTask;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.builder.model.BuildType;
import com.duy.project.file.android.AndroidProject;
import com.duy.project.file.java.JavaProject;

public class BuildTask<T extends JavaProject> extends AsyncTask<AndroidProject, Object, Boolean> {
    private static final String TAG = "BuildApkTask";
    private IBuilder<T> builder;
    private BuildTask.CompileListener<T> listener;
    private Exception exception;

    public BuildTask(IBuilder<T> builder, BuildTask.CompileListener listener) {
        this.builder = builder;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onStart();
    }

    @Override
    protected Boolean doInBackground(AndroidProject... params) {
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
