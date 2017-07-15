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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.duy.pascal.interperter.declaration.lang.value.VariableDeclaration;
import com.duy.frontend.R;
import com.duy.frontend.debug.utils.SpanUtils;
import com.duy.frontend.themefont.themes.database.CodeTheme;
import com.duy.frontend.themefont.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Duy on 08-Jun-17.
 */

public class VariableAdapter extends RecyclerView.Adapter<VariableAdapter.VariableHolder> {

    private static final String TAG = "VariableAdapter";
    private Context context;
    private ArrayList<VariableDeclaration> variableItems = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private CodeTheme codeTheme;
    private SpanUtils spanUtils;
    private List<Boolean> updateList = new ArrayList<>();
    private OnExpandValueListener onExpandValueListener;

    public CodeTheme getCodeTheme() {
        return codeTheme;
    }

    public SpanUtils getSpanUtils() {
        return spanUtils;
    }

    public VariableAdapter(Context context) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.codeTheme = ThemeManager.getDefault((context));
        this.spanUtils = new SpanUtils(codeTheme);
    }

    public OnExpandValueListener getOnExpandValueListener() {
        return onExpandValueListener;
    }

    public void setOnExpandValueListener(OnExpandValueListener onExpandValueListener) {
        this.onExpandValueListener = onExpandValueListener;
    }

    public ArrayList<VariableDeclaration> getVariableItems() {
        return variableItems;
    }

    public void setData(List<VariableDeclaration> vars, List<Boolean> updateList) {
        this.updateList = updateList;
        variableItems.clear();
        variableItems.addAll(vars);
        notifyDataSetChanged();
    }

    public void add(VariableDeclaration item) {
        variableItems.add(item);
        notifyItemInserted(variableItems.size() - 1);
    }

    public void clearData() {
        variableItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public VariableHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VariableHolder(layoutInflater.inflate(R.layout.list_item_var, parent, false));
    }

    @Override
    public void onBindViewHolder(VariableHolder holder, int position) {
        final VariableDeclaration var = variableItems.get(position);
        holder.txtName.setText(spanUtils.createVarSpan(var));

        Log.d(TAG, "onBindViewHolder: " + updateList.get(position));
        if (updateList.get(position)) { //update value
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.7f);
            alphaAnimation.setDuration(500);
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            holder.txtName.startAnimation(alphaAnimation);
        } else {
            holder.txtName.clearAnimation();
        }
        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onExpandValueListener != null) {
                    onExpandValueListener.onExpand(var);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return variableItems.size();
    }

    public interface OnExpandValueListener {
        void onExpand(VariableDeclaration var);
    }

    /**
     * Created by Duy on 08-Jun-17.
     */

    public static class VariableHolder extends RecyclerView.ViewHolder {
        public TextView txtName;

        public VariableHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
        }
    }
}
