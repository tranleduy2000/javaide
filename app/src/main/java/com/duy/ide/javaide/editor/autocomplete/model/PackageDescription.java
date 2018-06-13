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

package com.duy.ide.javaide.editor.autocomplete.model;

import android.support.annotation.NonNull;
import android.text.Editable;

import com.duy.ide.editor.view.IEditAreaView;
import com.duy.ide.javaide.editor.autocomplete.internal.CompletePackage;

import java.util.HashMap;

/**
 * @see CompletePackage
 */
public class PackageDescription extends JavaSuggestItemImpl {
    private String mName;
    private PackageDescription mParentPkg;
    private HashMap<String, PackageDescription> mChild = new HashMap<>();

    private PackageDescription(String childName, PackageDescription parent) {
        this.mName = childName;
        this.mParentPkg = parent;
    }

    public static PackageDescription root() {
        return new PackageDescription("", null);
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int cursor = getEditor().getCursor();
            final int length = getIncomplete().length();
            final int start = cursor - length;

            Editable editable = editorView.getEditableText();

            if (isLeaf()) {
                //static access
                String text = mName.replace("$", ".");
                // why not add semicolon (;) , in some case you need declared variable with full class
                // name, not import package
                editable.replace(start, cursor, text);
                editorView.setSelection(start + text.length());
            } else {
                String text = mName + ".";
                editable.replace(start, cursor, text);
                editorView.setSelection(start + text.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public char getTypeHeader() {
        return 'p';
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReturnType() {
        return null;
    }


    public HashMap<String, PackageDescription> getChild() {
        return mChild;
    }

    private boolean isRoot() {
        return mName.isEmpty() || mParentPkg == null;
    }

    private boolean isLeaf() {
        return mChild.isEmpty();
    }

    public PackageDescription get(String key) {
        if (!key.contains(".")) {
            return mChild.get(key);
        } else {
            return get(key.substring(0, key.indexOf(".")))
                    .get(key.substring(key.indexOf(".") + 1));
        }
    }

    public void put(String pkg) {
        if (pkg.contains(".")) {
            String first = pkg.substring(0, pkg.indexOf("."));
            if (get(first) == null) {
                put(first);
            }
            get(first).put(pkg.substring(pkg.indexOf(".") + 1));
        } else {
            this.mChild.put(pkg, new PackageDescription(pkg, this));
        }
    }

    public PackageDescription remove(String child) {
        if (child.contains(".")) {
            return this.mChild.remove(child.substring(0, child.indexOf(".")));
        } else {
            return this.mChild.remove(child);
        }
    }

    @Override
    public String toString() {
        return mName;
    }
}
