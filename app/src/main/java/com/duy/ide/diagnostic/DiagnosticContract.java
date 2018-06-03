package com.duy.ide.diagnostic;

import com.android.ide.common.blame.Message;

import java.util.List;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticContract {
    public interface View {
        public void display(List<Message> diagnostics);

        public void clear();

        public void setPresenter(Presenter presenter);
    }

    public interface Presenter {
        public void click(Message diagnostic);

        public void clear();

        public void display(List<Message> diagnostics);

        void add(List<Message> message);
    }
}
