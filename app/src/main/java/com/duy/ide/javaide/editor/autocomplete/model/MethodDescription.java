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
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.dex.IClass;
import com.duy.ide.javaide.editor.autocomplete.dex.IMethod;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class MethodDescription extends JavaSuggestItemImpl implements Member, SuggestItem, IMethod {
    private String mName;
    private int mModifiers;
    private ArrayList<String> mParameterTypes = new ArrayList<>();
    @Nullable
    private IClass mReturnType;

    public MethodDescription(String name, IClass returnType,
                             long modifiers, ArrayList<String> parameterTypes) {
        this.mName = name;
        this.mReturnType = returnType;
        this.mModifiers = (int) modifiers;
        this.mParameterTypes = parameterTypes;
    }

    public MethodDescription(@NonNull Method method) {
        this.mName = method.getName();
        this.mModifiers = method.getModifiers();
        this.mReturnType = new ClassDescription(method.getReturnType());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes) {
            this.mParameterTypes.add(parameterType.getName());
        }
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            final int start = editorView.getSelectionStart() - length;

            Editable editable = editorView.getEditableText();
            editable.delete(start, editorView.getSelectionStart());
            String text = JavaUtil.getSimpleName(mName) + "();";
            if (getParameterTypes().size() > 0) {
                //Should end method?
                editable.insert(start, text);
                editorView.setSelection(start + text.length() - 2 /*(*/);
            } else {
                editable.insert(start, text);
                editorView.setSelection(start + text.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


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
        return mName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReturnType() {
        return mReturnType.getFullClassName();
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
        return mName + "(" + params.toString() + ")";
    }

    @Override
    public int getModifiers() {
        return mModifiers;
    }

    @Override
    public String getMethodName() {
        return mName;
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
