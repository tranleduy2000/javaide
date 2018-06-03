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

package com.duy.ide.editor.code;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.AndroidProjectManager;
import com.duy.android.compiler.project.ClassFile;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.EditPageContract;
import com.duy.ide.EditorControl;
import com.duy.ide.PagePresenter;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.diagnostic.DiagnosticFragment;
import com.duy.ide.diagnostic.DiagnosticPresenter;
import com.duy.ide.diagnostic.MessageFragment;
import com.duy.ide.diagnostic.MessagePresenter;
import com.duy.ide.editor.code.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.FileUtils;
import com.duy.ide.setting.AppSetting;
import com.duy.ide.view.SymbolListView;
import com.duy.projectview.ProjectFilePresenter;
import com.duy.projectview.ProjectManager;
import com.duy.projectview.view.dialog.DialogManager;
import com.duy.projectview.view.dialog.DialogNewAndroidProject;
import com.duy.projectview.view.dialog.DialogNewAndroidResource;
import com.duy.projectview.view.dialog.DialogNewClass;
import com.duy.projectview.view.dialog.DialogNewFolder;
import com.duy.projectview.view.dialog.DialogNewJavaProject;
import com.duy.projectview.view.dialog.DialogSelectType;
import com.duy.projectview.view.fragments.FolderStructureFragment;
import com.jecelyin.android.file_explorer.FileExplorerActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static com.duy.projectview.ProjectFileContract.Callback;
import static com.duy.projectview.ProjectFileContract.FileActionListener;
import static com.duy.projectview.ProjectFileContract.Presenter;
import static com.duy.projectview.view.fragments.FolderStructureFragment.newInstance;

/**
 * Created by Duy on 09-Mar-17.
 */
