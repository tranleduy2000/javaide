package com.duy.editor.project_files.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.editor.completion.Template;
import com.duy.editor.file.FileManager;
import com.duy.editor.project_files.ProjectFile;

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
    private RadioGroup mModifiers;
    private View root;
    @Nullable
    private OnCreateClassListener listener;

    public static DialogNewClass newInstance(ProjectFile p, String currentPackage) {
        Bundle args = new Bundle();
        args.putSerializable("project_file", p);
        args.putString("current_package", currentPackage);
        DialogNewClass fragment = new DialogNewClass();
        fragment.setArguments(args);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root = view;
        mEditName = view.findViewById(R.id.edit_class_name);
        mKind = view.findViewById(R.id.spinner_kind);
        mModifiers = view.findViewById(R.id.modifiers);
        view.findViewById(R.id.btn_create).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
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

    private void createNewClass() {
        String className = mEditName.getText().toString();
        if (className.isEmpty()) {
            mEditName.setError(getString(R.string.enter_name));
            return;
        }
        int visibility = mKind.getSelectedItemPosition() == 0 ? Modifier.PUBLIC : Modifier.PRIVATE;
        int checkedRadioButtonId = mModifiers.getCheckedRadioButtonId();
        int modifier = 0;
        if (checkedRadioButtonId == R.id.rad_abstract) {
            modifier = Modifier.ABSTRACT;
        } else if (checkedRadioButtonId == R.id.rad_final) {
            modifier = Modifier.FINAL;
        }
        String content = Template.createJava(className, visibility, modifier);
        Bundle arguments = getArguments();
        ProjectFile project_file = (ProjectFile) arguments.getSerializable("project_file");
        String currentPackage = arguments.getString("current_package");
        currentPackage = currentPackage.replace(".", "/");
        File file = new File(project_file.getRootDir(), "src/main/java/" + currentPackage);
        if (!file.exists()) file.mkdirs();
        File classf = new File(file, className);
        FileManager.saveFile(classf, content);

        if (listener != null) {
            listener.onClassCreated(classf);
            Toast.makeText(getContext(), "success!", Toast.LENGTH_SHORT).show();
            this.dismiss();
        }
    }

    public interface OnCreateClassListener {
        void onClassCreated(File classF);
    }
}
