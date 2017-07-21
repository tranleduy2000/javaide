package com.duy.ide.autocomplete.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.EditText;

import com.duy.ide.autocomplete.autocomplete.PatternFactory;
import com.duy.ide.autocomplete.util.JavaUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.duy.ide.autocomplete.autocomplete.PatternFactory.lastMatchStr;
import static com.duy.ide.autocomplete.util.EditorUtil.getCurrentClassSimpleName;
import static com.duy.ide.autocomplete.util.EditorUtil.getPossibleClassName;
import static com.duy.ide.autocomplete.util.EditorUtil.getWord;

/**
 * Created by Duy on 21-Jul-17.
 */

public class JavaClassManager {
    private static final String TAG = "JavaClassManager";

    public static Pair<ArrayList<String>, Boolean> determineClassName(EditText editor, int pos, String text,
                                                                      @NonNull String prefix, String suffix,
                                                                      @Nullable Class preReturnType) {
        Log.d(TAG, "determineClassName() called with: text = [" + text + "], prefix = [" + prefix + "], suffix = [" + suffix + "], preReturnType = [" + preReturnType + "]");

        try {
            ArrayList<String> classNames = null;
                  String className;
            boolean isInstance;
            isInstance = prefix.matches("\\)$");

            if (prefix.isEmpty() || prefix.equals("this")) {
                className = getCurrentClassSimpleName(editor);
                isInstance = true;
            } else {
                String word = getWord(editor, pos);
                if (word.contains("((")) {
                    className = Pattern.compile("[^)]*").matcher(prefix).group(); // TODO: 20-Jul-17  exception
                } else {
                    className = prefix;
                }
            }
            Log.d(TAG, "determineClassName instance = " + isInstance);

            if (JavaUtil.isValidClassName(className) && text.contains(".")) {
                int start = Math.max(0, pos - 2500);
                CharSequence range = editor.getText().subSequence(start, pos);

                //BigInteger num = new BigInteger(); -> BigInteger num =
                className = lastMatchStr(range, PatternFactory.makeInstance(prefix));

                if (className != null) {
                    //BigInteger num =  -> BigInteger
                    className = className.replaceAll("\\s?" + prefix + "\\s?[,;=)]", "");
                    //generic ArrayList<String> -> ArrayList
                    className = className.replaceAll("<.*>", "");
                    isInstance = true;
                }
            }
            Log.d(TAG, "determineClassName className = " + className);
            if (JavaUtil.isValidClassName(className)) {
                classNames = new ArrayList<>();
                classNames.addAll(getPossibleClassName(editor, className, prefix));
                if (preReturnType != null) {
                    classNames.add(preReturnType.getName()); // TODO: 20-Jul-17 quickhack
                }
                isInstance = true;
            }

            Log.d(TAG, "determineClassName() returned: " + classNames);
            return new Pair<>(classNames, isInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
