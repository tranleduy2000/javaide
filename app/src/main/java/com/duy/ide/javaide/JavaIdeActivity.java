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

package com.duy.ide.javaide;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.utils.StdLogger;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.BuildTask;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.JavaBuilder;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.FileCollection;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.android.compiler.utils.ProjectUtils;
import com.duy.ide.R;
import com.duy.ide.code.api.CodeFormatProvider;
import com.duy.ide.diagnostic.DiagnosticContract;
import com.duy.ide.java.Builder;
import com.duy.ide.java.setting.SettingsActivity;
import com.duy.ide.java.utils.DonateUtils;
import com.duy.ide.java.utils.RootUtils;
import com.duy.ide.java.utils.StoreUtil;
import com.duy.ide.javaide.autocomplete.JavaAutoCompleteProvider;
import com.duy.ide.javaide.formatter.JavaIdeCodeFormatProvider;
import com.duy.ide.javaide.run.activities.ExecuteActivity;
import com.duy.ide.javaide.run.dialog.DialogRunConfig;
import com.duy.ide.javaide.sample.activities.JavaSampleActivity;
import com.duy.ide.javaide.setting.CompilerSettingActivity;
import com.duy.ide.javaide.uidesigner.inflate.DialogLayoutPreview;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.pluscubed.logcat.ui.LogcatActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

