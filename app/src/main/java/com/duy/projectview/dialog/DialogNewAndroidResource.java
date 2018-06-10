package com.duy.projectview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.android.compiler.utils.IOUtils;
import com.duy.ide.R;
import com.duy.ide.javaide.FileChangeListener;
import com.duy.ide.javaide.editor.autocomplete.autocomplete.PatternFactory;

import java.io.File;

import static android.view.ViewGroup.LayoutParams;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewAndroidResource extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewClass";
    private EditText mEditName;
    @Nullable
    private FileChangeListener listener;
    private File currentFolder;

    public static DialogNewAndroidResource newInstance(@NonNull File currentFolder) {
        DialogNewAndroidResource fragment = new DialogNewAndroidResource();
        fragment.currentFolder = currentFolder;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_xml, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FileChangeListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_create:
                createNewFile();
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditName = view.findViewById(R.id.edit_class_name);
        view.findViewById(R.id.btn_create).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }


    private void createNewFile() {
        String fileName = mEditName.getText().toString();
        if (fileName.isEmpty()) {
            mEditName.setError(getString(R.string.enter_name));
            return;
        }
        if (!fileName.matches(PatternFactory.FILE_NAME.pattern())) {
            mEditName.setError(getString(R.string.invalid_name));
            return;
        }

        try {
            if (!fileName.endsWith(".xml")) {
                fileName += ".xml";
            }
            File xmlFile = new File(currentFolder, fileName);
            xmlFile.getParentFile().mkdirs();

            String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
            if (currentFolder.getName().matches("^color(-v[0-9]+)?")) {
                content += "<selector>\n</selector>";
            } else if (currentFolder.getName().matches("^menu(-v[0-9]+)?")) {
                content += "<menu " +
                        "xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                        "\n" +
                        "</menu>";
            } else if (currentFolder.getName().matches("^values(-v[0-9]+)?")) {
                content += "<resources>\n</resources>";
            } else if (currentFolder.getName().matches("^layout(-v[0-9]+)?")) {
                content += "<LinearLayout\n" +
                        "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\">" +
                        "\n" +
                        "</LinearLayout>";
            }

            IOUtils.writeAndClose(content, xmlFile);
            if (listener != null) {
                listener.onFileCreated(xmlFile);
            }
            dismiss();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can not create new file", Toast.LENGTH_SHORT).show();
        }
    }

}
