package com.duy.compile.diagnostic;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.util.Log;

import com.duy.ide.EditPageContract;
import com.duy.ide.editor.BaseEditorActivity;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticPresenter implements DiagnosticContract.Presenter {

    private static final String TAG = "DiagnosticPresenter";
    private DiagnosticContract.View view;
    private BaseEditorActivity mMainActivity;
    private EditPageContract.Presenter mPagePresenter;

    public DiagnosticPresenter(BaseEditorActivity mainActivity,
                               @NonNull DiagnosticContract.View view,
                               EditPageContract.Presenter pagePresenter) {
        this.mMainActivity = mainActivity;
        this.view = view;
        this.mPagePresenter = pagePresenter;
        view.setPresenter(this);
    }

    @Override
    public void click(Diagnostic diagnostic) {
        Log.d(TAG, "click() called with: diagnostic = [" + diagnostic + "]");
        mMainActivity.closeDrawer(GravityCompat.START);

        Object source = diagnostic.getSource();
        if (source instanceof JavaFileObject) {
            String path = ((JavaFileObject) source).getName();
            int i = mPagePresenter.gotoPage(path);
            if (i == -1) {
                mPagePresenter.addPage(path, true);
            }
            EditPageContract.View editor = mPagePresenter.getCurrentPage();
            if (editor == null) {
                Log.d(TAG, "click: editor null");
                return;
            }
            int startPosition = (int) diagnostic.getStartPosition();
            int endPosition = (int) diagnostic.getEndPosition();
            editor.highlightError(startPosition, endPosition);
            editor.setCursorPosition(endPosition);
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
