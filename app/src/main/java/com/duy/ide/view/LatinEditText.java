package com.duy.ide.view;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.AttributeSet;

import java.util.regex.Pattern;

/**
 * Created by Duy on 17-Jul-17.
 */

public class LatinEditText extends AppCompatEditText {

    private static final Pattern ACCEPT_CHAR = Pattern.compile("[a-zA-Z0-9._ ]");

    public LatinEditText(Context context) {
        super(context);
        init(context);
    }

    public LatinEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LatinEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (!(ACCEPT_CHAR.matcher(source.charAt(i) + "").find())) {
                        return "";
                    }
                }
                return null;
            }
        };
        setFilters(new InputFilter[]{filter});
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }
}
