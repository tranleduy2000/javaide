/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.sample.adapters;

import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duy.ide.R;
import com.duy.ide.javaide.sample.fragments.SelectProjectFragment;
import com.duy.ide.javaide.sample.model.CodeCategory;

/**
 * Adapter for list sample code
 * <p>
 * Created by Duy on 08-Apr-17.
 */
public class ProjectCategoryAdapter extends RecyclerView.Adapter<ProjectCategoryAdapter.ViewHolder> {

    private SelectProjectFragment.ProjectClickListener listener;
    private LayoutInflater layoutInflater;
    private CodeCategory categories;

    public ProjectCategoryAdapter(FragmentActivity activity, CodeCategory categories) {
        this.layoutInflater = LayoutInflater.from(activity);
        this.categories = categories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.list_item_sample_category, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.txtName.setText(categories.getProject(position).getName());
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onProjectClick(categories.getProject(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setListener(SelectProjectFragment.ProjectClickListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public View root;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            root = itemView.findViewById(R.id.root);
        }
    }
}
