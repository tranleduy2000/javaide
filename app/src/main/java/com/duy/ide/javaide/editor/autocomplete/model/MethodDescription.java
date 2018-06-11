package com.duy.ide.javaide.editor.autocomplete.model;

import android.support.annotation.NonNull;
import android.text.Editable;

import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription extends JavaSuggestItemImpl implements Member, SuggestItem {
    private String name, type;
    private int modifiers;
    private String simpleName;
    private ArrayList<String> parameterTypes = new ArrayList<>();

    public MethodDescription(String name, String type, long modifiers, ArrayList<String> parameterTypes) {
        this.name = name;
        this.type = type;
        this.modifiers = (int) modifiers;
        this.parameterTypes = parameterTypes;
    }

    public MethodDescription(@NonNull Method method) {
        this.name = method.getName();
        this.modifiers = method.getModifiers();
        this.type = method.getReturnType().getName();
        this.simpleName = JavaUtil.getSimpleName(name);
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            this.parameterTypes.add(parameterType.getName());
        }
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            final int start = editorView.getSelectionStart() - length;

            Editable editable = editorView.getEditableText();
            editable.delete(start, editorView.getSelectionStart());
            String text = simpleName + "();";
            if (getParameterTypes().size() > 0) {
                //Should end method?
                editable.insert(start, text);
                editorView.setSelection(start + simpleName.length() + 1/*(*/);
            } else {
                editable.insert(start, text);
                editorView.setSelection(start + text.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public ArrayList<String> getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public char getTypeHeader() {
        return 'm'; //method
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReturnType() {
        return type;
    }


    @Override
    public int getSuggestionPriority() {
        return METHOD_DESC;
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameterTypes.size(); i++) {
            String parameterType = parameterTypes.get(i);
            if (i == parameterTypes.size() - 1) {
                params.append(JavaUtil.getSimpleName(parameterType));
                break;
            }
            params.append(JavaUtil.getSimpleName(parameterType)).append(",");
        }
        return name + "(" + params.toString() + ")";
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    public String getSimpleName() {
        return simpleName;
    }
}
