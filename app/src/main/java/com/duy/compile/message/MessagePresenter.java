package com.duy.compile.message;

/**
 * Created by duy on 19/07/2017.
 */

public class MessagePresenter implements MessageContract.Presenter {


    private MessageContract.View view;

    public MessagePresenter(MessageContract.View view) {

        this.view = view;
    }

    @Override
    public void clear() {
        view.clear();
    }

    @Override
    public void append(char[] chars, int start, int end) {
        view.append(chars, start, end);
    }
}
