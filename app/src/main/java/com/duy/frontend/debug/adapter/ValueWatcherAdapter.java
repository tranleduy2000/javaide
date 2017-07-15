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

package com.duy.frontend.debug.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duy.frontend.R;
import com.duy.frontend.debug.model.VariableItem;

import java.util.ArrayList;

//import butterknife.BindView;

public class ValueWatcherAdapter extends RecyclerView.Adapter<ValueWatcherAdapter.ViewHolder> {
    private ArrayList<VariableItem> listData = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public ValueWatcherAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public ValueWatcherAdapter(ArrayList<VariableItem> listData, Context context) {
        this.listData = listData;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addVariable(VariableItem debugItem) {
        listData.add(debugItem);
        notifyItemInserted(getItemCount() - 1);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_debug_variable, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bindVar(listData.get(position));
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void clear() {
        listData.clear();
        notifyDataSetChanged();
    }

    public void onVariableChangeValue(String name, Object old, Object newV) {
        boolean change = false;
        for (VariableItem variableItem : listData) {
            if (variableItem.getName().equalsIgnoreCase(name)) {
                variableItem.setValue(newV);
                change = true;
            }
        }
        if (change) notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtValue;
        View view;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            txtValue = (TextView) itemView.findViewById(R.id.txt_value);
            view = itemView.findViewById(R.id.background);
        }

        public void bindVar(VariableItem variableItem) {
            txtName.setText(variableItem.getName() +
                    (variableItem.getValue() != null
                            ? " = " + variableItem.getValue().toString()
                            : " = null"));
        }
    }
}
