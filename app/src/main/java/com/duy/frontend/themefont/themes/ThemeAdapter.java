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

package com.duy.frontend.themefont.themes;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.frontend.R;
import com.duy.frontend.code.CodeSample;
import com.duy.frontend.editor.view.EditorView;
import com.duy.frontend.setting.JavaPreferences;
import com.duy.frontend.themefont.themes.database.CodeTheme;
import com.duy.frontend.themefont.themes.database.ThemeDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<CodeTheme> mThemes = new ArrayList<>();
    private LayoutInflater mInflater;
    private JavaPreferences mPascalPreferences;
    private Activity mContext;

    @Nullable
    private ThemeFragment.OnThemeSelectListener onThemeSelectListener;
    private ThemeDatabase mDatabase;

    public ThemeAdapter(Activity context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mPascalPreferences = new JavaPreferences(context);
        loadTheme(context);
        mDatabase = new ThemeDatabase(context);
    }

    private void loadTheme(Context context) {
        HashMap<String, CodeTheme> all = ThemeManager.getAll(context);
        for (Map.Entry<String, CodeTheme> entry : all.entrySet()) mThemes.add(entry.getValue());
        Collections.sort(mThemes, new Comparator<CodeTheme>() {
            @Override
            public int compare(CodeTheme codeTheme, CodeTheme t1) {
                return codeTheme.getName().compareTo(t1.getName());
            }
        });
    }

    public void clear() {
        mThemes.clear();
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_theme, parent, false);
        return new CodeThemeHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int pos) {
        CodeThemeHolder view = (CodeThemeHolder) holder;
        final CodeTheme entry = mThemes.get(pos);
        if (entry.isBuiltin()) {
            view.imgDelete.setVisibility(View.GONE);
        } else {
            view.imgDelete.setVisibility(View.VISIBLE);
            view.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDatabase.delete(entry);
                    remove(pos);
                    Toast.makeText(mContext, R.string.deleted, Toast.LENGTH_SHORT).show();
                }
            });
        }
//        view.editorView.setLineError(new LineInfo(3, 0, ""));
        view.editorView.setCodeTheme(entry);
        view.editorView.setTextHighlighted(CodeSample.DEMO_THEME);
        view.txtTitle.setText(entry.getName());
        view.btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPascalPreferences.setTheme(entry.getName());
                Toast.makeText(mContext, mContext.getString(R.string.select) + " " + entry.getName(),
                        Toast.LENGTH_SHORT).show();
                if (onThemeSelectListener != null) {
                    onThemeSelectListener.onThemeSelect(String.valueOf(entry));
                }
            }
        });
    }

    private void remove(int pos) {
        mThemes.remove(pos);
        notifyItemRemoved(pos);
    }

    @Override
    public int getItemCount() {
        return mThemes.size();
    }

    @Nullable
    public ThemeFragment.OnThemeSelectListener getOnThemeSelectListener() {
        return onThemeSelectListener;
    }

    public void setOnThemeSelectListener(@Nullable ThemeFragment.OnThemeSelectListener onThemeSelectListener) {
        this.onThemeSelectListener = onThemeSelectListener;
    }

    public void reload(Context context) {
        mThemes.clear();
        loadTheme(context);
        notifyDataSetChanged();
    }

    private static class CodeThemeHolder extends RecyclerView.ViewHolder {
        public View imgDelete;
        EditorView editorView;
        TextView txtTitle;
        Button btnSelect;

        CodeThemeHolder(View itemView) {
            super(itemView);
            editorView = itemView.findViewById(R.id.editor_view);
            txtTitle = itemView.findViewById(R.id.txt_title);
            btnSelect = itemView.findViewById(R.id.btn_select);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }

}
