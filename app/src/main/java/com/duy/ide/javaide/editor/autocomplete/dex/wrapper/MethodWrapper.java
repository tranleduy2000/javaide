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
import com.duy.ide.javaide.editor.autocomplete.dex.IMethod;
import com.duy.ide.javaide.editor.autocomplete.dex.TypeConverter;

import java.lang.reflect.Method;

public class MethodWrapper implements IMethod {
    private Method method;

    public MethodWrapper(Method method) {
        this.method = method;
    }

    @Override
    public String getMethodName() {
        return method.getName();
    }

    @Override
    public IClass getMethodReturnType() {
        return new ClassWrapper(method.getReturnType());
    }

    @Override
    public IClass[] getMethodParameterTypes() {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return TypeConverter.toIClass(parameterTypes);
    }

    @Override
    public int getModifiers() {
        return method.getModifiers();
    }
}
