package com.duy.autocomplete.util;

import android.util.Log;
import android.widget.EditText;

import com.duy.autocomplete.autocomplete.PatternFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.duy.autocomplete.autocomplete.PatternFactory.firstMatch;

/**
 * Created by Duy on 21-Jul-17.
 */

public class ImportUtil {
    private static final String TAG = "ImportUtil";

    /**
     * Add import statement if import does not already exist.
     *
     * @param editor
     * @param className
     */
    public static void importClass(EditText editor, String className) {
        String packageName = JavaUtil.getPackageName(className);
        if (getImportedClassName(editor, className) == null
                && !packageName.equals("java.lang")
                && !packageName.equals(EditorUtil.getCurrentPackage(editor))) {
            organizeImports(editor, "import " + className + ";");
        }
    }

    public static String getImportedClassName(EditText editor, String className) {
        Pattern pattern = PatternFactory.makeImport(className);
        Matcher matcher = pattern.matcher(editor.getText());
        if (matcher.find()) {
            return matcher.group(2);
        }
        return PatternFactory.match(editor.getText(), pattern);
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

}
