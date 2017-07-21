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
        if (selEnd > -1) {
            Editable text = editText.getText();
            char bracket = text.charAt(selEnd);
            if (Arrays.binarySearch(BRACKET, bracket) > 0) {
                if (isOpen(bracket)) {
                    findClose(bracket, selEnd);
                } else { //cloes cursor
                    findOpen(bracket, selEnd);
                }
            }
        }
    }

    private void findClose(char close, int selEnd) {
        Editable text = editText.getText();
        int cursor = selEnd + 1;
        int count = 1;
        boolean find = false;
        while (cursor < text.length()) {
            char chatAtCursor = text.charAt(cursor);
            if (isRelative(chatAtCursor, close)) {
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
                codeTheme.getTextColor()), selEnd, selEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (find) {
            text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                    codeTheme.getTextColor()), cursor, cursor, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void findOpen(char bracket, int selEnd) {
        Editable text = editText.getText();
        int cursor = selEnd - 1;
        int count = 1;
        boolean find = false;
        while (cursor > 0) {
            char chatAtCursor = text.charAt(cursor);
            if (isRelative(bracket, chatAtCursor)) {
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
                codeTheme.getTextColor()), selEnd, selEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (find) {
            text.setSpan(new BracketSpan(codeTheme.getBracketColor(),
                    codeTheme.getTextColor()), cursor, cursor, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isOpen(char bracket) {
        return bracket == '(' || bracket == '[' || bracket == '{';
    }
}
