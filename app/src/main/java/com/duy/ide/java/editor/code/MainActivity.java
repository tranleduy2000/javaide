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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.duy.JavaApplication;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.BuildTask;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.JavaBuilder;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.FileCollection;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.android.compiler.utils.ProjectUtils;
import com.duy.ide.java.Builder;
import com.duy.ide.java.MenuEditor;
import com.duy.ide.R;
import com.duy.ide.java.diagnostic.DiagnosticFragment;
import com.duy.ide.java.editor.code.view.EditorView;
import com.duy.ide.java.editor.code.view.IndentEditText;
import com.duy.ide.javaide.uidesigner.inflate.DialogLayoutPreview;
import com.duy.ide.javaide.autocomplete.AutoCompleteProvider;
import com.duy.ide.javaide.autocomplete.model.Description;
import com.duy.ide.javaide.autocomplete.util.JavaUtil;
import com.duy.ide.javaide.run.activities.ExecuteActivity;
import com.duy.ide.javaide.run.dialog.DialogRunConfig;
import com.duy.ide.javaide.sample.activities.DocumentActivity;
import com.duy.ide.javaide.sample.activities.JavaSampleActivity;
import com.duy.ide.java.setting.AppSetting;
import com.duy.ide.java.utils.RootUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends ProjectManagerActivity implements
        DrawerLayout.DrawerListener,
        DialogRunConfig.OnConfigChangeListener,
        Builder {
    public static final int REQUEST_CODE_SAMPLE = 1015;

    private static final String TAG = "MainActivity";

    private MenuEditor mMenuEditor;
    private Dialog mDialog;
    private MenuItem mActionRun;
    private ProgressBar mCompileProgress;
    private AutoCompleteProvider mAutoCompleteProvider;

    private void populateAutoCompleteService(AutoCompleteProvider provider) {
        mPagePresenter.setAutoCompleteProvider(provider);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuEditor = new MenuEditor(this, this);
        initView();
        startAutoCompleteService();
    }

    protected void startAutoCompleteService() {
        Log.d(TAG, "startAutoCompleteService() called");
        if (mAutoCompleteProvider == null) {
            if (mProject != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAutoCompleteProvider = new AutoCompleteProvider(MainActivity.this);
                        mAutoCompleteProvider.load(mProject);
                        populateAutoCompleteService(mAutoCompleteProvider);
                    }
                }).start();
            }
        } else {
            populateAutoCompleteService(mAutoCompleteProvider);
        }
    }

    public void initView() {
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
        mCompileProgress = findViewById(R.id.compile_progress);
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
        mActionRun = menu.findItem(R.id.action_run);
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

        final CheckBox ckbRegex = alertDialog.findViewById(R.id.ckb_regex);
        final CheckBox ckbMatch = alertDialog.findViewById(R.id.ckb_match_key);
        final EditText editFind = alertDialog.findViewById(R.id.txt_find);
        final EditText editReplace = alertDialog.findViewById(R.id.edit_replace);
        if (editFind != null) {
            editFind.setText(getPreferences().getString(AppSetting.LAST_FIND));
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
                getPreferences().put(AppSetting.LAST_FIND, editFind.getText().toString());
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
        if (mProject != null) {
            if (mProject instanceof AndroidAppProject) {
                compileAndroidProject();
            } else {
                compileJavaProject();
            }
        } else {
            Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
        }
    }

    private void compileAndroidProject() {
        if (mProject instanceof AndroidAppProject) {
            if (!((AndroidAppProject) mProject).getManifestFile().exists()) {
                Toast.makeText(this, "Can not find AndroidManifest.xml", Toast.LENGTH_SHORT).show();
                return;
            }
            //check launcher activity
            if (((AndroidAppProject) mProject).getLauncherActivity() == null) {
                String msg = getString(R.string.can_not_find_launcher_activity);
                Snackbar.make(findViewById(R.id.coordinate_layout), msg, Snackbar.LENGTH_LONG)
                        .setAction(R.string.config, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                            }
                        }).show();
                return;
            }


            final AndroidAppBuilder builder = new AndroidAppBuilder(this, (AndroidAppProject) mProject);
            builder.setStdOut(mMessagePresenter.getStdOut());
            builder.setStdErr(mMessagePresenter.getStdErr());
            builder.setLogger(mMessagePresenter);

            final BuildTask<AndroidAppProject> buildTask = new BuildTask<>(builder, new BuildTask.CompileListener<AndroidAppProject>() {
                @Override
                public void onStart() {
                    updateUiStartCompile();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(MainActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                    openDrawer(GravityCompat.START);
                    updateUIFinish();
                }

                @Override
                public void onComplete() {
                    updateUIFinish();
                    Toast.makeText(MainActivity.this, R.string.build_success, Toast.LENGTH_SHORT).show();
                    mFilePresenter.refresh(mProject);
                    mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    RootUtils.installApk(MainActivity.this, ((AndroidAppProject) mProject).getApkSigned());
                }

            });
            buildTask.execute();
        } else {
            if (mProject != null) {
                toast("This is Java project, please create new Android project");
            } else {
                toast("You need create project");
            }
        }
    }


    private void compileJavaProject() {
        final IBuilder<JavaProject> builder = new JavaBuilder(this, mProject);
        builder.setStdOut(mMessagePresenter.getStdOut());
        builder.setStdErr(mMessagePresenter.getStdErr());
        final BuildTask.CompileListener<JavaProject> listener = new BuildTask.CompileListener<JavaProject>() {
            @Override
            public void onStart() {
                updateUiStartCompile();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                openDrawer(GravityCompat.START);
                mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
                updateUIFinish();
            }

            @Override
            public void onComplete() {
                updateUIFinish();
                Toast.makeText(MainActivity.this, R.string.compile_success, Toast.LENGTH_SHORT).show();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runJava(mProject);
                    }
                }, 200);
            }
        };
        BuildTask<JavaProject> buildTask = new BuildTask<>(builder, listener);
        buildTask.execute();
    }

    private void runJava(final JavaProject project) {
        final File currentFile = getCurrentFile();
        if (currentFile == null || !ProjectUtils.isFileBelongProject(project, currentFile)) {
            ArrayList<File> javaSrcDirs = new ArrayList<>();
            javaSrcDirs.add(project.getJavaSrcDir());
            FileCollection fileCollection = new FileCollection(javaSrcDirs);
            final ArrayList<File> javaSources = fileCollection.filter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(".java");
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] names = new String[javaSources.size()];
            for (int i = 0; i < javaSources.size(); i++) {
                names[i] = javaSources.get(i).getName();
            }
            builder.setTitle(R.string.select_class_to_run);
            builder.setItems(names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(MainActivity.this, ExecuteActivity.class);
                    intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
                    intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, javaSources.get(which));
                    startActivity(intent);
                }
            });
            builder.create().show();
        } else {
            Intent intent = new Intent(MainActivity.this, ExecuteActivity.class);
            intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
            intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, currentFile);
            startActivity(intent);
        }
    }

    /**
     * replace dialog find
     */
    public void showDialogFind() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setView(R.layout.dialog_find);
