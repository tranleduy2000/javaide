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

package com.duy.ide.javaide.editor.autocomplete.internal;

import com.android.annotations.NonNull;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.editor.autocomplete.model.ConstructorDescription;

import java.util.ArrayList;
import java.util.List;

public class CompleteConstructor implements IJavaCompleteMatcher {

    private final JavaDexClassLoader mClassLoader;

    public CompleteConstructor(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void getSuggestion(@NonNull Editor editor,
                              @NonNull String incomplete,
                              @NonNull List<SuggestItem> suggestItems) {
        if (incomplete.isEmpty()) {
            return;
        }
        ArrayList<ClassDescription> classes = mClassLoader.findClassWithPrefix(incomplete);
        for (ClassDescription clazz : classes) {
            ArrayList<ConstructorDescription> constructors = clazz.getConstructors();
            for (ConstructorDescription c : constructors) {
                c.setEditor(editor);
                c.setIncomplete(incomplete);
            }
            suggestItems.addAll(constructors);
        }
    }

}
