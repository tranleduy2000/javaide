package com.duy.projectview.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.javaide.autocomplete.Patterns;
import com.duy.ide.file.FileManager;
import com.duy.android.compiler.file.JavaProject;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewJavaProject extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewProject";
    private EditText editAppName, editMainClass, editPackage;
    private Button btnCreate, btnCancel;
    @Nullable
    private OnCreateProjectListener listener;

    public static DialogNewJavaProject newInstance() {

        Bundle args = new Bundle();

        DialogNewJavaProject fragment = new DialogNewJavaProject();
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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_java_project, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editMainClass = view.findViewById(R.id.edit_main_class);
        editPackage = view.findViewById(R.id.edit_package_name);
        editAppName = view.findViewById(R.id.edit_project_name);
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
            File root = new File(FileManager.EXTERNAL_DIR);
            String mainClassName = editPackage.getText() + "." + editMainClass.getText();
            String projectName = editAppName.getText().toString();
            String packageName = editPackage.getText().toString();
            JavaProject projectFile = new JavaProject(root, mainClassName, packageName);
            try {
                projectFile.createMainClass();
                if (listener != null) {
                    listener.onProjectCreated(projectFile);
                }
                this.dismiss();
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
        //check project name
        if (editAppName.getText().toString().isEmpty()) {
            editAppName.setError(getString(R.string.enter_name));
            return false;
        }

        //check package name
        String packageName = editPackage.getText().toString();
        if (editPackage.getText().toString().isEmpty()) {
            editPackage.setError(getString(R.string.enter_package));
            return false;
        }
        if (!Patterns.PACKAGE_NAME.matcher(packageName).find()) {
            editPackage.setError("Invalid package name");
            return false;
        }

        //check class name
        String mainClasName = editMainClass.getText().toString();
        if (mainClasName.isEmpty()) {
            editMainClass.setError(getString(R.string.enter_name));
            return false;
        }
        if (!Patterns.RE_IDENTIFIER.matcher(mainClasName).find()) {
            this.editMainClass.setText("Invalid name");
            return false;
        }

        return true;
    }


    public interface OnCreateProjectListener {
        void onProjectCreated(JavaProject projectFile);
    }
}
