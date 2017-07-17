package com.duy.project_files.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.duy.editor.R;
import com.duy.editor.code.CompileManager;
import com.duy.project_files.ProjectFile;
import com.duy.project_files.ProjectFileContract;
import com.duy.project_files.holder.IconTreeItemHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FolderStructureFragment extends Fragment
        implements ProjectFileContract.View {
    public static final String TAG = "FolderStructureFragment";

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            IconTreeItemHolder.TreeItem i = (IconTreeItemHolder.TreeItem) value;
            if (listener != null && i.getFile().isFile()) {
                listener.onFileClick(i.getFile(), null);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            IconTreeItemHolder.TreeItem i = (IconTreeItemHolder.TreeItem) value;
            if (listener != null && i.getFile().isFile()) {
                listener.onFileLongClick(i.getFile(), null);
            }
            return true;
        }
    };
    private ProjectFile mProjectFile;
    @Nullable
    private ProjectFileContract.FileActionListener listener;
    private ViewGroup mContainerView;
    private ProjectFileContract.Presenter presenter;

    public static FolderStructureFragment newInstance(@NonNull ProjectFile projectFile) {

        Bundle args = new Bundle();
        args.putSerializable(CompileManager.PROJECT_FILE, projectFile);
        FolderStructureFragment fragment = new FolderStructureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private AndroidTreeView mTreeView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (ProjectFileContract.FileActionListener) getActivity();
        } catch (ClassCastException e) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_default, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContainerView = view.findViewById(R.id.container);

        TreeNode root = TreeNode.root();
        TreeNode fileStructure = createFileStructure();
        if (fileStructure != null) {
            root.addChildren(fileStructure);
        }

        mTreeView = new AndroidTreeView(getContext(), root);
        mTreeView.setDefaultAnimation(false);
        mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        mTreeView.setDefaultViewHolder(IconTreeItemHolder.class);
        mTreeView.setDefaultNodeClickListener(nodeClickListener);
        mTreeView.setDefaultNodeLongClickListener(nodeLongClickListener);
        mContainerView.addView(mTreeView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                mTreeView.restoreState(state);
            }
        }
    }

    @Nullable
    private TreeNode createFileStructure() {
        ProjectFile projectFile = (ProjectFile)
                getArguments().getSerializable(CompileManager.PROJECT_FILE);
        if (projectFile == null) return null;
        return createFileStructure(projectFile);
    }

    @Nullable
    private TreeNode createFileStructure(ProjectFile projectFile) {
        File rootDir = new File(projectFile.getRootDir());
        TreeNode root = new TreeNode(new IconTreeItemHolder.TreeItem(rootDir, listener));
        try {
            root.addChildren(getNode(rootDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private ArrayList<TreeNode> getNode(File parent) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        try {
            if (parent.isDirectory()) {
                File[] child = parent.listFiles();
                if (child != null) {
                    for (File file : child) {
                        TreeNode node = new TreeNode(new IconTreeItemHolder.TreeItem(file, listener));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new IconTreeItemHolder.TreeItem(parent, listener));
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.expandAll:
//                mTreeView.expandAll();
//                break;
//
//            case R.id.collapseAll:
//                mTreeView.collapseAll();
//                break;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", mTreeView.getSaveState());
    }



    @Override
    public void display(ProjectFile projectFile) {
        this.mProjectFile = projectFile;
        refresh();
    }

    @Override
    public void refresh() {
        if (mProjectFile == null) return;
        TreeNode root = TreeNode.root();
        TreeNode fileStructure = createFileStructure(mProjectFile);
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
        mTreeView.setDefaultViewHolder(IconTreeItemHolder.class);
        mTreeView.setDefaultNodeClickListener(nodeClickListener);
        mTreeView.setDefaultNodeLongClickListener(nodeLongClickListener);
        if (saveState != null) {
            mTreeView.restoreState(saveState);
        }

        mContainerView.removeAllViews();
        mContainerView.addView(mTreeView.getView());
    }

    @Override
    public void setPresenter(ProjectFileContract.Presenter presenter) {
        this.presenter = presenter;
    }


}
