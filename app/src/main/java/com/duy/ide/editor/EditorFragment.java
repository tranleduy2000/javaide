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

package com.duy.ide.editor;

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

import com.duy.ide.EditPageContract;
import com.duy.ide.EditorControl;
import com.duy.ide.R;
import com.duy.ide.autocomplete.autocomplete.AutoCompleteProvider;
import com.duy.ide.code.CompileManager;
import com.duy.ide.editor.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.view.LockableScrollView;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.barenca.jastyle.ASFormatter;
import net.barenca.jastyle.FormatterHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by Duy on 15-Mar-17.
 * Editor fragment
 */
public class EditorFragment extends Fragment implements EditorListener, EditPageContract.View {
    private static final String TAG = "EditorFragment";
    @Nullable
    private EditorView mCodeEditor;
    @Nullable
    private LockableScrollView mScrollView;
    private FileManager mFileManager;
    private Handler handler = new Handler();
    private Dialog dialog;
    private EditPageContract.Presenter mPresenter;
    private AutoCompleteProvider autoCompleteProvider;

//    private LockableHorizontalScrollView mHorizontalScrollView;

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
    public void gotoLine(int line, int col) {
        // TODO: 19/07/2017
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
        mCodeEditor.goToLine(line);
    }

    @Override
    public void formatCode() {
        new TaskFormatCode().execute(getCode());
    }

    @Override
    public void highlightError(long startPosition, long endPosition) {
        if (mCodeEditor != null) {
            mCodeEditor.highlightError(startPosition, endPosition);
        }
    }

    @Override
    public void setCursorPosition(int endPosition) {
        if (mCodeEditor != null) {
            mCodeEditor.requestFocus();
            mCodeEditor.setSelection(endPosition);
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
        if (mCodeEditor == null) return;
        if (mCodeEditor.canUndo()) {
            mCodeEditor.undo();
        } else {
            Toast.makeText(getContext(), R.string.cant_undo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void redo() {
        if (mCodeEditor == null) return;
        if (mCodeEditor.canRedo()) {
            mCodeEditor.redo();
        } else {
            Toast.makeText(getContext(), R.string.cant_redo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void paste() {
        if (mCodeEditor != null) {
            mCodeEditor.paste();
        }
    }

    @Override
    public void copyAll() {
        if (mCodeEditor != null) {
            mCodeEditor.copyAll();
        }
    }

    @Nullable
    @Override
    public String getCode() {
        return mCodeEditor != null ? mCodeEditor.getCleanText() : null;
    }

    @Override
    public void insert(@NonNull CharSequence text) {
        if (mCodeEditor != null) {
            mCodeEditor.insert(text);
        }
    }

    public EditorView getEditor() {
        return mCodeEditor;
    }

    public void refreshCodeEditor() {
        if (mCodeEditor != null) {
            mCodeEditor.updateFromSettings();
            mCodeEditor.refresh();
        }
    }

    public String getFilePath() {
        String path = getArguments().getString(CompileManager.FILE_PATH);
        if (path == null) {
            return "";
        } else {
            return path;
        }
    }

    public void setAutoCompleteProvider(AutoCompleteProvider autoCompleteProvider) {
        if (mCodeEditor != null){
            mCodeEditor.setAutoCompleteProvider(autoCompleteProvider);
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
                String source = params[0];
                if (source == null) return null;
                source = source.replace("{", "{\n");
                ASFormatter formatter = new ASFormatter();
                Reader in = new BufferedReader(new StringReader(source));
                formatter.setJavaStyle();
                return FormatterHelper.format(in, formatter);
            } catch (Exception e) {
                //format unexpected
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && mCodeEditor != null) {
                mCodeEditor.setTextHighlighted(result);
            }
            dismissDialog();
        }
    }


}
