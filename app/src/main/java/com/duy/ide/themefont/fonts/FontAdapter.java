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

package com.duy.ide.themefont.fonts;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.duy.ide.R;
import com.duy.ide.setting.JavaPreferences;

import java.util.LinkedList;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private LinkedList<FontEntry> listFonts = new LinkedList<>();
    private JavaPreferences pascalPreferences;
    @Nullable
    private OnFontSelectListener onFontSelectListener;

    public FontAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        pascalPreferences = new JavaPreferences(context);
        this.listFonts = FontManager.getAll(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_font_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        final FontEntry fontEntry = listFonts.get(pos);

        holder.txtSample.setTextSize(pascalPreferences.getEditorTextSize() * 2);
        holder.txtSample.setTypeface(FontManager.getFont(fontEntry, context));
        String name = fontEntry.name;
        name = name.replace("_", " ").replace("-", " ").toLowerCase();
        if (name.contains(".")) {
            holder.txtName.setText(name.substring(0, name.indexOf(".")));
        } else {
            holder.txtName.setText(name);
        }
        holder.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFontSelectListener != null) {
                    onFontSelectListener.onFontSelected(fontEntry);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listFonts.size();
    }

    @Nullable
    public OnFontSelectListener getOnFontSelectListener() {
        return onFontSelectListener;
    }

    public void setOnFontSelectListener(@Nullable OnFontSelectListener onFontSelectListener) {
        this.onFontSelectListener = onFontSelectListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtSample;
        TextView txtName;
        Button btnSelect;

        public ViewHolder(View itemView) {
            super(itemView);
            txtSample = itemView.findViewById(R.id.txt_sample);
            txtName = itemView.findViewById(R.id.txt_name);
            btnSelect = itemView.findViewById(R.id.btn_select);

        }
    }


}
