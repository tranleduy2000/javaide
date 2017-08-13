package com.duy.compile;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.compile.external.CommandManager;
import com.duy.project.file.java.JavaProjectFolder;
import com.sun.tools.javac.main.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class CompileJavaTask extends AsyncTask<JavaProjectFolder, Object, Integer> {
    private static final String TAG = "CompileJavaTask";
    private ArrayList<Diagnostic> mDiagnostics = new ArrayList<>();
    private JavaProjectFolder projectFile;
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
    protected Integer doInBackground(JavaProjectFolder... params) {
        if (params[0] == null) return null;
        this.projectFile = params[0];
        PrintStream printStream = new PrintStream(new OutputStream() {
            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                publishProgress(b, off, error);
            }

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
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

        int status = CommandManager.compileJava(projectFile, printStream, listener);
        if (status == Main.EXIT_OK) {
            try {
                CommandManager.convertToDexFormat(projectFile, printStream);
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
            byte[] chars = (byte[]) values[0];
            int start = (int) values[1];
            int end = (int) values[2];
            if (compileListener != null) {
                compileListener.onNewMessage(new String(chars, start, end));
            }
            Log.d(TAG, new String(chars, start, end));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(final Integer result) {
        super.onPostExecute(result);
        if (result != Main.EXIT_OK) {
            if (compileListener != null) compileListener.onError(error, mDiagnostics);
        } else {
            if (compileListener != null) compileListener.onComplete(projectFile, mDiagnostics);
        }
    }

    public interface CompileListener {
        void onStart();

        void onError(Exception e, ArrayList<Diagnostic> diagnostics);

        void onComplete(JavaProjectFolder projectFile, List<Diagnostic> diagnostics);

        void onNewMessage(char[] chars, int start, int end);

        void onNewMessage(String msg);
    }
}
