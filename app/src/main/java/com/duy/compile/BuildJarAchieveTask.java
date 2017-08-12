package com.duy.compile;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.compile.external.CommandManager;
import com.duy.project.file.java.JavaProjectFolder;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

public class BuildJarAchieveTask extends AsyncTask<JavaProjectFolder, Object, File> {
    private static final String TAG = "BuildJarAchieveTask";
    private BuildJarAchieveTask.CompileListener listener;
    private DiagnosticCollector mDiagnosticCollector;
    private Exception error;

    public BuildJarAchieveTask(BuildJarAchieveTask.CompileListener context) {
        this.listener = context;
        mDiagnosticCollector = new DiagnosticCollector();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onStart();
    }

    @Override
    protected File doInBackground(JavaProjectFolder... params) {
        JavaProjectFolder projectFile = params[0];
        if (params[0] == null) return null;
        OutputStream out = new OutputStream() {
            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                publishProgress(b, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }
        };
        try {
            out.write("Clean...\n".getBytes());
            projectFile.clean();
        } catch (IOException ignored) {
        }

        try {
            return CommandManager.buildJarAchieve(projectFile, out, mDiagnosticCollector);
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
            Log.d(TAG, new String(chars, start, end));
        } catch (Exception e) {
            Log.e(TAG, "onProgressUpdate: ", e);
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

    public static interface CompileListener {
        void onStart();

        void onError(Exception e, List<Diagnostic> diagnostics);

        void onComplete(File jarfile, List<Diagnostic> diagnostics);

        void onNewMessage(byte[] chars, int start, int end);
    }
}
