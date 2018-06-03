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
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.Template;
import com.duy.ide.R;
import com.duy.projectview.ProjectFileContract;
import com.duy.projectview.utils.ProjectFileUtil;

import java.io.File;
import java.lang.reflect.Modifier;

import static android.view.ViewGroup.LayoutParams;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewClass extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewClass";
    private EditText mEditName;
    private Spinner mKind;
    private RadioGroup mModifiers, mVisibility;
    @Nullable
    private ProjectFileContract.FileActionListener listener;
    private EditText mPackage;
    @NonNull
    private JavaProject project;
    @Nullable
    private String currentPackage;
    @Nullable
    private File currentFolder;

    public static DialogNewClass newInstance(@NonNull JavaProject project,
                                             @Nullable String currentPackage,
                                             @Nullable File currentFolder) {
        DialogNewClass fragment = new DialogNewClass();
        fragment.setProject(project);
        fragment.setCurrentFolder(currentFolder);
        fragment.setCurrentPackage(currentPackage);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_class, container, false);
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
                createNewClass();
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditName = view.findViewById(R.id.edit_class_name);
        mKind = view.findViewById(R.id.spinner_kind);
        mModifiers = view.findViewById(R.id.modifiers);
        mPackage = view.findViewById(R.id.edit_package_name);
        mVisibility = view.findViewById(R.id.visibility);

        initPackage();


        view.findViewById(R.id.btn_create).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    private void initPackage() {
        if (currentPackage == null || currentPackage.isEmpty()) {
            if (currentFolder != null) {
                currentPackage = ProjectFileUtil.findPackage(project.getJavaSrcDirs().get(0), currentFolder);
            }
        }

        mPackage.setText(currentPackage);
    }

    private void createNewClass() {
        String className = mEditName.getText().toString();
        if (className.isEmpty()) {
            mEditName.setError(getString(R.string.enter_name));
            return;
        }
        String currentPackage = mPackage.getText().toString();
        if (currentPackage.trim().isEmpty()) {
            mPackage.setError(getString(R.string.enter_package));
            return;
        }

        int visibility = mVisibility.getCheckedRadioButtonId() == R.id.rad_public ? Modifier.PUBLIC : Modifier.PRIVATE;
        int checkedRadioButtonId = mModifiers.getCheckedRadioButtonId();
        int modifier = 0;
        if (checkedRadioButtonId == R.id.rad_abstract) {
            modifier = Modifier.ABSTRACT;
        } else if (checkedRadioButtonId == R.id.rad_final) {
            modifier = Modifier.FINAL;
        }
        int kind = mKind.getSelectedItemPosition();
        String content = Template.createJava(currentPackage, className, kind, visibility, modifier, false);


        File classf = project.createClass(currentPackage, className, content);
        if (listener != null) {
            listener.onNewFileCreated(classf);
            Toast.makeText(getContext(), "success!", Toast.LENGTH_SHORT).show();
            this.dismiss();
        }
    }

    public void setProject(@NonNull JavaProject project) {
        this.project = project;
    }

    public void setCurrentFolder(@Nullable File currentFolder) {
        this.currentFolder = currentFolder;
    }

    public void setCurrentPackage(@Nullable String currentPackage) {
        this.currentPackage = currentPackage;
    }
}
