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
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.duy.ide.R;

import java.io.File;

public class DialogDeleteFile extends BaseDialog {

    private File toBeDeleted;
    private OnFileDeletedListener listener;

    public DialogDeleteFile(Context context, File toBeDeleted, DialogDeleteFile.OnFileDeletedListener listener) {
        super(context);
        this.toBeDeleted = toBeDeleted;
        this.listener = listener;
    }

    @Override
    public AlertDialog show() {
        AlertDialog.Builder builder = getBuilder();
        builder.setMessage(getString(R.string.remove_file_msg) + " " + toBeDeleted.getName());
        builder.setTitle(R.string.delete_file);
        builder.setIcon(R.drawable.ic_delete_forever_white_24dp);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    toBeDeleted.delete();
                    listener.onDeleteSuccess(toBeDeleted);
                } catch (Exception e) {
                    listener.onDeleteFailed(toBeDeleted, e);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        return alertDialog;
    }

    public interface OnFileDeletedListener {
        /**
         * the files before, not exist
         */
        void onDeleteSuccess(File deleted);

        void onDeleteFailed(File deleted, Exception e);
    }
}
