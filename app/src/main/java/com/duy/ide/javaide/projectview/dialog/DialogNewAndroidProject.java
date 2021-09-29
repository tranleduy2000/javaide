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
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.AndroidProjectManager;
import com.duy.ide.BuildConfig;
import com.duy.ide.R;
import com.duy.ide.javaide.editor.autocomplete.internal.Patterns;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewAndroidProject extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewAndroidProject";
    private EditText editAppName, editPackage;
    private Button btnCreate, btnCancel;
    @Nullable
    private DialogNewJavaProject.OnCreateProjectListener listener;
    private EditText mActivityName, layoutName;
    private CheckBox mAppCompat;

    public static DialogNewAndroidProject newInstance() {

        Bundle args = new Bundle();

        DialogNewAndroidProject fragment = new DialogNewAndroidProject();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogNewJavaProject.OnCreateProjectListener) getActivity();
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
        return inflater.inflate(R.layout.dialog_new_android_project, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editPackage = view.findViewById(R.id.edit_package_name);
        editAppName = view.findViewById(R.id.edit_project_name);
        btnCreate = view.findViewById(R.id.btn_create);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreate.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        mActivityName = view.findViewById(R.id.edit_activity_name);
        layoutName = view.findViewById(R.id.edit_layout_name);
        mAppCompat = view.findViewById(R.id.backwards_compatibility);
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
            ///create new android project
            String packageName = editPackage.getText().toString();
            String activityName = mActivityName.getText().toString();
            String mainLayoutName = layoutName.getText().toString();
            String appName = editAppName.getText().toString();
            String projectName = appName.replaceAll("\\s+", "");
            boolean useAppCompat = BuildConfig.DEBUG;
            try {
                AndroidProjectManager projectManager = new AndroidProjectManager(getContext());
                AndroidAppProject project = projectManager.createNewProject(getContext(),
                        Environment.getSdkAppDir(),
                        projectName,
                        packageName,
                        activityName,
                        mainLayoutName,
                        appName, useAppCompat);
                if (listener != null) listener.onProjectCreated(project);
                this.dismiss();
            } catch (Exception e) {
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
        //check app name
        if (editAppName.getText().toString().isEmpty()) {
            editAppName.setError(getString(R.string.enter_name));
            return false;
        }
        String packageName = editPackage.getText().toString();
        if (packageName.isEmpty()) {
            editPackage.setError(getString(R.string.enter_package));
            return false;
        }
        if (!packageName.contains(".")) {
            editPackage.setError("Invalid package name: The package name must be least one '.' separator");
            return false;
        }
        if (!Patterns.PACKAGE_NAME.matcher(packageName).find()) {
            editPackage.setError("Invalid package name");
            return false;
        }

        //check activity name
        String activityName = this.mActivityName.getText().toString();
        if (activityName.isEmpty()) {
            this.mActivityName.setError(getString(R.string.enter_name));
            return false;
        }
        if (!Patterns.IDENTIFIER.matcher(activityName).find()) {
            this.mActivityName.setText("Invalid name");
            return false;
        }

        //check layout name
        if (layoutName.getText().toString().isEmpty()) {
            layoutName.setError(getString(R.string.enter_name));
            return false;
        }
        if (!Patterns.IDENTIFIER.matcher(layoutName.getText().toString()).find()) {
            layoutName.setText("Invalid name");
            return false;
        }

        return true;
    }

}
