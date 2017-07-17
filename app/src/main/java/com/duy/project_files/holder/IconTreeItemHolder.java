package com.duy.project_files.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duy.editor.R;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

/**
 * Created by Bogdan Melnychuk on 2/12/15.
 */
public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<IconTreeItemHolder.IconTreeItem> {
    private static final String TAG = "IconTreeItemHolder";
    private TextView txtName;
    private LayoutInflater inflater;
    private ImageView imgArrow;
    public IconTreeItemHolder(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View createNodeView(final TreeNode node, IconTreeItem value) {
        View view = inflater.inflate(R.layout.layout_icon_node, null, false);
        txtName = view.findViewById(R.id.node_value);
        txtName.setText(value.getFile().getName());
        imgArrow = view.findViewById(R.id.img_arrow);

        File file = value.getFile();
        setIcon((ImageView) view.findViewById(R.id.img_icon), file);
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
        imgArrow.setImageResource(active ? R.drawable.ic_keyboard_arrow_down_white_18dp :
                R.drawable.ic_keyboard_arrow_right_white_24dp);
    }

    public static class IconTreeItem {

        private File file;

        public IconTreeItem(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }
    }
}
