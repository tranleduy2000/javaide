package com.duy.autocomplete.autocomplete;

import android.support.annotation.Nullable;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Duy on 20-Jul-17.
 */

public class PatternFactory {
    public static final Pattern PACKAGE = Pattern.compile("package\\s+[^;]*;");
    public static final Pattern IMPORT = Pattern.compile("import\\s+[^;]*\\s?;");
    public static final Pattern WORD = Pattern.compile("[^\\s-]+$");

    public static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9]*");
    public static final Pattern ANNOTATION = Pattern.compile("@[A-Za-z][A-Za-z0-9]*");
    public static final Pattern BRACKET = Pattern.compile("\\[(.*?)\\]");
    public static final Pattern MODIFIERS = Pattern.compile("\\b(public|protected|private|abstract|static|final|strictfp)\\b");

    public static final Pattern SPLIT_NON_WORD = Pattern.compile("\\W+");
    public static final String SPLIT_NON_WORD_STR = "\\W+";

    public static final String[] PRIMITIVE_TYPE = new String[]{"boolean", "byte", "char", "int",
            "short", "long", "float", "double"};
    public static final String[] KEYWORD_MODIFIERS = new String[]{"public", "private", "protected",
            "static", "final", "synchronized", "volatile", "transient", "native", "strictfp"};
    public static final String[] KEYWORD_TYPE = new String[]{"class", "interface", "enum"};
    public static final String[] KEYWORD;

    static {
        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, PRIMITIVE_TYPE);
        Collections.addAll(list, KEYWORD_MODIFIERS);
        Collections.addAll(list, KEYWORD_TYPE);
        KEYWORD = (String[]) list.toArray();
    }
    public static Pattern makeImport(String className) {
        return Pattern.compile("(import\\s+)(.*" + className + ")(\\s?;)");
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
                prefix + "\\s?[,;=)]");
    }
}
