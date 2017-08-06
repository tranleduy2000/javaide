package com.duy.compile;

import android.os.AsyncTask;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.compile.external.CommandManager;
import com.duy.project.file.java.JavaProjectFile;
import com.sun.tools.javac.main.Main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class CompileJavaTask extends AsyncTask<JavaProjectFile, Object, Integer> {
    private ArrayList<Diagnostic> mDiagnostics = new ArrayList<>();
    private JavaProjectFile projectFile;
    @Nullable
    private CompileListener compileListener;
    private Exception error;


    public CompileJavaTask(CompileListener compileListener) {
        this.compileListener = compileListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (compileListener != null) compileListener.onStart();
    }

    @Override
    protected Integer doInBackground(JavaProjectFile... params) {
        if (params[0] == null) return null;
        this.projectFile = params[0];
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
        DiagnosticListener listener = new DiagnosticListener() {
            @Override
            public void report(Diagnostic diagnostic) {
                mDiagnostics.add(diagnostic);
            }
        };
        //clean task
        projectFile.clean();
        projectFile.createBuildDir();

        int status = CommandManager.compileJava(projectFile, printWriter, listener);
        if (status != Main.EXIT_ERROR) {
            try {
                CommandManager.convertToDexFormat(projectFile);
            } catch (Exception e) {
                this.error = e;
                e.printStackTrace();
                publishProgress(e.getMessage().toCharArray(), 0, e.getMessage().length());
                status = Main.EXIT_ERROR;
            }
        }
        return status;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        try {
            char[] chars = (char[]) values[0];
            int start = (int) values[1];
            int end = (int) values[2];
            if (compileListener != null) compileListener.onNewMessage(chars, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(final Integer result) {
        super.onPostExecute(result);
        if (result == null) {
            if (compileListener != null) compileListener.onError(error, mDiagnostics);
        } else {
            if (compileListener != null) compileListener.onComplete(projectFile, mDiagnostics);
        }
    }

    public interface CompileListener {
        void onStart();

        void onError(Exception e, ArrayList<Diagnostic> diagnostics);

        void onComplete(JavaProjectFile projectFile, List<Diagnostic> diagnostics);

        void onNewMessage(char[] chars, int start, int end);
    }
}
