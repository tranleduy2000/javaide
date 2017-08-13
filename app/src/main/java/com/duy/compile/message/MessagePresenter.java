package com.duy.compile.message;

import android.widget.Toast;

import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.editor.code.ProjectManagerActivity;

/**
 * Created by duy on 19/07/2017.
 */

public class MessagePresenter implements MessageContract.Presenter {

    private ProjectManagerActivity activity;
    private BottomPageAdapter adapter;
    private MessageContract.View view;


    public MessagePresenter(ProjectManagerActivity activity, BottomPageAdapter adapter) {
        this.activity = activity;
        this.adapter = adapter;
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.setPresenter(this);
        }
    }

    @Override
    public void clear() {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view == null) {
            Toast.makeText(activity, "An unexpected error has occurred with the system, please restart ide.",
                    Toast.LENGTH_LONG).show();
        } else {
            view.clear();
        }
    }

    @Override
    public void append(byte[] chars, int start, int end) {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.append(chars, start, end);
        }
    }

    @Override
    public void append(char[] chars, int start, int end) {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.append(chars, start, end);
        }
    }

    @Override
    public void append(String s) {
        this.view = (MessageContract.View) adapter.getExistingFragment(0);
        if (view != null) {
            view.append(s);
        }
    }
}
