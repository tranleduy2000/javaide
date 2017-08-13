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

import com.duy.compile.CompileManager;
import com.duy.ide.EditPageContract;
import com.duy.ide.EditorControl;
import com.duy.ide.R;
import com.duy.ide.autocomplete.AutoCompleteProvider;
import com.duy.ide.editor.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.FileUtils;
import com.duy.ide.formatter.FormatFactory;
import com.duy.ide.view.LockableScrollView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Duy on 15-Mar-17.
 * Editor fragment
 */
public class EditorFragment extends Fragment implements EditorListener, EditPageContract.View {
    private static final String TAG = "EditorFragment";
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
        String path = getArguments().getString(CompileManager.FILE_PATH);
        StringBuilder code = fileManager.fileToString(path);
        mCodeEditor.setFileExt(FileUtils.ext(path));
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
        if (autoCompleteProvider != null) mCodeEditor.setAutoCompleteProvider(autoCompleteProvider);
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
        String filePath = getArguments().getString(CompileManager.FILE_PATH);
        if (filePath != null) {
            new JavaFormatCode(getContext(), FormatFactory.getType(new File(filePath))).execute(filePath);
        }
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
            mCodeEditor.setSelection(Math.min(endPosition, mCodeEditor.length()));
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
        this.autoCompleteProvider = autoCompleteProvider;
        if (mCodeEditor != null) {
            mCodeEditor.setAutoCompleteProvider(autoCompleteProvider);
        }
    }

    private class JavaFormatCode extends AsyncTask<String, Void, String> {
        private Exception error;
        private Context context;
        private FormatFactory.Type type;

        public JavaFormatCode(Context context, FormatFactory.Type type) {
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
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null && mCodeEditor != null) {
                int selectionEnd = mCodeEditor.getSelectionEnd();
                mCodeEditor.setTextHighlighted(result);
                if (selectionEnd > 0 && selectionEnd < mCodeEditor.length()) {
                    mCodeEditor.setSelection(selectionEnd);
                }
            }
            dismissDialog();
        }
    }


}
