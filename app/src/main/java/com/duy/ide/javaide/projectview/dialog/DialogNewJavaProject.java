/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.projectview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.ide.R;
import com.duy.ide.javaide.utils.DLog;
import com.duy.ide.javaide.editor.autocomplete.internal.PatternFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewJavaProject extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewProject";
    @Nullable
    private OnCreateProjectListener listener;
    private EditText mEditAppName, mEditMainClass, mEditPackage;

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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_java_project, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditMainClass = view.findViewById(R.id.edit_main_class);
        mEditPackage = view.findViewById(R.id.edit_package_name);
        mEditAppName = view.findViewById(R.id.edit_project_name);

        Button btnCreate = view.findViewById(R.id.btn_create);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
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
                if (doCreateProject()) {
                    dismiss();
                }
                break;
        }
    }

    private boolean doCreateProject() {
        if (isValid()) {
            try {
                File dirToCreate = new File(Environment.getSdkAppDir().getPath());
                String projectName = mEditAppName.getText().toString();

                JavaProjectManager manager = new JavaProjectManager(getContext());
                JavaProject project = manager.createNewProject(dirToCreate, projectName);

                String mainClassName = mEditMainClass.getText().toString();
                String packageName = mEditPackage.getText().toString();

                project.createMainClass(packageName, mainClassName);

                if (listener != null) {
                    listener.onProjectCreated(project);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Can not create project. Error " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        return false;
    }

    /**
     * check input data
     *
     * @return true if package name, class name,... are valid.
     */
    private boolean isValid() {
        //check project name
        if (mEditAppName.getText().toString().isEmpty()) {
            if (DLog.DEBUG) DLog.d(TAG, "isValid: app name");
            mEditAppName.setError(getString(R.string.enter_name));
            return false;
        }
        if (!mEditAppName.getText().toString().matches(PatternFactory.IDENTIFIER.pattern())) {
            mEditAppName.setError("Only accept A-Za-z0-9_");
            return false;
        }

        //check package name
        String packageName = mEditPackage.getText().toString();
        if (mEditPackage.getText().toString().isEmpty()) {
            mEditPackage.setError(getString(R.string.enter_package));
            return false;
        }

        if (!packageName.matches(PatternFactory.PACKAGE_NAME.pattern())) {
            mEditPackage.setError("Invalid package name");
            return false;
        }

        //check class name
        String mainClassName = mEditMainClass.getText().toString();
        if (mainClassName.isEmpty()) {
            mEditMainClass.setError(getString(R.string.enter_name));
            return false;
        }
        if (!mainClassName.matches(PatternFactory.IDENTIFIER.pattern())) {
            mEditMainClass.setError("Invalid name");
            return false;
        }

        return true;
    }


    public interface OnCreateProjectListener {
        void onProjectCreated(JavaProject projectFile);
    }
}
