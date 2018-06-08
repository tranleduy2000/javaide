package com.duy.projectview.view.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.duy.ide.R;
import com.duy.projectview.ProjectFileContract;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Duy on 13-Aug-17.
 */

public class DialogManager {
    public static void showDialogCopyFile(final String fromFile, final File toDir,
                                          final Context context, final ProjectFileContract.Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.copy_file);
        builder.setView(R.layout.dialog_new_file);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText editText = alertDialog.findViewById(R.id.edit_file_name);
        editText.setText(new File(fromFile).getName());
        alertDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    editText.setError(context.getString(R.string.enter_name));
                    return;
                }
                try {
                    File toFile = new File(toDir, name);
                    IOUtils.copy(new FileInputStream(fromFile), new FileOutputStream(toFile));
                    callback.onSuccess(toFile);
                } catch (IOException e) {
                    callback.onFailed(e);
                }
                alertDialog.cancel();
            }
        });
        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        ((TextView) alertDialog.findViewById(R.id.txt_title)).setText(R.string.copy_file);

    }
}
