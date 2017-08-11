package com.duy.compile.message;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.editor.completion.Patterns;

import java.util.regex.Matcher;

/**
 * Created by duy on 19/07/2017.
 */

public class MessageFragment extends android.support.v4.app.Fragment implements MessageContract.View {
    public static final String TAG = "MessageFragment";
    private static final String KEY_COMPILE_MSG = "compile_msg";
    private TextView mCompileMsg;
    private ScrollView mScrollView;
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
        mCompileMsg = view.findViewById(R.id.txt_message);
        mCompileMsg.setTypeface(Typeface.MONOSPACE);
        mScrollView = view.findViewById(R.id.scrollView);
        if (savedInstanceState != null) {
            mCompileMsg.setText(savedInstanceState.getString(KEY_COMPILE_MSG));
        }
    }

    @Override
    public void append(String text) {
        mCompileMsg.append(text);
        mScrollView.fullScroll(View.FOCUS_DOWN);
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
        CharSequence charSequence = new String(chars).subSequence(start, end);
        SpannableString spannableString = new SpannableString(charSequence);
        Matcher matcher = Patterns.FILE_JAVA.matcher(spannableString);
        int color = ContextCompat.getColor(getContext(), R.color.dark_color_file_java);
        while (matcher.find()) {
            spannableString.setSpan(new ForegroundColorSpan(color), matcher.start(), matcher.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mCompileMsg.append(spannableString);
        mScrollView.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void clear() {
        if (mCompileMsg != null) {
            mCompileMsg.setText("");
        } else {
            Toast.makeText(getContext(), R.string.system_err_msg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setPresenter(@Nullable MessageContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
