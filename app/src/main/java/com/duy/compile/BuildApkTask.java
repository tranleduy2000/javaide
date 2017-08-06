package com.duy.compile;

import android.os.AsyncTask;

import com.duy.compile.external.CommandManager;
import com.duy.project.file.android.AndroidProjectFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

public class BuildApkTask extends AsyncTask<AndroidProjectFile, Object, File> {
    private BuildApkTask.CompileListener listener;
    private DiagnosticCollector mDiagnosticCollector;
    private Exception error;

    public BuildApkTask(BuildApkTask.CompileListener context) {
        this.listener = context;
        mDiagnosticCollector = new DiagnosticCollector();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onStart();
    }

    @Override
    protected File doInBackground(AndroidProjectFile... params) {
        AndroidProjectFile projectFile = params[0];
        if (params[0] == null) return null;
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(@android.support.annotation.NonNull byte[] b) throws IOException {
                super.write(b);
            }

            @Override
            public void write(@android.support.annotation.NonNull byte[] b, int off, int len) throws IOException {
                publishProgress(b, off, len);

            }

            @Override
            public void write(int b) throws IOException {

            }
        };
        //clean
        projectFile.clean();
        projectFile.createBuildDir();
        try {
            return CommandManager.buildApk(projectFile, outputStream, mDiagnosticCollector);
        } catch (Exception e) {
            this.error = e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        try {
            byte[] chars = (byte[]) values[0];
            int start = (int) values[1];
            int end = (int) values[2];
            listener.onNewMessage(chars, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(final File result) {
        super.onPostExecute(result);

        if (result == null) {
            listener.onError(error, mDiagnosticCollector.getDiagnostics());
        } else {
            listener.onComplete(result, mDiagnosticCollector.getDiagnostics());
        }
    }

    public  interface CompileListener {
        void onStart();

        void onError(Exception e, List<Diagnostic> diagnostics);

        void onComplete(File apk, List<Diagnostic> diagnostics);

        void onNewMessage(byte[] chars, int start, int end);
    }
}
