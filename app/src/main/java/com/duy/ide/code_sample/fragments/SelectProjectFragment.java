package com.duy.ide.code_sample.fragments;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.duy.ide.R;
import com.duy.ide.code_sample.model.SampleUtil;

import java.io.IOException;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SelectProjectFragment extends Fragment {
    public static final String TAG = "SampleFragment";

    private ProjectClickListener listener;

    public static SelectProjectFragment newInstance(String category) {

        Bundle args = new Bundle();
        args.putString("category", category);
        SelectProjectFragment fragment = new SelectProjectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final android.support.v7.widget.ListViewCompat listViewCompat = view.findViewById(R.id.list_view);
        final String[] names = getCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, names);
        listViewCompat.setAdapter(adapter);
        listViewCompat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) listener.onProjectClick(names[position]);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ProjectClickListener) getActivity();
        } catch (Exception e) {
            //class cast exception
        }
    }

    private String[] getCategories() {
        AssetManager assets = getContext().getAssets();
        String[] samples = new String[0];
        try {
            samples = assets.list(SampleUtil.ASSET_SAMPLE_PATH + "/" + getArguments().getString("category"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    public interface ProjectClickListener {
        void onProjectClick(String sampleName);
    }
}
