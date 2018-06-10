package com.duy.projectview.dialog;

import android.content.Context;

import com.duy.projectview.ProjectFileContract;

import java.io.File;

/**
 * Created by Duy on 13-Aug-17.
 */

public class DialogManager {
    public static void showDialogCopyFile(final String fromFile, final File toDir,
                                          final Context context, final ProjectFileContract.Callback callback) {
        new DialogCopyFile(context, fromFile, toDir, callback).show();
    }
}
