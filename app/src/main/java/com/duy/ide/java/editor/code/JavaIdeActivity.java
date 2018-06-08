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

import android.content.DialogInterface;
import android.content.Intent;
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
import com.duy.ide.java.MenuEditor;
import com.duy.ide.java.utils.RootUtils;
import com.duy.ide.javaide.autocomplete.JavaAutoCompleteProvider;
import com.duy.ide.javaide.autocomplete.util.JavaUtil;
import com.duy.ide.javaide.run.activities.ExecuteActivity;
import com.duy.ide.javaide.run.dialog.DialogRunConfig;
import com.duy.ide.javaide.sample.activities.JavaSampleActivity;
import com.duy.ide.javaide.uidesigner.inflate.DialogLayoutPreview;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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
    private MenuEditor mMenuEditor;
    private ProgressBar mCompileProgress;
    private JavaAutoCompleteProvider mAutoCompleteProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMenuEditor = new MenuEditor(this, this);
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
        // TODO: 09-Jun-18 java code format
        return super.getCodeFormatProvider();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = mMenuEditor.onOptionsItemSelected(item);
        return handled || super.onOptionsItemSelected(item);
    }

    @Override
    public void invalidateOptionsMenu() {
        super.invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return mMenuEditor.onCreateOptionsMenu(menu);
    }

    @Override
    public void runProject() {
        saveAll(RC_BUILD_PROJECT);
    }

    @Override
    protected void onSaveComplete(int requestCode) {
        super.onSaveComplete(requestCode);
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
//            builder.setStdOut(mMessagePresenter.getStdOut());
//            builder.setStdErr(mMessagePresenter.getStdErr());
//            builder.setLogger(mMessagePresenter);

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
    public void runFile(String filePath) {
        saveAll(0);
        // TODO: 09-Jun-18 save all
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
        if (mCompileProgress != null) mCompileProgress.setVisibility(View.VISIBLE);
        hideKeyboard();
        openDrawer(GravityCompat.START);

//        mMessagePresenter.resume((JavaApplication) getApplication());
//        mMessagePresenter.clear();
//        mMessagePresenter.append("Compiling...\n");

        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mDiagnosticPresenter.clear();

//        mBottomPage.setCurrentItem(0);

    }

    private void updateUIFinish() {
//        mMessagePresenter.pause((JavaApplication) getApplication());
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