//        final AlertDialog alertDialog = builder.create();
//        alertDialog.show();
//        final CheckBox ckbRegex = alertDialog.findViewById(R.id.ckb_regex);
//        final CheckBox ckbMatch = alertDialog.findViewById(R.id.ckb_match_key);
//        final CheckBox ckbWordOnly = alertDialog.findViewById(R.id.ckb_word_only);
//        final EditText editFind = alertDialog.findViewById(R.id.txt_find);
//        editFind.setText(getPreferences().getString(AppSetting.LAST_FIND));
//        alertDialog.findViewById(R.id.btn_replace).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
//                if (editorFragment != null) {
//                    editorFragment.doFind(editFind.getText().toString(),
//                            ckbRegex.isChecked(),
//                            ckbWordOnly.isChecked(),
//                            ckbMatch.isChecked());
//                }
//                getPreferences().put(AppSetting.LAST_FIND, editFind.getText().toString());
//                alertDialog.dismiss();
//            }
//        });
//        alertDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
//            }
//        });


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
            case REQUEST_CODE_SAMPLE:
                if (resultCode == RESULT_OK) {
                    String projectPath = data.getStringExtra(JavaSampleActivity.PROJECT_PATH);
                    JavaProjectManager manager = new JavaProjectManager(this);
                    JavaProject javaProject = null;
                    try {
                        javaProject = manager.loadProject(new File(projectPath), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (javaProject != null) {
                        onProjectCreated(javaProject);
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
    public void runFile(String filePath) {
        saveCurrentFile();
        if (mProject == null) return;
        String className = JavaUtil.getClassName(mProject.getJavaSrcDirs().get(0), filePath);
        if (className == null) {
            Toast.makeText(this, ("Class \"" + filePath + "\"" + "invalid"), Toast.LENGTH_SHORT).show();
            return;
        }
        runProject();
    }

    @Override
    public void previewLayout(String path) {
        saveCurrentFile();
        File currentFile = getCurrentFile();
        if (currentFile != null) {
            DialogLayoutPreview dialogPreview = DialogLayoutPreview.newInstance(currentFile);
            dialogPreview.show(getSupportFragmentManager(), DialogLayoutPreview.TAG);
        } else {
            Toast.makeText(this, "Can not find file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void createKeyStore() {
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

    @Override
    public void onConfigChange(JavaProject projectFile) {
        this.mProject = projectFile;
        if (projectFile != null) {
            JavaProjectManager.saveProject(this, projectFile);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateUiStartCompile() {
        if (mActionRun != null) mActionRun.setEnabled(false);
        if (mCompileProgress != null) mCompileProgress.setVisibility(View.VISIBLE);
        hideKeyboard();
        openDrawer(GravityCompat.START);

        mMessagePresenter.resume((JavaApplication) getApplication());
        mMessagePresenter.clear();
        mMessagePresenter.append("Compiling...\n");

        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mDiagnosticPresenter.clear();

        mBottomPage.setCurrentItem(0);

    }

    private void updateUIFinish() {
        mMessagePresenter.pause((JavaApplication) getApplication());
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