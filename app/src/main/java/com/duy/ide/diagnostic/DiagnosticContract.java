package com.duy.ide.diagnostic;

import android.support.annotation.WorkerThread;

import com.android.ide.common.blame.Message;

import java.util.List;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticContract {
    public interface View {
        @WorkerThread
        void display(List<Message> diagnostics);

        @WorkerThread
        void clear();

        void setPresenter(Presenter presenter);

        @WorkerThread
        void appendMessages(List<Message> messages);
    }


    public interface Presenter {
        void click(Message diagnostic);

        void clear();

        void display(List<Message> diagnostics);

        void appendMessages(List<Message> message);
    }
}
