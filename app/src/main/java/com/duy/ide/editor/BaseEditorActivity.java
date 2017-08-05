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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.TextView;
import android.widget.Toast;

import com.commonsware.cwac.pager.PageDescriptor;
import com.commonsware.cwac.pager.SimplePageDescriptor;
import com.duy.compile.diagnostic.DiagnosticFragment;
import com.duy.compile.diagnostic.DiagnosticPresenter;
import com.duy.compile.message.MessageFragment;
import com.duy.compile.message.MessagePresenter;
import com.duy.ide.EditorControl;
import com.duy.ide.PagePresenter;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.adapters.BottomPageAdapter;
import com.duy.ide.editor.view.EditorView;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.FileUtils;
import com.duy.ide.setting.JavaPreferences;
import com.duy.ide.view.SymbolListView;
import com.duy.project.dialog.DialogNewClass;
import com.duy.project.dialog.DialogNewProject;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFile;
import com.duy.project.file.java.ProjectFileContract;
import com.duy.project.file.java.ProjectFilePresenter;
import com.duy.project.file.java.ProjectManager;
import com.duy.project.fragments.FolderStructureFragment;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by Duy on 09-Mar-17.
 */
public abstract class BaseEditorActivity extends AbstractAppCompatActivity
        implements SymbolListView.OnKeyListener, EditorControl,
        ProjectFileContract.FileActionListener,
        DialogNewProject.OnCreateProjectListener, DialogNewClass.OnCreateClassListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "BaseEditorActivity";

    private static final String KEY_PROJECT_FILE = "KEY_PROJECT_FILE";
    protected final boolean SELECT = true;

    protected final Handler mHandler = new Handler();
    protected FileManager mFileManager;
    protected EditorPagerAdapter mPageAdapter;
    protected SlidingUpPanelLayout mContainerOutput;
    protected JavaProjectFile mProjectFile;
    protected ProjectFileContract.Presenter mFilePresenter;

    protected ViewPager mBottomPage;
    protected PagePresenter mPagePresenter;
    protected DiagnosticPresenter mDiagnosticPresenter;
    protected MessagePresenter mMessagePresenter;
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    TabLayout mTabLayout;
    @Nullable
    View mContainerSymbol; //don't support in landscape mode
    @Nullable
    SymbolListView mKeyList;
    ViewPager mViewPager;
    private KeyBoardEventListener keyBoardListener;
    private MessageFragment mMessageFragment;
    private DiagnosticFragment mDiagnosticFragment;

    protected void onShowKeyboard() {
        mTabLayout.setVisibility(View.GONE);
        JavaPreferences preferences = getPreferences();
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
            this.mProjectFile = (JavaProjectFile) savedInstanceState.getSerializable(KEY_PROJECT_FILE);
        } else {
            this.mProjectFile = ProjectManager.getLastProject(this);
        }
        bindView();
        setupToolbar();
        setupFileView(savedInstanceState);
        setupEditor();
        FragmentManager fm = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mMessageFragment = (MessageFragment) fm.findFragmentByTag(MessageFragment.TAG);
            mDiagnosticFragment = (DiagnosticFragment) fm.findFragmentByTag(DiagnosticFragment.TAG);
        }
        if (mMessageFragment == null) {
            mMessageFragment = MessageFragment.newInstance();
        }
        mMessagePresenter = new MessagePresenter(this, mMessageFragment);
        if (mDiagnosticFragment == null) {
            mDiagnosticFragment = DiagnosticFragment.newInstance();
        }
        mDiagnosticPresenter = new DiagnosticPresenter(this, mDiagnosticFragment, mPagePresenter);


        BottomPageAdapter bottomAdapter = new BottomPageAdapter(fm, mDiagnosticFragment, mMessageFragment);

        mBottomPage = (ViewPager) findViewById(R.id.bottom_page);
        mBottomPage.setAdapter(bottomAdapter);
        mBottomPage.getAdapter().notifyDataSetChanged();
        mBottomPage.setOffscreenPageLimit(BottomPageAdapter.COUNT);

        TabLayout bottomTab = (TabLayout) findViewById(R.id.bottom_tab);
        bottomTab.setupWithViewPager(mBottomPage);

        //create project if need
        createProjectIfNeed();
    }

    private void createProjectIfNeed() {
        if (mProjectFile == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    showDialogCreateProject();
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
            folderStructureFragment = FolderStructureFragment.newInstance(mProjectFile);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container_file, folderStructureFragment, FolderStructureFragment.TAG).commit();
        mFilePresenter = new ProjectFilePresenter(folderStructureFragment);
    }

    private void setupEditor() {
        ArrayList<File> editorFiles = mFileManager.getEditorFiles();
        ArrayList<PageDescriptor> descriptors = new ArrayList<>();
        for (File editorFile : editorFiles) {
            descriptors.add(new SimplePageDescriptor(editorFile.getPath(), editorFile.getName()));
        }
        mPageAdapter = new EditorPagerAdapter(getSupportFragmentManager(), descriptors);
        mViewPager.setAdapter(mPageAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(this);

        mPagePresenter = new PagePresenter(this, mViewPager, mPageAdapter, mTabLayout, mFileManager);
        mPagePresenter.invalidateTab();
    }

    protected void bindView() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mKeyList = (SymbolListView) findViewById(R.id.recycler_view);
        mFileManager = new FileManager(this);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mContainerSymbol = findViewById(R.id.container_symbol);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mContainerOutput = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
    }

    public void setupToolbar() {
        //setup action bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);

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
        keyBoardListener = new KeyBoardEventListener(this);
        mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyBoardListener);
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
        if (mProjectFile != null) {
            ProjectManager.saveProject(this, mProjectFile);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent intent = getIntent();
//        if (intent != null) {
//            if (intent.getStringExtra(CompileManager.FILE_PATH) != null) {
//                String filePath = intent.getStringExtra(CompileManager.FILE_PATH);
//                //No need save last file because it is the frist file
//                addNewPageEditor(new File(filePath), SELECT);
//                //Remove path
//                intent.removeExtra(CompileManager.FILE_PATH);
//            }
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        if (intent.getStringExtra(CompileManager.FILE_PATH) != null) {
//            String filePath = intent.getStringExtra(CompileManager.FILE_PATH);
//            File file = new File(filePath);
//            if (!file.exists()) {
//                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            addNewPageEditor(file, SELECT);
//            //remove path
//            intent.removeExtra(CompileManager.FILE_PATH);
//        }
    }

    protected abstract String getCode();

    /**
     * delete a file
     *
     * @param file - file need delete
     * @return true if delete success
     */
    @Override
    public boolean doRemoveFile(final File file, final ProjectFileContract.ActionCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.remove_file_msg) + " " + file.getName());
        builder.setTitle(R.string.delete_file);
        builder.setIcon(R.drawable.ic_delete_forever_white_24dp);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = mPageAdapter.getPositionForTag(file.getPath());
                boolean success = mFileManager.deleteFile(file);
                if (success) {
                    if (position >= 0) {
                        removePage(position);
                    }
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
                                Toast.makeText(BaseEditorActivity.this, R.string.can_not_save_file,
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
        mDrawerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(keyBoardListener);
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
        outState.putSerializable(KEY_PROJECT_FILE, mProjectFile);
    }


    public void openDrawer(int gravity) {
        try {
            mDrawerLayout.openDrawer(gravity);
        } catch (Exception e) {
            //not found drawer
        }
    }

    @Override
    public void onProjectCreated(@NonNull JavaProjectFile projectFile) {
        Log.d(TAG, "onProjectCreated() called with: projectFile = [" + projectFile + "]");

        //save project
        this.mProjectFile = projectFile;
        ProjectManager.saveProject(this, projectFile);

        //remove all edit page
        while (mPageAdapter.getCount() > 0) {
            removePage(0);
        }

        //show file structure of project
        mFilePresenter.show(projectFile, true);
        openDrawer(GravityCompat.START);

        ClassFile mainClass = projectFile.getMainClass();
        if (mainClass != null && mainClass.exist(projectFile)) {
            addNewPageEditor(new File(mainClass.getPath(projectFile)), true);
        }
    }

    @Override
    public void onClassCreated(File classF) {
        mFilePresenter.show(mProjectFile, true);
        addNewPageEditor(classF, true);
    }

    @Override
    public void onFileClick(File file, ProjectFileContract.ActionCallback callBack) {
        if (FileManager.canEdit(file)) {
            //save current file
            addNewPageEditor(file, SELECT);
            //close drawer
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public void onFileLongClick(File file, ProjectFileContract.ActionCallback callBack) {
        if (FileUtils.canRead(file)) {
            showFileInfo(file);
        }
    }

    @Override
    public boolean doCreateNewClass(File file, ProjectFileContract.ActionCallback callBack) {
        showDialogCreateClass(file);
        return false;
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
        TextView txtInfo = (TextView) dialog.findViewById(R.id.txt_info);
        txtInfo.setText(file.getPath());
        EditorView editorView = (EditorView) dialog.findViewById(R.id.editor_view);
        if (editorView != null) {
            editorView.setTextHighlighted(mFileManager.fileToString(file));
        }
    }

    public void showDialogCreateProject() {
        DialogNewProject dialogNewProject = DialogNewProject.newInstance();
        dialogNewProject.show(getSupportFragmentManager(), DialogNewProject.TAG);
    }

    public void showDialogCreateClass(@Nullable File file) {
        if (mProjectFile != null) {
            DialogNewClass dialogNewClass;
            if (file != null) {
                dialogNewClass = DialogNewClass.newInstance(mProjectFile, null, file);
            } else {
                dialogNewClass = DialogNewClass.newInstance(mProjectFile, mProjectFile.getPackageName(), null);
            }
            dialogNewClass.show(getSupportFragmentManager(), DialogNewClass.TAG);
        } else {
            complain("You need create project");
        }
    }

    void complain(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        EditorFragment fm = mPageAdapter.getExistingFragment(position);
        if (fm != null) {
            setTitle(fm.getTag());
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void closeDrawer(int start) {
        if (mDrawerLayout.isDrawerOpen(start)) {
            mDrawerLayout.closeDrawer(start);
        }
    }


    /**
     * Listener keyboard hide/show
     * if the keyboard is showing, we will hide the toolbar for more space
     */
    private class KeyBoardEventListener implements ViewTreeObserver.OnGlobalLayoutListener {
        BaseEditorActivity activity;

        KeyBoardEventListener(BaseEditorActivity activityIde) {
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
