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

package com.duy.ide.java.sample.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.java.sample.model.CodeProjectSample;
import com.duy.ide.editor.code.view.EditorView;
import com.duy.ide.utils.clipboard.ClipboardManagerCompat;
import com.duy.ide.utils.clipboard.ClipboardManagerCompatFactory;

import java.util.ArrayList;

/**
 * Adapter for list sample code
 * <p>
 * Created by Duy on 08-Apr-17.
 */
public class CodeSampleAdapter extends RecyclerView.Adapter<CodeSampleAdapter.CodeHolder> {
    private final ClipboardManagerCompat clipboardManagerCompat;
    private ArrayList<CodeProjectSample> codeSampleEntries = new ArrayList<>();
    private ArrayList<CodeProjectSample> originalData = new ArrayList<>();
    private Context context;
    private LayoutInflater inflater;
    private OnCodeClickListener listener;

    public CodeSampleAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        clipboardManagerCompat = ClipboardManagerCompatFactory.newInstance(context);
    }

    @Override
    public CodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item_code, parent, false);
        return new CodeHolder(view);
    }

    @Override
    public void onBindViewHolder(final CodeHolder holder, int position) {
        final CodeProjectSample codeSampleEntry = codeSampleEntries.get(position);
        holder.bind(codeSampleEntry, listener, clipboardManagerCompat);
    }

    public void setListener(OnCodeClickListener listener) {
        this.listener = listener;
    }

    public void addCodes(ArrayList<CodeProjectSample> listCodeCategories) {
        this.originalData.addAll(listCodeCategories);
        this.codeSampleEntries.addAll(listCodeCategories);
    }

    @Override
    public int getItemCount() {
        return codeSampleEntries.size();
    }

    public void query(String query) {
        int size = codeSampleEntries.size();
        codeSampleEntries.clear();
        notifyItemRangeRemoved(0, size);

        int count = 0;
        for (CodeProjectSample codeSampleEntry : originalData) {
            if (codeSampleEntry.getName().contains(query) ||
                    codeSampleEntry.getContent().contains(query)) {
                CodeProjectSample clone = codeSampleEntry.clone();
                clone.setQuery(query);
                codeSampleEntries.add(clone);
                notifyItemInserted(codeSampleEntries.size() - 1);
                count++;
            }
        }
        if (count == 0) {
            Toast.makeText(context, "No matching", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, count + " file", Toast.LENGTH_SHORT).show();
        }
    }


    public interface OnCodeClickListener {
        void onPlay(String code);

        //            void onCopy(String code);
        void onEdit(String code);
    }


    public static class CodeHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        View btnPlay;
        View btnEdit;
        View btnCopy;
        EditorView editorView;


        public CodeHolder(View view) {
            super(view);
            txtTitle = (TextView) view.findViewById(R.id.txt_title);
            btnPlay = view.findViewById(R.id.img_play);
            btnEdit = view.findViewById(R.id.img_edit);
            btnCopy = view.findViewById(R.id.img_copy);
            editorView = (EditorView) view.findViewById(R.id.editor_view);
        }

        public void bind(CodeProjectSample codeSampleEntry,
                         final OnCodeClickListener listener,
                         final ClipboardManagerCompat clipboardManagerCompat) {
            //set code
            final String content = codeSampleEntry.getContent();

            editorView.setMaxLines(50);
            editorView.setEllipsize(TextUtils.TruncateAt.END);
            editorView.setCanEdit(false);
            editorView.disableTextChangedListener();
            editorView.setText(content);
            editorView.refresh();
            if (codeSampleEntry.getQuery() != null && !codeSampleEntry.getQuery().isEmpty()) {
                editorView.find(codeSampleEntry.getQuery(), false, false, false);
            }

            txtTitle.setText(codeSampleEntry.getName());

            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onPlay(content);
                }
            });
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) listener.onEdit(content);
                }
            });
            btnCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clipboardManagerCompat.setText(content);
                }
            });
        }
    }
}
