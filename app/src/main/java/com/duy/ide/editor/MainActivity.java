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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.duy.compile.BuildApkTask;
import com.duy.compile.BuildJarAchieveTask;
import com.duy.compile.CompileJavaTask;
import com.duy.compile.CompileManager;
import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.ide.Builder;
import com.duy.ide.MenuEditor;
import com.duy.ide.R;
import com.duy.ide.autocomplete.AutoCompleteProvider;
import com.duy.ide.autocomplete.model.Description;
import com.duy.ide.autocomplete.util.JavaUtil;
import com.duy.ide.code_sample.activities.DocumentActivity;
import com.duy.ide.code_sample.activities.SampleActivity;
import com.duy.ide.editor.view.EditorView;
import com.duy.ide.editor.view.IndentEditText;
import com.duy.ide.setting.JavaPreferences;
import com.duy.ide.themefont.activities.ThemeFontActivity;
import com.duy.project.file.android.AndroidProjectFolder;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFolder;
import com.duy.project.file.java.ProjectManager;
import com.duy.project.utils.ClassUtil;
import com.duy.run.dialog.DialogRunConfig;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;

public class MainActivity extends BaseEditorActivity implements
        DrawerLayout.DrawerListener,
        DialogRunConfig.OnConfigChangeListener,
        Builder {
    public static final int ACTION_FILE_SELECT_CODE = 1012;
    public static final int ACTION_PICK_MEDIA_URL = 1013;
    public static final int ACTION_CREATE_SHORTCUT = 1014;
    public static final int REQUEST_CODE_SAMPLE = 1015;

    private static final String TAG = "MainActivity";

    private CompileManager mCompileManager;
    private MenuEditor mMenuEditor;
    private Dialog mDialog;
    private MenuItem mActionRun;
    private ProgressBar mCompileProgress;

    private void populateAutoCompleteService(AutoCompleteProvider provider) {
        mPagePresenter.setAutoCompleteProvider(provider);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompileManager = new CompileManager(this);
        mMenuEditor = new MenuEditor(this, this);
        initView(savedInstanceState);

    }

    protected void startAutoCompleteService() {
        if (mProjectFile != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AutoCompleteProvider provider = new AutoCompleteProvider(MainActivity.this);
                    provider.load(mProjectFile);
                    populateAutoCompleteService(provider);
                }
            }).start();
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
        onKeyClick(v, IndentEditText.TAB_CHARACTER);
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
        saveAllFile();
        if (mProjectFile != null) {
            if (mProjectFile instanceof AndroidProjectFolder) {
                //check launcher activity
                if (((AndroidProjectFolder) mProjectFile).getLauncherActivity() == null) {
                    String msg = getString(R.string.can_not_find_launcher_activity);
                    Snackbar.make(findViewById(R.id.coordinate_layout), msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.config, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                }
                            }).show();
                    return;
                }
                compileAndroidProject();
            } else {
                //check main class exist
                if (mProjectFile.getMainClass() == null
                        || mProjectFile.getPackageName() == null
                        || mProjectFile.getPackageName().isEmpty()
                        || !mProjectFile.getMainClass().exist(mProjectFile)) {
                    String msg = getString(R.string.main_class_not_define);
                    Snackbar.make(findViewById(R.id.coordinate_layout), msg, Snackbar.LENGTH_LONG)
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
                    Snackbar.make(findViewById(R.id.coordinate_layout), msg, Snackbar.LENGTH_LONG)
                            .setAction(R.string.config, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    showDialogRunConfig();
                                }
                            }).show();
                    return;
                }
                compileJavaProject();
            }
        } else {
            Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
        }
    }

    private void compileAndroidProject() {
        buildApk();
    }


    private void compileJavaProject() {
        CompileJavaTask.CompileListener compileListener = new CompileJavaTask.CompileListener() {
            @Override
            public void onStart() {
                updateUiStartCompile();
            }

            @Override
            public void onError(Exception e, ArrayList<Diagnostic> diagnostics) {
                Toast.makeText(MainActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                openDrawer(GravityCompat.START);
                mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
                mDiagnosticPresenter.display(diagnostics);
                updateUIFinish();
            }

            @Override
            public void onComplete(final JavaProjectFolder projectFile,
                                   final List<Diagnostic> diagnostics) {
                updateUIFinish();
                Toast.makeText(MainActivity.this, R.string.compile_success, Toast.LENGTH_SHORT).show();
                mDiagnosticPresenter.display(diagnostics);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mCompileManager.executeDex(projectFile, mProjectFile.getDexedClassesFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, 200);
            }

            @Override
            public void onNewMessage(char[] chars, int start, int end) {
                mMessagePresenter.append(chars, start, end);
            }
        };
        new CompileJavaTask(compileListener).execute(mProjectFile);
    }

    @Override
    public void buildJar() {
        saveAllFile();
        if (mProjectFile != null) {
            new BuildJarAchieveTask(new BuildJarAchieveTask.CompileListener() {
                @Override
                public void onStart() {
                    updateUiStartCompile();
                }

                @Override
                public void onError(Exception e, List<Diagnostic> diagnostics) {
                    Toast.makeText(MainActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                    openDrawer(GravityCompat.START);
                    mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
                    mDiagnosticPresenter.display(diagnostics);
                    mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    updateUIFinish();
                }

                @Override
                public void onComplete(File jarfile, List<Diagnostic> diagnostics) {
                    Toast.makeText(MainActivity.this, R.string.build_success + " " + jarfile.getPath(),
                            Toast.LENGTH_SHORT).show();
                    mFilePresenter.refresh(mProjectFile);
                    mDiagnosticPresenter.display(diagnostics);
                    mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    updateUIFinish();
                }

                @Override
                public void onNewMessage(byte[] chars, int start, int end) {
                    mMessagePresenter.append(chars, start, end);
                }
            }).execute(mProjectFile);
        } else {
            complain("You need create project");
        }
    }

    public void buildApk() {
        saveAllFile();
        if (mProjectFile instanceof AndroidProjectFolder) {
            ((AndroidProjectFolder) mProjectFile).checkKeyStoreExits(this);
            new BuildApkTask(new BuildApkTask.CompileListener() {
                @Override
                public void onStart() {
                    updateUiStartCompile();
                }

                @Override
                public void onError(Exception e, List<Diagnostic> diagnostics) {
                    Toast.makeText(MainActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                    openDrawer(GravityCompat.START);
                    mDiagnosticPresenter.display(diagnostics);
                    updateUIFinish();
                }

                @Override
                public void onComplete(File apk, List<Diagnostic> diagnostics) {
                    updateUIFinish();
                    Toast.makeText(MainActivity.this, R.string.build_success + " " + apk.getPath(),
                            Toast.LENGTH_SHORT).show();
                    mFilePresenter.refresh(mProjectFile);
                    mDiagnosticPresenter.display(diagnostics);
                    mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    onFileClick(apk, null);
                }

                @Override
                public void onNewMessage(byte[] chars, int start, int end) {
                    mMessagePresenter.append(new String(chars, start, end));
                }
            }).execute((AndroidProjectFolder) mProjectFile);
        } else {
            if (mProjectFile != null) {
                complain("This is Java project, please create new Android project");
            } else {
                complain("You need create project");
            }
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
            case REQUEST_CODE_SAMPLE:
                if (resultCode == RESULT_OK) {
                    final JavaProjectFolder projectFile = (JavaProjectFolder)
                            data.getSerializableExtra(SampleActivity.PROJECT_FILE);
                    if (projectFile != null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onProjectCreated(projectFile);
                            }
                        }, 100);
                    }
                }
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
    public void runFile(String filePath) {
        if (mProjectFile == null) return;
        boolean canRun = ClassUtil.hasMainFunction(new File(filePath));
        if (!canRun) {
            Toast.makeText(this, (getString(R.string.main_not_found)), Toast.LENGTH_SHORT).show();
            return;
        }
        String className = JavaUtil.getClassName(mProjectFile.dirJava, filePath);
        if (className == null) {
            Toast.makeText(this, ("Class \"" + filePath + "\"" + "invalid"), Toast.LENGTH_SHORT).show();
            return;
        }
        mProjectFile.setMainClass(new ClassFile(className));
        runProject();
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)
                || mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            if (mContainerOutput.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                return;
            } else {
                mDrawerLayout.closeDrawers();
                return;
            }
        }

//        /*
//          check can undo
//         */
//        if (getPreferences().getBoolean(getString(R.string.key_back_undo))) {
//            undo();
//            return;
//        }

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

    public void showDialogRunConfig() {
        if (mProjectFile != null) {
            DialogRunConfig dialogRunConfig = DialogRunConfig.newInstance(mProjectFile);
            dialogRunConfig.show(getSupportFragmentManager(), DialogRunConfig.TAG);
        } else {
            Toast.makeText(this, "Please create project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConfigChange(JavaProjectFolder projectFile) {
        this.mProjectFile = projectFile;
        if (projectFile != null) {
            ProjectManager.saveProject(this, projectFile);
        }
    }


    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateUiStartCompile() {
        if (mActionRun != null) mActionRun.setEnabled(false);
        if (mCompileProgress != null) mCompileProgress.setVisibility(View.VISIBLE);
        hideKeyboard();
        openDrawer(GravityCompat.START);
        mMessagePresenter.clear();
        mMessagePresenter.append("Compiling...\n");

        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mDiagnosticPresenter.clear();

        mBottomPage.setCurrentItem(0);
    }

    private void updateUIFinish() {
        if (mActionRun != null) mActionRun.setEnabled(true);
        if (mCompileProgress != null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCompileProgress.setVisibility(View.GONE);
                }
            }, 500);
        }
    }


}