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

import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.util.EditorUtil;

public class StatementParser {
    /**
     * " Search back from the cursor position till meeting '{' or ';'.
     * " '{' means statement start, ';' means end of a previous statement.
     *
     * @return statement before cursor
     * " Note: It's the base for parsing. And It's OK for most cases.
     */
    public static String resolveStatementFromCursor(Editor editor) {
        String lineBeforeCursor = getCurrentLine(editor);
        if (lineBeforeCursor.matches("^\\s*(import|package)\\s+")) {
            return lineBeforeCursor;
        }
        int oldCursor = editor.getCursor();
        int newCursor = oldCursor - 1;
        while (true) {
            if (newCursor == 0) break;
            char c = editor.getText().charAt(newCursor);
            if (c == '{' || c == '}' || c == ';') {
                newCursor++;
                break;
            }
            newCursor--;
        }
        String statement = editor.getText().subSequence(newCursor, oldCursor).toString();
        return mergeLine(statement);
    }


    public static String getCurrentLine(Editor editText) {
        return EditorUtil.getLineBeforeCursor(editText, editText.getCursor());
    }

    public static String mergeLine(String statement) {
        statement = cleanStatement(statement);
        return statement;
    }

    /**
     * set string literal empty, remove comments, trim begining or ending spaces
     * case: ' 	sb. /* block comment"/ append( "stringliteral" ) // comment '
     * return 'sb.append("")'
     */
    private static String cleanStatement(String code) {
        if (code.matches("\\s*")) {
            return "";
        }
        code = removeComment(code); //clear all comment
        //clear all string content
        code = code.replaceAll(Patterns.STRINGS.toString(), "\"\"");
        code = EditorUtil.trimLeft(code);
        code = code.replaceAll("[\n\t\r]", "");
        return code;
    }

    /**
     * remove all comment
     */
    private static String removeComment(String code) {
        return code.replaceAll(Patterns.JAVA_COMMENTS.toString(), "");
    }

}
