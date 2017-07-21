package com.duy.testapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.util.Log;

import com.duy.testapplication.autocomplete.AutoCompleteProvider;
import com.duy.testapplication.model.Description;

import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteCodeEditText extends AppCompatEditText {
    private static final String TAG = "AutoCompleteCodeEditTex";
    private AutoCompleteProvider mAutoCompleteProvider;

    public void setAutoCompleteProvider(AutoCompleteProvider mAutoCompleteProvider) {
        this.mAutoCompleteProvider = mAutoCompleteProvider;
    }

    public AutoCompleteCodeEditText(Context context) {
        super(context);
        init(context);
    }

    public AutoCompleteCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public AutoCompleteCodeEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTypeface(Typeface.MONOSPACE);
        String str =
                "package com.duy.example;\n" +
                        "import java.util.ArrayList;\n" +
                        "ArrayList list = new ArrayList();" +
                        "\nlist.a";
        setText(str);
        setSelection(str.length());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (mAutoCompleteProvider != null) {
            try {
                ArrayList<? extends Description> suggestions =
                        mAutoCompleteProvider.getSuggestions(this, getSelectionEnd());
                if (suggestions != null) {
                    for (Description suggestion : suggestions) {
                        Log.d(TAG, "onTextChanged: " + suggestion);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
    }
}
