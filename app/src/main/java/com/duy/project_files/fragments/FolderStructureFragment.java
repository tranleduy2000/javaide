package com.duy.project_files.fragments;

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
import com.duy.project_files.holder.IconTreeItemHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FolderStructureFragment extends Fragment {
    private AndroidTreeView tView;
    private int counter = 0;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_default, null, false);
        ViewGroup containerView = rootView.findViewById(R.id.container);

        TreeNode root = TreeNode.root();
        root.addChildren(createFileStructure());

        tView = new AndroidTreeView(getActivity(), root);
        tView.setDefaultAnimation(false);
        tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
        tView.setDefaultViewHolder(IconTreeItemHolder.class);
        tView.setDefaultNodeClickListener(nodeClickListener);
        tView.setDefaultNodeLongClickListener(nodeLongClickListener);

        containerView.addView(tView.getView());

        if (savedInstanceState != null) {
            String state = savedInstanceState.getString("tState");
            if (!TextUtils.isEmpty(state)) {
                tView.restoreState(state);
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
        TreeNode root = new TreeNode(new IconTreeItemHolder.IconTreeItem(rootDir));
        try {
            root.addChildren(getNode(rootDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    private ArrayList<TreeNode> getNode(File parent) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        try {
            if (parent.isDirectory()) {
                File[] child = parent.listFiles();
                if (child != null) {
                    for (File file : child) {
                        TreeNode node = new TreeNode(new IconTreeItemHolder.IconTreeItem(file));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new IconTreeItemHolder.IconTreeItem(parent));
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.expandAll:
//                tView.expandAll();
//                break;
//
//            case R.id.collapseAll:
//                tView.collapseAll();
//                break;
        }
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tState", tView.getSaveState());
    }


}
