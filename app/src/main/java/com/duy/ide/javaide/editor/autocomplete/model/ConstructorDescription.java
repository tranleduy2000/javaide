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

import com.duy.ide.BuildConfig;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ConstructorDescription extends JavaSuggestItemImpl {
    private ArrayList<IClass> mParameterTypes = new ArrayList<>();
    private String mConstructorName;

    public ConstructorDescription(Constructor constructor) {
        mConstructorName = constructor.getName();
        Class[] parameterTypes = constructor.getParameterTypes();
        for (Class parameterType : parameterTypes) {
            IClass type = JavaClassManager.getInstance().getClassWrapper(parameterType);
            mParameterTypes.add(type);
        }
    }

    public ConstructorDescription(String name, List<IClass> paramTypes) {
        mConstructorName = name;
        mParameterTypes.addAll(paramTypes);
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            int cursor = getEditor().getCursor();
            final int start = cursor - length;

            Editable editable = editorView.getEditableText();
            if (getParameterTypes().size() > 0) {
                editable.replace(start, cursor, getSimpleName() + "()");
                editorView.setSelection(start + getSimpleName().length() + 1 /*between two parentheses*/);
            } else {
                String text = getSimpleName() + "();";
                editable.replace(start, cursor, text);
                editorView.setSelection(start + text.length());
            }

            PackageImporter.importClass(editorView.getEditableText(), mConstructorName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return getSimpleName() + "(" + paramsToString(getParameterTypes()) + ")";
    }

    @Override
    public char getTypeHeader() {
        //just debug
        return BuildConfig.DEBUG ? 'z' : 'c'; //class
    }

    @Override
    public String getDescription() {
        return "Class constructor";
    }

    @Override
    public String getReturnType() {
        return "";
    }


    @Override
    public String toString() {
        return getSimpleName() + "()";
    }

    private String paramsToString(@NonNull ArrayList<IClass> parameterTypes) {
        StringBuilder result = new StringBuilder();
        boolean firstTime = true;
        for (IClass parameterType : parameterTypes) {
            if (firstTime) {
                firstTime = false;
            } else {
                result.append(",");
            }
            result.append(parameterType.getSimpleName());
        }
        return result.toString();
    }

    public ArrayList<IClass> getParameterTypes() {
        return mParameterTypes;
    }

    private String getSimpleName() {
        return JavaUtil.getSimpleName(mConstructorName);
    }
}
