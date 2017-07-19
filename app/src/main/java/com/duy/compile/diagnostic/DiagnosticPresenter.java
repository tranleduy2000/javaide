package com.duy.compile.diagnostic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.editor.EditPageContract;
import com.duy.editor.editor.MainActivity;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticPresenter implements DiagnosticContract.Presenter {

    private static final String TAG = "DiagnosticPresenter";
    private DiagnosticContract.View view;
    private MainActivity mainActivity;
    private EditPageContract.Presenter mPagePresenter;

    public DiagnosticPresenter(@NonNull DiagnosticContract.View view,
                               EditPageContract.Presenter pagePresenter) {
        this.view = view;
        this.mPagePresenter = pagePresenter;
        view.setPresenter(this);
    }

    @Override
    public void click(Diagnostic diagnostic) {
        Log.d(TAG, "click() called with: diagnostic = [" + diagnostic + "]");
        Object source = diagnostic.getSource();
        if (source instanceof JavaFileObject) {
            String path = ((JavaFileObject) source).getName();
            int i = mPagePresenter.gotoPage(path);
            if (i == -1) {
                mPagePresenter.addPage(path, true);
            }
            EditPageContract.View editor = mPagePresenter.getCurrentPage();
            if (editor == null) return;
            long startPosition = diagnostic.getStartPosition();
            long endPosition = diagnostic.getEndPosition();
            editor.highlightError(startPosition, endPosition);
        } else {
            // TODO: 19/07/2017 implement other
        }
    }

    @Override
    public void clear() {
        view.clear();
    }

    public void display(List<Diagnostic> diagnostics) {
        view.display(diagnostics);
    }
}
