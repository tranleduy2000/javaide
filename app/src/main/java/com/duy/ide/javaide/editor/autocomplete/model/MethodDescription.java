/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.model;

import android.support.annotation.NonNull;
import android.text.Editable;

import com.android.annotations.Nullable;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.IMethod;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription extends JavaSuggestItemImpl implements Member, SuggestItem, IMethod {
    private String mMethodName;
    private int mModifiers;
    private ArrayList<String> mParameterTypes = new ArrayList<>();
    private ArrayList<IClass> mParameters = new ArrayList<>();

    /**
     * Return type of method, null if it is constructor
     */
    @Nullable
    private IClass mReturnType;

    public MethodDescription(String name, IClass returnType,
                             long modifiers, ArrayList<String> parameterTypes) {
        mMethodName = name;
        mReturnType = returnType;
        mModifiers = (int) modifiers;
        mParameterTypes = parameterTypes;
    }

    public MethodDescription(@NonNull String methodName,
                             int modifiers,
                             @NonNull List<IClass> parameters,
                             @Nullable IClass returnType) {
        mMethodName = methodName;
        mModifiers = modifiers;
        mParameters.addAll(parameters);
        mReturnType = returnType;
    }

    public MethodDescription(@NonNull Method method) {
        mMethodName = method.getName();
        mModifiers = method.getModifiers();
        mReturnType = JavaClassManager.getInstance().getClassWrapper(method.getReturnType());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            mParameterTypes.add(parameterType.getName());
        }
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            final int start = editorView.getSelectionStart() - length;

            Editable editable = editorView.getEditableText();
            editable.delete(start, editorView.getSelectionStart());
            String simpleName = JavaUtil.getSimpleName(mMethodName);
            String text = simpleName + "()" + (shouldAddSemicolon(getEditor()) ? ";" : "");
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

    private boolean shouldAddSemicolon(Editor editor) {
        int cursor = editor.getCursor();
        String text = editor.getText();
        while (cursor < editor.length()) {
            char c = text.charAt(cursor);
            if (c == ' ' || c == '\n') {
                cursor++;
                continue;
            }
            if (c == ';') {
                return false;
            } else {
                break;
            }
        }

        if (getMethodReturnType() == null) {
            return true;
        }
        if (void.class.getName().equals(getMethodReturnType().getFullClassName())) {
            return true;
        }
        return false;
    }

    public ArrayList<String> getParameterTypes() {
        return mParameterTypes;
    }

    @Override
    public char getTypeHeader() {
        return 'm'; //method
    }

    @Override
    public String getName() {
        return mMethodName + "(" + paramsToString() + ")";
    }

    private String paramsToString() {
        StringBuilder result = new StringBuilder();
        boolean firstTime = true;
        ArrayList<String> parameterTypes = getParameterTypes();
        for (String parameterType : parameterTypes) {
            if (firstTime) {
                firstTime = false;
            } else {
                result.append(",");

            }
            result.append(JavaUtil.getSimpleName(parameterType));
        }
        return result.toString();
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReturnType() {
        if (mReturnType == null) {
            return "";
        }
        return mReturnType.getSimpleName();
    }

    @Override
    public int getSuggestionPriority() {
        return METHOD_DESC;
    }

    @Override
    public String toString() {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < mParameterTypes.size(); i++) {
            String parameterType = mParameterTypes.get(i);
            if (i == mParameterTypes.size() - 1) {
                params.append(JavaUtil.getSimpleName(parameterType));
                break;
            }
            params.append(JavaUtil.getSimpleName(parameterType)).append(",");
        }
        return mMethodName + "(" + params.toString() + ")";
    }

    @Override
    public int getModifiers() {
        return mModifiers;
    }

    @Override
    public String getMethodName() {
        return mMethodName;
    }

    @Override
    public IClass[] getMethodParameterTypes() {
        // TODO: 13-Jun-18
        return null;
    }

    @Override
    public IClass getMethodReturnType() {
        // TODO: 13-Jun-18
        return mReturnType;
    }
}
