package com.duy.projectview.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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
    private EditText editName;

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

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        View view = layoutInflater.inflate(R.layout.dialog_new_file, null);


        editName = view.findViewById(R.id.edit_file_name);
        editName.setText(new File(fromFile).getName());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = editName.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    editName.setError(getString(R.string.enter_name));
                    return;
                }
                try {
                    File toFile = new File(toDir, name);
                    IOUtils.copy(new FileInputStream(fromFile), new FileOutputStream(toFile));
                    callback.onSuccess(toFile);
                } catch (IOException e) {
                    callback.onFailed(e);
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.setView(view);
        builder.show();

        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}
