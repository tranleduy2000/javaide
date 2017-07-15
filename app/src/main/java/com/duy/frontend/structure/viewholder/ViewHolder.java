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

package com.duy.frontend.structure.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.duy.frontend.R;
import com.unnamed.b.atv.model.TreeNode;

/**
 * Uses for program, function and procedure node
 * <p>
 * Created by Duy on 29-Mar-17.
 */
public class ViewHolder extends TreeNode.BaseNodeViewHolder<StructureItem> {

    public ViewHolder(Context context) {
        super(context);
    }

    @Override
    public View createNodeView(final TreeNode node, StructureItem value) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.layout_selectable_header, null, false);

        TextView tvValue = (TextView) view.findViewById(R.id.txt_name);
        tvValue.setText(value.text);

        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        String prefix = StructureType.ICONS[value.type];

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .textColor(StructureType.COLORS_FOREGROUND[value.type]).bold()
                .endConfig()
                .buildRound(prefix, StructureType.COLORS_BACKGROUND[value.type]);
        iconView.setImageDrawable(drawable);

        return view;
    }

    @Override
    public void toggle(boolean active) {

    }

    @Override
    public int getContainerStyle() {
        return R.style.TreeNodeStyleCustom;
    }
}
