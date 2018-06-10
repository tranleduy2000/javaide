package com.duy.ide.javaide.projectview.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.interfaces.Predicate;
import com.duy.file.explorer.FileExplorerActivity;
import com.duy.ide.R;
import com.duy.ide.javaide.FileChangeListener;
import com.duy.ide.javaide.projectview.ProjectFileContract;
import com.duy.ide.javaide.projectview.dialog.DialogCopyFile;
import com.duy.ide.javaide.projectview.dialog.DialogDeleteFile;
import com.duy.ide.javaide.projectview.dialog.DialogNewAndroidProject;
import com.duy.ide.javaide.projectview.dialog.DialogNewAndroidResource;
import com.duy.ide.javaide.projectview.dialog.DialogNewClass;
import com.duy.ide.javaide.projectview.dialog.DialogNewFolder;
import com.duy.ide.javaide.projectview.dialog.DialogSelectType;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.app.Activity.RESULT_OK;
import static android.widget.FrameLayout.LayoutParams;
import static com.duy.android.compiler.env.Environment.getSdkAppDir;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FolderStructureFragment extends Fragment implements ProjectFileContract.View, ProjectFileContract.FileActionListener {
    public static final String TAG = "FolderStructureFragment";
    private static final int REQUEST_PICK_FILE = 498;

    private File mLastSelectedDir = null;
    @Nullable
    private FileChangeListener mParentListener;

    private ViewGroup mContainerView;
    private TextView mTxtProjectName;
    private AndroidTreeView mTreeView;

    private SharedPreferences mPreferences;
    @Nullable
    private JavaProject mProject;

    private TreeNode.TreeNodeClickListener mNodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
            File file = i.getFile();
            if (mParentListener != null && file.isFile()) {
                mParentListener.doOpenFile(file);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener mNodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
            File file = i.getFile();
            if (file.isFile()) {
                showFileInfo(file);
            } else if (file.isDirectory()) {
                showDialogNew(file);
            }
            return true;
        }
    };

    /**
     * Create folder view, project can be null, we will init after
     */
    public static FolderStructureFragment newInstance(@Nullable JavaProject projectFile) {
        FolderStructureFragment fragment = new FolderStructureFragment();
        fragment.setProject(projectFile);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_structure, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContainerView = view.findViewById(R.id.container);
        mTxtProjectName = view.findViewById(R.id.txt_project_name);

        view.findViewById(R.id.img_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        display(mProject, true);
        view.findViewById(R.id.img_expand_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTreeView != null) expand(mTreeView.getRoot());
            }
        });
        view.findViewById(R.id.img_collapse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTreeView != null) mTreeView.collapseAll();
            }
        });
        if (savedInstanceState != null) {
            if (mTreeView != null) {
                String state = savedInstanceState.getString("tState");
                if (!TextUtils.isEmpty(state)) {
                    mTreeView.restoreState(state);
                }
            }
        } else if (mTreeView != null) {
            String state = mPreferences.getString("tree_state", "");
            if (!state.isEmpty()) mTreeView.restoreState(state);
        }

        view.findViewById(R.id.img_add_dependencies).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNewModule();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_FILE:
                if (resultCode == RESULT_OK) {
                    String file = FileExplorerActivity.getFile(data);
                    if (file == null) {
                        return;
                    }
                    DialogCopyFile dialog = new DialogCopyFile(getContext(), file, mLastSelectedDir,
                            new ProjectFileContract.Callback() {
                                @Override
                                public void onSuccess(File file) {
                                    refresh();
                                }

                                @Override
                                public void onFailed(@Nullable Exception e) {
                                    if (e != null) {
                                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    dialog.show();
                }
                break;
        }
    }

    @Nullable
    private TreeNode createFileStructure(@Nullable JavaProject projectFile) {
        if (projectFile == null) return null;
        File rootDir = projectFile.getRootDir();
        TreeNode root = new TreeNode(new FolderHolder.TreeItem(rootDir, rootDir, this));
        try {
            root.addChildren(getNode(rootDir, rootDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }


    private ArrayList<TreeNode> getNode(File projectFile, File parent) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        try {
            if (parent.isDirectory()) {
                File[] child = parent.listFiles();

                if (child != null) {
                    ArrayList<File> dirs = new ArrayList<>();
                    ArrayList<File> files = new ArrayList<>();
                    for (File file : child) {
                        if (file.isFile() && !file.isHidden()) files.add(file);
                        if (file.isDirectory() && !file.isHidden()) dirs.add(file);
                    }
                    Collections.sort(dirs, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    for (File file : dirs) {
                        TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, file, this));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(projectFile, file));
                        }
                        nodes.add(node);
                    }
                    for (File file : files) {
                        TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, file, this));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(projectFile, file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, parent, this));
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTreeView != null) {
            outState.putString("tState", mTreeView.getSaveState());
        }
    }


    @Override
    public void display(JavaProject projectFile, boolean expand) {
        this.mProject = projectFile;

        if (mProject != null && mTxtProjectName != null) {
            boolean isAndroid = mProject instanceof AndroidAppProject;
            String text = (isAndroid ? "Android: " : "Java: ") + mProject.getProjectName();
            mTxtProjectName.setText(text);
        }

        TreeNode root = refresh();
        if (expand && mTreeView != null) {
            expand(root);
        }
    }

    private void expand(TreeNode root) {
        if (mTreeView == null || mProject == null) {
            return;
        }
        expandRecursive(root, new Predicate<TreeNode>() {
            @Override
            public boolean accept(TreeNode node) {
                FolderHolder.TreeItem value = (FolderHolder.TreeItem) node.getValue();
                if (value == null) {
                    return true;
                }
                File file = value.getFile();
                return !(file.getName().equals("build"));
            }
        });

    }

    private void expandRecursive(TreeNode node, Predicate<TreeNode> test) {
        if (test.accept(node)) {
            mTreeView.expandNode(node, false);
            if (node.getChildren().size() > 0) {
                for (TreeNode treeNode : node.getChildren()) {
                    expandRecursive(treeNode, test);
                }
            }
        }
    }


    @Override
    public TreeNode refresh() {
        if (mProject == null) {
            return null;
        }
        TreeNode root = TreeNode.root();
        TreeNode fileStructure = createFileStructure(mProject);
        if (fileStructure != null) {
            root.addChildren(fileStructure);
        }

        String saveState = null;
        if (mTreeView != null) {
            saveState = mTreeView.getSaveState();
        }

        mTreeView = new AndroidTreeView(getContext(), root);
        mTreeView.setDefaultAnimation(false);
        mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        mTreeView.setDefaultViewHolder(FolderHolder.class);
        mTreeView.setDefaultNodeClickListener(mNodeClickListener);
        mTreeView.setDefaultNodeLongClickListener(mNodeLongClickListener);
        if (saveState != null) {
            mTreeView.restoreState(saveState);
        }
        mContainerView.removeAllViews();
        View view = mTreeView.getView();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContainerView.addView(view, params);

        return root;
    }

    @Override
    public void setPresenter(ProjectFileContract.Presenter presenter) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mParentListener = (FileChangeListener) getActivity();
        } catch (ClassCastException ignored) {
        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onDestroyView() {
        if (mTreeView != null) {
            String saveState = mTreeView.getSaveState();
            mPreferences.edit().putString("tree_state", saveState).apply();
        }
        super.onDestroyView();
    }

    /**
     * show dialog with file info
     * filePath, path, size, extension ...
     *
     * @param file - file to show info
     */
    private void showFileInfo(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(file.getName());
        String message =
                "Path: " + file.getPath() + "\n" +
                        "Size: " + file.length() + " byte";
        builder.setMessage(message);
        builder.create().show();
    }

    public void setProject(JavaProject mProject) {
        this.mProject = mProject;
    }

    @Override
    public void onFileClick(@NonNull File file, @Nullable ProjectFileContract.Callback callBack) {

    }

    @Override
    public void onNewFileCreated(@NonNull File file) {
        if (mParentListener != null) {
            mParentListener.doOpenFile(file);
        }
    }

    @Override
    public void clickRemoveFile(final File file, final ProjectFileContract.Callback callback) {
        DialogDeleteFile dialogNewFile = new DialogDeleteFile(getContext(), file,
                new DialogDeleteFile.OnFileDeletedListener() {
                    @Override
                    public void onDeleteSuccess(File deleted) {
                        callback.onSuccess(file);
                        Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                        if (mParentListener != null) {
                            mParentListener.onFileDeleted(file);
                        }
                    }

                    @Override
                    public void onDeleteFailed(File deleted, Exception e) {
                        callback.onFailed(e);
                        Toast.makeText(getContext(), R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                });
        dialogNewFile.show();
    }

    @Override
    public void onClickNewButton(File file, ProjectFileContract.Callback callback) {
        showDialogNew(file);
    }

    @Override
    public void clickNewModule() {

    }


    private void showDialogNew(@NonNull File parent) {
        mLastSelectedDir = parent;
        DialogSelectType dialogSelectType = DialogSelectType.newInstance(parent,
                new DialogSelectType.OnFileTypeSelectListener() {
                    @Override
                    public void onTypeSelected(File currentDir, String type) {
                        if (type.equals(getString(R.string.java_file))) {
                            createNewClass(currentDir);

                        } else if (type.equals(getString(R.string.xml_file))) {
                            showDialogCreateNewXml(currentDir);

                        } else if (type.equals(getString(R.string.select_from_storage))) {
                            selectFromStorage(currentDir);

                        } else if (type.equals(getString(R.string.create_new_folder))) {
                            showDialogCreateNewFolder(currentDir);
                        }
                    }
                });
        dialogSelectType.show(getChildFragmentManager(), DialogNewAndroidProject.TAG);
    }

    private void selectFromStorage(File currentDir) {
        mLastSelectedDir = currentDir;
        String path = Environment.getExternalStorageDirectory().getPath();
        Intent intent = new Intent(getContext(), FileExplorerActivity.class);
        intent.putExtra(FileExplorerActivity.EXTRA_MODE, FileExplorerActivity.MODE_PICK_FILE);
        intent.putExtra(FileExplorerActivity.EXTRA_INIT_PATH, path);
        intent.putExtra(FileExplorerActivity.EXTRA_HOME_PATH, getSdkAppDir());
        intent.putExtra(FileExplorerActivity.EXTRA_ENCODING, "UTF-8");
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    private void showDialogCreateNewXml(@NonNull File file) {
        if (mProject != null) {
            DialogNewAndroidResource dialog = DialogNewAndroidResource.newInstance(file);
            dialog.show(getChildFragmentManager(), DialogNewClass.TAG);
        } else {
            Toast.makeText(getContext(), "Can not create Android resource file", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show dialog create new folder
     *
     * @param file - current file, uses for determine current directory, it can be null
     */
    private void showDialogCreateNewFolder(@NonNull File file) {
        if (mProject != null) {
            DialogNewFolder newFolder = DialogNewFolder.newInstance(file);
            newFolder.show(getChildFragmentManager(), DialogNewClass.TAG);
        } else {
            Toast.makeText(getContext(), "Can not create new folder", Toast.LENGTH_SHORT).show();
        }
    }

    public void createNewClass(@NonNull File folder) {
        if (mProject != null) {
            DialogNewClass dialogNewClass
                    = DialogNewClass.newInstance(mProject, mProject.getPackageName(), folder);
            dialogNewClass.show(getChildFragmentManager(), DialogNewClass.TAG);
        } else {
            Toast.makeText(getContext(), "Can not create new class", Toast.LENGTH_SHORT).show();
        }
    }

}
