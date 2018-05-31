package com.duy.project.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.project.ProjectFileContract;
import com.duy.project.file.java.JavaProject;

import java.io.File;

/**
 * Created by Duy on 20-Dec-17.
 */

public class DialogNewFolder extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewFolder";

    private static final String KEY_PROJECT_FILE = "project_file";
    private static final String KEY_PARENT_FILE = "parent_file";
    private EditText mEditName;
    @Nullable
    private ProjectFileContract.FileActionListener listener;

    public static DialogNewFolder newInstance(@NonNull JavaProject p, @Nullable File currentFolder) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PROJECT_FILE, p);
        args.putSerializable(KEY_PARENT_FILE, currentFolder);
        DialogNewFolder fragment = new DialogNewFolder();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_folder, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ProjectFileContract.FileActionListener) getActivity();
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
                createNewFolder();
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


    private void createNewFolder() {
        String fileName = mEditName.getText().toString();
        if (fileName.isEmpty()) {
            mEditName.setError(getString(R.string.enter_name));
            return;
        }
        try {
            File parent = (File) getArguments().getSerializable(KEY_PARENT_FILE);
            File folder = new File(parent, fileName);
            if (!parent.exists()) {
                parent.mkdirs();
            }
            folder.mkdir();
            listener.onNewFileCreated(folder);
            dismiss();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can not create new file", Toast.LENGTH_SHORT).show();
        }
    }
}
