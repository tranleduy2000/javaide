package com.duy.compile.message;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duy.editor.R;

/**
 * Created by duy on 19/07/2017.
 */

public class MessageFragment extends android.support.v4.app.Fragment implements MessageContract.View {
    public static MessageFragment newInstance() {

        Bundle args = new Bundle();

        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_msg,container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void append(String text) {

    }

    @Override
    public void append(char[] chars, int start, int end) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void setPresenter(MessageContract.Presenter presenter) {

    }
}
