/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.java.editor.code;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duy.android.compiler.utils.IOUtils;
import com.duy.ide.R;
import com.duy.ide.java.CompileManager;
import com.duy.ide.java.EditPageContract;
import com.duy.ide.java.editor.code.view.EditorView;
import com.duy.ide.java.file.FileManager;
import com.duy.ide.javaide.autocomplete.JavaAutoCompleteProvider;
import com.duy.ide.javaide.formatter.FormatFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Duy on 15-Mar-17.
 * Editor fragment
 */
public class EditorFragment extends Fragment implements EditorListener, EditPageContract.SourceView {
    private static final String TAG = "EditorFragment";
    private EditorView mCodeEditor;
    private FileManager mFileManager;
    private Dialog dialog;
    private EditPageContract.Presenter mPresenter;
    private JavaAutoCompleteProvider autoCompleteProvider;


    public static EditorFragment newInstance(String filePath) {
        EditorFragment editorFragment = new EditorFragment();
        Bundle bundle = new Bundle();
        bundle.putString(CompileManager.FILE_PATH, filePath);
        editorFragment.setArguments(bundle);
        return editorFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileManager = new FileManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_java_editor, container, false);
        mCodeEditor = view.findViewById(R.id.code_editor);
        String path = getArguments().getString(CompileManager.FILE_PATH);
        try {
            String code = IOUtils.toStringAndClose(new File(path));
            mCodeEditor.setText(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (autoCompleteProvider != null) {
            mCodeEditor.setAutoCompleteProvider(autoCompleteProvider);
        }
    }

    @Override
    public void onStop() {
        saveFile();
        if (mCodeEditor != null && getFilePath() != null) {
//            mCodeEditor.saveHistory(getFilePath());
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mCodeEditor.restoreHistory(getFilePath());
    }

    @Override
    public void gotoLine(int line, int col) {
    }

    @Override
    public void display(String src) {
        if (mCodeEditor != null) {
            mCodeEditor.setText(src);
        }
    }

    @Override
    public void display(File src) {
        try {
            StringBuilder srcStr = FileManager.streamToString(new FileInputStream(src));
            if (mCodeEditor != null) {
                mCodeEditor.setText(srcStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPresenter(EditPageContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void saveFile() {
        if (mCodeEditor == null) return;
        String filePath = getArguments().getString(CompileManager.FILE_PATH);
        boolean result;
        if (filePath != null) {
            try {
                String code = getCode();
                result = FileManager.saveFile(filePath, code);
                if (result) {
                    //do some thing
                } else {
                    Toast.makeText(getContext(), getString(R.string.can_not_save_file) + " " + (new File(filePath).getName()),
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void goToLine(int line) {
//        mCodeEditor.goToLine(line);
    }

    @Override
    public void formatCode() {
        String filePath = getArguments().getString(CompileManager.FILE_PATH);
        if (filePath != null) {
            new FormatSource(getContext(), FormatFactory.getType(new File(filePath))).execute(getCode());
        }
    }

    @Override
    public File getCurrentFile() {
        String filePath = getArguments().getString(CompileManager.FILE_PATH);
        if (filePath != null) {
            return new File(filePath);
        } else {
            return null;
        }
    }

    private void showDialog(String msg) {
        dismissDialog();
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(msg);
        progressDialog.show();
        this.dialog = progressDialog;
    }

    private void dismissDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    @Override
    public void undo() {
        if (mCodeEditor == null) {
            return;
        }
        mCodeEditor.undo();
    }

    @Override
    public void redo() {
        if (mCodeEditor == null) {
            return;
        }
        mCodeEditor.redo();
    }

    @Nullable
    @Override
    public String getCode() {
        return mCodeEditor != null ? mCodeEditor.getText().toString() : null;
    }

    @Override
    public void insert(@NonNull CharSequence text) {
        mCodeEditor.insert(text);
    }

    public EditorView getEditor() {
        return mCodeEditor;
    }

    public void refreshCodeEditor() {

    }

    public String getFilePath() {
        String path = getArguments().getString(CompileManager.FILE_PATH);
        if (path == null) {
            return "";
        } else {
            return path;
        }
    }

    public void setAutoCompleteProvider(JavaAutoCompleteProvider autoCompleteProvider) {
        this.autoCompleteProvider = autoCompleteProvider;
        if (mCodeEditor != null) {
            mCodeEditor.setAutoCompleteProvider(autoCompleteProvider);
        }
    }

    private class FormatSource extends AsyncTask<String, Void, String> {
        private Exception error;
        private Context context;
        private FormatFactory.Type type;

        public FormatSource(Context context, FormatFactory.Type type) {
            this.context = context;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog("Formatting...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String src = params[0];
                if (src == null) return null;
                return FormatFactory.format(context, src, type);
            } catch (Exception e) {
                //format unexpected
                error = e;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && mCodeEditor != null) {
                int selectionEnd = mCodeEditor.getSelectionEnd();
                if (selectionEnd > 0 && selectionEnd < mCodeEditor.length()) {
                    mCodeEditor.setSelection(selectionEnd);
                }
            }
            dismissDialog();
        }
    }


}
