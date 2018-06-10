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

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.AndroidProjectManager;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.file.explorer.FileExplorerActivity;
import com.duy.ide.R;
import com.duy.ide.core.api.IdeActivity;
import com.duy.ide.editor.EditorDelegate;
import com.duy.ide.editor.IEditorDelegate;
import com.duy.ide.javaide.projectview.ProjectFileContract;
import com.duy.ide.javaide.projectview.ProjectFilePresenter;
import com.duy.ide.javaide.projectview.dialog.DialogNewAndroidProject;
import com.duy.ide.javaide.projectview.dialog.DialogNewClass;
import com.duy.ide.javaide.projectview.dialog.DialogNewJavaProject;
import com.duy.ide.javaide.projectview.dialog.DialogSelectType;
import com.duy.ide.javaide.projectview.view.fragments.FolderStructureFragment;
import com.duy.ide.javaide.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 09-Mar-17.
 */
public abstract class ProjectManagerActivity extends IdeActivity
        implements FileChangeListener, DialogNewJavaProject.OnCreateProjectListener {
    private static final String TAG = "BaseEditorActivity";

    private static final int REQUEST_OPEN_JAVA_PROJECT = 58;
    private static final int REQUEST_OPEN_ANDROID_PROJECT = 704;

    protected JavaProject mProject;
    protected ProjectFileContract.Presenter mFilePresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupToolbar();
        createProjectIfNeed();

        mPreferences.setAppTheme(1);
        mPreferences.setEditorTheme("allure-contrast.json.properties");
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.activity_default_ide;
    }

    @Override
    public int getThemeId() {
        return R.style.AppThemeDark;
    }

    private void createProjectIfNeed() {
        if (mProject == null) {
            createJavaProject();
        }
    }

    @Override
    protected void initLeftNavigationView(@NonNull NavigationView nav) {
        super.initLeftNavigationView(nav);
        if (mProject == null) {
            mProject = JavaProjectManager.getLastProject(this);
        }

        String tag = FolderStructureFragment.TAG;
        FolderStructureFragment folderStructureFragment = FolderStructureFragment.newInstance(mProject);
        ViewGroup viewGroup = nav.findViewById(R.id.left_navigation_content);
        viewGroup.removeAllViews();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.left_navigation_content, folderStructureFragment, tag).commit();
        mFilePresenter = new ProjectFilePresenter(folderStructureFragment);

    }


    public void setupToolbar() {
//        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
//            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
//                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//            // Set the drawer toggle as the DrawerListener
//            mDrawerLayout.setDrawerListener(mDrawerToggle);
//            mDrawerToggle.syncState();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProject != null) {
            JavaProjectManager.saveProject(this, mProject);
        }
    }

    @Override
    public void onFileDeleted(File deleted) {
        Pair<Integer, IEditorDelegate> position = mTabManager.getEditorDelegate(deleted);
        if (position != null) {
            mTabManager.closeTab(position.first);
        }
    }

    /**
     * @return current file selected
     */
    @Nullable
    protected File getCurrentFile() {
        EditorDelegate editorFragment = getCurrentEditorDelegate();
        if (editorFragment != null) {
            return editorFragment.getDocument().getFile();
        }
        return null;
    }

    public void openDrawer(int gravity) {
        try {
            mDrawerLayout.openDrawer(gravity);
        } catch (Exception e) {
            //not found drawer
        }
    }

    @Override
    public void onProjectCreated(@NonNull JavaProject projectFile) {
        Log.d(TAG, "onProjectCreated() called with: projectFile = [" + projectFile + "]");

        //save project
        mProject = projectFile;
        JavaProjectManager.saveProject(this, projectFile);

        //remove all edit page
        mTabManager.closeAllTab();

        //show file structure of project
        mFilePresenter.show(projectFile, true);
        mDiagnosticPresenter.hidePanel();
        mDiagnosticPresenter.clear();

        openDrawer(GravityCompat.START);
        startAutoCompleteService();
    }

    protected abstract void startAutoCompleteService();

    @Override
    public void onFileCreated(File newFile) {
        mFilePresenter.refresh(mProject);
        openFile(newFile.getPath());
    }

    @Override
    public void doOpenFile(File toEdit) {
        if (FileUtils.canEdit(toEdit)) {
            //save current file
            openFile(toEdit.getPath());
            //close drawer
            closeDrawers();
        } else {
            openFileByAnotherApp(toEdit);
        }
    }

    private void openFileByAnotherApp(File file) {
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            } else {
                uri = Uri.fromFile(file);
            }
            //create intent open file
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String ext = FileUtils.fileExt(file.getPath());
            String mimeType = myMime.getMimeTypeFromExtension(ext != null ? ext : "");
            intent.setDataAndType(uri, mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_OPEN_JAVA_PROJECT: {
                if (resultCode == RESULT_OK) {
                    String path = FileExplorerActivity.getFile(data);
                    if (path == null) {
                        return;
                    }
                    JavaProjectManager manager = new JavaProjectManager(this);
                    try {
                        JavaProject javaProject = manager.loadProject(new File(path), true);
                        onProjectCreated(javaProject);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Can not import project. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case REQUEST_OPEN_ANDROID_PROJECT: {
                if (resultCode == RESULT_OK) {
                    AndroidProjectManager manager = new AndroidProjectManager(this);
                    String file = FileExplorerActivity.getFile(data);
                    try {
                        AndroidAppProject project = manager.loadProject(new File(file), true);
                        onProjectCreated(project);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Can not import project. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
        }
    }


    public void createNewClass(@Nullable File folder) {
        if (folder == null) {
            File file = getCurrentFile();
            if (file != null) {
                folder = file.getParentFile();
            }
        }
        if (mProject != null && folder != null) {
            DialogNewClass dialogNewClass;
            dialogNewClass = DialogNewClass.newInstance(mProject, mProject.getPackageName(),
                    folder);
            dialogNewClass.show(getSupportFragmentManager(), DialogNewClass.TAG);
        } else {
            toast("Can not create new class");
        }
    }

    public void openJavaProject() {
        String destPath = com.duy.android.compiler.env.Environment.getSdkAppDir().getAbsolutePath();
        FileExplorerActivity.startPickPathActivity(this, destPath,
                "UTF-8", REQUEST_OPEN_JAVA_PROJECT);
    }

    public void openAndroidProject() {
        String destPath = com.duy.android.compiler.env.Environment.getSdkAppDir().getAbsolutePath();
        FileExplorerActivity.startPickPathActivity(this, destPath,
                "UTF-8", REQUEST_OPEN_ANDROID_PROJECT);

    }

    public void createJavaProject() {
        DialogNewJavaProject dialogNewProject = DialogNewJavaProject.newInstance();
        dialogNewProject.show(getSupportFragmentManager(), DialogNewJavaProject.TAG);
    }

    public void createAndroidProject() {
        DialogNewAndroidProject dialogNewProject = DialogNewAndroidProject.newInstance();
        dialogNewProject.show(getSupportFragmentManager(), DialogNewAndroidProject.TAG);
    }

    public void showDialogNew(@Nullable File parent) {
        DialogSelectType dialogSelectType = DialogSelectType.newInstance(parent, new DialogSelectType.OnFileTypeSelectListener() {
            @Override
            public void onTypeSelected(File parent, String ext) {
            }
        });
        dialogSelectType.show(getSupportFragmentManager(), DialogNewAndroidProject.TAG);
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


}
