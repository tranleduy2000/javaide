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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.duy.android.compiler.project.ClassFile;
import com.duy.android.compiler.project.ClassUtil;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.R;
import com.duy.ide.javaide.run.activities.ExecuteActivity;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by Duy on 17-Jul-17.
 */

public class DialogRunConfig extends AppCompatDialogFragment {
    public static final String TAG = "DialogRunConfig";
    public static final String ARGS = "program_args";
    private Spinner mClasses;
    private EditText mArgs;
    private EditText mPackage;
    private SharedPreferences mPref;
    private JavaProject mProject;
    @Nullable
    private OnConfigChangeListener listener;

    public static DialogRunConfig newInstance(JavaProject project) {

        DialogRunConfig fragment = new DialogRunConfig();
        fragment.mProject = project;
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
        mProject = (JavaProject) getArguments().getSerializable(ExecuteActivity.DEX_FILE);
        mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (mProject == null) {
            return;
        }
        setupSpinnerMainClass(view, mProject);
        mArgs = view.findViewById(R.id.edit_arg);
        mArgs.setText(mPref.getString(ARGS, ""));
        mPackage = view.findViewById(R.id.edit_package_name);
        mPackage.setText(mProject.getPackageName());

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
        mPref.edit().putString(ARGS, mArgs.getText().toString()).apply();

        Object selectedItem = mClasses.getSelectedItem();
        if (selectedItem != null) {

            //check main class
            ClassFile classFile = new ClassFile(selectedItem.toString());
            String path = classFile.getPath(mProject);
            if (!ClassUtil.hasMainFunction(new File(path))) {
                Toast.makeText(getContext(), "Can not find main function", Toast.LENGTH_SHORT).show();
            }
            mProject.setPackageName(mPackage.getText().toString());

            if (listener != null) listener.onConfigChange(mProject);
            this.dismiss();
        }
    }

    private void setupSpinnerMainClass(View view, JavaProject projectFile) {
        ArrayList<String> names = listClassName(projectFile.getJavaSrcDirs().get(0));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, names);
        adapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);
        mClasses = view.findViewById(R.id.spinner_main_class);
        mClasses.setAdapter(adapter);
    }

    public ArrayList<String> listClassName(File src) {
        if (!src.exists()) return new ArrayList<>();

        String[] exts = new String[]{"java"};
        Collection<File> files = FileUtils.listFiles(src, exts, true);

        ArrayList<String> classes = new ArrayList<>();
        String srcPath = src.getPath();
        for (File file : files) {
            String javaPath = file.getPath();
            javaPath = javaPath.substring(srcPath.length() + 1, javaPath.length() - 5); //.java
            javaPath = javaPath.replace(File.separator, ".");
            classes.add(javaPath);
        }
        return classes;
    }


    public interface OnConfigChangeListener {
        void onConfigChange(JavaProject projectFile);
    }
}