public class JavaIdeActivity extends ProjectManagerActivity implements
        DialogRunConfig.OnConfigChangeListener,
        Builder {
    public static final int REQUEST_CODE_SAMPLE = 1015;

    private static final String TAG = "MainActivity";
    private static final int RC_BUILD_PROJECT = 131;
    private ProgressBar mCompileProgress;
    private JavaAutoCompleteProvider mAutoCompleteProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        startAutoCompleteService();
    }

    @Override
    protected void populateDiagnostic(@NonNull DiagnosticContract.Presenter diagnosticPresenter) {
        //init here, set output parser
        // TODO: 09-Jun-18 output parser AAPT and JAVA
    }

    private void populateAutoCompleteService(JavaAutoCompleteProvider provider) {
//        mPagePresenter.setAutoCompleteProvider(provider);
    }

    @Override
    protected CodeFormatProvider getCodeFormatProvider() {
        return new JavaIdeCodeFormatProvider(this);
    }

    protected void startAutoCompleteService() {
        Log.d(TAG, "startAutoCompleteService() called");
        if (mAutoCompleteProvider == null) {
            if (mProject != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mAutoCompleteProvider = new JavaAutoCompleteProvider(JavaIdeActivity.this);
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
        mCompileProgress = findViewById(R.id.compile_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu container) {
        container.add(0, R.id.action_run, 0, R.string.run)
                .setIcon(MenuManager.makeToolbarNormalIcon(this, R.drawable.ic_play_arrow_white_24dp))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(container);

        MenuItem fileMenu = container.findItem(R.id.menu_file);
        fileMenu.getSubMenu().add(0,
                R.id.action_new_java_project, 0,
                R.string.new_java_project)
                .setIcon(MenuManager.makeMenuNormalIcon(this, R.drawable.ic_create_new_folder_white_24dp))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        fileMenu.getSubMenu().add(0,
                R.id.action_open_java_project, 0,
                R.string.open_java_project)
                .setIcon(MenuManager.makeMenuNormalIcon(this, R.drawable.ic_create_new_folder_white_24dp))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.action_run:
                runProject();
                break;
            case R.id.action_report_bug: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/tranleduy2000/javaide/issues"));
                startActivity(intent);
                break;
            }
            case R.id.action_donate:
                DonateUtils.showDialogDonate(this);
                break;
            case R.id.action_new_java_project:
                showDialogCreateJavaProject();
                break;
            case R.id.action_new_android_project:
                showDialogCreateAndroidProject();
                break;
            case R.id.action_new_file:
                createNewFile(null);
                break;
            case R.id.action_new_class:
                showDialogCreateNewClass(null);
                break;
            case R.id.action_open_java_project:
                showDialogOpenJavaProject();
                break;
            case R.id.action_open_android_project:
                showDialogOpenAndroidProject();
                break;
            case R.id.action_sample:
                startActivityForResult(new Intent(this, JavaSampleActivity.class),
                        JavaIdeActivity.REQUEST_CODE_SAMPLE);
                break;
            case R.id.action_see_logcat:
                startActivity(new Intent(this, LogcatActivity.class));
                break;
            case R.id.action_install_cpp_nide:
                StoreUtil.gotoPlayStore(this, "com.duy.c.cpp.compiler");
                break;
            case R.id.action_compiler_setting:
                startActivity(new Intent(this, CompilerSettingActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void runProject() {
        saveAll(RC_BUILD_PROJECT);
    }

    @Override
    protected void onSaveComplete(int requestCode) {
        super.onSaveComplete(requestCode);
        switch (requestCode) {
            case RC_BUILD_PROJECT:
                if (mProject != null) {
                    if (mProject instanceof AndroidAppProject) {
                        compileAndroidProject();
                    } else {
                        compileJavaProject();
                    }
                } else {
                    Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
                }
                break;
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
//            builder.setStdOut(mMessagePresenter.getStdOut());
//            builder.setStdErr(mMessagePresenter.getStdErr());
//            builder.setLogger(mMessagePresenter);
            builder.setStdOut(System.out);
            builder.setStdErr(System.err);
            builder.setLogger(new StdLogger(StdLogger.Level.VERBOSE));

            final BuildTask<AndroidAppProject> buildTask = new BuildTask<>(builder, new BuildTask.CompileListener<AndroidAppProject>() {
                @Override
                public void onStart() {
                    updateUiStartCompile();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(JavaIdeActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                    openDrawer(GravityCompat.START);
                    updateUIFinish();
                }

                @Override
                public void onComplete() {
                    updateUIFinish();
                    Toast.makeText(JavaIdeActivity.this, R.string.build_success, Toast.LENGTH_SHORT).show();
                    mFilePresenter.refresh(mProject);
//                    mDiagnosticPresenter.hidePanel();
                    RootUtils.installApk(JavaIdeActivity.this, ((AndroidAppProject) mProject).getApkSigned());
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
//        builder.setStdOut(mMessagePresenter.getStdOut());
//        builder.setStdErr(mMessagePresenter.getStdErr());

        builder.setStdOut(System.out);
        builder.setStdErr(System.err);

        final BuildTask.CompileListener<JavaProject> listener = new BuildTask.CompileListener<JavaProject>() {
            @Override
            public void onStart() {
                updateUiStartCompile();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(JavaIdeActivity.this, R.string.failed_msg, Toast.LENGTH_SHORT).show();
                openDrawer(GravityCompat.START);
//                mBottomPage.setCurrentItem(DiagnosticFragment.INDEX);
                updateUIFinish();
            }

            @Override
            public void onComplete() {
                updateUIFinish();
                Toast.makeText(JavaIdeActivity.this, R.string.compile_success, Toast.LENGTH_SHORT).show();
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
                    Intent intent = new Intent(JavaIdeActivity.this, ExecuteActivity.class);
                    intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
                    intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, javaSources.get(which));
                    startActivity(intent);
                }
            });
            builder.create().show();
        } else {
            Intent intent = new Intent(JavaIdeActivity.this, ExecuteActivity.class);
            intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
            intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, currentFile);
            startActivity(intent);
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
    public void previewLayout(String path) {
        saveAll(0);
        // TODO: 09-Jun-18 save all
        File currentFile = getCurrentFile();
        if (currentFile != null) {
            DialogLayoutPreview dialogPreview = DialogLayoutPreview.newInstance(currentFile);
            dialogPreview.show(getSupportFragmentManager(), DialogLayoutPreview.TAG);
        } else {
            Toast.makeText(this, "Can not find file", Toast.LENGTH_SHORT).show();
        }
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
        setMenuStatus(R.id.action_run, MenuDef.STATUS_DISABLED);
        if (mCompileProgress != null) {
            mCompileProgress.setVisibility(View.VISIBLE);
        }

        hideKeyboard();

//        mMessagePresenter.resume((JavaApplication) getApplication());
//        mMessagePresenter.clear();
//        mMessagePresenter.append("Compiling...\n");

//        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mDiagnosticPresenter.showPanel();
        mDiagnosticPresenter.clear();

//        mBottomPage.setCurrentItem(0);
    }

    private void updateUIFinish() {
        setMenuStatus(R.id.action_run, MenuDef.STATUS_NORMAL);
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