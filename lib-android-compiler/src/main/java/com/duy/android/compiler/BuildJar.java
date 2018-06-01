package com.duy.android.compiler;

import android.content.Context;
import android.os.AsyncTask;

import com.duy.android.compiler.builder.JavaProjectBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.file.JavaProject;

import java.io.File;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;

public class BuildJar extends AsyncTask<JavaProject, Object, File> {
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
            JavaProjectBuilder builder = new JavaProjectBuilder(context, projectFile, mDiagnosticCollector);
            if (builder.build(BuildType.DEBUG)) {
                return projectFile.getOutJarArchive();
            }
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
