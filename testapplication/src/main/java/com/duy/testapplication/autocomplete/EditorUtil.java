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

    private static final String TAG = "EditorUtil";

    public static String getCurrentClassName(EditText editor) {
        return null;
    }

    @NonNull
    public static String getCurrentClassSimpleName(EditText editor) {
        String className = getCurrentClassName(editor);
        int i = className.indexOf(".");
        if (i == -1) return className;
        else {
            return className.substring(className.lastIndexOf(".") + 1);
        }
    }


    public static ArrayList<String> getPossibleClassName(EditText editText, String simpleName, String prefix) {
        ArrayList<String> classList = new ArrayList<>();
        String importedClassName = getImportedClassName(editText, simpleName);
        if (importedClassName != null) {
            classList.add(importedClassName);
        } else {
            if (!prefix.contains(".")) {
                classList.add(getCurrentPackage(editText) + "." + simpleName);
                classList.add("java.lang." + simpleName);
            } else {
                classList.add(prefix);
            }
        }
        return classList;
    }

    @Nullable
    public static String getLine(EditText editText, int pos) {
        if (pos < 0 || pos > editText.length()) return null;
        int line = LineUtils.getLineFromIndex(pos, editText.getLayout().getLineCount(), editText.getLayout());
        int lineStart = editText.getLayout().getLineStart(line);
        int lineEnd = editText.getLayout().getLineEnd(line);
        return editText.getText().subSequence(lineStart, lineEnd).toString();
    }

    @Nullable
    public static String getWord(EditText editText, int pos, boolean removeParentheses) {
        String line = getLine(editText, pos).toString();
        return getLastWord(line, removeParentheses);
    }

    @Nullable
    public static String getWord(EditText editText, int pos) {
        String line = getLine(editText, pos).toString();
        return getLastWord(line, false);
    }

    @Nullable
    public static String getLastWord(String line, boolean removeParentheses) {
        String result = PatternFactory.lastMatchStr(line, PatternFactory.WORD);
        if (result != null) {
            return removeParentheses ? result.replaceAll(".*\\(", "") : result;
        } else {
            return null;
        }
    }

    @Nullable
    public static String getPreWord(EditText editor, int pos) {
        CharSequence line = getLine(editor, pos);
        String[] split = line.toString().split("[^\\s-]+$");
        return split.length >= 2 ? split[split.length - 2] : null;
    }

    public static String getImportedClassName(EditText editor, String className) {
        return PatternFactory.match(editor.getText(), PatternFactory.makeImport(className));
    }

    public static Pair<ArrayList<String>, Boolean> determineClassName(EditText editor, int pos, String text,
                                                                      @Nullable String prefix, String suffix,
                                                                      Class preReturnType) {
        try {
            ArrayList<String> classNames = null;
            String classSimpleName = null;
            boolean instance = false;
            if (prefix != null) {
                instance = prefix.matches("\\)$");
            }
            if (prefix != null && prefix.equals("this")) {
                classSimpleName = getCurrentClassSimpleName(editor);
                instance = true;
            } else if (prefix != null) {
                String word = getWord(editor, pos);
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
                classNames = getPossibleClassName(editor, classSimpleName, prefix);
            } else {
                classNames = new ArrayList<>();
                classNames.add(preReturnType.getName()); // TODO: 20-Jul-17
                instance = true;
            }

            return new Pair<>(classNames, instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void organizeImports(EditText editor, String importStr) {
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


    public static ArrayList<String> getImports(EditText editor) {
        return PatternFactory.allMatch(editor.getText(), PatternFactory.IMPORT);
    }

    /**
     * Add import statement if import does not already exist.
     *
     * @param editor
     * @param className
     */
    public void importClass(EditText editor, String className) {
        String packageName = JavaUtil.getPackageName(className);
        if (getImportedClassName(editor, className) == null
                && !packageName.equals("java.lang")
                && !packageName.equals(getCurrentPackage(editor))) {
            organizeImports(editor, "import " + className + ";");
        }
    }
}
