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

import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.model.KeywordDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompleteKeyword extends JavaCompleteMatcherImpl {
    private final static Set<String> KEYWORDS;

    static {
        String[] kws = {
                "abstract", "continue", "for", "new", "switch",
                "assert", "default", "if", "package", "synchronized",
                "boolean", "do", "goto", "private", "this",
                "break", "double", "implements", "protected", "throw",
                "byte", "else", "import", "public", "throws",
                "case", "enum", "instanceof", "return", "transient",
                "catch", "extends", "int", "short", "try",
                "char", "final", "interface", "static", "void",
                "class", "finally", "long", "strictfp", "volatile",
                "const", "float", "native", "super", "while",
                // literals
                "null", "true", "false"
        };
        Set<String> s = new HashSet<>(Arrays.asList(kws));
        KEYWORDS = Collections.unmodifiableSet(s);
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> result) {
        if (statement.contains(" ")) {
            statement = statement.substring(statement.lastIndexOf(" ") + 1);
        }
        if (statement.isEmpty()) {
            return false;
        }
        getSuggestion(editor, statement, result);
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        for (String keyword : KEYWORDS) {
            if (keyword.startsWith(incomplete) && !keyword.equals(incomplete)) {
                KeywordDescription e = new KeywordDescription(keyword);
                setInfo(e, editor, incomplete);
                suggestItems.add(e);
            }
        }
    }
}
