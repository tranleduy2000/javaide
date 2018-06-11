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
import com.duy.ide.javaide.editor.autocomplete.model.JavaSuggestItemImpl;

import java.util.ArrayList;
import java.util.regex.Pattern;

public abstract class JavaCompleteMatcherImpl implements IJavaCompleteMatcher {
    //statement end with character, number or dot
    public static final Pattern END_WITH_CHARACTER_OR_DOT
            = Pattern.compile("[.0-9A-Za-z_]\\s*$", Pattern.MULTILINE);
    //statement end with dot "str."
    public static final Pattern END_WITH_DOT = Pattern.compile("\\.\\s*$", Pattern.MULTILINE);

    /**
     * Check statement is valid when end with dot
     */
    public static final Pattern VALID_WHEN_END_WITH_DOT
            = Pattern.compile(
            "[" +
                    "\"" + //string: "Hello".|
                    ")" + //expression in parentheses: (new String("")).|
                    "0-9A-Za-z_" + //name of variable or identifier: variable.|
                    "\\]" + //array access: array[].|
                    "]\\s*\\.\\s*$", Pattern.MULTILINE);
    public static final Pattern KEYWORD_DOT
            = Pattern.compile("(" + Patterns.RE_KEYWORDS.pattern() + ")\\s*\\.\\s*$");


    public static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    public static final Pattern METHOD_NAME = IDENTIFIER;
    public static final Pattern CLASS_NAME = IDENTIFIER;
    public static final Pattern VARIABLE_NAME = IDENTIFIER;
    public static final Pattern PACKAGE_NAME = Pattern.compile(
            "^" + IDENTIFIER.pattern() + "(\\." + IDENTIFIER.pattern() + ")*$");

    private static final String TAG = "JavaCompleteMatcherImpl";

    protected void setInfo(ArrayList<SuggestItem> members, Editor editor, String incomplete) {
        for (SuggestItem member : members) {
            if (member instanceof JavaSuggestItemImpl) {
                setInfo((JavaSuggestItemImpl) member, editor, incomplete);
            } else {
                if (DLog.DEBUG) DLog.w(TAG, "setInfo: not a java suggestion " + member);
            }
        }
    }

    protected void setInfo(JavaSuggestItemImpl member, Editor editor, String incomplete) {
        member.setEditor(editor);
        member.setIncomplete(incomplete);
    }
}
