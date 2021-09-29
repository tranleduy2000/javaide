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

import com.android.annotations.Nullable;
import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.IField;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Duy on 20-Jul-17.
 */

public class FieldDescription extends JavaSuggestItemImpl implements Member, IField {
    @NonNull
    private String mName;
    @NonNull
    private IClass mType;
    private int mModifiers;
    @Nullable
    private String mValue;

    public FieldDescription(int modifiers, IClass type, @NonNull String name, String initValue) {
        mName = name;
        mType = type;
        mModifiers = modifiers;
        mValue = initValue;
    }

    public FieldDescription(Field field) {
        mName = field.getName();
        mType = JavaClassManager.getInstance().getClassWrapper(field.getType());
        mModifiers = field.getModifiers();

        if (Modifier.isStatic(mModifiers)) {
            try {
                boolean primitive = field.getType().isPrimitive();
                Object o = field.get(null);
                if (primitive) {
                    mValue = o.toString();
                } else {
                    mValue = o.getClass().getSimpleName();
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        insertImpl(editorView, mName);
    }


    @Override
    public char getTypeHeader() {
        return 'f'; //field
    }

    @Override
    public String getName() {
        if (mValue == null) {
            return mName;
        } else {
            return mName + "(" + mValue + ")";
        }
    }

    @Override
    public String getDescription() {
        return mValue;
    }

    @Override
    public String getReturnType() {
        if (mType == null) {
            return "";
        }
        return mType.getSimpleName();
    }

    @Override
    public int getSuggestionPriority() {
        return JavaSuggestItemImpl.FIELD_DESC;
    }

    @Override
    public String toString() {
        return mName;
    }


    @Override
    public int getModifiers() {
        return mModifiers;
    }

    @Override
    public String getFieldName() {
        return mName;
    }

    @Override
    public IClass getFieldType() {
        return mType;
    }

    @Override
    public int getFieldModifiers() {
        return mModifiers;
    }

    @Override
    public Object getFieldValue() {
        return mValue;
    }
}
