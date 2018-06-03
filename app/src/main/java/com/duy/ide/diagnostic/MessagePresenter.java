package com.duy.ide.diagnostic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.ide.common.blame.parser.aapt.AaptOutputParser;
import com.android.utils.ILogger;
import com.duy.JavaApplication;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.diagnostic.parser.JavaOutputParser;
import com.duy.ide.diagnostic.parser.ToolOutputParser;
import com.duy.ide.editor.code.ProjectManagerActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by duy on 19/07/2017.
 */

public class MessagePresenter implements MessageContract.Presenter, ILogger {
    private static final String TAG = "MessagePresenter";
    private PrintStream stdOut;
    private PrintStream stdErr;

    private ProjectManagerActivity mActivity;
    private BottomPageAdapter mAdapter;
    private MessageContract.View mLogView;
    private DiagnosticPresenter mDiagnosticPresenter;
    private ToolOutputParser mToolOutputParser;

    /**
     * @param diagnosticPresenter -use for add message parsed from stdout and stderrl
     */
    public MessagePresenter(ProjectManagerActivity activity,
                            BottomPageAdapter adapter,
                            DiagnosticPresenter diagnosticPresenter) {
        mActivity = activity;
        mAdapter = adapter;
        mLogView = (MessageContract.View) adapter.getExistingFragment(0);
        mDiagnosticPresenter = diagnosticPresenter;
        if (mLogView != null) {
            mLogView.setPresenter(this);
        }

        initParser();
    }


    private void initParser() {
        stdErr = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                if (mLogView != null) {
                    mLogView.appendErr(b, off, len);
                }
            }
        });
        stdOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                if (mLogView != null) {
                    mLogView.appendOut(b, off, len);
                }
            }
        });

        PatternAwareOutputParser[] patternAwareOutputParsers = {
                new AaptOutputParser(),
                new JavaOutputParser()
        };
        mToolOutputParser = new ToolOutputParser(patternAwareOutputParsers, getILogger());
    }

    @Override
    public void clear() {
        this.mLogView = (MessageContract.View) mAdapter.getExistingFragment(0);
        if (mLogView != null) {
            mLogView.clear();
        }
    }

    @Override
    public void append(String message) {
        this.mLogView = (MessageContract.View) mAdapter.getExistingFragment(0);
        if (mLogView != null) {
            mLogView.append(message);
        }
    }

    @Override
    public void resume(JavaApplication application) {
        Log.d(TAG, "resume() called with: application = [" + application + "]");

        application.addStdOut(stdOut);
        application.addStdErr(stdErr);
    }

    @Override
    public void pause(JavaApplication application) {
        Log.d(TAG, "pause() called with: application = [" + application + "]");

        application.removeOutStream(stdOut);
        application.removeErrStream(stdErr);
    }

    public ILogger getILogger() {

        return this;
    }

    @Override
    public void error(Throwable t, String msgFormat, Object... args) {

    }

    @Override
    public void warning(String msgFormat, Object... args) {

    }

    @Override
    public void info(String msgFormat, Object... args) {

    }

    @Override
    public void verbose(String msgFormat, Object... args) {

    }

    public PrintStream getStdErr() {
        return stdErr;
    }

    public PrintStream getStdOut() {
        return stdOut;
    }
}
