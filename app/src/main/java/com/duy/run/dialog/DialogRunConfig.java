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

import com.duy.ide.R;
import com.duy.compile.CompileManager;
import com.duy.ide.file.FileManager;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFile;
import com.duy.project.utils.ClassUtil;

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
    private EditText mPackage;
    private SharedPreferences mPref;
    private JavaProjectFile projectFile;
    @Nullable
    private OnConfigChangeListener listener;

    public static DialogRunConfig newInstance(JavaProjectFile project) {

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
        projectFile = (JavaProjectFile) getArguments().getSerializable(CompileManager.PROJECT_FILE);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (projectFile == null) {
            return;
        }
        setupSpinnerMainClass(view, projectFile);
        mArgs = view.findViewById(R.id.edit_arg);
        mArgs.setText(mPref.getString(CompileManager.ARGS, ""));
        mPackage = view.findViewById(R.id.edit_package_name);
        mPackage.setText(projectFile.getPackageName());

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

            //check main class
            ClassFile classFile = new ClassFile(selectedItem.toString());
            String path = classFile.getPath(projectFile);
            if (!ClassUtil.hasMainFunction(new File(path))) {
                Toast.makeText(getContext(), "Can not find main function", Toast.LENGTH_SHORT).show();
            }
            projectFile.setMainClass(classFile);
            projectFile.setPackageName(mPackage.getText().toString());

            if (listener != null) listener.onConfigChange(projectFile);
            this.dismiss();
        }
    }

    private void setupSpinnerMainClass(View view, JavaProjectFile projectFile) {
        ArrayList<String> names = FileManager.listClassName(projectFile.dirJava);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, names);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mClasses = view.findViewById(R.id.spinner_main_class);
        mClasses.setAdapter(adapter);

        if (projectFile.getMainClass() != null) {
            String mainClassName = projectFile.getMainClass().getName();
            for (int i = 0; i < names.size(); i++) {
                String s = names.get(i);
                if (s.equalsIgnoreCase(mainClassName)) {
                    mClasses.setSelection(i);
                    break;
                }
            }
        }
    }

    public interface OnConfigChangeListener {
        void onConfigChange(JavaProjectFile projectFile);
    }
}
