package com.duy.ide.java.code_sample.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duy.ide.R;
import com.duy.ide.java.code_sample.adapters.ProjectCategoryAdapter;
import com.duy.ide.java.code_sample.model.CodeCategory;
import com.duy.ide.java.code_sample.model.CodeProjectSample;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SelectProjectFragment extends Fragment {
    public static final String TAG = "SampleFragment";

    private ProjectClickListener listener;

    public static SelectProjectFragment newInstance(CodeCategory category) {

        Bundle args = new Bundle();
        args.putSerializable("category", category);
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
        final CodeCategory category = (CodeCategory) getArguments().getSerializable("category");
        final RecyclerView recyclerView = view.findViewById(R.id.list_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ProjectCategoryAdapter adapter = new ProjectCategoryAdapter(getActivity(), category);
        adapter.setListener(listener);
        recyclerView.setAdapter(adapter);
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


    public interface ProjectClickListener extends SelectCategoryFragment.CategoryClickListener {
        void onProjectClick(CodeProjectSample codeProjectSample);
    }
}
