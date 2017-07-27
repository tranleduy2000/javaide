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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.compile.external.CommandManager;
import com.duy.ide.MenuEditor;
import com.duy.ide.R;
import com.duy.ide.autocomplete.AutoCompleteService;
import com.duy.ide.autocomplete.autocomplete.AutoCompleteProvider;
import com.duy.ide.autocomplete.model.Description;
import com.duy.ide.code.CompileManager;
import com.duy.ide.code_sample.activities.DocumentActivity;
import com.duy.ide.editor.view.AutoIndentEditText;
import com.duy.ide.editor.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.FileSelectListener;
import com.duy.ide.setting.JavaPreferences;
import com.duy.ide.themefont.activities.ThemeFontActivity;
import com.duy.project.ProjectFile;
import com.duy.project.ProjectManager;
import com.duy.project.dialog.DialogSelectDirectory;
import com.duy.project.utils.ClassUtil;
import com.duy.run.dialog.DialogRunConfig;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;

public class MainActivity extends BaseEditorActivity implements
        DrawerLayout.DrawerListener,
        DialogRunConfig.OnConfigChangeListener,
        FileSelectListener {
    public static final int ACTION_FILE_SELECT_CODE = 1012;
    public static final int ACTION_PICK_MEDIA_URL = 1013;
    public static final int ACTION_CREATE_SHORTCUT = 1014;
    private static final String TAG = "MainActivity";
    private CompileManager mCompileManager;
    private MenuEditor mMenuEditor;
    private Dialog mDialog;
    private MenuItem mActionRun;
    private ProgressBar mCompileProgress;

    private AutoCompleteService mAutoCompleteService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AutoCompleteService.ACBinder acBinder = (AutoCompleteService.ACBinder) service;
            mAutoCompleteService = acBinder.getService();
            populateAutoCompleteService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAutoCompleteService = null;
        }
    };

    private void populateAutoCompleteService() {
        Log.d(TAG, "populateAutoCompleteService() called");
        mAutoCompleteService.setCallback(new AutoCompleteService.OnAutoCompleteServiceLoadListener() {
            @Override
            public void onLoaded(@NonNull AutoCompleteProvider provider) {
                Log.d(TAG, "onLoaded() called with: provider = [" + provider + "]");

                mPagePresenter.setAutoCompleteProvider(provider);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompileManager = new CompileManager(this);
        mMenuEditor = new MenuEditor(this, this);
        initView(savedInstanceState);

        startAutoCompleteService();

        tryToAccessFile();
    }

    private void tryToAccessFile() {
        Log.d(TAG, "tryToAccessFile() called");

        File file = new File(getFilesDir(), "system/bin/aapt");
        if (file.exists()) {
            Log.d(TAG, "tryToAccessFile: " + file.canWrite() + file.canRead() + file.canExecute());
            file.setExecutable(true, true);
            file.setReadable(true, true);
            file.setWritable(true, true);
        }
    }


    private void startAutoCompleteService() {
        Intent intent = new Intent(this, AutoCompleteService.class);
        if (!bindService(intent, mServiceConnection, BIND_AUTO_CREATE)) {
            Log.e(TAG, "startAutoCompleteService: bind service failed");
        }
    }


    public void initView(Bundle savedInstanceState) {
        mDrawerLayout.addDrawerListener(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mDrawerLayout.closeDrawers();
                return mMenuEditor.onOptionsItemSelected(item);
            }
        });
        View tab = findViewById(R.id.img_tab);
        if (tab != null) {
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    insertTab(v);
                }
            });
        }
        mCompileProgress = (ProgressBar) findViewById(R.id.compile_progress);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mMenuEditor.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    void insertTab(View v) {
        onKeyClick(v, AutoIndentEditText.TAB_CHARACTER);
    }

    @Override
    public void onKeyClick(View view, String text) {
        EditorFragment currentFragment = mPageAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.insert(text);
        }
    }

    @Override
    public void onKeyLongClick(String text) {
        EditorFragment currentFragment = mPageAdapter.getCurrentFragment();
        if (currentFragment != null) {
            currentFragment.insert(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean r = mMenuEditor.onCreateOptionsMenu(menu);
        mActionRun = menu.findItem(R.id.action_edit_run);
        return r;
    }

    /**
     * create dialog find and replace
     */
    @Override
    public void findAndReplace() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.dialog_find_and_replace);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);
        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);
        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);
        final EditText editReplace = (EditText) alertDialog.findViewById(R.id.edit_replace);
        if (editFind != null) {
            editFind.setText(getPreferences().getString(JavaPreferences.LAST_FIND));
        }
        View find = alertDialog.findViewById(R.id.btn_replace);
        assert find != null;
        assert editFind != null;
        assert editReplace != null;
        assert ckbRegex != null;
        assert ckbMatch != null;
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
                if (editorFragment != null) {

                    editorFragment.doFindAndReplace(
                            editFind.getText().toString(),
                            editReplace.getText().toString(),
                            ckbRegex.isChecked(),
                            ckbMatch.isChecked());
                }
                getPreferences().put(JavaPreferences.LAST_FIND, editFind.getText().toString());
                alertDialog.dismiss();
            }
        });
        View cancle = alertDialog.findViewById(R.id.btn_cancel);
        assert cancle != null;
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    public void runProject() {
        if (mProjectFile != null) {
            //check main class exist
            if (mProjectFile.getMainClass() == null || !mProjectFile.getMainClass().exist(mProjectFile)
                    || mProjectFile.getPackageName() == null || mProjectFile.getPackageName().isEmpty()) {
                String msg = getString(R.string.main_class_not_define);
                Snackbar.make(mDrawerLayout, msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.config, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialogRunConfig();
                            }
                        }).show();
                return;
            }
            //check main function exist
            if (!ClassUtil.hasMainFunction(new File(mProjectFile.getMainClass().getPath(mProjectFile)))) {
                SpannableStringBuilder msg = new SpannableStringBuilder(getString(R.string.can_not_find_main_func));
                Spannable clasz = new SpannableString(mProjectFile.getMainClass().getName());
                clasz.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.dark_color_accent))
                        , 0, clasz.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                msg.append(clasz);
                Snackbar.make(mDrawerLayout, msg, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.config, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialogRunConfig();
                            }
                        }).show();
                return;
            }
            saveAllFile();
            new CompileTask(this).execute(mProjectFile);
        } else {
            Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void buildJar() {
        if (mProjectFile != null) {
            new BuildJarAchieveTask(this).execute(mProjectFile);
        } else {
            Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * replace dialog find
     */
    public void showDialogFind() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.dialog_find);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final CheckBox ckbRegex = (CheckBox) alertDialog.findViewById(R.id.ckb_regex);
        final CheckBox ckbMatch = (CheckBox) alertDialog.findViewById(R.id.ckb_match_key);
        final CheckBox ckbWordOnly = (CheckBox) alertDialog.findViewById(R.id.ckb_word_only);
        final EditText editFind = (EditText) alertDialog.findViewById(R.id.txt_find);
        editFind.setText(getPreferences().getString(JavaPreferences.LAST_FIND));
        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
                if (editorFragment != null) {
                    editorFragment.doFind(editFind.getText().toString(),
                            ckbRegex.isChecked(),
                            ckbWordOnly.isChecked(),
                            ckbMatch.isChecked());
                }
                getPreferences().put(JavaPreferences.LAST_FIND, editFind.getText().toString());
                alertDialog.dismiss();
            }
        });
        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });


    }

    @Override
    public void saveCurrentFile() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.saveFile();
        }
    }

    @Override
    public void showDocumentActivity() {
        Intent intent = new Intent(this, DocumentActivity.class);
        startActivity(intent);
    }

    public String getCode() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            return editorFragment.getCode();
        }
        return "";
    }

    private void buildSuggestData() {

    }

    private void showErrorDialog(Exception e) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getPreferences().isShowListSymbol()) {
            if (mContainerSymbol != null && mKeyList != null) {
                mKeyList.setListener(this);
                mContainerSymbol.setVisibility(View.VISIBLE);
            }
        } else {
            if (mContainerSymbol != null) {
                mContainerSymbol.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull SharedPreferences sharedPreferences, @NonNull String s) {
        if (s.equals(getString(R.string.key_show_suggest_popup))
                || s.equals(getString(R.string.key_show_line_number))
                || s.equals(getString(R.string.key_pref_word_wrap))) {
            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
            if (editorFragment != null) {
                editorFragment.refreshCodeEditor();
            }
        } else if (s.equals(getString(R.string.key_show_symbol))) {
            if (mContainerSymbol != null) {
                mContainerSymbol.setVisibility(getPreferences().isShowListSymbol()
                        ? View.VISIBLE : View.GONE);
            }
        } else if (s.equals(getString(R.string.key_show_suggest_popup))) {
            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
            if (editorFragment != null) {
                EditorView editor = editorFragment.getEditor();
                editor.setSuggestData(new ArrayList<Description>());
            }
        }
        //toggle ime/no suggest mode
        else if (s.equalsIgnoreCase(getString(R.string.key_ime_keyboard))) {
            EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
            if (editorFragment != null) {
                EditorView editor = editorFragment.getEditor();
                editorFragment.refreshCodeEditor();
            }
        } else {
            super.onSharedPreferenceChanged(sharedPreferences, s);
        }
    }

    /**
     * show dialog create new source file
     */
    @Override
    public void createNewFile(View view) {
//        DialogCreateNewFile dialogCreateNewFile = DialogCreateNewFile.Companion.getInstance();
//        dialogCreateNewFile.show(getSupportFragmentManager(), DialogCreateNewFile.Companion.getTAG());
//        dialogCreateNewFile.setListener(new DialogCreateNewFile.OnCreateNewFileListener() {
//            @Override
//            public void onFileCreated(@NonNull File file) {
//                saveFile();
//                //add to view
//                addNewPageEditor(file, SELECT);
//                mDrawerLayout.closeDrawers();
//            }
//
//            @Override
//            public void onCancel() {
//            }
//        });
    }

    @Override
    public void goToLine() {
        final AppCompatEditText edittext = new AppCompatEditText(this);
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setMaxEms(5);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.goto_line)
                .setView(edittext)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String line = edittext.getText().toString();
                        if (!line.isEmpty()) {
                            EditorFragment editorFragment
                                    = mPageAdapter.getCurrentFragment();
                            if (editorFragment != null) {
                                editorFragment.goToLine(Integer.parseInt(line));
                            }
                        }
                        dialog.cancel();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public void formatCode() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.formatCode();
        }
    }

    @Override
    public void undo() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.undo();
        }
    }

    @Override
    public void redo() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.redo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ACTION_FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    // Get the path
                    String path;
                    try {
                        path = mFileManager.getPath(this, uri);
                        mFileManager.setWorkingFilePath(path);
                        addNewPageEditor(new File(path), SELECT);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case ACTION_PICK_MEDIA_URL:
                if (resultCode == RESULT_OK) {
                    String path = data.getData().toString();
                    EditorFragment currentFragment = mPageAdapter.getCurrentFragment();
                    if (currentFragment != null && path != null) {
                        currentFragment.insert(path);
                    }
                }
                break;
            case ACTION_CREATE_SHORTCUT:
                data.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
                getApplicationContext().sendBroadcast(data);
                break;
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        closeKeyBoard();
    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void paste() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.paste();
        }
    }

    @Override
    public void copyAll() {
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            editorFragment.copyAll();
        }
    }

    @Override
    public void selectThemeFont() {
        startActivity(new Intent(this, ThemeFontActivity.class));
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)
                || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (mContainerOutput.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }

        /*
          check can undo
         */
        if (getPreferences().getBoolean(getString(R.string.key_back_undo))) {
            undo();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.exit_mgs)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    public void openDrawer(int gravity) {
        mDrawerLayout.openDrawer(gravity);
    }

    public void showDialogRunConfig() {
        if (mProjectFile != null) {
            DialogRunConfig dialogRunConfig = DialogRunConfig.newInstance(mProjectFile);
            dialogRunConfig.show(getSupportFragmentManager(), DialogRunConfig.TAG);
        } else {
            Toast.makeText(this, "Please create project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigChange(ProjectFile projectFile) {
        this.mProjectFile = projectFile;
        ProjectManager.saveProject(this, projectFile);
    }

    public void showDialogOpenProject() {
        DialogSelectDirectory dialog = DialogSelectDirectory.newInstance(FileManager.EXTERNAL_DIR, 2);
        dialog.show(getSupportFragmentManager(), DialogSelectDirectory.TAG);
    }

    @Override
    public void onFileSelected(File file, int request) {
        Log.d(TAG, "onFileSelected() called with: file = [" + file + "], request = [" + request + "]");

        switch (request) {
            case 2: //import new project
                saveCurrentFile();
                ProjectFile pf = ProjectManager.createProjectIfNeed(file);
                Log.d(TAG, "onFileSelected pf = " + pf);
                if (pf != null) {
                    super.onProjectCreated(pf);
                }
                break;

        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class CompileTask extends AsyncTask<ProjectFile, Object, File> {
        private Context mContext;
        private ArrayList<Diagnostic> mDiagnostics = new ArrayList<>();

        CompileTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mActionRun != null) mActionRun.setEnabled(false);
            if (mCompileProgress != null) mCompileProgress.setVisibility(View.VISIBLE);
            hideKeyboard();
            openDrawer(GravityCompat.START);
            mMessagePresenter.clear();
            mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            mDiagnosticPresenter.clear();
        }

        @Override
        protected File doInBackground(ProjectFile... params) {
            if (params[0] == null) return null;
            PrintWriter printWriter = new PrintWriter(new Writer() {
                @Override
                public void write(@NonNull char[] chars, int i, int i1) throws IOException {
                    publishProgress(chars, i, i1);
                }

                @Override
                public void flush() throws IOException {

                }

                @Override
                public void close() throws IOException {

                }
            });
            DiagnosticListener listener = new DiagnosticListener() {
                @Override
                public void report(Diagnostic diagnostic) {
                    mDiagnostics.add(diagnostic);
                }
            };
            return CommandManager.compile(mProjectFile, printWriter, listener);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            try {
                char[] chars = (char[]) values[0];
                int start = (int) values[1];
                int end = (int) values[2];
                mMessagePresenter.append(chars, start, end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(final File result) {
            super.onPostExecute(result);
            mDiagnosticPresenter.display(mDiagnostics);

            if (mActionRun != null) mActionRun.setEnabled(true);
            if (mCompileProgress != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCompileProgress.setVisibility(View.GONE);
                    }
                }, 500);
            }
            if (result == null) {
                Toast.makeText(mContext, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(GravityCompat.START);
                mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
            } else {
                Toast.makeText(mContext, R.string.compile_success, Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCompileManager.executeDex(mProjectFile, result);
                    }
                }, 200);
            }
        }
    }

    private class BuildJarAchieveTask extends AsyncTask<ProjectFile, Object, File> {
        private Context mContext;
        private ArrayList<Diagnostic> mDiagnostics = new ArrayList<>();

        BuildJarAchieveTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mActionRun != null) mActionRun.setEnabled(false);
            if (mCompileProgress != null) mCompileProgress.setVisibility(View.VISIBLE);
            hideKeyboard();
            openDrawer(GravityCompat.START);
            mMessagePresenter.clear();
            mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            mDiagnosticPresenter.clear();
        }

        @Override
        protected File doInBackground(ProjectFile... params) {
            if (params[0] == null) return null;
            PrintWriter printWriter = new PrintWriter(new Writer() {
                @Override
                public void write(@NonNull char[] chars, int i, int i1) throws IOException {
                    publishProgress(chars, i, i1);
                }

                @Override
                public void flush() throws IOException {

                }

                @Override
                public void close() throws IOException {

                }
            });
            DiagnosticListener listener = new DiagnosticListener() {
                @Override
                public void report(Diagnostic diagnostic) {
                    mDiagnostics.add(diagnostic);
                }
            };
            return CommandManager.buildJarAchieve(mProjectFile, printWriter, listener);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            try {
                char[] chars = (char[]) values[0];
                int start = (int) values[1];
                int end = (int) values[2];
                mMessagePresenter.append(chars, start, end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(final File result) {
            super.onPostExecute(result);
            mDiagnosticPresenter.display(mDiagnostics);

            if (mActionRun != null) mActionRun.setEnabled(true);
            if (mCompileProgress != null) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCompileProgress.setVisibility(View.GONE);
                    }
                }, 500);
            }
            if (result == null) {
                Toast.makeText(mContext, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                mDrawerLayout.openDrawer(GravityCompat.START);
                mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
            } else {
                Toast.makeText(mContext, R.string.build_success + " " + result.getPath(),
                        Toast.LENGTH_SHORT).show();
                mFilePresenter.refresh(mProjectFile);
                mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }


}