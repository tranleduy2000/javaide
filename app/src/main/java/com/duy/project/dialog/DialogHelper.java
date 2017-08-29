package com.duy.project.dialog;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.annotations.NonNull;
import com.duy.ide.R;
import com.duy.ide.file.FileManager;

import java.io.File;

/**
 * Created by Duy on 28-Aug-17.
 */

public class DialogHelper {
    public static void createNewFolder(@NonNull Context context, @NonNull File parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.new_folder);
        builder.setView(R.layout.dialog_new_xml);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText mEditName = alertDialog.findViewById(R.id.edit_class_name);
        alertDialog.findViewById(R.id.btn_create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = mEditName.getText().toString();
                if (fileName.isEmpty()) {
                    mEditName.setError(getString(R.string.enter_name));
                    return;
                }
                try {
                    File parent = (File) getArguments().getSerializable(KEY_PARENT_FILE);
                    File xml = new File(parent, fileName);
                    if (!parent.exists()) parent.mkdirs();
                    xml.createNewFile();
                    String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
                    FileManager.saveFile(xml, header);
                    if (listener != null) {
                        listener.onFileCreated(xml);
                    }
                    dismiss();
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Can not create new file", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
