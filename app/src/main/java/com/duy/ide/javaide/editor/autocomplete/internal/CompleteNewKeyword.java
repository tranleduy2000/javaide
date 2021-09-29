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
import com.duy.ide.javaide.editor.autocomplete.model.ConstructorDescription;
import com.duy.ide.javaide.editor.autocomplete.model.PrimitiveArrayConstructorDescription;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaDexClassLoader;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Suggest constructor
 */
public class CompleteNewKeyword extends JavaCompleteMatcherImpl {
    private static final Pattern NEW_CLASS = Pattern.compile(
            "(\\s*new\\s+)(" + CONSTRUCTOR.pattern() + ")$", Pattern.MULTILINE);
    private static final String[] PRIMITIVE_ARRAY_TYPE =
            {"boolean", "byte", "double", "char", "float", "int", "long", "short"};

    private final JavaDexClassLoader mClassLoader;

    public CompleteNewKeyword(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process(JCTree.JCCompilationUnit ast,
                           Editor editor, Expression expression,
                           String statement, ArrayList<SuggestItem> result) {
        Matcher matcher = NEW_CLASS.matcher(statement);
        if (matcher.find()) {
            String incompleteCts = matcher.group(2);
            return getSuggestionInternal(editor, incompleteCts, result);
        }
        return false;
    }

    @Override
    public void getSuggestion(@NonNull Editor editor,
                              @NonNull String incomplete,
                              @NonNull List<SuggestItem> suggestItems) {
        getSuggestionInternal(editor, incomplete, suggestItems);
    }

    private boolean getSuggestionInternal(@NonNull Editor editor, @NonNull String incomplete,
                                          @NonNull List<SuggestItem> suggestItems) {
        if (incomplete.isEmpty()) {
            return false;
        }

        boolean handled = false;
        //try to find constructor
        List<IClass> classes = mClassLoader.findAllWithPrefix(incomplete);
        for (IClass clazz : classes) {
            List<ConstructorDescription> constructors = clazz.getConstructors();
            setInfo(constructors, editor, incomplete);
            suggestItems.addAll(constructors);
            handled = true;
        }
        for (String type : PRIMITIVE_ARRAY_TYPE) {
            if (type.startsWith(incomplete)) {
                PrimitiveArrayConstructorDescription c
                        = new PrimitiveArrayConstructorDescription(type);
                setInfo(c, editor, incomplete);
                suggestItems.add(c);
                handled = true;
            }
        }

        return handled;
    }

}
