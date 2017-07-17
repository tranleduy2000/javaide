package com.duy.run.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.code.CompileManager;
import com.duy.editor.file.FileManager;
import com.duy.project_files.ClassFile;
import com.duy.project_files.ProjectFile;

import java.io.File;
import java.util.ArrayList;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by Duy on 17-Jul-17.
 */

public class DialogRunConfig extends AppCompatDialogFragment {
    public static final String TAG = "DialogRunConfig";
    private Spinner mClasses;
    private EditText mArgs;
    private SharedPreferences mPref;
    private ProjectFile projectFile;
    @Nullable
    private OnConfigChangeListener listener;

    public static DialogRunConfig newInstance(ProjectFile project) {

        Bundle args = new Bundle();
        args.putSerializable(CompileManager.PROJECT_FILE, project);
        DialogRunConfig fragment = new DialogRunConfig();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (OnConfigChangeListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_run_config, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        projectFile = (ProjectFile) getArguments().getSerializable(CompileManager.PROJECT_FILE);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (projectFile == null) {
            return;
        }
        setupSpinnerMainClass(view, projectFile);
        mArgs = view.findViewById(R.id.edit_arg);
        mArgs.setText(mPref.getString(CompileManager.ARGS, ""));
        view.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
    }

    private void save() {
        mPref = getDefaultSharedPreferences(getContext());
        mPref.edit().putString(CompileManager.ARGS, mArgs.getText().toString()).apply();

        Object selectedItem = mClasses.getSelectedItem();
        if (selectedItem != null) {
            String mainClass = selectedItem.toString();
            String pkg = mainClass.substring(0, mainClass.lastIndexOf(".") - 1);
            String name = mainClass.substring(mainClass.indexOf(".") + 1);
            ClassFile classFile = new ClassFile(name, pkg);
            projectFile.setMainClass(classFile);
            if (listener != null) listener.onConfigChange(projectFile);
            this.dismiss();
        }
    }

    private void setupSpinnerMainClass(View view, ProjectFile projectFile) {
        String projectDir = projectFile.getProjectDir();
        File root = new File(projectDir);
        File src = new File(root, "src/main/java");
        ArrayList<String> name = FileManager.listClassName(src);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, name);
        mClasses = view.findViewById(R.id.spinner_main_class);
        mClasses.setAdapter(adapter);

    }

    public interface OnConfigChangeListener {
        void onConfigChange(ProjectFile projectFile);
    }
}
