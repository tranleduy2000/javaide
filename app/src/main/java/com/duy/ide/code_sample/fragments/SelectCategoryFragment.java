package com.duy.ide.code_sample.fragments;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duy.ide.R;
import com.duy.ide.code_sample.adapters.CategoryAdapter;

import java.io.IOException;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SelectCategoryFragment extends Fragment {
    private CategoryClickListener listener;

    public static SelectCategoryFragment newInstance() {

        Bundle args = new Bundle();

        SelectCategoryFragment fragment = new SelectCategoryFragment();
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
        final RecyclerView recyclerView = view.findViewById(R.id.list_view);
        final String[] categories = getCategories();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        CategoryAdapter adapter = new CategoryAdapter(getActivity(), categories);
        adapter.setListener(listener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (CategoryClickListener) getActivity();
        } catch (Exception e) {
            //class cast exception
        }
    }

    private String[] getCategories() {
        AssetManager assets = getContext().getAssets();
        String[] samples = new String[0];
        try {
            samples = assets.list("sample");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    public interface CategoryClickListener {
        void onCategoryClick(String category);
    }

}
