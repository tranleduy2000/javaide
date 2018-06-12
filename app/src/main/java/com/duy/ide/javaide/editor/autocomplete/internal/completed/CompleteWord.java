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
import com.duy.ide.javaide.editor.autocomplete.dex.JavaDexClassLoader;
import com.duy.ide.javaide.editor.autocomplete.internal.JavaCompleteMatcherImpl;
import com.duy.ide.javaide.editor.autocomplete.model.KeywordDescription;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaParser;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteThisKeyword.addNonStaticMethods;
import static com.duy.ide.javaide.editor.autocomplete.internal.completed.CompleteThisKeyword.addNonStaticVariable;

/**
 * Final complete, no context, suggest class name, method name,
 * variable or any start with incomplete
 */
public class CompleteWord extends JavaCompleteMatcherImpl {
    private static final String TAG = "CompleteWord";
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

    private JavaParser mJavaParser;
    private JavaDexClassLoader mClassLoader;

    public CompleteWord(JavaParser javaParser, JavaDexClassLoader classLoader) {
        this.mJavaParser = javaParser;
        mClassLoader = classLoader;
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> result) throws Exception {
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        getSuggestionInternal(editor, incomplete, suggestItems);
    }

    private boolean getSuggestionInternal(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        boolean result = getPossibleInCurrentFile(editor, incomplete, suggestItems);
        result |= getKeyword(editor, incomplete, suggestItems);
        return result;
    }

    private boolean getKeyword(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        boolean found = false;
        for (String keyword : KEYWORDS) {
            if (keyword.startsWith(incomplete) && !keyword.equals(incomplete)) {
                KeywordDescription e = new KeywordDescription(keyword);
                setInfo(e, editor, incomplete);
                suggestItems.add(e);
                found = true;
            }
        }
        return found;
    }

    private boolean getPossibleInCurrentFile(@NonNull Editor editor, @NonNull String incomplete,
                                             @NonNull List<SuggestItem> suggestItems) {
        if (incomplete.endsWith(" ")) {
            return false;
        }
        JCTree.JCCompilationUnit ast;
        try {
            ast = mJavaParser.parse(editor.getText());
        } catch (Exception e) {
            if (DLog.DEBUG) DLog.d(TAG, "process: can not parse");
            return false;
        }
        ArrayList<SuggestItem> result = new ArrayList<>();
        //parse current file
        getPossibleResult(editor, result, ast, incomplete);

        //find all class start with incomplete
        ArrayList<? extends SuggestItem> classes = mClassLoader.findAllWithPrefix(incomplete);

        setInfo(classes, editor, incomplete);
        result.addAll(classes);

        return classes.size() > 0;
    }

    private void getPossibleResult(Editor editor, ArrayList<SuggestItem> result,
                                   JCTree.JCCompilationUnit ast, String incomplete) {
        if (ast == null) {
            return;
        }
        //current file declare
        com.sun.tools.javac.util.List<JCTree> typeDecls = ast.getTypeDecls();
        if (typeDecls.isEmpty()) {
            return;
        }
        JCTree jcTree = typeDecls.get(0);
        if (jcTree instanceof JCTree.JCClassDecl) {
            com.sun.tools.javac.util.List<JCTree> members =
                    ((JCTree.JCClassDecl) jcTree).getMembers();
            for (JCTree member : members) {
                if (member instanceof JCTree.JCVariableDecl) {
                    addNonStaticVariable((JCTree.JCVariableDecl) member, editor, incomplete, result);
                } else if (member instanceof JCTree.JCMethodDecl) {
                    JCTree.JCMethodDecl method = (JCTree.JCMethodDecl) member;
                    addNonStaticMethods(method, editor, incomplete, result);

                    //if the cursor in method scope
                    if (method.getStartPosition() <= editor.getCursor()
                            && method.getBody().getEndPosition(ast.endPositions) >= editor.getCursor()) {
                        collectFromMethod(editor, incomplete, result, method);
                    }
                }
            }
        }
    }

    private void collectFromMethod(Editor editor, String incomplete, ArrayList<SuggestItem> result,
                                   JCTree.JCMethodDecl method) {
        //add field from start position of method to the cursor
        com.sun.tools.javac.util.List<JCTree.JCStatement> statements
                = method.getBody().getStatements();
        for (JCTree.JCStatement statement : statements) {
            if (statement instanceof JCTree.JCVariableDecl) {
                JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) statement;
                addNonStaticVariable(field, editor, incomplete, result);
            }
        }
        //add params
        com.sun.tools.javac.util.List<JCTree.JCVariableDecl> parameters = method.getParameters();
        for (JCTree.JCVariableDecl parameter : parameters) {
            addNonStaticVariable(parameter, editor, incomplete, result);
        }

    }
}
