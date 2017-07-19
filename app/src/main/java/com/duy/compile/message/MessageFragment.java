package com.duy.compile.message;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duy.editor.R;

/**
 * Created by duy on 19/07/2017.
 */

public class MessageFragment extends android.support.v4.app.Fragment implements MessageContract.View {
    private static final String KEY_COMPILE_MSG = "compile_msg";
    private TextView mCompileMsg;
    @Nullable
    private MessageContract.Presenter presenter;

    public static MessageFragment newInstance() {

        Bundle args = new Bundle();

        MessageFragment fragment = new MessageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_msg, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCompileMsg = view.findViewById(R.id.txt_msg);
        mCompileMsg.setTypeface(Typeface.MONOSPACE);
        if (savedInstanceState != null) {
            mCompileMsg.setText(savedInstanceState.getString(KEY_COMPILE_MSG));
        }
    }

    @Override
    public void append(String text) {
        mCompileMsg.append(text);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCompileMsg != null) {
            outState.putString(KEY_COMPILE_MSG, mCompileMsg.getText().toString());
        }
    }

    @Override
    public void append(char[] chars, int start, int end) {
        mCompileMsg.append(new String(chars), start, end);
    }

    @Override
    public void clear() {
        mCompileMsg.setText("");
    }

    @Override
    public void setPresenter(MessageContract.Presenter presenter) {

        this.presenter = presenter;
    }
}
