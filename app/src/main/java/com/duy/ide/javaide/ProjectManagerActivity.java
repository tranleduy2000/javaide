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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
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
import com.duy.ide.core.IdeActivity;
import com.duy.ide.java.EditorControl;
import com.duy.ide.java.file.FileUtils;
import com.duy.projectview.ProjectFileContract;
import com.duy.projectview.ProjectFilePresenter;
import com.duy.projectview.view.dialog.DialogManager;
import com.duy.projectview.view.dialog.DialogNewAndroidProject;
import com.duy.projectview.view.dialog.DialogNewAndroidResource;
import com.duy.projectview.view.dialog.DialogNewClass;
import com.duy.projectview.view.dialog.DialogNewFolder;
import com.duy.projectview.view.dialog.DialogNewJavaProject;
import com.duy.projectview.view.dialog.DialogSelectType;
import com.duy.projectview.view.fragments.FolderStructureFragment;
import com.jecelyin.editor.v2.editor.EditorDelegate;
import com.jecelyin.editor.v2.editor.IEditorDelegate;

import java.io.File;
import java.io.IOException;

import static com.duy.projectview.ProjectFileContract.Callback;
import static com.duy.projectview.ProjectFileContract.FileActionListener;

/**
 * Created by Duy on 09-Mar-17.
 */
public abstract class ProjectManagerActivity extends IdeActivity
        implements EditorControl, FileActionListener,
        DialogNewJavaProject.OnCreateProjectListener,
        DialogSelectType.OnFileTypeSelectListener {
    private static final String TAG = "BaseEditorActivity";

    private static final int REQUEST_OPEN_JAVA_PROJECT = 58;
    private static final int REQUEST_OPEN_ANDROID_PROJECT = 704;
    private static final int REQUEST_PICK_FILE = 75;

    protected JavaProject mProject;

    protected ProjectFileContract.Presenter mFilePresenter;
    private File mLastSelectedDir = null;

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
    protected int getThemeId() {
        return R.style.AppThemeDark_NoActionBar;
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

    /**
     * delete a file
     *
     * @param file - file need delete
     * @return true if delete success
     */
    @Override
    public boolean clickRemoveFile(final File file, final Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.remove_file_msg) + " " + file.getName());
        builder.setTitle(R.string.delete_file);
        builder.setIcon(R.drawable.ic_delete_forever_white_24dp);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Pair<Integer, IEditorDelegate> position = mTabManager.getEditorDelegate(file);
                if (position != null) {
                    mTabManager.closeTab(position.first);
                }
                boolean success = true;
                try {
                    file.delete();
                } catch (Exception e) {
                    success = false;
                }
                if (success) {
                    callback.onSuccess(null);
                    Toast.makeText(getApplicationContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                    callback.onFailed(null);
                }

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
        return false;
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
        // TODO: 09-Jun-18 close last project
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
    public void onNewFileCreated(@NonNull File file) {
        mFilePresenter.refresh(mProject);
        openFile(file.getPath());
    }

    @Override
    public void onFileClick(@NonNull File file, Callback callBack) {
        if (FileUtils.canEdit(file)) {
            //save current file
            openFile(file.getPath());
            //close drawer
            closeDrawers();
        } else {
            openFileByAnotherApp(file);
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
    public boolean onClickNewButton(File file, Callback callback) {
        showDialogNew(file);
        return false;
    }

    @Override
    public void clickNewModule() {
        if (mProject != null) {
        } else {
            Toast.makeText(this, "Please create new project", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTypeSelected(File currentDir, String type) {
        mLastSelectedDir = currentDir;

        if (type.equals(getString(R.string.java_file))) {
            createNewClass(currentDir);

        } else if (type.equals(getString(R.string.xml_file))) {
            showDialogCreateNewXml(currentDir);

        } else if (type.equals(getString(R.string.select_from_storage))) {
            String path = Environment.getExternalStorageDirectory().getPath();
            FileExplorerActivity.startPickFileActivity(this, path, path, REQUEST_PICK_FILE);

        } else if (type.equals(getString(R.string.create_new_folder))) {
            showDialogCreateNewFolder(currentDir);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    String file = FileExplorerActivity.getFile(data);
                    if (file == null) {
                        return;
                    }
                    DialogManager.showDialogCopyFile(file, mLastSelectedDir, this,
                            new Callback() {
                                @Override
                                public void onSuccess(File file) {
                                    mFilePresenter.refresh(mProject);
                                }

                                @Override
                                public void onFailed(@Nullable Exception e) {
                                    if (e != null) {
                                        Toast.makeText(ProjectManagerActivity.this, e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
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

    /**
     * Show dialog create new folder
     *
     * @param file - current file, uses for determine current directory, it can be null
     */
    private void showDialogCreateNewFolder(@Nullable File file) {
        if (mProject != null && file != null) {
            DialogNewFolder newFolder = DialogNewFolder.newInstance(mProject, file);
            newFolder.show(getSupportFragmentManager(), DialogNewClass.TAG);
        } else {
            toast("Can not create new folder");
        }
    }

    private void showDialogCreateNewXml(File file) {
        if (mProject != null && file != null) {
            DialogNewAndroidResource dialog = DialogNewAndroidResource.newInstance(mProject, file);
            dialog.show(getSupportFragmentManager(), DialogNewClass.TAG);
        } else {
            toast("Can not create Android resource file");
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
            dialogNewClass = DialogNewClass.newInstance(mProject, null, folder);
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
        DialogSelectType dialogSelectType = DialogSelectType.newInstance(parent);
        dialogSelectType.show(getSupportFragmentManager(), DialogNewAndroidProject.TAG);
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void closeDrawer(int start) {
        if (mDrawerLayout.isDrawerOpen(start)) mDrawerLayout.closeDrawer(start);
    }


}
