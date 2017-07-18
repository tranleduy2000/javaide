package com.duy.project_files.holder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duy.editor.R;
import com.duy.project_files.ProjectFile;
import com.duy.project_files.ProjectFileContract;
import com.duy.project_files.utils.ProjectFileUtils;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;


public class FolderHolder extends TreeNode.BaseNodeViewHolder<FolderHolder.TreeItem> {
    private static final String TAG = "FolderHolder";
    private TextView txtName;
    private LayoutInflater inflater;
    private ImageView imgArrow;
    private boolean leaf = false;

    public FolderHolder(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final TreeItem item) {
        View view = inflater.inflate(R.layout.list_item_file, null, false);
        txtName = view.findViewById(R.id.node_value);
        txtName.setText(item.getFile().getName());
        imgArrow = view.findViewById(R.id.img_arrow);
        this.leaf = node.isLeaf();
        View imgNew = view.findViewById(R.id.img_add);

        if (!ProjectFileUtils.inSrcDir(item.getProjectFile(), item.getFile())) {
            imgNew.setVisibility(View.GONE);
        }
        if (node.isLeaf()) {
            imgArrow.setVisibility(View.INVISIBLE);
            imgNew.setVisibility(View.GONE);
        }

        final File file = item.getFile();
        setIcon((ImageView) view.findViewById(R.id.img_icon), file);

        final ProjectFileContract.FileActionListener listener = item.getListener();
        view.findViewById(R.id.img_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.doRemoveFile(file, new ProjectFileContract.ActionCallback() {
                        @Override
                        public void onSuccess(File old) {
                            getTreeView().removeNode(node);
                        }

                        @Override
                        public void onFailed(@Nullable Exception e) {

                        }
                    });
                }
            }
        });
        imgNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.doCreateNewFile(file, new ProjectFileContract.ActionCallback() {
                        @Override
                        public void onSuccess(File newf) {
                            TreeNode child = new TreeNode(new TreeItem(item.getProjectFile(), newf, listener));
                            getTreeView().addNode(node, child);
                        }

                        @Override
                        public void onFailed(@Nullable Exception e) {

                        }
                    });
                }
            }
        });

        return view;
    }


    private void setIcon(ImageView view, File fileDetail) {
        String fileName = fileDetail.getName();
        if (fileDetail.isDirectory()) {
            view.setImageResource(R.drawable.ic_folder_white_24dp);
        } else if (fileName.endsWith(".java")) {
            view.setImageResource(R.drawable.ic_java_file_white);
        } else if (fileName.endsWith(".jar")) {
            view.setImageResource(R.drawable.ic_jar_file_white);
        } else if (fileName.endsWith(".class")) {
            view.setImageResource(R.drawable.ic_class_file_white);
        } else {
            view.setImageResource(R.drawable.ic_insert_drive_file_white_24dp);
        }
    }

    @Override
    public void toggle(boolean active) {
        if (!leaf) {
            imgArrow.setImageResource(active ? R.drawable.ic_keyboard_arrow_down_white_24dp :
                    R.drawable.ic_keyboard_arrow_right_white_24dp);
        }
    }


    public static class TreeItem {
        private File projectFile;
        @NonNull
        private File file;
        @Nullable
        private ProjectFileContract.FileActionListener listener;

        public TreeItem(@NonNull File projectFile, @Nullable File file,
                        @Nullable ProjectFileContract.FileActionListener listener) {
            this.projectFile = projectFile;
            this.file = file;
            this.listener = listener;
        }

        public File getProjectFile() {
            return projectFile;
        }

        @Nullable
        public ProjectFileContract.FileActionListener getListener() {
            return listener;
        }

        @NonNull
        public File getFile() {
            return file;
        }
    }
}
