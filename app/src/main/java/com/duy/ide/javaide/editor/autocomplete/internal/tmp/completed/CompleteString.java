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

package com.duy.ide.javaide.editor.autocomplete.internal.tmp.completed;

import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaClassReader;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.utils.DLog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompleteString extends JavaCompleteMatcherImpl {
    static final Pattern STRING_DOT =
            Pattern.compile("\"\\s*\\.\\s*$", Pattern.MULTILINE);
    static final Pattern STRING_DOT_EXPR
            = Pattern.compile("\"\\s*\\.\\s*(" + METHOD_NAME.pattern() + ")$");
    private static final String TAG = "CompleteString";
    private final JavaDexClassLoader mClassLoader;

    public CompleteString(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> suggestItems) {
        Matcher matcher = STRING_DOT.matcher(statement);
        if (matcher.find()) {
            if (DLog.DEBUG) DLog.d(TAG, "process: string dot found");
            getSuggestion(editor, "", suggestItems);
            return true;
        }
        matcher = STRING_DOT_EXPR.matcher(statement);
        if (matcher.find()) {
            if (DLog.DEBUG) DLog.d(TAG, "process: string dot expr found");
            String incompleteMethod = matcher.group(1);
            getSuggestion(editor, incompleteMethod, suggestItems);
            return true;
        }
        return false;
    }

    /**
     * Complete string only filter method, not filter field or constructor
     */
    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        JavaClassReader reader = mClassLoader.getClassReader();
        ClassDescription stringClass = reader.readClassByName(String.class.getName(), null);
        assert stringClass != null;
        ArrayList<SuggestItem> methods = new ArrayList<>();
        stringClass.getMethods(methods, incomplete);
        setInfo(methods, editor, incomplete);
        suggestItems.addAll(methods);
    }
}
