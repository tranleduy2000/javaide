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
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.IField;
import com.duy.ide.javaide.editor.autocomplete.parser.IMethod;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaClassManager;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaDexClassLoader;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.sun.tools.javac.tree.JCTree.JCErroneous;
import static com.sun.tools.javac.tree.JCTree.JCExpression;

/**
 * This class very complex, implement after
 */
public class CompleteExpression extends JavaCompleteMatcherImpl {
    private static final String TAG = "CompleteExpression";
    private final JavaDexClassLoader mClassLoader;
    private Map<JCTree, Integer> mEndPositions;

    private JCCompilationUnit mAst;
    private IClass mCurrentType;

    private int mCursor;
    private Editor mEditor;

    public CompleteExpression(@NonNull JavaDexClassLoader classLoader) {
        mClassLoader = classLoader;
    }

    public void prepare(Editor editor, JCCompilationUnit ast) {
        mEditor = editor;
        mCursor = editor.getCursor();
        mAst = ast;
        mEndPositions = ast.endPositions;

        List<JCTree> typeDecls = mAst.getTypeDecls();
        for (JCTree typeDecl : typeDecls) {
            if (typeDecl instanceof JCTree.JCClassDecl) {
                int startPosition = typeDecl.getStartPosition();
                int endPosition = typeDecl.getEndPosition(mEndPositions);
                if (startPosition <= mCursor && mCursor <= endPosition) {
                    String simpleName = ((JCTree.JCClassDecl) typeDecl).getSimpleName().toString();
                    JCExpression packageName = ast.getPackageName();
                    mCurrentType = JavaClassManager.getInstance()
                            .getParsedClass(packageName + "." + simpleName);
                }
            }
        }
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {

    }

    @Override
    public boolean process(JCCompilationUnit ast, Editor editor, Expression expression,
                           String unused, ArrayList<SuggestItem> result) {
        try {
            prepare(editor, ast);
            return expression != null && performComplete(expression.getExpression(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean performComplete(JCExpression jcExpression, ArrayList<SuggestItem> result) {
        if (DLog.DEBUG) DLog.d(TAG, "analyse: class = " + jcExpression.getClass());
        //error expression
        if (jcExpression instanceof JCErroneous) {
            System.out.println("CompleteExpression.JCErroneous");
            List<? extends JCTree> errorTrees = ((JCErroneous) jcExpression).getErrorTrees();
            JCTree errExpr = errorTrees.get(0);
            if (errExpr instanceof JCExpression) {
                return performComplete((JCExpression) errExpr, result);
            }

        } else if (jcExpression instanceof JCFieldAccess) {
            //arrayList.to <-  JCFieldAccess
            //arrayList.add("Hello");
            //s.toLower, incomplete method but give JCFieldAccess
            return performCompleteFieldAccess((JCFieldAccess) jcExpression, result);

        } else if (jcExpression instanceof JCTree.JCIdent) {
            return performCompleteIdent((JCTree.JCIdent) jcExpression, result);

        }
        return false;
    }

    /**
     * Suggestion method, variable
     */
    private boolean performCompleteIdent(JCTree.JCIdent jcIdent, ArrayList<SuggestItem> result) {
        return false;
    }

    private boolean performCompleteFieldAccess(@NonNull JCFieldAccess jcFieldAccess,
                                               @NonNull ArrayList<SuggestItem> result) {
        int startPosition = jcFieldAccess.getStartPosition();
        int cursorOffset = mCursor - startPosition;
        int incompleteLength = jcFieldAccess.getIdentifier().length();
        String incomplete = jcFieldAccess.getIdentifier().toString();
        //simple hack, improve later
        if (incomplete.equals("<error>")) {
            incomplete = "";
        }
        if (DLog.DEBUG) DLog.d(TAG, "incomplete = " + incomplete);

        JCExpression expression = jcFieldAccess.getExpression();
        IClass type = new TypeResolver(mClassLoader, mAst, mCurrentType)
                .resolveType(expression, mCursor);
        if (type != null) {
            List<IMethod> methods = type.getMethods();
            for (IMethod method : methods) {
                if (method.getMethodName().startsWith(incomplete)) {
                    setInfo(method, mEditor, incomplete);
                    result.add(method);
                }
            }
            ArrayList<IField> fields = type.getFields();
            for (IField field : fields) {
                if (field.getFieldName().startsWith(incomplete)) {
                    setInfo(field, mEditor, incomplete);
                    result.add(field);
                }
            }

            return true;
        }
        return false;
    }


}
