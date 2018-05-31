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
        return new ViewHolder(layoutInflater.inflate(R.layout.list_item_category, parent, false));
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
