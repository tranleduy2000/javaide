package com.duy.testapplication.autocomplete;

import android.support.annotation.Nullable;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Duy on 20-Jul-17.
 */

public class PatternFactory {
    public static final Pattern PACKAGE = Pattern.compile("package\\s+[^;]*;");
    public static final Pattern IMPORT = Pattern.compile("import\\s+[^;]*\\s?;");
    public static final Pattern WORD = Pattern.compile("[^\\s-]+$");
    public static final Pattern CLASS_NAME = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
    public static final Pattern SPLIT_NON_WORD = Pattern.compile("\\W+");
    public static final String SPLIT_NON_WORD_STR = "\\W+";

    public static Pattern makeImport(String className) {
        return Pattern.compile("import\\s+(.*" + className + ")\\s?;");
    }


    @Nullable
    public static String lastMatchStr(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        if (list.size() == 0) return null;
        return list.get(list.size() - 1);
    }

    @Nullable
    public static String match(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) return matcher.group();
        else return null;
    }

    public static ArrayList<String> allMatch(CharSequence text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        ArrayList<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }


    public static int lastMatch(EditText editor, Pattern pattern) {
        int last = -1;
        Matcher matcher = pattern.matcher(editor.getText());
        while (matcher.find()) last = matcher.end();
        return last;
    }

    public static int firstMatch(EditText editor, Pattern pattern) {
        Matcher matcher = pattern.matcher(editor.getText());
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    public static Pattern makeInstance(String prefix) {
        return Pattern.compile("([A-Z][a-zA-Z0-9_]*)(<[A-Z][a-zA-Z0-9_<>, ]*>)?\\s?" +
                prefix + "[,;=\\s)]");
    }
}
