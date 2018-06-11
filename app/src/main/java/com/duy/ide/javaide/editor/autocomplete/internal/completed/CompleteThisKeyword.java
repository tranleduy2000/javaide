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

package com.duy.ide.javaide.editor.autocomplete.internal.completed;

import com.android.annotations.NonNull;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl;
import com.duy.ide.javaide.editor.autocomplete.internal.Patterns;
import com.duy.ide.javaide.editor.autocomplete.model.FieldDescription;
import com.duy.ide.javaide.editor.autocomplete.model.MethodDescription;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.tools.javac.code.Flags.STATIC;

public class CompleteThisKeyword extends JavaCompleteMatcherImpl {
    private static final Pattern THIS_DOT = Pattern.compile("this\\s*\\.\\s*$");
    private static final Pattern THIS_DOT_EXPR = Pattern.compile("this\\s*\\.\\s*(" + Patterns.IDENTIFIER.pattern() + ")$");
    private static final String TAG = "CompleteThisAndSuperKey";
    private JavaParser mJavaParser;

    public CompleteThisKeyword(JavaParser javaParser) {
        this.mJavaParser = javaParser;
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> result) throws Exception {
        Matcher matcher = THIS_DOT.matcher(statement);
        if (matcher.find()) {
            JCTree.JCCompilationUnit ast;
            try {
                ast = mJavaParser.parse(editor.getText());
            } catch (Exception e) {
                if (DLog.DEBUG) DLog.d(TAG, "process: can not parse");
                return false;
            }
            getSuggestionInternal(editor, result, ast, "");
            return true;
        }
        matcher = THIS_DOT_EXPR.matcher(statement);
        if (matcher.find()) {
            JCTree.JCCompilationUnit ast;
            try {
                ast = mJavaParser.parse(editor.getText());
            } catch (Exception e) {
                if (DLog.DEBUG) DLog.d(TAG, "process: can not parse");
                return false;
            }
            String incomplete = matcher.group(1);
            getSuggestionInternal(editor, result, ast, incomplete);
            return true;
        }
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {

    }

    private void getSuggestionInternal(@NonNull Editor editor, @NonNull ArrayList<SuggestItem> result,
                                       @NonNull JCTree.JCCompilationUnit unit,
                                       @NonNull String incomplete) {
        if (unit == null) {
            return;
        }
        //current file declare
        com.sun.tools.javac.util.List<JCTree> typeDecls = unit.getTypeDecls();
        if (typeDecls.isEmpty()) {
            return;
        }
        JCTree jcTree = typeDecls.get(0);
        if (jcTree instanceof JCTree.JCClassDecl) {
            com.sun.tools.javac.util.List<JCTree> members =
                    ((JCTree.JCClassDecl) jcTree).getMembers();
            for (JCTree member : members) {
                if (member instanceof JCTree.JCVariableDecl) {
                    addNonStaticVariable(member, editor, incomplete, result);
                } else if (member instanceof JCTree.JCMethodDecl) {
                    JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                    addNonStaticMethods(method, editor, incomplete, result);
/*
                    //if the cursor in method scope
                    if (method.getStartPosition() <= editor.getCursor()
                            && method.getBody().getEndPosition(unit.endPositions) >= editor.getCursor()) {
                        //add field from start position of method to the cursor
                        com.sun.tools.javac.util.List<JCTree.JCStatement> statements
                                = method.getBody().getStatements();
                        for (JCTree.JCStatement statement : statements) {
                            if (statement instanceof JCTree.JCVariableDecl) {
                                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) statement;
                                addVariable(field, incomplete, result);
                            }
                        }
                        //add params
                        com.sun.tools.javac.util.List<JCTree.JCVariableDecl> parameters = method.getParameters();
                        for (JCTree.JCVariableDecl parameter : parameters) {
                            JCTree.JCVariableDecl field = parameter;
                            addVariable(field, incomplete, result);
                        }
                    }*/
                }
            }
        }
    }

    private void addNonStaticMethods(JCTree.JCMethodDecl method, Editor editor, String incomplete, ArrayList<SuggestItem> result) {
        if ((method.getModifiers().flags & STATIC) != 0) {
            if (DLog.DEBUG) DLog.d(TAG, "addNonStaticMethods: static method");
            return;
        }
        if (method.getName().toString().startsWith(incomplete)) {
            com.sun.tools.javac.util.List<JCTree.JCTypeParameter> typeParameters
                    = method.getTypeParameters();
            ArrayList<String> paramsStr = new ArrayList<>();
            for (JCTree.JCTypeParameter typeParameter : typeParameters) {
                paramsStr.add(typeParameter.toString());
            }
            MethodDescription desc = new MethodDescription(
                    method.getName().toString(),
                    method.getReturnType().toString(),
                    method.getModifiers().flags,
                    paramsStr);
            setInfo(desc, editor, incomplete);
            result.add(desc);
        }
    }

    private void addNonStaticVariable(JCTree member, Editor editor, String incomplete,
                                      ArrayList<SuggestItem> result) {
        JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) member;
        if ((field.getModifiers().flags & STATIC) != 0) {
            if (DLog.DEBUG) DLog.d(TAG, "addNonStaticVariable: static variable");
            return;
        }

        if (field.getName().toString().startsWith(incomplete)) {
            int flags = (int) field.getModifiers().flags;
            FieldDescription desc = new FieldDescription(
                    field.getName().toString(),
                    field.getType().toString(),
                    flags);
            setInfo(desc, editor, incomplete);
            result.add(desc);
        }
    }
}
