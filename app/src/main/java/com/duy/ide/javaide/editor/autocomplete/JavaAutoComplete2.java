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

import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.code.api.SuggestionProvider;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteClassMember;
import com.duy.ide.javaide.editor.autocomplete.internal.IJavaCompleteMatcher;
import com.duy.ide.javaide.editor.autocomplete.internal.JavaPackageManager;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.internal.StatementParser;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteClassDeclared;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteNewKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompletePackage;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteString;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteThisKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteWord;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;

import java.io.File;
import java.util.ArrayList;


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
        mJavaAutoCompletes.add(new CompletePackage(mJavaPackageManager));
        mJavaAutoCompletes.add(new CompleteString(mClassLoader));
        mJavaAutoCompletes.add(new CompleteClassDeclared(mClassLoader));
        mJavaAutoCompletes.add(new CompleteThisKeyword(mJavaParser));
        mJavaAutoCompletes.add(new CompleteWord(mJavaParser, mClassLoader));
    }

    @Override
    public ArrayList<SuggestItem> getSuggestions(Editor editor) {
        ArrayList<SuggestItem> result = new ArrayList<>();
        try {

            String statement = StatementParser.resolveStatementFromCursor(editor);
            for (IJavaCompleteMatcher autoComplete : mJavaAutoCompletes) {
                try {
                    boolean handled = autoComplete.process(editor, statement, result);
                    if (handled) {
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
