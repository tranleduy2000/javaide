package com.duy.projectview.view.dialog;

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

import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.R;
import com.duy.projectview.ProjectFileContract;

import java.io.File;

/**
 * Created by Duy on 20-Dec-17.
 */

public class DialogNewFolder extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewFolder";

    private EditText mEditName;
    @Nullable
    private ProjectFileContract.FileActionListener listener;
    @NonNull
    private JavaProject project;
    @Nullable
    private File currentFolder;

    public static DialogNewFolder newInstance(@NonNull JavaProject project, @Nullable File currentFolder) {
        DialogNewFolder fragment = new DialogNewFolder();
        fragment.setProject(project);
        fragment.setCurrentFolder(currentFolder);
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
            File folder = new File(currentFolder, fileName);
            folder.mkdirs();
            listener.onNewFileCreated(folder);
            dismiss();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can not create new file", Toast.LENGTH_SHORT).show();
        }
    }

    public void setProject(@NonNull JavaProject project) {
        this.project = project;
    }

    public void setCurrentFolder(@Nullable File currentFolder) {
        this.currentFolder = currentFolder;
    }
}
