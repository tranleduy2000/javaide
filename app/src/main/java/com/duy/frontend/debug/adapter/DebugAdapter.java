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
import com.duy.frontend.debug.model.DebugItem;

import java.util.ArrayList;

//import butterknife.BindView;

public class DebugAdapter extends RecyclerView.Adapter<DebugAdapter.ViewHolder> {
    private ArrayList<DebugItem> listData = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;

    public DebugAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public DebugAdapter(ArrayList<DebugItem> listData, Context context) {
        this.listData = listData;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void addLine(DebugItem debugItem) {
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
        if (listData.get(position).getType() == DebugItem.TYPE_VAR) {
            holder.bindMsg(listData.get(position).toString());
        } else {
            holder.bindMsg(listData.get(position).toString());
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public void clear() {
        listData.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
//        @BindView(R.id.txt_title)
        TextView txtName;
//        @BindView(R.id.txt_value)
        TextView txtValue;
//        @BindView(R.id.background)
        View background;

        ViewHolder(View view) {
            super(view);
            txtName = (TextView) view.findViewById(R.id.txt_title);
            txtValue = (TextView) view.findViewById(R.id.txt_value);
            this.background = view.findViewById(R.id.background);
//            ButterKnife.bind(this, background);
        }

        public void bindVar(DebugItem debugItem) {
            txtName.setText(debugItem.getMsg1());
            txtValue.setText(debugItem.getMsg2());
        }

        public void bindMsg(String msg) {
            txtName.setText(msg);
            txtValue.setText("");
        }
    }
}
