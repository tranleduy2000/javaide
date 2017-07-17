package com.duy.project_files.holder;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.duy.editor.R;
import com.unnamed.b.atv.model.TreeNode;

/**
 * Created by Bogdan Melnychuk on 2/12/15.
 */
public class IconTreeItemHolder extends TreeNode.BaseNodeViewHolder<IconTreeItemHolder.IconTreeItem> {
    private static final String TAG = "IconTreeItemHolder";
    private TextView tvValue;
    private LayoutInflater inflater;
    @Nullable
    private TreeNode node;

    public IconTreeItemHolder(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View createNodeView(final TreeNode node, IconTreeItem value) {
        this.node = node;
        View view = inflater.inflate(R.layout.layout_icon_node, null, false);
        tvValue = view.findViewById(R.id.node_value);
        tvValue.setText(value.text);

        return view;
    }

    @Override
    public void toggle(boolean active) {
    }

    public static class IconTreeItem {
        public int icon;
        public String text;

        public IconTreeItem(String text) {
            this.text = text;
        }
    }
}
