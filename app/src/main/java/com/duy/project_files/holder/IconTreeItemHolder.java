package com.duy.project_files.holder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.duy.editor.R;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

/**
 * Created by Bogdan Melnychuk on 2/12/15.
 */
public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<IconTreeItemHolder.IconTreeItem> {
    private static final String TAG = "IconTreeItemHolder";
    private TextView tvValue;
    private LayoutInflater inflater;

    public IconTreeItemHolder(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View createNodeView(final TreeNode node, IconTreeItem value) {
        View view = inflater.inflate(R.layout.layout_icon_node, null, false);
        tvValue = view.findViewById(R.id.node_value);
        tvValue.setText(value.getFile().getName());

        File file = value.getFile();

        return view;
    }

    @Override
    public void toggle(boolean active) {
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
