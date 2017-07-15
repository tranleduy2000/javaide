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

package com.duy.frontend.code_sample.fragments;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duy.frontend.DLog;
import com.duy.frontend.R;
import com.duy.frontend.code_sample.adapters.CodeSampleAdapter;
import com.duy.frontend.code_sample.model.CodeCategory;
import com.duy.frontend.code_sample.model.CodeSampleEntry;
import com.duy.frontend.file.FileManager;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Duy on 28-Apr-17.
 */

@SuppressWarnings("DefaultFileTemplate")
public class FragmentCodeSample extends Fragment {
    private static final String TAG = "FragmentCodeSample";

    private CodeSampleAdapter adapter;

    public static FragmentCodeSample newInstance(String category) {
        FragmentCodeSample fragmentCodeSample = new FragmentCodeSample();
        Bundle bundle = new Bundle();
        bundle.putString(TAG, category);
        fragmentCodeSample.setArguments(bundle);
        return fragmentCodeSample;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_sample, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        adapter = new CodeSampleAdapter(getContext());
        try {
            adapter.setListener((CodeSampleAdapter.OnCodeClickListener) getActivity());
        } catch (Exception ignored) {
        }
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new LoadCodeTask().execute();
    }

    public void query(String query) {
        adapter.query(query);
    }

    private class LoadCodeTask extends AsyncTask<Object, Object, Void> {
        private ArrayList<CodeSampleEntry> codeSampleEntries = new ArrayList<>();

        @Override
        protected Void doInBackground(Object... params) {
            try {
                String category = getArguments().getString(TAG);
                CodeCategory codeCategory = new CodeCategory(category, "");
                String[] list;
                String path = "code_sample/" + getArguments().getString(TAG).toLowerCase();
                try {
                    Context context = getContext();
                    if (category == null) {
                        return null;
                    }
                    AssetManager assets = context.getAssets();
                    list = assets.list(path);
                    for (String fileName : list) {
                        if (fileName.endsWith(".pas")) {
                            StringBuilder content =
                                    FileManager.streamToString(assets.open(path + "/" + fileName));
                            codeCategory.addCodeItem(new CodeSampleEntry(fileName, content));
                        }
                    }
                } catch (IOException ignored) {
                    DLog.e(ignored);
                }
                codeSampleEntries.addAll(codeCategory.getCodeSampleEntries());
            } catch (Exception e) {
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            adapter.addCodes(codeSampleEntries);
            adapter.notifyDataSetChanged();
        }
    }
}
