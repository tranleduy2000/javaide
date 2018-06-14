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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.IField;
import com.duy.ide.javaide.editor.autocomplete.parser.IMethod;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 20-Jul-17.
 */

public class ClassDescription extends JavaSuggestItemImpl implements IClass {
    private static final String TAG = "ClassDescription";
    @NonNull
    private final String mClassName;
    private final ArrayList<ConstructorDescription> mConstructors = new ArrayList<>();
    private final ArrayList<IField> mFields = new ArrayList<>();
    private final ArrayList<IMethod> mMethods = new ArrayList<>();
    private final ArrayList<IClass> mImplements = new ArrayList<>();
    private final int mModifiers;
    private final boolean mPrimitive, mAnnotation, mEum;

    //init later
    @Nullable
    private IClass mSuperClass;

    public ClassDescription(Class c) {
        mClassName = c.getName();
        mModifiers = c.getModifiers();
        mPrimitive = c.isPrimitive();
        mAnnotation = c.isAnnotation();
        mEum = c.isEnum();
    }

    public ClassDescription(String className, int modifiers,
                            boolean isPrimitive,
                            boolean isAnnotation,
                            boolean isEnum) {
        mClassName = className;
        mModifiers = modifiers;
        mPrimitive = isPrimitive;
        mAnnotation = isAnnotation;
        mEum = isEnum;
    }

    @Override
    public String getName() {
        if (getPackageName().isEmpty()) {
            return getSimpleName();
        }
        return getSimpleName() + " (" + getPackageName() + ")";
    }

    @Override
    public final String getDescription() {
        return getPackageName();
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
            PackageImporter.importClass(editable, getFullClassName());

            if (DLog.DEBUG) DLog.d(TAG, "onSelectThis: import class " + this);
        } catch (Exception e) {
            if (DLog.DEBUG) DLog.e(TAG, "import class " + this, e);
        }
    }

    @Override
    public String getSimpleName() {
        return JavaUtil.getSimpleName(mClassName);
    }

    @Override
    public String getFullClassName() {
        return mClassName;
    }

    @Override
    @Nullable
    public IClass getSuperclass() {
        return mSuperClass;
    }

    public String getPackageName() {
        return JavaUtil.getPackageName(mClassName);
    }

    public ArrayList<ConstructorDescription> getConstructors() {
        return mConstructors;
    }

    public ArrayList<IField> getFields() {
        return mFields;
    }

    public void addConstructor(ConstructorDescription constructorDescription) {
        this.mConstructors.add(constructorDescription);
    }

    public void addField(IField fieldDescription) {
        mFields.add(fieldDescription);
    }

    public void addMethod(IMethod methodDescription) {
        mMethods.add(methodDescription);
    }

    public List<IMethod> getMethods() {
        return mMethods;
    }

    @Override
    public String toString() {
        return mClassName;
    }

    @SuppressWarnings("ConstantConditions")
    public ArrayList<SuggestItem> getMember(String prefix) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        for (ConstructorDescription constructor : mConstructors) {
            if (!prefix.isEmpty()) {
                if (constructor.getName().startsWith(prefix)) {
                    result.add(constructor);
                }
            }
        }
        for (IField field : mFields) {
            if (prefix.isEmpty() || field.getFieldName().startsWith(prefix)) {
                result.add(field);
            }
        }
        getMethods(result, prefix);
        return result;
    }

    public void getMethods(ArrayList<SuggestItem> result, String prefix) {
        for (IMethod method : mMethods) {
            if (prefix.isEmpty() || method.getMethodName().startsWith(prefix)) {
                result.add(method);
            }
        }
    }


    @Override
    public int getModifiers() {
        return mModifiers;
    }

    @Override
    public boolean isInterface() {
        return false;
    }


    public boolean isEnum() {
        return mEum;
    }

    @Override
    public boolean isPrimitive() {
        return mPrimitive;
    }

    @Override
    public boolean isAnnotation() {
        return mAnnotation;
    }

    @Override
    public IMethod getMethod(String methodName, IClass[] argsType) {
        // TODO: 13-Jun-18 support types
        for (IMethod method : mMethods) {
            if (method.getMethodName().equals(methodName)) {
                return method;
            }
        }

        if (getSuperclass() != null) {
            return getSuperclass().getMethod(methodName, argsType);
        }
        return null;
    }

    @Override
    public IField getField(String name) {
        for (IField field : mFields) {
            if (field.getFieldName().equals(name)) {
                return field;
            }
        }
        if (getSuperclass() != null) {
            return getSuperclass().getField(name);
        }
        return null;
    }

    /**
     * Lazy init
     */
    public void initMembers(Class c) {
        mImplements.clear();
        Class[] interfaces = c.getInterfaces();
        for (Class anInterface : interfaces) {
            mImplements.add(JavaClassManager.getInstance().getClassWrapper(anInterface));
        }

        if (c.getSuperclass() != null) {
            mSuperClass = JavaClassManager.getInstance().getClassWrapper(c.getSuperclass());
        } else {
            if (!getFullClassName().equals(Object.class.getName())
                    && mSuperClass == null) {
                mSuperClass = JavaClassManager.getInstance().getParsedClass(Object.class.getName());
            }
        }

        for (Constructor constructor : c.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                addConstructor(new ConstructorDescription(constructor));
            }
        }

        for (Field field : c.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers())) {
                if (!field.getName().equals(field.getDeclaringClass().getName())) {
                    addField(new FieldDescription(field));
                }
            }
        }

        for (Method method : c.getMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                addMethod(new MethodDescription(method));
            }
        }
    }

    public void setSuperclass(IClass superclass) {
        this.mSuperClass = superclass;
    }
}
