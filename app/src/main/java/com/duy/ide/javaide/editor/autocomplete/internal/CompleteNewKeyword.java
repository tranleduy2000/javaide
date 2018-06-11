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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Suggest constructor
 */
public class CompleteNewKeyword extends JavaCompleteMatcherImpl {
    private static final Pattern NEW_CLASS =
            Pattern.compile("(\\s*new\\s+)(" + CONSTRUCTOR.pattern() + ")$", Pattern.MULTILINE);
    private final JavaDexClassLoader mClassLoader;

    public CompleteNewKeyword(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> result) {
        Matcher matcher = NEW_CLASS.matcher(statement);
        if (matcher.find()) {
            String incompleteCts = matcher.group(2);
            getSuggestion(editor, incompleteCts, result);
            return true;
        }
        return false;
    }

    @Override
    public void getSuggestion(@NonNull Editor editor,
                              @NonNull String incomplete,
                              @NonNull List<SuggestItem> suggestItems) {
        if (incomplete.isEmpty()) {
            return;
        }

        //try to find constructor
        ArrayList<ClassDescription> classes = mClassLoader.findAllWithPrefix(incomplete);
        for (ClassDescription clazz : classes) {
            ArrayList<ConstructorDescription> constructors = clazz.getConstructors();
            setInfo(constructors, editor, incomplete);
            suggestItems.addAll(constructors);
        }
    }

}
