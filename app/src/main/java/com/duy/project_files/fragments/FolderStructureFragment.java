package com.duy.project_files.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
        implements ProjectFileContract.OnItemClickListener, ProjectFileContract.View {
    private static final String TAG = "FolderStructureFragment";

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            return true;
        }
    };
    private ProjectFile projectFile;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_default, null, false);

        ViewGroup containerView = rootView.findViewById(R.id.container);

        TreeNode root = TreeNode.root();
        root.addChildren(createFileStructure());

        mTreeView = new AndroidTreeView(getActivity(), root);
        mTreeView.setDefaultAnimation(false);
        mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        mTreeView.setDefaultViewHolder(IconTreeItemHolder.class);
        mTreeView.setDefaultNodeClickListener(nodeClickListener);
        mTreeView.setDefaultNodeLongClickListener(nodeLongClickListener);
        containerView.addView(mTreeView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                mTreeView.restoreState(state);
            }
        }

        return rootView;
    }

    @Nullable
    private TreeNode createFileStructure() {
        ProjectFile projectFile = (ProjectFile)
                getArguments().getSerializable(CompileManager.PROJECT_FILE);
        if (projectFile == null) return null;

        File rootDir = new File(projectFile.getRootDir());
        TreeNode root = new TreeNode(new IconTreeItemHolder.IconTreeItem(rootDir, this));
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
                        TreeNode node = new TreeNode(new IconTreeItemHolder.IconTreeItem(file, this));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new IconTreeItemHolder.IconTreeItem(parent, this));
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
    public void onClickDelete(File file) {
        Log.d(TAG, "onClickDelete() called with: file = [" + file + "]");


    }

    @Override
    public void onClickCreateNew(File parent) {
        Log.d(TAG, "onClickCreateNew() called with: parent = [" + parent + "]");

    }

    @Override
    public void display(ProjectFile projectFile) {
        this.projectFile = projectFile;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void setPresenter(ProjectFileContract.Presenter presenter) {

    }
}
