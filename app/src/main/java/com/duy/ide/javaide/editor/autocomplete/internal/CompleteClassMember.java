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

import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaClassReader;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;

import java.util.ArrayList;
import java.util.List;

public class CompleteClassMember extends JavaCompleteMatcherImpl implements IJavaCompleteMatcher {
    private static final String TAG = "CompleteClassMember";
    private final JavaDexClassLoader mClassLoader;

    public CompleteClassMember(JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    @Override
    public boolean process() {
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String expression, List<SuggestItem> suggestItems) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "getSuggestion() called with:" +
                    " editor = [" + editor + "]," +
                    " expression = [" + expression + "]," +
                    " suggestItems = [" + suggestItems + "]");
        }

        final String className;
        final String incomplete;
        if (expression.contains(".")) {
            className = expression.substring(0, expression.lastIndexOf("."));
            incomplete = expression.substring(expression.lastIndexOf(".") + 1);
        } else {
            className = "";
            incomplete = expression;
        }
        JavaClassReader classReader = mClassLoader.getClassReader();
        ClassDescription clazz = classReader.readClassByName(className, null);
        if (clazz != null) {
            if (DLog.DEBUG) DLog.d(TAG, "getSuggestion: found " + className);
            ArrayList<SuggestItem> members = clazz.getMember(incomplete);
            setInfo(members, editor, incomplete);
            suggestItems.addAll(members);
        }
    }
}
