package com.duy.compile;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.compile.builder.JavaProjectBuilder;
import com.duy.compile.builder.model.BuildType;
import com.duy.project.file.java.JavaProject;

import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class BuildJava extends AsyncTask<JavaProject, Object, Boolean> {
    private static final String TAG = "CompileJavaTask";
    private ArrayList<Diagnostic> mDiagnostics = new ArrayList<>();
    private final DiagnosticListener mListener = new DiagnosticListener() {
        @Override
        public void report(Diagnostic diagnostic) {
            mDiagnostics.add(diagnostic);
        }
    };
    private JavaProject projectFile;
    private Context context;
    @Nullable
    private CompileListener compileListener;
    private Throwable error;

    public BuildJava(Context context, CompileListener compileListener) {
        this.context = context;
        this.compileListener = compileListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (compileListener != null) compileListener.onStart();
    }

    @Override
    protected Boolean doInBackground(JavaProject... params) {
        if (params[0] == null) return null;
        this.projectFile = params[0];
        JavaProjectBuilder builder = new JavaProjectBuilder(context, projectFile, mListener);
        return builder.build(BuildType.DEBUG);
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        try {
            char[] chars = (char[]) values[0];
            int start = (int) values[1];
            int end = (int) values[2];
            if (compileListener != null) {
                compileListener.onNewMessage(new String(chars, start, end));
            }
            Log.d(TAG, new String(chars, start, end));
        } catch (Exception e) {
            Log.e(TAG, "onProgressUpdate: ", e);
        }
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        super.onPostExecute(result);
        if (result) {
            if (compileListener != null) compileListener.onError(error, mDiagnostics);
        } else {
            if (compileListener != null) compileListener.onComplete(projectFile, mDiagnostics);
        }
    }

    public interface CompileListener {
        void onStart();

        void onError(Throwable e, ArrayList<Diagnostic> diagnostics);

        void onComplete(JavaProject projectFile, List<Diagnostic> diagnostics);

        void onNewMessage(byte[] chars, int start, int end);

        void onNewMessage(String msg);
    }
}
