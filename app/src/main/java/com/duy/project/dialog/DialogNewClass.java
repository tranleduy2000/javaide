package com.duy.project.dialog;

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

import com.duy.ide.R;
import com.duy.ide.editor.completion.Template;
import com.duy.project.file.java.JavaProjectFolder;
import com.duy.project.utils.ProjectFileUtil;

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
    private OnCreateFileListener listener;

    private EditText mPackage;

    private static final String KEY_PROJECT_FILE = "project_file";
    private static final String KEY_PARENT_FILE = "parent_file";
    private static final String KEY_PACKAGE = "current_package";

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
            listener = (OnCreateFileListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public static DialogNewClass newInstance(@NonNull JavaProjectFolder p,
                                             @Nullable String currentPackage,
                                             @Nullable File currentFolder) {
        Bundle args = new Bundle();
        args.putSerializable(KEY_PROJECT_FILE, p);
        args.putString(KEY_PACKAGE, currentPackage);
        args.putSerializable(KEY_PARENT_FILE, currentFolder);
        DialogNewClass fragment = new DialogNewClass();
        fragment.setArguments(args);
        return fragment;
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
        String packageName = getArguments().getString(KEY_PACKAGE);
        if (packageName == null || packageName.isEmpty()) {
            File currentFolder = (File) getArguments().getSerializable(KEY_PARENT_FILE);
            JavaProjectFolder projectFile = (JavaProjectFolder) getArguments().getSerializable(KEY_PROJECT_FILE);
            if (currentFolder != null && projectFile != null) {
                packageName = ProjectFileUtil.findPackage(projectFile.dirJava, currentFolder);
            }
        }

        mPackage.setText(packageName);
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


        Bundle arguments = getArguments();
        JavaProjectFolder projectFile = (JavaProjectFolder) arguments.getSerializable(KEY_PROJECT_FILE);

        if (projectFile != null) {
            File classf = JavaProjectFolder.createClass(projectFile, currentPackage, className, content);
            if (listener != null) {
                listener.onFileCreated(classf);
                Toast.makeText(getContext(), "success!", Toast.LENGTH_SHORT).show();
                this.dismiss();
            }
        }
    }

    public interface OnCreateFileListener {
        void onFileCreated(File classF);
    }
}
