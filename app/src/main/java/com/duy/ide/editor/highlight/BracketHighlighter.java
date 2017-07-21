package com.duy.ide.editor.highlight;

import android.text.Editable;
import android.text.Spanned;
import android.widget.EditText;

import com.duy.ide.editor.view.spans.BracketSpan;
import com.duy.ide.themefont.themes.database.CodeTheme;
import com.spartacusrex.spartacuside.helper.Arrays;

/**
 * Created by Duy on 21-Jul-17.
 */

public class BracketHighlighter {
    private static final char[] BRACKET;

    static {
        BRACKET = new char[]{'{', '}', '[', ']', '(', ')'};
        Arrays.sort(BRACKET);
    }

    private EditText editText;
    private CodeTheme codeTheme;

    public BracketHighlighter(EditText editText, CodeTheme codeTheme) {
        this.editText = editText;
        this.codeTheme = codeTheme;
    }

    public void setCodeTheme(CodeTheme codeTheme) {
        this.codeTheme = codeTheme;
    }

    private boolean isRelative(char open, char close) {
        if (open == '(' && close == ')') {
            return true;
        } else if (open == '{' && close == '}') {
            return true;
        } else if (open == '[' && close == ']') {
            return true;
        }
        return false;
    }

    public void onSelectChange(int selStart, int selEnd) {
        try {
            if (selEnd > -1 && selEnd < editText.length() && selEnd - 1 >= 0) {
                Editable text = editText.getText();
                int index = selEnd - 1;
                char bracket = text.charAt(index);
                if (Arrays.binarySearch(BRACKET, bracket) > 0) {
                    if (isOpen(bracket)) {
                        findClose(bracket, index);
                    } else { //close cursor
                        findOpen(bracket, index);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findClose(char open, int selEnd) {
        Editable text = editText.getText();
        int cursor = selEnd + 1;
        int count = 1;
        boolean find = false;
        while (cursor < text.length()) {
            char chatAtCursor = text.charAt(cursor);
            if (isRelative(open, chatAtCursor)) {
                if (!isOpen(chatAtCursor)) {
                    count++;
                } else {
                    count--;
                }
                if (count == 0) {
                    find = true;
                    break;
                }
            }
            cursor--;
        }
        BracketSpan[] spans = text.getSpans(0, text.length(), BracketSpan.class);
        for (BracketSpan span : spans) {
            text.removeSpan(span);
        }
        text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                codeTheme.getTextColor()), selEnd, selEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (find) {
            text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                    codeTheme.getTextColor()), cursor, cursor + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void findOpen(char bracket, int selEnd) {
        Editable text = editText.getText();
        int cursor = selEnd - 1;
        int count = 1;
        boolean find = false;
        while (cursor > 0) {
            char chatAtCursor = text.charAt(cursor);
            if (isRelative(chatAtCursor, bracket)) {
                if (isOpen(chatAtCursor)) {
                    count--;
                } else {
                    count++;
                }
                if (count == 0) {
                    find = true;
                    break;
                }
            }
            cursor--;
        }
        BracketSpan[] spans = text.getSpans(0, text.length(), BracketSpan.class);
        for (BracketSpan span : spans) {
            text.removeSpan(span);
        }
        text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                codeTheme.getTextColor()), selEnd, selEnd + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (find) {
            text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                    codeTheme.getTextColor()), cursor, cursor + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isOpen(char bracket) {
        return bracket == '(' || bracket == '[' || bracket == '{';
    }
}
