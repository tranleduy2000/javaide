package com.duy.project.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.duy.ide.R;
import com.duy.compile.CompileManager;
import com.duy.project.ProjectFile;
import com.duy.project.ProjectFileContract;
import com.duy.project.holder.FolderHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.util.ArrayList;

import static android.widget.FrameLayout.LayoutParams;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FolderStructureFragment extends Fragment
        implements ProjectFileContract.View {
    public static final String TAG = "FolderStructureFragment";
    private final android.os.Handler mHandler = new android.os.Handler();
    private ProjectFile mProjectFile;
    @Nullable
    private ProjectFileContract.FileActionListener listener;
    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
            if (listener != null && i.getFile().isFile()) {
                listener.onFileClick(i.getFile(), null);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
            if (listener != null && i.getFile().isFile()) {
                listener.onFileLongClick(i.getFile(), null);
            }
            return true;
        }
    };
    private ViewGroup mContainerView;
    private ProjectFileContract.Presenter presenter;
    @Nullable
    private AndroidTreeView mTreeView;
    private SharedPreferences mPref;

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
        return inflater.inflate(R.layout.fragment_folder_structure, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProjectFile = (ProjectFile) getArguments().getSerializable(CompileManager.PROJECT_FILE);
        mContainerView = view.findViewById(R.id.container);
        view.findViewById(R.id.img_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        refresh();
        view.findViewById(R.id.img_expand_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTreeView != null) mTreeView.expandAll();
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
            String state = mPref.getString("tree_state", "");
            if (!state.isEmpty()) mTreeView.restoreState(state);
        }
    }

    @Nullable
    private TreeNode createFileStructure(ProjectFile projectFile) {
        File rootDir = new File(projectFile.getRootDir());
        TreeNode root = new TreeNode(new FolderHolder.TreeItem(rootDir, rootDir, listener));
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
                    for (File file : child) {
                        TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, file, listener));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(projectFile, file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, parent, listener));
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
        if (mTreeView != null) {
            outState.putString("tState", mTreeView.getSaveState());
        }
    }


    @Override
    public void display(ProjectFile projectFile, boolean expand) {
        this.mProjectFile = projectFile;
        refresh();
        if (expand && mTreeView != null) mTreeView.expandAll();
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
        mTreeView.setDefaultViewHolder(FolderHolder.class);
        mTreeView.setDefaultNodeClickListener(nodeClickListener);
        mTreeView.setDefaultNodeLongClickListener(nodeLongClickListener);
        if (saveState != null) {
            mTreeView.restoreState(saveState);
        }
        mContainerView.removeAllViews();
        View view = mTreeView.getView();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mContainerView.addView(view, params);

    }

    @Override
    public void setPresenter(ProjectFileContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.listener = (ProjectFileContract.FileActionListener) getActivity();
        } catch (ClassCastException e) {
        }
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onDestroyView() {
        if (mTreeView != null) {
            String saveState = mTreeView.getSaveState();
            mPref.edit().putString("tree_state", saveState).apply();
        }
        super.onDestroyView();
    }
}
