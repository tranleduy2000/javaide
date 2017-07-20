package com.duy.testapplication;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteCodeEditText extends AppCompatEditText {
    private static final String TAG = "AutoCompleteCodeEditTex";

    public AutoCompleteCodeEditText(Context context) {
        super(context);
    }

    public AutoCompleteCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoCompleteCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        Log.d(TAG, "onTextChanged() called with: text = [" + text + "], start = [" + start + "], lengthBefore = [" + lengthBefore + "], lengthAfter = [" + lengthAfter + "]");

    }
}
