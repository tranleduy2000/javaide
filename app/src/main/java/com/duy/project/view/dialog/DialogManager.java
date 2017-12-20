package com.duy.project.view.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.duy.ide.R;
import com.duy.project.view.fragments.FolderStructureFragment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 13-Aug-17.
 */

public class DialogManager {
    public static void showDialogCopyFile(final String selectedFile, final File parentFile,
                                          final Context context, final FolderStructureFragment.Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.copy_file);
        builder.setView(R.layout.dialog_new_file);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText editText = (EditText) alertDialog.findViewById(R.id.edit_file_name);
        editText.setText(new File(selectedFile).getName());
        alertDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    editText.setError(context.getString(R.string.enter_name));
                    return;
                }
                File file = new File(parentFile, name);
                try {
                    FileUtils.copyFile(new File(selectedFile), file);
                    callback.onSuccess(file);
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
