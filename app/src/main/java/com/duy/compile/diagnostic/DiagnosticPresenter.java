package com.duy.compile.diagnostic;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.ide.EditPageContract;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.editor.BaseEditorActivity;

import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticPresenter implements DiagnosticContract.Presenter {

    private static final String TAG = "DiagnosticPresenter";
    private BaseEditorActivity mMainActivity;
    private BottomPageAdapter adapter;
    private EditPageContract.Presenter mPagePresenter;
    @Nullable
    private DiagnosticContract.View view;

    public DiagnosticPresenter(BaseEditorActivity mainActivity,
                               @NonNull BottomPageAdapter adapter,
                               EditPageContract.Presenter pagePresenter) {
        this.mMainActivity = mainActivity;
        this.adapter = adapter;
        this.mPagePresenter = pagePresenter;
        this.view = (DiagnosticFragment) adapter.getExistingFragment(1);
        if (view != null) {
            view.setPresenter(this);
        }
    }

    @Override
    public void click(Diagnostic diagnostic) {
        Log.d(TAG, "click() called with: diagnostic = [" + diagnostic + "]");
        mMainActivity.closeDrawer(GravityCompat.START);

        Object source = diagnostic.getSource();
        if (source instanceof JavaFileObject && diagnostic.getKind() == Diagnostic.Kind.ERROR) {
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
        this.view = (DiagnosticContract.View) adapter.getExistingFragment(1);
        if (view != null) {
            view.clear();
        }
    }

    public void display(List<Diagnostic> diagnostics) {
        view.display(diagnostics);
    }
}
