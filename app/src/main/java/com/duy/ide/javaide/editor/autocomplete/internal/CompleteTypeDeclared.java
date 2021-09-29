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
import com.android.annotations.Nullable;
import com.duy.common.interfaces.Filter;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.parser.IClass;
import com.duy.ide.javaide.editor.autocomplete.parser.JavaDexClassLoader;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Complete class declaration
 * <p>
 * public? static? final? class Name (extends otherClass)? (implements otherInterfaces)?
 * public? enum Name
 * public? final? interface Name (extends otherInterfaces)?
 */
public class CompleteTypeDeclared extends JavaCompleteMatcherImpl {
    //case: public class A
    //case: public class Name extends OtherClass
    //case: class Name extends C implements D
    public static final Pattern CLASS_DECLARE = Pattern.compile(
            //more modifiers, public static final ....
            "((public|protected|private|abstract|static|final|strictfp)\\s+)*" +
                    //type
                    "((class|inteface|enum)\\s+)" +
                    //name
                    "([a-zA-Z_][a-zA-Z0-9_]*)" +
                    //inherit
                    "(\\s+extends\\s+([a-zA-Z_][a-zA-Z0-9_]*))?" +
                    "(\\s+implements\\s+([a-zA-Z_][a-zA-Z0-9_]*))?");
    private static final Pattern END_EXTENDS
            = Pattern.compile("\\s+extends\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");
    private static final Pattern END_IMPLEMENTS
            = Pattern.compile("\\s+implements\\s+([a-zA-Z_][a-zA-Z0-9_]*)$");

    private static final String TAG = "CompleteClassDeclared";
    private JavaDexClassLoader mClassLoader;

    public CompleteTypeDeclared(JavaDexClassLoader classLoader) {

        mClassLoader = classLoader;
    }

    @Override
    public boolean process(JCTree.JCCompilationUnit ast, Editor editor, Expression expression, String statement, ArrayList<SuggestItem> result) {
        Matcher matcher = CLASS_DECLARE.matcher(statement);
        if (matcher.find()) {
            matcher = END_IMPLEMENTS.matcher(statement);
            if (matcher.find()) {
                if (DLog.DEBUG) DLog.d(TAG, "process: END_IMPLEMENTS found");
                String incompleteInterface = matcher.group(1);
                return getSuggestionInternal(editor, incompleteInterface, result,
                        "interface");

            }

            matcher = END_EXTENDS.matcher(statement);
            if (matcher.find()) {
                if (DLog.DEBUG) DLog.d(TAG, "process: END_EXTENDS found");
                String incompleteInterface = matcher.group(1);
                return getSuggestionInternal(editor, incompleteInterface, result,
                        "class");

            }

        }
        return false;
    }

    /**
     * @param declareType - enum, class, annotation, interface
     */
    private boolean getSuggestionInternal(@NonNull Editor editor,
                                          @NonNull String incomplete,
                                          @NonNull List<SuggestItem> result,
                                          @Nullable String declareType) {

        //filter interfaces or classes
        Filter<IClass> filter = null;
        switch (declareType) {
            case "interface":
                filter = new Filter<IClass>() {
                    @Override
                    public boolean accept(IClass clazz) {
                        if (Modifier.isFinal(clazz.getModifiers())) {
                            return false;
                        }
                        // TODO: 11-Jun-18 package private can extends
                        if (!Modifier.isPublic(clazz.getModifiers())) {
                            return false;
                        }
                        return clazz.isInterface();
                    }
                };
                break;
            case "class":
                filter = new Filter<IClass>() {
                    @Override
                    public boolean accept(IClass clazz) {
                        if (Modifier.isFinal(clazz.getModifiers())) {
                            return false;
                        }
                        // TODO: 11-Jun-18 package private can extends
                        if (!Modifier.isPublic(clazz.getModifiers())) {
                            return false;
                        }
                        return !clazz.isInterface();
                    }
                };
                break;
        }

        List<IClass> classes = mClassLoader.findAllWithPrefix(incomplete, filter);
        if (classes.size() > 0) {
            setInfo(classes, editor, incomplete);
            result.addAll(classes);
            if (DLog.DEBUG) DLog.d(TAG, "getSuggestionInternal() returned: " + true);
            return true;
        }
        if (DLog.DEBUG) DLog.d(TAG, "getSuggestionInternal() returned: " + false);
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String incomplete, List<SuggestItem> suggestItems) {
        getSuggestionInternal(editor, incomplete, suggestItems, null);
    }
}
