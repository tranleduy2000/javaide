package com.duy.testapplication.autocomplete;

import android.support.v4.util.Pair;
import android.widget.EditText;

import com.duy.testapplication.dex.JavaDexClassLoader;
import com.duy.testapplication.model.Description;
import com.duy.testapplication.model.SuggestModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompleteProvider {
    private JavaDexClassLoader mClassLoader;
    private Class preReturnType;

    public AutoCompleteProvider() {

    }

    public ArrayList<Object> getSuggestions(EditText editor, int position, String origPrefix) {
        // text: 'package.Class.me', prefix: 'package.Class', suffix: 'me'
        // text: 'package.Cla', prefix: 'package', suffix: 'Cla'
        // text: 'Cla', prefix: '', suffix: 'Cla'
        // line: 'new Cla', text: 'Cla', prevWord: 'new'
        String line = EditorUtil.getLine(editor, position);
        String preWord = EditorUtil.getPreWord(editor, position);
        String text = EditorUtil.getWord(editor, position).replace("@", "");
        String prefix = null;
        if (text.contains(".")) {
            prefix = text.substring(0, text.lastIndexOf("."));
        }
        String suffix = origPrefix.replace(".", "");
        boolean couldBeClass = suffix.matches(PatternFactory.CLASS_NAME.toString()) || prefix != null;
        boolean instance = false;

        ArrayList<Object> result = null;

        if (couldBeClass) {
            ArrayList<Class> classes = this.mClassLoader.findClass(text);
            if (preWord.equals("new") && classes.size() > 0) {
                for (Class aClass : classes) {
                    Constructor[] constructors = aClass.getConstructors();
                    for (Constructor constructor : constructors) {
                        // TODO: 20-Jul-17
                        result.add(new SuggestModel());
                    }
                }
            } else {
                result = new ArrayList<>();
                result.addAll(classes);
            }
        }


        if (result == null || result.size() == 0) {
            Pair<ArrayList<String>, Boolean> r = EditorUtil.determineClassName(editor, position, text, prefix, suffix, preReturnType);
            if (r != null) {
                ArrayList<String> classes = r.first;
                instance = r.second;
                for (String className : classes) {
                    result = mClassLoader.findClassMember(className, suffix);
                    String superClass = mClassLoader.findSuperClassName(className);
                    while (superClass != null) {
                        ArrayList<Object> classMember = mClassLoader.findClassMember(superClass, suffix);
                        if (classMember != null) {
                            result.addAll(classMember);
                        }
                        superClass = mClassLoader.findSuperClassName(superClass);
                    }
                }
                return result;
            }
        }


        ArrayList<String> duplicateWorkaround = new ArrayList<>();


        return null;
    }

    public String getFormattedReturnType(Member member) {
        // TODO: 20-Jul-17
        return null;
    }

    private String createSnippet(Description desc, String line, String prefix, boolean addMemberClass) {
        boolean useFullClassName = desc.getType().equals("class")
                ? line.matches("^(import)") : prefix.contains(".");
        String text = useFullClassName ? desc.getClassName() : desc.getSimpleName();
        if (desc.getMember() != null) {
            text = (addMemberClass ? "${1:" + text + "}." : "")
                    + createMemberSnippet(desc.getMember(), desc.getType());
        }
        return text;
    }

    private String createMemberSnippet(com.duy.testapplication.model.Member member, com.duy.testapplication.model.Type type) {
        return null;
        // TODO: 20-Jul-17
    }

    public void onDidInsertSuggestion(EditText editText, SuggestModel suggestion) {
        if (suggestion.getType().equals("class")) {
            if (!suggestion.getSnippet().contains(".")) {
                EditorUtil.organizeImports(editText, suggestion.getDescription().getClassName());
            }
        } else if (suggestion.getDescription().getMember() != null) {
            this.preReturnType = suggestion.getDescription().getMember().getReturnType();
        }
        mClassLoader.touch(suggestion.getDescription());
    }
}
