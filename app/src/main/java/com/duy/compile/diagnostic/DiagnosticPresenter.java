package com.duy.compile.diagnostic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.editor.EditContract;
import com.duy.editor.editor.MainActivity;

import java.util.List;

import javax.tools.Diagnostic;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticPresenter implements DiagnosticContract.Presenter {

    private static final String TAG = "DiagnosticPresenter";
    private DiagnosticContract.View view;
    private MainActivity mainActivity;
    private EditContract.Presenter mEditPresenter;

    public DiagnosticPresenter(@NonNull DiagnosticContract.View view,
                               EditContract.Presenter editPresenter) {
        this.view = view;
        this.mEditPresenter = editPresenter;
        view.setPresenter(this);
    }

    @Override
    public void click(Diagnostic diagnostic) {
        Log.d(TAG, "click() called with: diagnostic = [" + diagnostic + "]");

    }

    @Override
    public void clear() {
        view.clear();
    }

    public void display(List<Diagnostic> diagnostics) {
        view.display(diagnostics);
    }
}
