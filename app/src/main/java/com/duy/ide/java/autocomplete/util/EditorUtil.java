package com.duy.ide.java.autocomplete.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.duy.ide.java.autocomplete.autocomplete.PackageImporter;
import com.duy.ide.java.autocomplete.autocomplete.PatternFactory;

import java.util.ArrayList;
import java.util.regex.Matcher;

/**
 * Created by Duy on 20-Jul-17.
 */

public class EditorUtil {

    private static final String TAG = "EditorUtil";

    public EditorUtil() {
    }

    @Nullable
    public static String getCurrentPackage(EditText editText) {
        Matcher matcher = PatternFactory.PACKAGE.matcher(editText.getText());
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    public static String getCurrentClassName(String editor) {
        // TODO: 21-Jul-17
        return "com.duy.Main";
    }

    @NonNull
    public static String getCurrentClassSimpleName(String editor) {
        String className = getCurrentClassName(editor);
        int i = className.indexOf(".");
        if (i == -1) return className;
        else {
            return className.substring(className.lastIndexOf(".") + 1);
        }
    }


    public static ArrayList<String> getPossibleClassName(String source, String simpleName, String prefix) {
        Log.d(TAG, "getPossibleClassName() called with:  simpleName = [" + simpleName + "], prefix = [" + prefix + "]");

        ArrayList<String> classList = new ArrayList<>();
        String importedClassName = PackageImporter.getImportedClassName(source, simpleName);
        Log.d(TAG, "getPossibleClassName importedClassName = " + importedClassName);
        if (importedClassName != null) {
            classList.add(importedClassName);
        } else {
            if (!prefix.contains(".")) {
                classList.add(getCurrentClassName(source)); //current member
                if (simpleName != null && !simpleName.isEmpty()) {
                    classList.add("java.lang." + simpleName); //default java.lang package
                } else if (!prefix.isEmpty()) {
                    classList.add("java.lang." + prefix);
                }
            } else {
                classList.add(prefix);
            }
        }
        Log.d(TAG, "getPossibleClassName() returned: " + classList);
        return classList;
    }

    public static String getLine(EditText editText, int pos) {
        if (pos < 0 || pos > editText.length()) return "";
        int line = LineUtils.getLineFromIndex(pos, editText.getLayout().getLineCount(), editText.getLayout());

        int lineStart = editText.getLayout().getLineStart(line);
        int lineEnd = editText.getLayout().getLineEnd(line);
        return editText.getText().subSequence(lineStart, lineEnd).toString();
    }

    @Nullable
    public static String getWord(EditText editText, int pos, boolean removeParentheses) {
        String line = getLine(editText, pos).trim();
        return getLastWord(line, removeParentheses);
    }

    @NonNull
    public static String getWord(EditText editText, int pos) {
        String line = getLine(editText, pos).trim();
        return getLastWord(line, false);
    }

    @NonNull
    public static String getLastWord(String line, boolean removeParentheses) {
        String result = PatternFactory.lastMatchStr(line, PatternFactory.WORD);
        if (result != null) {
            return removeParentheses ? result.replaceAll(".*\\(", "") : result;
        } else {
            return "";
        }
    }

    @Nullable
    public static String getPreWord(EditText editor, int pos) {
        String line = getLine(editor, pos);
        String[] split = line.split(PatternFactory.SPLIT_NON_WORD_STR);
        return split.length >= 2 ? split[split.length - 2] : null;
    }

    @NonNull
    public static String getLineBeforeCursor(EditText editText, int pos) {
        if (pos < 0 || pos > editText.length()) return "";
        int line = LineUtils.getLineFromIndex(pos, editText.getLayout().getLineCount(), editText.getLayout());
        int lineStart = editText.getLayout().getLineStart(line);
        return editText.getText().subSequence(lineStart, pos).toString();
    }
}
