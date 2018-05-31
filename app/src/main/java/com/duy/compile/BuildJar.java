package com.duy.compile;

import android.content.Context;
import android.os.AsyncTask;

import com.duy.compile.builder.CompileHelper;
import com.duy.project.file.java.JavaProject;

import java.io.File;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

public class BuildJar extends AsyncTask<JavaProject, Object, File> {
    private static final String TAG = "BuildJarAchieveTask";
    private Context context;
    private BuildJar.CompileListener listener;
    private DiagnosticCollector mDiagnosticCollector;
    private Exception error;

    public BuildJar(Context context, CompileListener listener) {
        this.context = context;
        this.listener = listener;
        mDiagnosticCollector = new DiagnosticCollector();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onStart();
    }

    @Override
    protected File doInBackground(JavaProject... params) {
        JavaProject projectFile = params[0];
        if (params[0] == null) {
            return null;
        }
        try {
            projectFile.clean();
            return CompileHelper.buildJarAchieve(context, projectFile, mDiagnosticCollector);
        } catch (Exception e) {
            this.error = e;
        }
        return null;
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

    public interface CompileListener {
        void onStart();

        void onError(Exception e, List<Diagnostic> diagnostics);

        void onComplete(File jarfile, List<Diagnostic> diagnostics);
    }
}
