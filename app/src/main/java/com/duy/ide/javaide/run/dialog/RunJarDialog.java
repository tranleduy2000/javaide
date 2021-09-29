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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.file.explorer.FileExplorerActivity;
import com.duy.ide.R;

import java.io.File;

public class RunJarDialog extends AppCompatDialogFragment implements View.OnClickListener {

    private static final int RC_SELECT_JAR = 847;
    private static final int RC_ADD_CLASSPATH = 82;
    private EditText mEditJarPath;
    private EditText mEditClassPath;

    public static RunJarDialog newInstance() {

        Bundle args = new Bundle();

        RunJarDialog fragment = new RunJarDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_run_jar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditJarPath = view.findViewById(R.id.edit_jar_path);
        mEditClassPath = view.findViewById(R.id.edit_classpath);

        view.findViewById(R.id.btn_select_jar).setOnClickListener(this);
        view.findViewById(R.id.btn_add_classpath).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_select_jar:
                selectFile(RC_SELECT_JAR);
                break;
            case R.id.btn_add_classpath:
                selectFile(RC_ADD_CLASSPATH);
                break;
            case R.id.btn_ok:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RC_ADD_CLASSPATH: {
                final String filePath = FileExplorerActivity.getFile(data);
                if (filePath == null) {
                    return;
                }
                File jar = new File(filePath);
                if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
                    Toast.makeText(getContext(), "The file is not jar file", Toast.LENGTH_SHORT).show();
                    return;
                }
                mEditClassPath.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!(mEditClassPath.length() == 0)) {
                            mEditClassPath.append(File.separator);
                        }
                        mEditClassPath.append(filePath);
                    }
                });
                break;
            }
            case RC_SELECT_JAR: {
                final String filePath = FileExplorerActivity.getFile(data);
                if (filePath == null) {
                    return;
                }
                File jar = new File(filePath);
                if (!jar.isFile() || !jar.getName().endsWith(".jar")) {
                    Toast.makeText(getContext(), "The file is not jar file", Toast.LENGTH_SHORT).show();
                    return;
                }
                mEditJarPath.post(new Runnable() {
                    @Override
                    public void run() {
                        mEditJarPath.setText(filePath);
                    }
                });
                break;
            }
        }
    }

    private void selectFile(int requestCode) {
        Intent it = new Intent(getContext(), FileExplorerActivity.class);
        it.putExtra(FileExplorerActivity.EXTRA_MODE, FileExplorerActivity.MODE_PICK_FILE);
        startActivityForResult(it, requestCode);
    }
}
