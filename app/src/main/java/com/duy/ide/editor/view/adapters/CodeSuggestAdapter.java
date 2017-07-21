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

package com.duy.ide.editor.view.adapters;

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

import com.duy.compile.diagnostic.SpanUtil;
import com.duy.ide.R;
import com.duy.ide.autocomplete.model.ClassDescription;
import com.duy.ide.autocomplete.model.Description;
import com.duy.ide.setting.JavaPreferences;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Duy on 26-Apr-17.
 */
public class CodeSuggestAdapter extends ArrayAdapter<Description> {
    private static final String TAG = "CodeSuggestAdapter";
    @NonNull
    private Context context;
    @NonNull
    private LayoutInflater inflater;
    @NonNull
    private ArrayList<Description> clone;
    @NonNull
    private ArrayList<Description> suggestion;
    private int resourceID;
    @Nullable
    private OnSuggestItemClickListener listener;
    private float editorTextSize;

    @SuppressWarnings("unchecked")
    public CodeSuggestAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<Description> objects) {
        super(context, resource, objects);
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.clone = (ArrayList<Description>) objects.clone();
        this.suggestion = new ArrayList<>();
        this.resourceID = resource;

        JavaPreferences javaPreferences = new JavaPreferences(context);
        editorTextSize = javaPreferences.getEditorTextSize();
    }

    public ArrayList<Description> getAllItems() {
        return clone;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resourceID, null);
        }

        final Description item = getItem(position);
        TextView txtName = convertView.findViewById(R.id.txt_name);
        txtName.setTypeface(Typeface.MONOSPACE);
        txtName.setTextSize(editorTextSize);
        TextView txtType = convertView.findViewById(R.id.txt_type);
        txtType.setTypeface(Typeface.MONOSPACE);
        txtType.setTextSize(editorTextSize);

        if (item != null) {
            if (item instanceof ClassDescription) {
                txtName.setText(SpanUtil.formatClass(context, (ClassDescription) item));
            } else {
                txtName.setText(item.toString());
                txtType.setText(item.getType() != null ? item.getType().getSimpleName() : "");
            }
        }
        return convertView;
    }


    public void clearAllData() {
        super.clear();
        clone.clear();
    }

    public void addData(@NonNull Collection<? extends Description> collection) {
        addAll(collection);
        clone.addAll(collection);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return codeFilter;
    }

    private Filter codeFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object value) {
            if (value == null) {
                return "";
            }
            return ((Description) value).getSnippet();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            suggestion.clear();
            if (constraint != null) {
                for (Description item : clone) {
//                    if (item.compareTo(constraint.toString()) == 0) {
                    suggestion.add(item);
//                    }
                }
                filterResults.values = suggestion;
                filterResults.count = suggestion.size();
            }
            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")

        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Description> filteredList = (ArrayList<Description>) results.values;
            clear();
            if (filteredList != null && filteredList.size() > 0) {
                addAll(filteredList);
            }
            notifyDataSetChanged();
        }
    };

    public void setListener(OnSuggestItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnSuggestItemClickListener {
        void onClickSuggest(Description description);
    }
}
