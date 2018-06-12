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

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;

public interface IClass {
    int getModifiers();

    @Nullable
    String getFullClassName();

    @Nullable
    String getSimpleName();

    boolean isInterface();

    boolean isEnum();

    @Nullable
    IMethod getMethod(@NonNull String methodName, @Nullable IClass[] argsType);

    @Nullable
    IField getField(@NonNull String name);
}
