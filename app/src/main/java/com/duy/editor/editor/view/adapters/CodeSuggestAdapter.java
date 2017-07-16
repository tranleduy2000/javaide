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

package com.duy.editor.editor.view.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.structure.viewholder.StructureType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Duy on 26-Apr-17.
 */
public class CodeSuggestAdapter extends ArrayAdapter<InfoItem> {
    private static final String TAG = "CodeSuggestAdapter";
    private final Context context;
    private final int colorKeyWord;
    private final int colorNormal;
    private final int colorVariable = 0xffFFB74D;
    private LayoutInflater inflater;
    private ArrayList<InfoItem> clone;
    private ArrayList<InfoItem> suggestion;
    private int resourceID;
    private Filter codeFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            if (resultValue == null) {
                return "";
            }
            return ((InfoItem) resultValue).getName();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            suggestion.clear();
            if (constraint != null) {
                for (InfoItem item : clone) {
                    if (item.compareTo(constraint.toString()) == 0) {
                        suggestion.add(item);
                    }
                }
                filterResults.values = suggestion;
                filterResults.count = suggestion.size();
            }
            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")

        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<InfoItem> filteredList = (ArrayList<InfoItem>) results.values;
            clear();
            if (filteredList != null && filteredList.size() > 0) {
                addAll(filteredList);
            }
            notifyDataSetChanged();
        }
    };

    @SuppressWarnings("unchecked")
    public CodeSuggestAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<InfoItem> objects) {
        super(context, resource, objects);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.clone = (ArrayList<InfoItem>) objects.clone();
        this.suggestion = new ArrayList<>();
        this.resourceID = resource;
        colorKeyWord = context.getResources().getColor(R.color.color_key_word_color);
        colorNormal = context.getResources().getColor(android.R.color.primary_text_dark);
    }

    public ArrayList<InfoItem> getAllItems() {
        return clone;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resourceID, null);
        }

        final InfoItem item = getItem(position);

        TextView txtName = (TextView) convertView.findViewById(R.id.txt_title);
        txtName.setText(item.getShow() != null ? item.getShow() : item.getName());
        switch (item.getType()) {
            case StructureType.TYPE_KEY_WORD:
                txtName.setTextColor(colorKeyWord);
                txtName.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            case StructureType.TYPE_VARIABLE:
                txtName.setTextColor(colorVariable);
                txtName.setTypeface(Typeface.DEFAULT);
                break;
            default:
                txtName.setTextColor(colorNormal);
                txtName.setTypeface(Typeface.DEFAULT);
                break;
        }
        View btnInfo = convertView.findViewById(R.id.img_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.getDescription() == null) {
                    Toast.makeText(context, R.string.no_document, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, item.getDescription(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return convertView;
    }


    public void clearAllData() {
        super.clear();
        clone.clear();
    }

    public void addData(@NonNull Collection<? extends InfoItem> collection) {
        addAll(collection);
        clone.addAll(collection);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return codeFilter;
    }


}