public abstract class ProjectManagerActivity extends AbstractAppCompatActivity
        implements SymbolListView.OnKeyListener,
        EditorControl,
        FileActionListener,
        DialogNewJavaProject.OnCreateProjectListener,
        DialogSelectType.OnFileTypeSelectListener {
    private static final String TAG = "BaseEditorActivity";

    /*Constants*/
    private static final String KEY_PROJECT_FILE = "KEY_PROJECT_FILE";
    private static final int REQUEST_OPEN_JAVA_PROJECT = 2;
    private static final int REQUEST_OPEN_ANDROID_PROJECT = 3;
    private static final int REQUEST_PICK_FILE = 4;

    protected final boolean SELECT = true;
    protected final Handler mHandler = new Handler();

    protected FileManager mFileManager;
    protected EditorPagerAdapter mPageAdapter;
    protected SlidingUpPanelLayout mContainerOutput;
    protected JavaProject mProject;
    protected Presenter mFilePresenter;
    protected ViewPager mBottomPage;
    protected PagePresenter mPagePresenter;
    protected DiagnosticPresenter mDiagnosticPresenter;
    protected MessagePresenter mMessagePresenter;
    protected AppBarLayout appBarLayout;
    protected DrawerLayout mDrawerLayout;
    protected NavigationView navigationView;
    protected TabLayout mTabLayout;
    protected Toolbar toolbar;
    @Nullable
    protected View mContainerSymbol; //don't support in landscape mode
    @Nullable
    protected SymbolListView mKeyList;
    protected ViewPager mViewPager;
    private KeyBoardEventListener mKeyBoardListener;

    protected void onShowKeyboard() {
        mTabLayout.setVisibility(View.GONE);
        AppSetting preferences = getPreferences();
        if (preferences.isShowListSymbol()) {
            if (mContainerSymbol != null) {
                mContainerSymbol.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void onHideKeyboard() {
        mTabLayout.setVisibility(View.VISIBLE);
        if (mContainerSymbol != null) {
            mContainerSymbol.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            this.mProject = (JavaProject) savedInstanceState.getSerializable(KEY_PROJECT_FILE);
        } else {
            this.mProject = ProjectManager.getLastProject(this);
        }
        bindView();
        setupToolbar();
        setupFileView(savedInstanceState);
        setupEditor();
        FragmentManager fm = getSupportFragmentManager();

        List<PageDescriptor> pageDescriptors = new ArrayList<>();
        pageDescriptors.add(new SimplePageDescriptor(MessageFragment.TAG, "Message"));
        pageDescriptors.add(new SimplePageDescriptor(DiagnosticFragment.TAG, "Diagnostic"));
        BottomPageAdapter bottomAdapter = new BottomPageAdapter(fm, pageDescriptors);

        mBottomPage = findViewById(R.id.bottom_page);
        mBottomPage.setAdapter(bottomAdapter);
        mBottomPage.setOffscreenPageLimit(bottomAdapter.getCount());

        mMessagePresenter = new MessagePresenter(this, bottomAdapter);
        mDiagnosticPresenter = new DiagnosticPresenter(this, bottomAdapter, mPagePresenter);

        TabLayout bottomTab = findViewById(R.id.bottom_tab);
        bottomTab.setupWithViewPager(mBottomPage);

        //create project if need
        createProjectIfNeed();
    }

    private void createProjectIfNeed() {
        if (mProject == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialogCreateJavaProject();
                }
            }, 100);
        }
    }

    private void setupFileView(Bundle savedInstanceState) {
        FolderStructureFragment folderStructureFragment = null;
        if (savedInstanceState != null) {
            folderStructureFragment = (FolderStructureFragment)
                    getSupportFragmentManager().findFragmentByTag(FolderStructureFragment.TAG);
        }
        if (folderStructureFragment == null) {
            folderStructureFragment = newInstance(mProject);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container_file, folderStructureFragment, FolderStructureFragment.TAG).commit();
        mFilePresenter = new ProjectFilePresenter(folderStructureFragment);
    }

    private void setupEditor() {
        Set<File> editorFiles = mFileManager.getEditorFiles();
        ArrayList<PageDescriptor> descriptors = new ArrayList<>();
        if (mProject != null) {
            for (File editorFile : editorFiles) {
                descriptors.add(new SimplePageDescriptor(editorFile.getPath(), editorFile.getName()));
            }
        } else {
            for (File editorFile : editorFiles) {
                mFileManager.removeTabFile(editorFile.getPath());
            }
        }
        mPageAdapter = new EditorPagerAdapter(getSupportFragmentManager(), descriptors);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setOffscreenPageLimit(mPageAdapter.getCount());
        mTabLayout.setupWithViewPager(mViewPager);

        mPagePresenter = new PagePresenter((MainActivity) this, mViewPager, mPageAdapter, mTabLayout, mFileManager);
        mPagePresenter.invalidateTab();
    }

    protected void bindView() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mKeyList = findViewById(R.id.recycler_view);
        mFileManager = new FileManager(this);
        navigationView = findViewById(R.id.navigation_view);
        mTabLayout = findViewById(R.id.tab_layout);
        mContainerSymbol = findViewById(R.id.container_symbol);
        mViewPager = findViewById(R.id.view_pager);
        mContainerOutput = findViewById(R.id.sliding_layout);
    }

    public void setupToolbar() {
        //setup action bar
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT) {
            ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            // Set the drawer toggle as the DrawerListener
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }

        //attach listener hide/show keyboard
        mKeyBoardListener = new KeyBoardEventListener(this);
        mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(mKeyBoardListener);
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
     * @param file          - file need load
     * @param selectNewPage - if <code>true</code>, the tab of file will be selected when initialized
     */
    protected void addNewPageEditor(@NonNull File file, boolean selectNewPage) {
        mPagePresenter.addPage(file, selectNewPage);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPagePresenter.pause();
        if (mProject != null) {
            ProjectManager.saveProject(this, mProject);
        }
    }


    protected abstract String getCode();

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
        EditorFragment editorFragment = mPageAdapter.getCurrentFragment();
        if (editorFragment != null) {
            String filePath = editorFragment.getFilePath();
            return new File(filePath);
        }
        return null;
    }

    @Override
    public void saveAs() {
        saveCurrentFile();
        final AppCompatEditText edittext = new AppCompatEditText(this);
        edittext.setHint(R.string.enter_new_file_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.save_as)
                .setView(edittext)
                .setIcon(R.drawable.ic_create_new_folder_white_24dp)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String fileName = edittext.getText().toString();
                        dialog.cancel();
                        File currentFile = getCurrentFile();
                        if (currentFile != null) {
                            try {
                                mFileManager.copy(currentFile.getPath(),
                                        currentFile.getParent() + "/" + fileName);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(ProjectManagerActivity.this, R.string.can_not_save_file,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
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
    public void saveAllFile() {
        for (int i = 0; i < mPageAdapter.getCount(); i++) {
            EditorFragment fm = mPageAdapter.getExistingFragment(i);
            if (fm != null) {
                fm.saveFile();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeKeyBoard();
        mDrawerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(mKeyBoardListener);
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
        outState.putSerializable(KEY_PROJECT_FILE, mProject);
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
        this.mProject = projectFile;
        ProjectManager.saveProject(this, projectFile);

        //remove all edit page
        while (mPageAdapter.getCount() > 0) {
            removePage(0);
        }

        //show file structure of project
        mFilePresenter.show(projectFile, true);
        mBottomPage.setCurrentItem(0);
        mContainerOutput.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mMessagePresenter.clear();
        mDiagnosticPresenter.clear();
        openDrawer(GravityCompat.START);

        ClassFile mainClass = projectFile.getMainClass();
        if (mainClass != null && mainClass.exist(projectFile)) {
            //add to database
            addNewPageEditor(new File(mainClass.getPath(projectFile)), true);
        }
    }

    protected abstract void startAutoCompleteService();

    @Override
    public void onNewFileCreated(@NonNull File file) {
        mFilePresenter.refresh(mProject);
        if (file.isFile()) addNewPageEditor(file, true);
    }

    @Override
    public void onFileClick(@NonNull File file, Callback callBack) {
        if (FileUtils.canEdit(file)) {
            //save current file
            addNewPageEditor(file, SELECT);
            //close drawer
            mDrawerLayout.closeDrawers();
        } else {
            boolean success = openFileByAnotherApp(file);
            if (!success) {
                showFileInfo(file);
            }
        }
    }

    private boolean openFileByAnotherApp(File file) {
        //don't open compiled file
        if (FileUtils.hasExtension(file, "class", "dex", "jar")) {
            Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
            return false;
        }
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
        return true;
    }

    @Override
    public void onFileLongClick(@NonNull File file, Callback callBack) {
        if (FileUtils.canRead(file)) {
            showFileInfo(file);
        } else {
            if (!openFileByAnotherApp(file)) {
                showFileInfo(file);
            }
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

    /**
     * show dialog with file info
     * filePath, path, size, extension ...
     *
     * @param file - file to show info
     */
    private void showFileInfo(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(file.getName());
        builder.setView(R.layout.dialog_view_file);
        AlertDialog dialog = builder.create();
        dialog.show();
        TextView txtInfo = dialog.findViewById(R.id.txt_info);
        txtInfo.setText(file.getPath() + "\n" +
                file.length() + " byte");
        EditorView editorView = dialog.findViewById(R.id.editor_view);
        if (editorView != null && FileUtils.canEdit(file)) {
            editorView.setTextHighlighted(mFileManager.fileToString(file));
        }
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

    @Override
    public void onFileTypeSelected(File parent, String type) {
        if (type.equals(getString(R.string.java_file))) {
            showDialogCreateNewClass(parent);
        } else if (type.equals(getString(R.string.xml_file))) {
            showDialogCreateNewXml(parent);
        } else if (type.equals(getString(R.string.from_storage))) {
            String path = Environment.getExternalStorageDirectory().getPath();
            FileExplorerActivity.startPickFileActivity(this, path, REQUEST_PICK_FILE, parent);
        } else if (type.equals(getString(R.string.new_folder))) {
            showDialogCreateNewFolder(parent);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    String selectedFile = FileExplorerActivity.getFile(data);
                    File parentFile = FileExplorerActivity.getParentFile(data);
                    DialogManager.showDialogCopyFile(selectedFile, parentFile, this, new Callback() {
                        @Override
                        public void onSuccess(File file) {
                            mFilePresenter.refresh(mProject);
                        }

                        @Override
                        public void onFailed(@Nullable Exception e) {
                            if (e != null) {
                                Toast.makeText(ProjectManagerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            case REQUEST_OPEN_JAVA_PROJECT: {
                if (resultCode == RESULT_OK) {
                    String file = FileExplorerActivity.getFile(data);
                    JavaProject pf = ProjectManager.createProjectIfNeed(getApplicationContext(),
                            new File(file));
                    if (pf != null) onProjectCreated(pf);
                    else
                        Toast.makeText(this, "Can not import project", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case REQUEST_OPEN_ANDROID_PROJECT: {
                if (resultCode == RESULT_OK) {
                    AndroidProjectManager androidProjectManager = new AndroidProjectManager(this);
                    String file = FileExplorerActivity.getFile(data);
                    AndroidAppProject project;
                    try {
                        project = androidProjectManager.loadProject(new File(file), true);
                    } catch (IOException e) {
                        project = null;
                        e.printStackTrace();
                    }
                    if (project != null) {
                        onProjectCreated(project);
                    } else {
                        Toast.makeText(this, "Can not import project", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
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

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showDialogOpenJavaProject() {
        FileExplorerActivity.startPickPathActivity(this, FileManager.EXTERNAL_DIR,
                null, REQUEST_OPEN_JAVA_PROJECT);
    }

    public void showDialogOpenAndroidProject() {
        FileExplorerActivity.startPickPathActivity(this, FileManager.EXTERNAL_DIR,
                null, REQUEST_OPEN_ANDROID_PROJECT);

    }


    public void closeDrawer(int start) {
        if (mDrawerLayout.isDrawerOpen(start)) mDrawerLayout.closeDrawer(start);
    }


    /**
     * Listener keyboard hide/show
     * if the keyboard is showing, we will hide the toolbar for more space
     */
    private class KeyBoardEventListener implements ViewTreeObserver.OnGlobalLayoutListener {
        ProjectManagerActivity activity;

        KeyBoardEventListener(ProjectManagerActivity activityIde) {
            this.activity = activityIde;
        }

        public void onGlobalLayout() {
            int i = 0;
            int navHeight = this.activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            navHeight = navHeight > 0 ? this.activity.getResources().getDimensionPixelSize(navHeight) : 0;
            int statusBarHeight = this.activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarHeight > 0) {
                i = this.activity.getResources().getDimensionPixelSize(statusBarHeight);
            }
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (activity.mDrawerLayout.getRootView().getHeight() - ((navHeight + i) + rect.height()) <= 0) {
                activity.onHideKeyboard();
            } else {
                activity.onShowKeyboard();
            }
        }
    }
}
