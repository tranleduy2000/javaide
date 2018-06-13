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
import com.duy.ide.javaide.editor.autocomplete.parser.PackageManager;
import com.duy.ide.javaide.editor.autocomplete.model.PackageDescription;
import com.duy.ide.javaide.utils.DLog;
import com.sun.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Complete package
 * e.g.
 * <p>
 * java.i|
 * java.util.|
 * java.util.Hash|
 */
public class CompletePackage extends JavaCompleteMatcherImpl {
    private static final String TAG = "CompletePackage";

    private static final Pattern PACKAGE
            = Pattern.compile("^\\s*(package)\\s+([_A-Za-z0-9.]+)");
    private static final Pattern IMPORT_OR_IMPORT_STATIC
            = Pattern.compile("^\\s*import(\\s+static)?\\s+([_A-Za-z0-9.]+)");

    @NonNull
    private final PackageManager mPackageManager;

    public CompletePackage(@NonNull PackageManager packageManager) {
        mPackageManager = packageManager;
    }

    @Override
    public boolean process(JCTree.JCCompilationUnit ast, Editor editor, Expression expression, String statement, ArrayList<SuggestItem> result) {
        Matcher matcher = PACKAGE.matcher(statement);
        if (matcher.find()) {
            if (DLog.DEBUG) DLog.d(TAG, "process: package found");
            String incompletePkg = matcher.group(2);
            if (DLog.DEBUG) DLog.d(TAG, "incompletePkg = " + incompletePkg);
            getSuggestion(editor, incompletePkg, result);
            return true;
        }

        matcher = IMPORT_OR_IMPORT_STATIC.matcher(statement);
        if (matcher.find()) {
            if (DLog.DEBUG) DLog.d(TAG, "process: import(static)? found");
            boolean isStatic = matcher.group(1) != null;
            // TODO: 11-Jun-18 support import static
            if (DLog.DEBUG) DLog.d(TAG, "isStatic = " + isStatic);
            String incompletePkg = matcher.group(2);
            if (DLog.DEBUG) DLog.d(TAG, "incompletePkg = " + incompletePkg);
            getSuggestion(editor, incompletePkg, result);
        }

        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String expr, List<SuggestItem> suggestItems) {
        getSuggestionInternal(editor, expr, suggestItems, false);
    }

    private void getSuggestionInternal(Editor editor, String expr, List<SuggestItem> suggestItems,
                                       boolean addSemicolon) {

        //package must be contains dot (.)
        if (!expr.contains(".")) {
            return;
        }

        //completed package java.lang
        int lastDotIndex = expr.lastIndexOf(".");
        String completedPart = expr.substring(0, lastDotIndex /*not contains dot*/);
        String incompletePart = expr.substring(lastDotIndex + 1);

        PackageDescription packages = mPackageManager.trace(completedPart);
        if (packages != null) {
            //members of current package
            //such as java has more member (util, io, lang)
            HashMap<String, PackageDescription> members = packages.getChild();
            for (Map.Entry<String, PackageDescription> entry : members.entrySet()) {
                PackageDescription packageDescription = entry.getValue();
                if (packageDescription.getName().startsWith(incompletePart)) {
                    setInfo(packageDescription, editor, incompletePart);
                    suggestItems.add(packageDescription);
                }

            }
        }
    }
}
