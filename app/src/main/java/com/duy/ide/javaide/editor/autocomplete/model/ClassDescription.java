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


import android.text.Editable;

import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.util.JavaUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ClassDescription extends JavaSuggestItemImpl {
    private static final String TAG = "ClassDescription";
    private final String simpleName;
    private final String className;
    private final String packageName;
    private final ArrayList<ConstructorDescription> constructors;
    private final ArrayList<FieldDescription> fields;
    private final ArrayList<MethodDescription> methods;

    private String extend;

    public ClassDescription(String simpleName, String className, String extend, long lastUsed) {
        this.simpleName = simpleName;
        this.className = className;
        this.extend = extend;
        packageName = JavaUtil.getPackageName(className);
        this.lastUsed = 0;

        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();
    }

    @SuppressWarnings("unused")
    public ClassDescription(Class value) {
        this.simpleName = value.getSimpleName();
        this.className = value.getName();
        if (value.getSuperclass() != null) {
            this.extend = value.getSuperclass().getName();
        }
        this.packageName = JavaUtil.getPackageName(className);
        this.lastUsed = 0;


        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();

        for (Constructor constructor : value.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                addConstructor(new ConstructorDescription(constructor));
            }
        }
        for (Field field : value.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                if (!field.getName().equals(field.getDeclaringClass().getName())) {
                    addField(new FieldDescription(field));
                }
            }
        }
        for (Method method : value.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                addMethod(new MethodDescription(method));
            }
        }
    }

    @Override
    public String getName() {
        return simpleName + " (" + packageName + ")";
    }

    @Override
    public final String getDescription() {
        return packageName;
    }

    @Override
    public String getReturnType() {
        return "";
    }

    @Override
    public char getTypeHeader() {
        return 'c';
    }

    @Override
    public int getSuggestionPriority() {
        return JavaSuggestItemImpl.CLASS_DESC;
    }

    public void onSelectThis(IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            final int start = editorView.getSelectionStart() - length;

            Editable editable = editorView.getEditableText();
            editable.delete(start, editorView.getSelectionStart());
            editable.insert(start, getSimpleName());
            PackageImporter.importClass(editable, getClassName());

            if (DLog.DEBUG) DLog.d(TAG, "onSelectThis: import class " + this);
        } catch (Exception e) {
            if (DLog.DEBUG) DLog.e(TAG, "import class " + this, e);
        }
    }


    public final String getSimpleName() {
        return simpleName;
    }

    public final String getClassName() {
        return className;
    }

    public final String getSuperClass() {
        return extend;
    }

    public final String getPackageName() {
        return packageName;
    }

    public ArrayList<ConstructorDescription> getConstructors() {
        return constructors;
    }

    public ArrayList<FieldDescription> getFields() {
        return fields;
    }

    public void addConstructor(ConstructorDescription constructorDescription) {
        this.constructors.add(constructorDescription);
    }

    public void addField(FieldDescription fieldDescription) {
        fields.add(fieldDescription);
    }

    public void addMethod(MethodDescription methodDescription) {
        methods.add(methodDescription);
    }

    public ArrayList<MethodDescription> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return simpleName + "(" + packageName + ")";
    }

    @SuppressWarnings("ConstantConditions")
    public ArrayList<SuggestItem> getMember(String prefix) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        for (ConstructorDescription constructor : constructors) {
            if (!prefix.isEmpty()) {
                if (constructor.getName().startsWith(prefix)) {
                    result.add(constructor);
                }
            }
        }
        for (FieldDescription field : fields) {
            if (prefix.isEmpty() || field.getName().startsWith(prefix)) {
                result.add(field);
            }
        }
        for (MethodDescription method : methods) {
            if (prefix.isEmpty() || method.getName().startsWith(prefix)) {
                result.add(method);
            }
        }
        return result;
    }
}
