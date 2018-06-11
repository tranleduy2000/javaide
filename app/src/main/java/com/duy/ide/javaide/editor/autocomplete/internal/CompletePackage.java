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
import com.duy.common.DLog;
import com.duy.ide.code.api.SuggestItem;
import com.duy.ide.editor.internal.suggestion.Editor;
import com.duy.ide.javaide.editor.autocomplete.model.PackageDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @NonNull
    private final JavaPackageManager mJavaPackageManager;

    public CompletePackage(@NonNull JavaPackageManager javaPackageManager) {
        mJavaPackageManager = javaPackageManager;
    }

    @Override
    public boolean process(Editor editor, String statement, ArrayList<SuggestItem> result) {
        Pattern compile = Pattern.compile("[.0-9A-Za-z_]\\s*$");
        if (compile.matcher(statement).find()){

        }
        return false;
    }

    @Override
    public void getSuggestion(Editor editor, String expr, List<SuggestItem> suggestItems) {
        if (DLog.DEBUG) {
            DLog.d(TAG, "getSuggestion() called with:" +
                    " editor = [" + editor + "]," +
                    " expr = [" + expr + "]," +
                    " suggestItems = [" + suggestItems + "]");
        }

        //package must be contains dot (.)
        if (!expr.contains(".")) {
            return;
        }

        //completed package java.lang
        int lastDotIndex = expr.lastIndexOf(".");
        String completedPkg = expr.substring(0, lastDotIndex /*not contains dot*/);
        String incompletePkg = expr.substring(lastDotIndex + 1);

        PackageDescription packages = mJavaPackageManager.trace(completedPkg);
        if (packages != null) {
            //members of current package
            //such as java has more member (util, io, lang)
            HashMap<String, PackageDescription> members = packages.getChild();
            for (Map.Entry<String, PackageDescription> entry : members.entrySet()) {
                PackageDescription packageDescription = entry.getValue();
                if (packageDescription.getName().startsWith(incompletePkg)) {
                    setInfo(packageDescription, editor, incompletePkg);
                    suggestItems.add(packageDescription);
                }

            }
        }
    }
}
