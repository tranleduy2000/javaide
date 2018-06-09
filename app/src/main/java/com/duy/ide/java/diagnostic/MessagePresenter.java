package com.duy.ide.java.diagnostic;

import android.support.annotation.NonNull;

import com.android.ide.common.blame.Message;
import com.android.ide.common.blame.parser.PatternAwareOutputParser;
import com.android.ide.common.blame.parser.aapt.AaptOutputParser;
import com.android.utils.ILogger;
import com.duy.JavaApplication;
import com.duy.ide.java.adapters.BottomPageAdapter;
import com.duy.ide.java.diagnostic.parser.ToolOutputParser;
import com.duy.ide.java.diagnostic.parser.java.JavaOutputParser;
import com.duy.ide.javaide.ProjectManagerActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

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
    private com.duy.ide.java.diagnostic.DiagnosticPresenter mDiagnosticPresenter;
    private ToolOutputParser mToolOutputParser;

    /**
     * @param diagnosticPresenter -use for add message parsed from stdout and stderrl
     */
    public MessagePresenter(ProjectManagerActivity activity,
                            BottomPageAdapter adapter,
                            com.duy.ide.java.diagnostic.DiagnosticPresenter diagnosticPresenter) {
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
                onNewErrorMessage(b, off, len);
            }
        });
        stdOut = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                onNewMessage(b, off, len);
            }
        });

        PatternAwareOutputParser[] patternAwareOutputParsers = {
                new AaptOutputParser(),
                new JavaOutputParser()
        };
        mToolOutputParser = new ToolOutputParser(patternAwareOutputParsers, this);
    }

    private void onNewMessage(byte[] b, int off, int len) {
        String output = new String(b, off, len);
        System.out.println(output);
        List<Message> messages = mToolOutputParser.parseToolOutput(output);
        mDiagnosticPresenter.appendMessages(messages);
        if (mLogView != null) {
            mLogView.append(output);
        }
    }


    private void onNewErrorMessage(byte[] b, int off, int len) {
        String output = new String(b, off, len);
        System.err.println(output);
        List<Message> messages = mToolOutputParser.parseToolOutput(output);
        mDiagnosticPresenter.appendMessages(messages);
        if (mLogView != null) {
            mLogView.append(output);
        }
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
    }

    @Override
    public void pause(JavaApplication application) {
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
