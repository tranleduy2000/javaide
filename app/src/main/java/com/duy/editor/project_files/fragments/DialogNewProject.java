package com.duy.editor.project_files.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.file.FileManager;
import com.duy.editor.project_files.ProjectFile;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewProject extends AppCompatDialogFragment implements View.OnClickListener {
    private EditText editProjectName, editMainClass, editPackage;
    private Button btnCreate, btnCancel;
    @Nullable
    private OnCreateProjectListener listener;
    public static final String TAG = "DialogNewProject";

    public static DialogNewProject newInstance() {

        Bundle args = new Bundle();

        DialogNewProject fragment = new DialogNewProject();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnCreateProjectListener) getActivity();
        } catch (Exception e) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_project, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editMainClass = view.findViewById(R.id.edit_main_class);
        editPackage = view.findViewById(R.id.edit_package_name);
        editProjectName = view.findViewById(R.id.edit_project_name);
        btnCreate = view.findViewById(R.id.btn_create);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_create:
                doCreateProject();
                break;
        }
    }

    private void doCreateProject() {
        if (isOk()) {
            ProjectFile projectFile = new ProjectFile(editMainClass.getText().toString(),
                    editPackage.getText().toString(), editProjectName.getText().toString());
            try {
                File f = projectFile.create(new File(FileManager.EXTERNAL_DIR));
                if (listener != null) {
                    listener.onProjectCreated(f);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Can not create project. Error " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * check input data
     *
     * @return true if all is ok
     */
    private boolean isOk() {
        if (editProjectName.getText().toString().isEmpty()) {
            editProjectName.setError(getString(R.string.enter_name));
            return false;
        }
        if (editPackage.getText().toString().isEmpty()) {
            editPackage.setError(getString(R.string.enter_name));
            return false;
        }
        if (editMainClass.getText().toString().isEmpty()) {
            editMainClass.setError(getString(R.string.enter_name));
            return false;
        }
        return true;
    }


    public interface OnCreateProjectListener {
        void onProjectCreated(File mainClass);
    }
}
