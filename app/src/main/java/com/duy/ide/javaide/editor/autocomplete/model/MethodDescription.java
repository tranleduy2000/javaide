package com.duy.ide.javaide.editor.autocomplete.model;

import android.support.annotation.NonNull;

import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.view.CodeEditor;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static com.duy.ide.javaide.editor.autocomplete.model.DescriptionImpl.METHOD_DESC;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription implements Member, SuggestItem {
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


    public String getInsertText() {
        if (getParameterTypes().size() > 0) {
            return getSimpleName() + "(" + CodeEditor.CURSOR + ");";
        } else {
            return getSimpleName() + "();" + CodeEditor.CURSOR;
        }
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
