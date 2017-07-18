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

package com.duy.editor.editor;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.duy.editor.EditorControl;
import com.duy.editor.R;
import com.duy.editor.code.CompileManager;
import com.duy.editor.editor.view.EditorView;
import com.duy.editor.file.FileManager;
import com.duy.editor.view.LockableScrollView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;

import java.io.File;

/**
 * Created by Duy on 15-Mar-17.
 * Editor fragment
 */
public class EditorFragment extends Fragment implements EditorListener {
    private static final String TAG = "EditorFragment";
    private EditorView mCodeEditor;
    @Nullable
    private LockableScrollView mScrollView;
    private FileManager mFileManager;
    private Handler handler = new Handler();


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

//    private LockableHorizontalScrollView mHorizontalScrollView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editor, container, false);
        mCodeEditor = view.findViewById(R.id.code_editor);
        mScrollView = view.findViewById(R.id.vertical_scroll);
//        mHorizontalScrollView = (LockableHorizontalScrollView) view.findViewById(R.id.horizontal_scroll);

        FileManager fileManager = new FileManager(getContext());
        StringBuilder code = fileManager.fileToString(getArguments().getString(CompileManager.FILE_PATH));
        mCodeEditor.setTextHighlighted(code);

        try {
            mCodeEditor.setEditorControl((EditorControl) getActivity());
        } catch (Exception ignored) {
        }

        if (mScrollView != null) {
            mCodeEditor.setVerticalScroll(mScrollView);
            mScrollView.setScrollListener(new LockableScrollView.ScrollListener() {
                @Override
                public void onScroll(int x, int y) {
                    mCodeEditor.updateTextHighlight();
                }
            });
        }
//        if (mHorizontalScrollView != null) {
//            mCodeEditor.setHorizontalScroll(mHorizontalScrollView);
//            mHorizontalScrollView.setScrollListener(new LockableHorizontalScrollView.ScrollListener() {
//                @Override
//                public void onScroll(int x, int y) {
//                    mCodeEditor.updateTextHighlight();
//                }
//            });
//        }
//        ArrayList<InfoItem> items = PascalLibraryManager.getAllMethodDescription(SystemLibrary.class, IOLib.class, FileLib.class);
//        for (String s : KeyWord.ALL_KEY_WORD) {
//            items.add(new InfoItem(StructureType.TYPE_KEY_WORD, s));
//        }
//        mCodeEditor.setSuggestData(items);
        return view;
    }


    @Override
    public void onStop() {
        saveFile();
        if (mCodeEditor != null && getFilePath() != null) {
            Log.i(TAG, "onStop: save edit history " + getFilePath());
            mCodeEditor.saveHistory(getFilePath());
        } else {
            Log.e(TAG, "can not save edit history");
            FirebaseAnalytics.getInstance(getContext()).logEvent("can_not_save_edit_history", new Bundle());
        }

        super.onStop();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCodeEditor.updateFromSettings();
        mCodeEditor.restoreHistory(getFilePath());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void saveAs() {

    }

    @Override
    public void doFindAndReplace(@NonNull String from, @NonNull String to, boolean regex, boolean matchCase) {
        mCodeEditor.replaceAll(from, to, regex, matchCase);
    }

    @Override
    public void doFind(@NonNull String find, boolean regex, boolean wordOnly, boolean matchCase) {
        mCodeEditor.find(find, regex, wordOnly, matchCase);
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
        mCodeEditor.goToLine(line);
    }

    private Dialog dialog;

    @Override
    public void formatCode() {
        new TaskFormatCode().execute(getCode());
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

    private class TaskFormatCode extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog("Formatting...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return new Formatter().formatSource(params[0]);
            } catch (FormatterException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                mCodeEditor.setTextHighlighted(result);
            }
            dismissDialog();
        }
    }

    @Override
    public void undo() {
        if (mCodeEditor.canUndo()) {
            mCodeEditor.undo();
        } else {
            Toast.makeText(getContext(), R.string.cant_undo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void redo() {
        if (mCodeEditor.canRedo()) {
            mCodeEditor.redo();
        } else {
            Toast.makeText(getContext(), R.string.cant_redo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void paste() {
        mCodeEditor.paste();
    }

    @Override
    public void copyAll() {
        mCodeEditor.copyAll();
    }

    @NonNull
    @Override
    public String getCode() {
        return mCodeEditor.getCleanText();
    }

    @Override
    public void insert(@NonNull CharSequence text) {
        mCodeEditor.insert(text);
    }

    public EditorView getEditor() {
        return mCodeEditor;
    }


    public void refreshCodeEditor() {
        mCodeEditor.updateFromSettings();
        mCodeEditor.refresh();
    }

    public String getFilePath() {
        String path = getArguments().getString(CompileManager.FILE_PATH);
        if (path == null) {
            return "";
        } else {
            return path;
        }
    }


}
