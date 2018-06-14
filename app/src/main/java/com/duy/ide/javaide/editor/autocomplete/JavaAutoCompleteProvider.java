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
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteExpression;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteNewKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.CompletePackage;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteString;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteThisKeyword;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteTypeDeclared;
import com.duy.ide.javaide.editor.autocomplete.internal.CompleteWord;
import com.duy.ide.javaide.editor.autocomplete.internal.Expression;
import com.duy.ide.javaide.editor.autocomplete.internal.ExpressionResolver;
import com.duy.ide.javaide.editor.autocomplete.internal.IJavaCompleteMatcher;
import com.duy.ide.javaide.editor.autocomplete.internal.PackageImporter;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;
import com.duy.ide.javaide.editor.autocomplete.parser.PackageManager;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class JavaAutoCompleteProvider implements SuggestionProvider {
    private static final String TAG = "JavaAutoComplete2";
    private final ArrayList<IJavaCompleteMatcher> mJavaAutoCompletes = new ArrayList<>();
    private JavaDexClassLoader mClassLoader;
    private PackageManager mPackageManager;
    private PackageImporter mPackageImporter;
    private JavaParser mJavaParser;
    private JavaClassManager mClassManager;

    public JavaAutoCompleteProvider(Context context) {
        File outDir = context.getDir("dex", Context.MODE_PRIVATE);
        mClassLoader = new JavaDexClassLoader(Environment.getClasspathFile(context), outDir);
        mPackageManager = new PackageManager();
        mJavaParser = new JavaParser();

        addAutoComplete();
    }

    public void load(JavaProject projectFile) {
        mClassLoader.loadAllClasses(projectFile);
        mPackageManager.init(projectFile, mClassLoader.getClassReader());
    }

    private void addAutoComplete() {
        mJavaAutoCompletes.add(new CompleteExpression(mClassLoader));
        mJavaAutoCompletes.add(new CompleteNewKeyword(mClassLoader));
        mJavaAutoCompletes.add(new CompletePackage(mPackageManager));
        mJavaAutoCompletes.add(new CompleteString(mClassLoader));
        mJavaAutoCompletes.add(new CompleteTypeDeclared(mClassLoader));
        mJavaAutoCompletes.add(new CompleteThisKeyword(mJavaParser));
        mJavaAutoCompletes.add(new CompleteWord(mJavaParser, mClassLoader));
    }

    @Override
    public ArrayList<SuggestItem> getSuggestions(Editor editor) {
        long time = System.currentTimeMillis();
        ArrayList<SuggestItem> result = new ArrayList<>();
        try {
            JCTree.JCCompilationUnit ast = mJavaParser.parse(editor.getText());
            List<IClass> classes = mJavaParser.parseClasses(ast);

            //should be update java class in Java class manager
            mClassLoader.updateClass(classes);

            ExpressionResolver resolver = new ExpressionResolver(ast, editor);
            Expression expression = resolver.getExpressionAtCursor();

            //like a hack for simple suggestion
            JCTree.JCExpression jcExpression = expression.getExpression();
            int startPosition = jcExpression.getStartPosition();
            String statement = editor.getText().substring(startPosition, editor.getCursor());

            for (IJavaCompleteMatcher autoComplete : mJavaAutoCompletes) {
                try {
                    boolean handled = autoComplete.process(ast, editor, expression, statement, result);
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
        if (DLog.DEBUG)
            DLog.d(TAG, "getSuggestions: time = " + (System.currentTimeMillis() - time));
        return result;
    }
}
