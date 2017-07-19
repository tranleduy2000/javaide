package com.duy.compile.message;

/**
 * Created by duy on 19/07/2017.
 */

public class MessageContract {
    public interface View {
        void append(String text);

        void append(char[] chars, int start, int end);

        void clear();

        void setPresenter(Presenter presenter);
    }

    public interface Presenter {

    }
}
