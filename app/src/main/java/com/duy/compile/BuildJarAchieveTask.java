package com.duy.compile;

import android.os.AsyncTask;

import com.android.annotations.NonNull;
import com.duy.compile.external.CommandManager;
import com.duy.project.file.java.JavaProjectFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

public class BuildJarAchieveTask extends AsyncTask<JavaProjectFile, Object, File> {
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
    protected File doInBackground(JavaProjectFile... params) {
        JavaProjectFile projectFile = params[0];
        if (params[0] == null) return null;
        PrintWriter printWriter = new PrintWriter(new Writer() {
            @Override
            public void write(@NonNull char[] chars, int i, int i1) throws IOException {
                publishProgress(chars, i, i1);
            }

            @Override
            public void flush() throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        });
        //clean
        projectFile.clean();
        projectFile.createBuildDir();
        try {
            return CommandManager.buildJarAchieve(projectFile, printWriter, mDiagnosticCollector);
        } catch (Exception e) {
            this.error = e;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        try {
            char[] chars = (char[]) values[0];
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

    public static interface CompileListener {
        void onStart();

        void onError(Exception e, List<Diagnostic> diagnostics);

        void onComplete(File jarfile, List<Diagnostic> diagnostics);

        void onNewMessage(char[] chars, int start, int end);
    }
}
