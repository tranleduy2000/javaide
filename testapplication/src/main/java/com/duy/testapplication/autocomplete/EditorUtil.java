package com.duy.testapplication.autocomplete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public String getImportedClassName(String className) {
        return match(editor.getText(), PatternFactory.makeImport(className));
    }


    public ArrayList<String> getPossibleClassName(CharSequence text, String simpleName, String prefix) {
        ArrayList<String> classList = new ArrayList<>();
        String importedClassName = getImportedClassName(simpleName);
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

    /**
     * Add import statement if import does not already exist.
     *
     * @param editor
     * @param className
     */
    public void importClass(EditText editor, String className) {
        String packageName = JavaUtil.getPackageName(className);
        if (this.getImportedClassName(className) == null
                && !packageName.equals("java.lang")
                && !packageName.equals(this.getCurrentPackage())) {
            this.organizeImports(editor, "import " + className + ";");
        }
    }

    public void organizeImports(EditText editor, String importStr) {
        ArrayList<String> imports = getImports(editor);
        imports.add(importStr);
        Collections.sort(imports, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        StringBuilder imp = new StringBuilder();
        for (String anImport : imports) {
            imp.append(imp.toString()).append("\n");
        }
        int first = firstMatch(editor, PatternFactory.IMPORT);
        int last = lastMatch(editor, PatternFactory.IMPORT);
        if (first >= 0 && last > first) {
            editor.getText().replace(first, last, "");
            editor.getText().insert(first, imp);
        }
    }

    private int firstMatch(EditText editor, Pattern pattern) {
        Matcher matcher = pattern.matcher(editor.getText());
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    private int lastMatch(EditText editor, Pattern pattern) {
        int last = -1;
        Matcher matcher = pattern.matcher(editor.getText());
        while (matcher.find()) last = matcher.end();
        return last;
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
