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

package com.duy.ide.javaide.editor.autocomplete;

import android.content.Context;

import com.android.annotations.NonNull;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.code.api.SuggestionProvider;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteClassMember;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteNewKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.CompletePackage;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteStaticAccess;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteString;
import com.duy.ide.javaide.editor.autocomplete.internal.IJavaCompleteMatcher;
import com.duy.ide.javaide.editor.autocomplete.internal.JavaPackageManager;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;

import java.io.File;
import java.util.ArrayList;

import static com.duy.ide.javaide.editor.autocomplete.JavaAutoCompleteProvider.getCurrentLine;
import static com.duy.ide.javaide.editor.autocomplete.JavaAutoCompleteProvider.mergeLine;

public class JavaAutoComplete2 implements SuggestionProvider {
    /**
     * Support complete java constructor
     */
    private final CompleteNewKeyword mCompleteNewKeyword;
    private final CompleteClassMember mCompleteClassMember;
    private final CompletePackage mCompletePackage;
    private final ArrayList<IJavaCompleteMatcher> mJavaAutoCompletes = new ArrayList<>();
    private JavaDexClassLoader mClassLoader;
    private JavaPackageManager mJavaPackageManager;
    private PackageImporter mPackageImporter;
    private JavaParser mJavaParser;
    private String statement = ""; //statement before cursor

    public JavaAutoComplete2(Context context) {
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(Environment.getClasspathFile(context), outDir);
        mJavaPackageManager = new JavaPackageManager();
        mJavaParser = new JavaParser();
        mCompleteNewKeyword = new CompleteNewKeyword(mClassLoader);
        mCompleteClassMember = new CompleteClassMember(mClassLoader);
        mCompletePackage = new CompletePackage(mJavaPackageManager);

        addAutoComplete();
    }

    public void load(JavaProject projectFile) {
        mClassLoader.loadAllClasses(projectFile);
        mJavaPackageManager.init(projectFile, mClassLoader.getClassReader());
    }

    private void addAutoComplete() {
        mJavaAutoCompletes.add(new CompleteClassMember(mClassLoader));
        mJavaAutoCompletes.add(new CompleteNewKeyword(mClassLoader));
        mJavaAutoCompletes.add(new CompleteKeyword());
        mJavaAutoCompletes.add(new CompletePackage(mJavaPackageManager));
        mJavaAutoCompletes.add(new CompleteStaticAccess());
        mJavaAutoCompletes.add(new CompleteString(mClassLoader));
    }

    @Override
    public ArrayList<SuggestItem> getSuggestions(Editor editor) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        try {

            String statement = getStatement(editor);
            for (IJavaCompleteMatcher autoComplete : mJavaAutoCompletes) {
                try {
                    autoComplete.process(editor, statement, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * " Search back from the cursor position till meeting '{' or ';'.
     * " '{' means statement start, ';' means end of a previous statement.
     *
     * @return statement before cursor
     * " Note: It's the base for parsing. And It's OK for most cases.
     */
    @NonNull
    private String getStatement(Editor editor) {
        String lineBeforeCursor = getCurrentLine(editor);
        if (lineBeforeCursor.matches("^\\s*(import|package)\\s+")) {
            return lineBeforeCursor;
        }
        int oldCursor = editor.getCursor();
        int newCursor;
        for (newCursor = oldCursor - 1; newCursor > 0; newCursor--) {
            char c = editor.getText().charAt(newCursor);
            if (c == '{' || c == '}' || c == ';') {
                break;
            }
        }
        String statement = editor.getText().subSequence(newCursor, oldCursor).toString();
        return mergeLine(statement);
    }
}
