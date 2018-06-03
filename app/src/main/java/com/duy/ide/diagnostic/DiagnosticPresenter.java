package com.duy.ide.diagnostic;

import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.util.Log;

import com.android.annotations.Nullable;
import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.SourceFile;
import com.android.ide.common.blame.SourceFilePosition;
import com.android.ide.common.blame.SourcePosition;
import com.duy.ide.EditPageContract;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.editor.code.ProjectManagerActivity;

import java.util.List;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticPresenter implements DiagnosticContract.Presenter {

    private static final String TAG = "DiagnosticPresenter";
    private ProjectManagerActivity mMainActivity;
    private BottomPageAdapter adapter;
    private EditPageContract.Presenter mPagePresenter;
    @Nullable
    private DiagnosticContract.View mView;

    public DiagnosticPresenter(ProjectManagerActivity mainActivity,
                               @NonNull BottomPageAdapter adapter,
                               EditPageContract.Presenter pagePresenter) {
        this.mMainActivity = mainActivity;
        this.adapter = adapter;
        this.mPagePresenter = pagePresenter;
        this.mView = (DiagnosticFragment) adapter.getExistingFragment(1);
        if (mView != null) {
            mView.setPresenter(this);
        }
    }

    @Override
    public void click(Message diagnostic) {
        Log.d(TAG, "click() called with: diagnostic = [" + diagnostic + "]");
        mMainActivity.closeDrawer(GravityCompat.START);

        List<SourceFilePosition> sourceFilePositions = diagnostic.getSourceFilePositions();
        SourceFilePosition source = sourceFilePositions.get(0);
        SourceFile file = source.getFile();
        if (file != null) {
            int i = mPagePresenter.gotoPage(file.getSourceFile());
            if (i == -1) {
                mPagePresenter.addPage(file.getSourceFile(), true);
            }
            EditPageContract.SourceView editor = mPagePresenter.getCurrentPage();
            if (editor == null) {
                Log.d(TAG, "click: editor null");
                return;
            }
            SourcePosition position = source.getPosition();
            int startPosition = position.getStartLine();
            int startColumn = position.getStartColumn();
            editor.gotoLine(startPosition, startColumn);
        }
    }

    @Override
    public void clear() {
        this.mView = getView();
        if (mView != null) {
            mView.setPresenter(this);
            mView.clear();
        }
    }

    public void display(List<Message> diagnostics) {
        this.mView = getView();
        if (mView != null) {
            mView.setPresenter(this);
            mView.display(diagnostics);
        }
    }

    @Override
    public void appendMessages(List<Message> messages) {
        this.mView = getView();
        if (mView != null) {
            try {
                mView.appendMessages(messages);
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
    }

    private DiagnosticContract.View getView() {
        return (DiagnosticContract.View) adapter.getExistingFragment(1);
    }
}
