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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.AndroidProjectManager;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.file.explorer.FileExplorerActivity;
import com.duy.ide.R;
import com.duy.ide.core.IdeActivity;
import com.duy.ide.java.EditPageContract;
import com.duy.ide.java.EditorControl;
import com.duy.ide.java.PagePresenter;
import com.duy.ide.java.file.FileManager;
import com.duy.ide.java.file.FileUtils;
import com.duy.projectview.ProjectFileContract;
import com.duy.projectview.ProjectFilePresenter;
import com.duy.projectview.view.dialog.DialogNewAndroidProject;
import com.duy.projectview.view.dialog.DialogNewAndroidResource;
import com.duy.projectview.view.dialog.DialogNewClass;
import com.duy.projectview.view.dialog.DialogNewFolder;
import com.duy.projectview.view.dialog.DialogNewJavaProject;
import com.duy.projectview.view.dialog.DialogSelectType;
import com.duy.projectview.view.fragments.FolderStructureFragment;
import com.jecelyin.editor.v2.editor.EditorDelegate;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;

import static com.duy.projectview.ProjectFileContract.Callback;
import static com.duy.projectview.ProjectFileContract.FileActionListener;

/**
 * Created by Duy on 09-Mar-17.
 */
public abstract class ProjectManagerActivity extends IdeActivity
        implements
        EditorControl,
        FileActionListener,
        DialogNewJavaProject.OnCreateProjectListener,
        DialogSelectType.OnFileTypeSelectListener {
    private static final String TAG = "BaseEditorActivity";

    /*Constants*/
    private static final int REQUEST_OPEN_JAVA_PROJECT = 2;
    private static final int REQUEST_OPEN_ANDROID_PROJECT = 3;
    private static final int REQUEST_PICK_FILE = 4;

    protected final Handler mHandler = new Handler();

    protected FileManager mFileManager;
    protected SlidingUpPanelLayout mContainerOutput;
    protected JavaProject mProject;
    protected ProjectFileContract.Presenter mFilePresenter;
    protected PagePresenter mPagePresenter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mProject == null) {
            this.mProject = JavaProjectManager.getLastProject(this);
        }
        bindView();
        setupToolbar();
        setupFileView(savedInstanceState);
        FragmentManager fm = getSupportFragmentManager();

//        List<PageDescriptor> pageDescriptors = new ArrayList<>();
//        pageDescriptors.add(new SimplePageDescriptor(MessageFragment.TAG, "Message"));
//        pageDescriptors.add(new SimplePageDescriptor(DiagnosticFragment.TAG, "Diagnostic"));
//        BottomPageAdapter bottomAdapter = new BottomPageAdapter(fm, pageDescriptors);
//
//        mBottomPage = findViewById(R.id.bottom_page);
//        mBottomPage.setAdapter(bottomAdapter);
//        mBottomPage.setOffscreenPageLimit(bottomAdapter.getCount());

