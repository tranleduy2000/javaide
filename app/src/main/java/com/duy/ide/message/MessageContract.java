package com.duy.ide.message;

import android.support.annotation.WorkerThread;

import com.duy.JavaApplication;

/**
 * Created by duy on 19/07/2017.
 */

public class MessageContract {
    public interface View {
        @WorkerThread
        void append(String text);

        @WorkerThread
        void appendOut(byte[] chars, int start, int end);

        @WorkerThread
        void appendErr(byte[] chars, int start, int end);

        @WorkerThread
        void clear();

        void setPresenter(Presenter presenter);
    }

    public interface Presenter {
        @WorkerThread
        void clear();

        @WorkerThread
        void append(String s);

        void resume(JavaApplication application);

        void pause(JavaApplication application);
    }
}
