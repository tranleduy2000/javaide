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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duy.pascal.interperter.ast.variablecontext.VariableContext;
import com.duy.frontend.R;
import com.duy.frontend.debug.CallStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 08-Jun-17.
 */

public class FrameAdapter extends RecyclerView.Adapter<FrameAdapter.FrameHolder> {
    private List<VariableContext> frames = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;
    @Nullable
    private OnFrameListener listener;

    public FrameAdapter(Context context, @Nullable OnFrameListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    public void setFrames(List<VariableContext> frames) {
        this.frames = frames;
        notifyDataSetChanged();
    }

    @Override
    public FrameHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FrameHolder(inflater.inflate(R.layout.list_item_frames, parent, false));
    }

    @Override
    public void onBindViewHolder(final FrameHolder holder, int position) {
        holder.txtName.setText(frames.get(holder.getAdapterPosition()).toString());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSelectFrame(new CallStack(frames.get(holder.getAdapterPosition())));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    public void clearData() {
        this.frames.clear();
    }

    public interface OnFrameListener {
        void onSelectFrame(CallStack stack);
    }

    static class FrameHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public View container;

        public FrameHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            container = itemView.findViewById(R.id.container);
        }
    }
}
