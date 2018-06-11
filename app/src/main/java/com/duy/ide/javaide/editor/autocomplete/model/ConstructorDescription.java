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
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Constructor;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ConstructorDescription extends JavaSuggestItemImpl {
    private Constructor constructor;
    private String simpleName;
    private String packageName;

    public ConstructorDescription(Constructor constructor) {
        this.constructor = constructor;
        this.simpleName = JavaUtil.getSimpleName(constructor.getName());
        this.packageName = JavaUtil.getPackageName(constructor.getName());
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            final int start = editorView.getSelectionStart() - length;

            Editable editable = editorView.getEditableText();
            editable.delete(start, editorView.getSelectionStart());

            if (constructor.getParameterTypes().length > 0) {
                editable.insert(start, simpleName + "()");
                editorView.setSelection(start + simpleName.length() + 1);
            } else {
                String text = simpleName + "();";
                editable.insert(start, text);
                editorView.setSelection(start + text.length());
            }

            PackageImporter.importClass(editorView.getEditableText(), constructor.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return simpleName;
    }

    @Override
    public char getTypeHeader() {
        //just debug
        return BuildConfig.DEBUG ? 'z' : 'c'; //class
    }

    @Override
    public String getDescription() {
        return packageName;
    }

    @Override
    public String getReturnType() {
        return "";
    }


    @Override
    public String toString() {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (i == parameterTypes.length - 1) {
                params.append(parameterType.getSimpleName());
                break;
            }
            params.append(parameterType.getSimpleName()).append(",");
        }
        return JavaUtil.getSimpleName(constructor.getName()) + "(" + params.toString() + ")";
    }
}
