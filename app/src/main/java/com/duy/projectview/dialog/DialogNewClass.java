package com.duy.projectview.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.R;
import com.duy.ide.javaide.editor.autocomplete.autocomplete.PatternFactory;
import com.duy.projectview.ProjectFileContract;
import com.duy.projectview.utils.ProjectFileUtil;
import com.squareup.javawriter.JavaWriter;

import java.io.File;
import java.io.StringWriter;
import java.util.Set;

import javax.lang.model.element.Modifier;

import static android.view.ViewGroup.LayoutParams;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewClass extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewClass";
    private EditText mEditName, mPackage;
    private Spinner mKind;
    private RadioGroup mModifiers, mVisibility;
    private CheckBox mCreateMainFunc;

    private ProjectFileContract.FileActionListener listener;
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
        mCreateMainFunc = view.findViewById(R.id.ckb_create_main_func);

        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);

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
        if (!className.matches(PatternFactory.IDENTIFIER.pattern())) {
            mEditName.setError(getString(R.string.invalid_name));
            return;
        }
        String currentPackage = mPackage.getText().toString();
        if (currentPackage.trim().isEmpty()) {
            mPackage.setError(getString(R.string.enter_package));
            return;
        }
        if (!currentPackage.matches(PatternFactory.PACKAGE_NAME.pattern())) {
            mPackage.setError(getString(R.string.invalid_name));
            return;
        }

        Set<javax.lang.model.element.Modifier> modifiers = new ArraySet<>();
        if ((mVisibility.getCheckedRadioButtonId() == R.id.rad_public)) {
            modifiers.add(javax.lang.model.element.Modifier.PUBLIC);
        }

        int checkedRadioButtonId = mModifiers.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.rad_abstract) {
            modifiers.add(javax.lang.model.element.Modifier.ABSTRACT);
        } else if (checkedRadioButtonId == R.id.rad_final) {
            modifiers.add(javax.lang.model.element.Modifier.FINAL);
        }
        String kind = mKind.getSelectedItem().toString();
        try {
            StringWriter out = new StringWriter();
            JavaWriter writer = new JavaWriter(out);
            writer.emitPackage(currentPackage)
                    .beginType(className, kind, modifiers)
                    .emitEmptyLine();
            if (mCreateMainFunc.isChecked()) {
                //public static void main
                modifiers.clear();
                modifiers.add(Modifier.PUBLIC);
                modifiers.add(Modifier.STATIC);
                writer.beginMethod("void", "main", modifiers, "String[]", "args")
                        .emitEmptyLine()
                        .endMethod()
                        .emitEmptyLine();
            }
            writer.endType();
            writer.close();

            String content = out.toString();
            File clazz = project.createClass(currentPackage, className, content);
            if (listener != null) {
                listener.onNewFileCreated(clazz);
                Toast.makeText(getContext(), "success!", Toast.LENGTH_SHORT).show();
                this.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
