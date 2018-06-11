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

package com.duy.ide.javaide.projectview.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;


public abstract class BaseDialog {
    private Context context;

    public BaseDialog(Context context) {
        this.context = context;
    }

    public abstract AlertDialog show();

    protected AlertDialog.Builder getBuilder() {
        return new AlertDialog.Builder(context);
    }

    protected LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(context);
    }

    protected Context getContext() {
        return context;
    }

    protected CharSequence getString(int id) {
        return getContext().getString(id);
    }

}
