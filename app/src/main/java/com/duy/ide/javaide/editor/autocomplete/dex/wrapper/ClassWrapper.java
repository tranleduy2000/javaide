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

package com.duy.ide.javaide.editor.autocomplete.dex.wrapper;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.ide.javaide.editor.autocomplete.dex.IClass;
import com.duy.ide.javaide.editor.autocomplete.dex.IField;
import com.duy.ide.javaide.editor.autocomplete.dex.IMethod;

import java.lang.reflect.Method;

public class ClassWrapper implements IClass {
    private Class<?> c;

    public ClassWrapper(@NonNull Class<?> c) {
        this.c = c;
    }

    @Override
    public int getModifiers() {
        return c.getModifiers();
    }

    @Override
    public String getFullClassName() {
        return c.getName();
    }

    @Override
    public String getSimpleName() {
        return c.getSimpleName();
    }

    @Override
    public boolean isInterface() {
        return c.isInterface();
    }

    @Override
    public boolean isEnum() {
        return c.isEnum();
    }

    @Nullable
    public IMethod getMethod(String name, IClass[] types) {
//        Class[] classes = TypeConverter.toClasses(types);
//        Method method = c.getMethod(name, classes);
//        return new MethodWrapper(method);
        // TODO: 13-Jun-18 support type
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(name)) {
                return new MethodWrapper(method);
            }
        }
        return null;
    }

    @Override
    public IField getField(String name) {
        try {
            return new FieldWrapper(c.getField(name));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }


}
