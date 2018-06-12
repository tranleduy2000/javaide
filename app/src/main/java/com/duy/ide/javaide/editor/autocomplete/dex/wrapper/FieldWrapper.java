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

import com.duy.ide.javaide.editor.autocomplete.dex.IClass;
import com.duy.ide.javaide.editor.autocomplete.dex.IField;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldWrapper implements IField {
    private Field field;

    public FieldWrapper(Field field) {
        this.field = field;


    }

    @Override
    public String getFieldName() {
        return field.getName();
    }

    @Override
    public IClass getFieldType() {
        return new ClassWrapper(field.getType());
    }

    @Override
    public int getFieldModifiers() {
        return field.getModifiers();
    }

    @Override
    public Object getFieldValue() {
        if (isStatic()) {
            try {
                return field.get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isStatic() {
        return Modifier.isStatic(getFieldModifiers());
    }
}
