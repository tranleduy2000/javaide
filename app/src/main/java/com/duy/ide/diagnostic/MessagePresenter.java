package com.duy.ide.diagnostic;

import android.support.annotation.NonNull;
import android.util.Log;

import com.duy.JavaApplication;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.editor.code.ProjectManagerActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Created by duy on 19/07/2017.
 */

public class MessagePresenter implements MessageContract.Presenter {
    private static final String TAG = "MessagePresenter";
    private ProjectManagerActivity activity;
    private BottomPageAdapter adapter;
    private MessageContract.View view;
    private final PrintStream out;
    private final PrintStream err;

    public MessagePresenter(ProjectManagerActivity activity, BottomPageAdapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.setPresenter(this);
        }
        err = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                if (view != null) {
                    view.appendErr(b, off, len);
                }
            }
        });
        out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(@NonNull byte[] b, int off, int len) throws IOException {
                if (view != null) {
                    view.appendOut(b, off, len);
                }
            }
        });
    }

    @Override
    public void clear() {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.clear();
        }
    }

    @Override
    public void append(String s) {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.append(s);
        }
    }

    @Override
    public void resume(JavaApplication application) {
        Log.d(TAG, "resume() called with: application = [" + application + "]");

        application.addStdOut(out);
        application.addStdErr(err);
    }

    @Override
    public void pause(JavaApplication application) {
        Log.d(TAG, "pause() called with: application = [" + application + "]");

        application.removeOutStream(out);
        application.removeErrStream(err);
    }

}
