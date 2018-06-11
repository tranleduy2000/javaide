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

/**
 * Created by Duy on 20-Jul-17.
 */

public class PrimitiveArrayConstructorDescription extends JavaSuggestItemImpl {
    private String name;

    public PrimitiveArrayConstructorDescription(String name) {
        this.name = name;
    }

    @Override
    public void onSelectThis(@NonNull IEditAreaView editorView) {
        try {
            final int length = getIncomplete().length();
            int cursor = getEditor().getCursor();
            final int start = cursor - length;

            Editable editable = editorView.getEditableText();
            editable.replace(start, cursor, name + "[]");
            editorView.setSelection(start + name.length() + 1 /*between two bracket*/);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return name + "[]";
    }

    @Override
    public char getTypeHeader() {
        return 'a';
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReturnType() {
        return "";
    }


    @Override
    public String toString() {
        return name + "[]";
    }

}
