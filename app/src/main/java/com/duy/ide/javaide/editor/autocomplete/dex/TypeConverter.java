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

package com.duy.ide.javaide.editor.autocomplete.dex;

import com.duy.ide.javaide.editor.autocomplete.dex.wrapper.ClassWrapper;

public class TypeConverter {

    public static Class[] toClasses(IClass[] types) {
        Class[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            classes[i] = types[i].getClass();
        }
        return classes;
    }

    public static IClass[] toIClass(Class[] classes) {
        IClass[] result = new IClass[classes.length];
        for (int i = 0; i < classes.length; i++) {
            result[i] = new ClassWrapper(classes[i]);
        }
        return result;
    }
}
