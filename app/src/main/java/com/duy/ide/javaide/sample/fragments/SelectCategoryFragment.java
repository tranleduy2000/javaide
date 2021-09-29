/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.sample.fragments;

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
import com.duy.ide.javaide.sample.adapters.CategoryAdapter;
import com.duy.ide.javaide.sample.model.CodeCategory;

import java.util.ArrayList;

/**
 * Created by Duy on 27-Jul-17.
 */

public class SelectCategoryFragment extends Fragment {
    public static final String TAG = "SelectCategoryFragment";
    private CategoryClickListener listener;

    public static SelectCategoryFragment newInstance(ArrayList<CodeCategory> codeCategories) {

        Bundle args = new Bundle();
        args.putSerializable("categories", codeCategories);
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
        final ArrayList<CodeCategory> categories =
                (ArrayList<CodeCategory>) getArguments().getSerializable("categories");
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


    public interface CategoryClickListener {
        void onCategoryClick(CodeCategory category);
    }

}
