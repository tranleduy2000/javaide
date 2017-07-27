package com.duy.compile.message;

import android.widget.Toast;

import com.duy.ide.editor.BaseEditorActivity;

/**
 * Created by duy on 19/07/2017.
 */

public class MessagePresenter implements MessageContract.Presenter {

    private BaseEditorActivity activity;
    private MessageContract.View view;


    public MessagePresenter(BaseEditorActivity activity, MessageContract.View view) {
        this.activity = activity;
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void clear() {
        if (view == null) {
            Toast.makeText(activity, "An unexpected error has occurred with the system, please restart ide.",
                    Toast.LENGTH_LONG).show();
        } else {
            view.clear();
        }
    }

    @Override
    public void append(char[] chars, int start, int end) {
        view.append(chars, start, end);
    }
}
