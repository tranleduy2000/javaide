package com.duy.projectview.dialog;

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

public class DialogCopyFile extends BaseDialog {
    private final String fromFile;
    private final File toDir;
    private final ProjectFileContract.Callback callback;
    private EditText mEditName;

    public DialogCopyFile(Context context, String fromFile, File toDir, ProjectFileContract.Callback callback) {
        super(context);
        this.fromFile = fromFile;
        this.toDir = toDir;
        this.callback = callback;
    }

    @Override
    public AlertDialog show() {
        AlertDialog.Builder builder = getBuilder();
        builder.setTitle(R.string.copy_file);
        builder.setView(R.layout.dialog_new_file);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        mEditName = alertDialog.findViewById(R.id.edit_file_name);
        mEditName.setText(new File(fromFile).getName());
        alertDialog
                .findViewById(R.id.btn_ok)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String name = mEditName.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            mEditName.setError(getString(R.string.enter_name));
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

        alertDialog.findViewById(R.id.btn_cancel)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        ((TextView) alertDialog.findViewById(R.id.txt_title)).setText(R.string.copy_file);
        return alertDialog;
    }
}
