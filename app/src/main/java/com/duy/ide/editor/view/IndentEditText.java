/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.editor.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatMultiAutoCompleteTextView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Duy on 12-May-17.
 */

public class IndentEditText extends AppCompatMultiAutoCompleteTextView {
    public static final String TAB_CHARACTER = "  ";
    public static final String TAB = "  "; //2 space
    public static final String CURSOR = "\u2622";
    private static final String TAG = "AutoIndentEditText";

    public IndentEditText(Context context) {
        super(context);
        init();

    }

    public IndentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public IndentEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public static char getCloseBracket(char open, int index) {
        switch (open) {
            case '(':
                return ')';
            case '{':
                return '}';
            case '[':
                return ']';
        }
        return 0;
    }

    public void applyTabWidth(Editable text, int start, int end) {
        /*String str = text.toString();
        float tabWidth = getPaint().measureText(INDEX_CHAR) * TAB_NUMBER;
        while (start < end) {
            int index = str.indexOf("\t", start);
            if (index < 0)
                break;
            text.setSpan(new CustomTabWidthSpan(Float.valueOf(tabWidth).intValue()), index, index + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = index + 1;
        }*/
    }

    public void applyTabWidth() {
        applyTabWidth(getText(), 0, getText().length());
    }

    private void init() {
        setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start,
                                               int end, Spanned dest, int dstart, int dend) {
                        if (end - start == 1 && start < source.length() && dstart < dest.length()) {
                            char c = source.charAt(start);
                            if (c == '\n') {
                                return indentLine(source, start, end, dest, dstart, dend);
                            } else {
                                return addBracket(source, start, end, dest, dstart, dend);
                            }
                        }
                        return source;
                    }
                }
        });//auto add bracket
        addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
            }

            @Override
            public void afterTextChanged(Editable editable) {
              if (editable.length() > start && count > 1) {
                    CharSequence newText = editable.subSequence(start, start + count);
                    int i = newText.toString().indexOf(CURSOR);
                    if (i > -1) {
                        editable.delete(start + i, start + i + 1);
                        setSelection(start);
                    }
                }
            }
        });
    }

    private CharSequence addBracket(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        switch (source.charAt(start)) {
            case '"':
                return "\"" + CURSOR + "\"";
            case '\'':
                return "'" + CURSOR + "'";
            case '(':
                return "(" + CURSOR + ")";
            case '{':
                return "{" + CURSOR + "}";
            case '[':
                return "[" + CURSOR + "]";
        }
        return source;
    }

    /**
     * @return the line above current cursor
     */
    @Nullable
    private CharSequence getPrevLine(Editable editable, Layout layout, int currentLine) {
        if (currentLine - 1 < 0) return null;
        int lineStart = layout.getLineStart(currentLine - 1);
        int lineEnd = layout.getLineEnd(currentLine - 1);
        return editable.subSequence(lineStart, lineEnd);
    }

    @Nullable
    protected CharSequence getNextLine(Editable editable, Layout layout, int currentLine) {
        if (currentLine + 1 > layout.getLineCount() - 1) return null;
        int lineStart = layout.getLineStart(currentLine + 1);
        int lineEnd = layout.getLineEnd(currentLine + 1);
        return editable.subSequence(lineStart, lineEnd);
    }

    @Nullable
    protected CharSequence getWordInCursor() {
        int pos = getSelectionStart();
        if (pos == -1) return "";
        Editable editableText = getEditableText();
        int start = pos, end = pos;
        while (start > 0 && Character.isLetterOrDigit(editableText.charAt(start))) start--;
        while (end < editableText.length() && Character.isLetterOrDigit(editableText.charAt(start)))
            end++;
        return editableText.subSequence(start, end);
    }


    private CharSequence indentLine(CharSequence source, int start, int end, Spanned dest,
                                    int dstart, int dend) {
        Log.d(TAG, "indentLine() called with: source = [" + source + "], start = [" + start + "], end = [" + end + "], dest = [" + dest + "], dstart = [" + dstart + "], dend = [" + dend + "]");

        String indent = "";
        int indexStart = dstart - 1;
        int indexEnd;
        boolean dataBefore = false;
        int parenthesesCount = 0;

        for (; indexStart > -1; --indexStart) {
            char c = dest.charAt(indexStart);
            if (c == '\n')
                break;
            if (c != ' ' && c != '\t') {
                if (!dataBefore) {
                    if (c == '{' ||
                            c == '+' ||
                            c == '-' ||
                            c == '*' ||
                            c == '/' ||
                            c == '%' ||
                            c == '^' ||
                            c == '=' ||
                            c == '[')
                        --parenthesesCount;
                    dataBefore = true;
                }
                if (c == '(')
                    --parenthesesCount;
                else if (c == ')')
                    ++parenthesesCount;
            }
        }

        if (indexStart > -1) {
            char charAtCursor = dest.charAt(dstart);
            for (indexEnd = ++indexStart; indexEnd < dend; ++indexEnd) {
                char c = dest.charAt(indexEnd);
                if (charAtCursor != '\n' && c == '/' && indexEnd + 1 < dend && dest.charAt(indexEnd) == c) {
                    indexEnd += 2;
                    break;
                }
                if (c != ' ' && c != '\t')
                    break;
            }
            indent += dest.subSequence(indexStart, indexEnd);
        }
        if (parenthesesCount < 0) {
            indent += TAB_CHARACTER;
        }
        Log.d(TAG, "indentLine: " + dest.charAt(dend) + " " + dest.charAt(dstart));


        //new line in bracket
        if (dest.charAt(dend) == '}' && dstart - 1 >= 0 && dest.charAt(dstart - 1) == '{') {
            int mstart = dstart - 2;
            while (mstart >= 0 && dest.charAt(mstart) != '\n') {
                mstart--;
            }
            String closeIndent = "";
            if (mstart >= 0) {
                mstart++;
                int zstart = mstart;
                while (zstart < dest.length() && dest.charAt(zstart) == ' ') {
                    zstart++;
                }
                closeIndent = dest.toString().substring(mstart, zstart);
            }
            return source + indent + CURSOR + "\n" + closeIndent;
        }
        return source + indent;
    }

}
