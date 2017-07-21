package com.duy.autocomplete.autocomplete;

import android.content.Context;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.widget.EditText;

import com.duy.autocomplete.dex.JavaDexClassLoader;
import com.duy.autocomplete.model.ClassConstructor;
import com.duy.autocomplete.model.ClassDescription;
import com.duy.autocomplete.model.Description;
import com.duy.autocomplete.model.Member;
import com.duy.autocomplete.util.EditorUtil;
import com.duy.autocomplete.util.ImportUtil;

import java.io.File;
import java.util.ArrayList;

import static com.duy.autocomplete.dex.JavaClassManager.determineClassName;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteProvider {
    private JavaDexClassLoader mClassLoader;
    private Class preReturnType;

    public AutoCompleteProvider(Context context) {
        File classpath = new File(Environment.getExternalStorageDirectory(), "android.jar");
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(classpath, outDir);
    }

    private static final String TAG = "AutoCompleteProvider";

    public void load() {
        mClassLoader.loadAllClasses(true);
    }


    public ArrayList<? extends Description> getSuggestions(EditText editor, int position) {
        // text: 'package.Class.me', prefix: 'package.Class', suffix: 'me'
        // text: 'package.Cla', prefix: 'package', suffix: 'Cla'
        // text: 'Cla', prefix: '', suffix: 'Cla'
        // line: 'new Cla', text: 'Cla', prevWord: 'new'
        String line = EditorUtil.getLine(editor, position);
        Log.d(TAG, "getSuggestions line = " + line);
        String preWord = EditorUtil.getPreWord(editor, position);
        Log.d(TAG, "getSuggestions preWord = " + preWord);
        String current = EditorUtil.getWord(editor, position).replace("@", "");
        Log.d(TAG, "getSuggestions text = " + current);
        String prefix = "";
        String suffix = "";
        if (current.contains(".")) {
            prefix = current.substring(0, current.lastIndexOf("."));
            suffix = current.substring(current.lastIndexOf(".") + 1);
        } else {
            suffix = current;
        }
        Log.d(TAG, "getSuggestions prefix = " + prefix + " ; suffix = " + suffix);

        boolean couldBeClass = suffix.matches(PatternFactory.IDENTIFIER.toString());
        Log.d(TAG, "getSuggestions couldBeClass = " + couldBeClass);

        boolean instance = false;

        ArrayList<Description> result = null;

        if (couldBeClass) {
            ArrayList<ClassDescription> classes = this.mClassLoader.findClass(current);
            if (preWord != null && preWord.equals("new")) {
                result = new ArrayList<>();
                for (ClassDescription description : classes) {
                    ArrayList<ClassConstructor> constructors = description.getConstructors();
                    for (ClassConstructor constructor : constructors) {
                        result.add(constructor);
                    }
                }
            } else {
                result = new ArrayList<>();
                for (ClassDescription aClass : classes) {
                    result.add(aClass);
                }
            }
        }


        if (result == null || result.size() == 0) {
            Pair<ArrayList<String>, Boolean> r
                    = determineClassName(editor, position, current, prefix, suffix, preReturnType);
            if (r != null) {
                ArrayList<String> classes = r.first;
                instance = r.second;
                for (String className : classes) {
                    result = mClassLoader.findClassMember(className, suffix);
                    String superClass = mClassLoader.findSuperClassName(className);
                    while (superClass != null) {
                        ArrayList<Description> classMember = mClassLoader.findClassMember(superClass, suffix);
                        if (classMember != null) {
                            for (Description description : classMember) {
                                result.add(description);
                            }
                        }
                        superClass = mClassLoader.findSuperClassName(superClass);
                    }
                }
            }
        }
        return result;
    }

    public String getFormattedReturnType(Member member) {
        // TODO: 20-Jul-17
        return null;
    }

    private String createSnippet(Description desc, String line, String prefix, boolean addMemberClass) {
//        boolean useFullClassName = desc.getType().equals("class")
//                ? line.matches("^(import)") : prefix.contains(".");
//        String text = useFullClassName ? desc.getClassName() : desc.getSimpleName();
//        if (desc.getMember() != null) {
//            text = (addMemberClass ? "${1:" + text + "}." : "")
//                    + createMemberSnippet(desc.getMember(), desc.getType());
//        }
//        return text;
        return "";
    }

    private String createMemberSnippet(Description member, com.duy.autocomplete.model.Type type) {
        return null;
        // TODO: 20-Jul-17
    }

    public void onDidInsertSuggestion(EditText editText, Description suggestion) {
        if (suggestion instanceof ClassDescription) {
            if (!suggestion.getSnippet().contains(".")) {
                ImportUtil.organizeImports(editText, ((ClassDescription) suggestion).getClassName());
            }
        } else if (suggestion instanceof Member) {
            this.preReturnType = suggestion.getType();
        }
        mClassLoader.touchClass(suggestion.getDescription());
    }
}
