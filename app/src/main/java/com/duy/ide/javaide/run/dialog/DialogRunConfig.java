package com.duy.ide.javaide.run.dialog;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.android.compiler.project.ClassFile;
import com.duy.android.compiler.project.ClassUtil;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.CompileManager;
import com.duy.ide.R;
import com.duy.ide.javaide.run.activities.ExecuteActivity;

import java.io.File;

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
    private JavaProject projectFile;
    @Nullable
    private OnConfigChangeListener listener;


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
        projectFile = (JavaProject) getArguments().getSerializable(ExecuteActivity.DEX_FILE);
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
            projectFile.setPackageName(mPackage.getText().toString());

            if (listener != null) listener.onConfigChange(projectFile);
            this.dismiss();
        }
    }

    private void setupSpinnerMainClass(View view, JavaProject projectFile) {

    }

    public interface OnConfigChangeListener {
        void onConfigChange(JavaProject projectFile);
    }
}