//        TabLayout bottomTab = findViewById(R.id.bottom_tab);
//        bottomTab.setupWithViewPager(mBottomPage);
//
        //create project if need
        createProjectIfNeed();
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.activity_default_ide;
    }

    private void createProjectIfNeed() {
        if (mProject == null) {
            showDialogCreateJavaProject();
        }
    }

    private void setupFileView(Bundle savedInstanceState) {
        FolderStructureFragment folderStructureFragment = null;
        if (savedInstanceState != null) {
            folderStructureFragment = (FolderStructureFragment)
                    getSupportFragmentManager().findFragmentByTag(FolderStructureFragment.TAG);
        }
        if (folderStructureFragment == null) {
            folderStructureFragment = FolderStructureFragment.newInstance(mProject);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container_file, folderStructureFragment, FolderStructureFragment.TAG).commit();
        mFilePresenter = new ProjectFilePresenter(folderStructureFragment);
    }


    protected void bindView() {
        mFileManager = new FileManager(this);
        mContainerOutput = findViewById(R.id.sliding_layout);
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

    /**
     * remove a page in <code>position</code>
     */
    protected void removePage(int position) {
        mPagePresenter.removePage(position);
    }

    /**
     * Add new page for editor
     * Check if not in list file, add it to tab and select tab of file
     *
     * @param file - file need load
     */
    protected void addNewPageEditor(@NonNull File file) {
        mPagePresenter.addPage(file, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPagePresenter.pause();
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
                mPagePresenter.removePage(file.getPath());
                boolean success = mFileManager.deleteFile(file);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeKeyBoard();
        mFileManager.destroy();
    }

    // closes the soft keyboard
    protected void closeKeyBoard() throws NullPointerException {
        // Central system API to the overall input method framework (IMF) architecture
        InputMethodManager inputManager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            // Base interface for a remotable object
            IBinder windowToken = currentFocus.getWindowToken();

            // Hide type
            int hideType = InputMethodManager.HIDE_NOT_ALWAYS;

            // Hide the KeyBoard
            inputManager.hideSoftInputFromWindow(windowToken, hideType);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
//        while (mPageAdapter.getCount() > 0) {
//            removePage(0);
//            getTabManager().newTab()
//        }

        //show file structure of project
        mFilePresenter.show(projectFile, true);
//        mBottomPage.setCurrentItem(0);
        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
            addNewPageEditor(file);
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
    public boolean clickCreateNewFile(File file, Callback callBack) {
        showDialogSelectFileType(file);
        return false;
    }

    @Override
    public void clickNewModule() {
        if (mProject != null) {
//            showDialogSelectFileType(mPageAdapter);
        } else {
            Toast.makeText(this, "Please create new project", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onFileTypeSelected(File currentDir, String type) {
        if (type.equals(getString(R.string.java_file))) {
            showDialogCreateNewClass(currentDir);
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
                    File parent = new File(file).getParentFile();
//                    DialogManager.showDialogCopyFile(file, this, new Callback() {
//                        @Override
//                        public void onSuccess(File file) {
//                            mFilePresenter.refresh(mProject);
//                        }
//
//                        @Override
//                        public void onFailed(@Nullable Exception e) {
//                            if (e != null) {
//                                Toast.makeText(ProjectManagerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
                break;
            case REQUEST_OPEN_JAVA_PROJECT: {
                if (resultCode == RESULT_OK) {
                    String file = FileExplorerActivity.getFile(data);
                    JavaProjectManager javaProjectManager = new JavaProjectManager(this);
                    try {
                        JavaProject javaProject = javaProjectManager.loadProject(new File(file), true);
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

    public void showDialogCreateNewClass(@Nullable File file) {
        if (file == null) {
            EditPageContract.SourceView currentPage = mPagePresenter.getCurrentPage();
            if (currentPage != null) {
                file = currentPage.getCurrentFile().getParentFile();
            }
        }
        if (mProject != null && file != null) {
            DialogNewClass dialogNewClass;
            dialogNewClass = DialogNewClass.newInstance(mProject, null, file);
            dialogNewClass.show(getSupportFragmentManager(), DialogNewClass.TAG);
        } else {
            toast("Can not create new class");
        }
    }

    public void showDialogOpenJavaProject() {
        FileExplorerActivity.startPickPathActivity(this, FileManager.EXTERNAL_DIR,
                null, REQUEST_OPEN_JAVA_PROJECT);
    }

    public void showDialogOpenAndroidProject() {
        FileExplorerActivity.startPickPathActivity(this, FileManager.EXTERNAL_DIR,
                null, REQUEST_OPEN_ANDROID_PROJECT);

    }

    public void showDialogCreateJavaProject() {
        DialogNewJavaProject dialogNewProject = DialogNewJavaProject.newInstance();
        dialogNewProject.show(getSupportFragmentManager(), DialogNewJavaProject.TAG);
    }

    public void showDialogCreateAndroidProject() {
        DialogNewAndroidProject dialogNewProject = DialogNewAndroidProject.newInstance();
        dialogNewProject.show(getSupportFragmentManager(), DialogNewAndroidProject.TAG);
    }

    public void showDialogSelectFileType(@Nullable File parent) {
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
