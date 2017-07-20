package com.duy.testapplication.autocomplete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.Layout;
import android.util.Log;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.duy.testapplication.autocomplete.PatternFactory.firstMatch;
import static com.duy.testapplication.autocomplete.PatternFactory.lastMatchStr;

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

    private static final String TAG = "EditorUtil";

    public String getCurrentClassName() {
        return null;
    }

    @NonNull
    public String getCurrentClassSimpleName(EditText editor) {
        String className = getCurrentClassName();
        int i = className.indexOf(".");
        if (i == -1) return className;
        else {
            return className.substring(className.lastIndexOf(".") + 1);
        }
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

    @Nullable
    public CharSequence getLine(EditText editText, int pos) {
        if (pos < 0 || pos > editText.length()) return null;
        int lineStart = editText.getLayout().getLineStart(pos);
        int lineEnd = editText.getLayout().getLineEnd(pos);
        return editText.getText().subSequence(lineStart, lineEnd);
    }

    @Nullable
    public String getWord(EditText editText, int pos, boolean removeParentheses) {
        String line = getLine(editText, pos).toString();
        return getLastWord(line, removeParentheses);
    }

    @Nullable
    public String getWord(EditText editText, int pos) {
        String line = getLine(editText, pos).toString();
        return getLastWord(line, false);
    }

    @Nullable
    public String getLastWord(String line, boolean removeParentheses) {
        String result = PatternFactory.lastMatchStr(line, PatternFactory.WORD);
        if (result != null) {
            return removeParentheses ? result.replaceAll(".*\\(", "") : result;
        } else {
            return null;
        }
    }

    @Nullable
    public String getPreWord(EditText editor, int pos) {
        CharSequence line = getLine(editor, pos);
        String[] split = line.toString().split("[^\\s-]+$");
        return split.length >= 2 ? split[split.length - 2] : null;
    }

    public String getImportedClassName(String className) {
        return PatternFactory.match(editor.getText(), PatternFactory.makeImport(className));
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
        Log.d(TAG, "organizeImports() called with: editor = [" + editor + "], importStr = [" + importStr + "]");

        ArrayList<String> imports = getImports(editor);
        imports.add(importStr);
        Collections.sort(imports, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareTo(t1);
            }
        });
        StringBuilder imp = new StringBuilder();
        for (String s : imports) {
            imp.append(s).append("\n");
        }
        int first = firstMatch(editor, PatternFactory.IMPORT);
        int last = PatternFactory.lastMatch(editor, PatternFactory.IMPORT);
        if (first >= 0 && last > first) {
            editor.getText().replace(first, last, "");
            editor.getText().insert(first, imp);
        }
    }


    public ArrayList<String> getImports(EditText editor) {
        return PatternFactory.allMatch(editor.getText(), PatternFactory.IMPORT);
    }


    public Pair<ArrayList<String>, Boolean> determineClassName(EditText editor, int pos, String text,
                                                               @Nullable String prefix, String suffix,
                                                               String preReturnType) {
        try {
            ArrayList<String> classNames = null;
            String classSimpleName = null;
            boolean instance = false;
            if (prefix != null) {
                instance = prefix.matches("\\)$");
            }
            if (prefix != null && prefix.equals("this")) {
                classSimpleName = this.getCurrentClassSimpleName(editor);
                instance = true;
            } else if (prefix != null) {
                String word = this.getWord(editor, pos);
                if (word.contains("((")) {
                    classSimpleName = Pattern.compile("[^)]*").matcher(prefix).group(); // TODO: 20-Jul-17  exception
                } else {
                    classSimpleName = prefix;
                }
            }

            if (!JavaUtil.isValidClassName(classSimpleName)
                    && !prefix.matches("\\.\\)")) {
                Layout layout = editor.getLayout();
                int start = Math.max(0, pos - 2500);
                int end = pos;
                CharSequence range = editor.getText().subSequence(start, end);

                //BigInteger num = new BigInteger(); -> BigInteger num =
                classSimpleName = lastMatchStr(range, PatternFactory.makeInstance(prefix));
                //BigInteger num =  -> BigInteger
                classSimpleName = classSimpleName.replaceAll("\\s?" + prefix + "[,;=\\s)]]", "");
                //generic ArrayList<String> -> ArrayList
                classSimpleName = classSimpleName.replaceAll("<.*>", "");

                instance = true;
            } else {

            }
            if (JavaUtil.isValidClassName(classSimpleName)) {
                classNames = getPossibleClassName(editor.getText(), classSimpleName, prefix);
            } else {
                classNames = new ArrayList<>();
                classNames.add(preReturnType);
                instance = true;
            }

            return new Pair<>(classNames, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
