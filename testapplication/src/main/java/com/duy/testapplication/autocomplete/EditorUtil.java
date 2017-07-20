package com.duy.testapplication.autocomplete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Duy on 20-Jul-17.
 */

public class EditorUtil {
    private EditText editor;

    public EditorUtil(EditText editable) {
        this.editor = editable;
    }

    @Nullable
    public String getCurrentPackage() {
        Matcher matcher = PatternFactory.PACKAGE.matcher(editor.getText());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    @NonNull
    public String getCurrentClassSimpleName() {
        String className = getCurrentClassName();
        int i = className.indexOf(".");
        if (i == -1) return className;
        else {
            return className.substring(className.lastIndexOf(".") + 1);
        }
    }

    public String getCurrentClassName() {
        return null;
    }

    public String isImportedClassName(String className) {
        return match(editor.getText(), PatternFactory.makeImport(className));
    }


    public ArrayList<String> getPossibleClassName(CharSequence text, String simpleName, String prefix) {
        ArrayList<String> classList = new ArrayList<>();
        String importedClassName = isImportedClassName(simpleName);
        if (importedClassName != null) {
            classList.add(importedClassName);
        } else {
            if (!prefix.contains(".")) {
                classList.add(getCurrentPackage() + "." + simpleName);
                classList.add("java.lang." + simpleName);
            } else {
                classList.add(prefix);
            }
        }
        return classList;
    }

    public CharSequence getLine(EditText editText, int pos) {
        int lineStart = editText.getLayout().getLineStart(pos);
        int lineEnd = editText.getLayout().getLineEnd(pos);
        return editText.getText().subSequence(lineStart, lineEnd);
    }

    @NonNull
    public String getWord(EditText editText, int pos, boolean removeParentheses) {
        String line = getLine(editText, pos).toString();
        return getLastWord(line, removeParentheses);
    }

    @NonNull
    public String getLastWord(String line, boolean removeParentheses) {
        String result = lastMatch(line, PatternFactory.WORD);
        if (result != null) {
            return removeParentheses ? result.replaceAll(".*\\(", "") : result;
        } else {
            return line;
        }
    }

    public void importClass(EditText editor, String className) {
        // TODO: 20-Jul-17  import class
    }


    public ArrayList<String> getImports(EditText editor) {
        return allMatch(editor.getText(), PatternFactory.IMPORT);
    }

    @Nullable
    private String lastMatch(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        if (list.size() == 0) return null;
        return list.get(list.size() - 1);
    }

    @Nullable
    private String match(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group();
        else return null;
    }

    private ArrayList<String> allMatch(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }
}
