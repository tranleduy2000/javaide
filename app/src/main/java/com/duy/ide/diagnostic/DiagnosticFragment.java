package com.duy.ide.diagnostic;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ide.common.blame.Message;
import com.duy.ide.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticFragment extends Fragment implements DiagnosticContract.View {
    public static final String TAG = "DiagnosticFragment";
    public static final int INDEX = 1;
    private RecyclerView mRecyclerView;
    private DiagnosticAdapter mAdapter;
    @Nullable
    private DiagnosticContract.Presenter presenter;

    public static DiagnosticFragment newInstance() {
        Bundle args = new Bundle();

        DiagnosticFragment fragment = new DiagnosticFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diagnostic, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new DiagnosticAdapter(getContext(), new ArrayList<Message>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        mAdapter.setListener(new DiagnosticAdapter.OnItemClickListener() {
            @Override
            public void onClick(Message diagnostic) {
                if (presenter != null) presenter.click(diagnostic);
            }
        });
    }

    @Override
    public void display(List<Message> diagnostics) {
        mAdapter.clear();
        mAdapter.addAll(diagnostics);
    }

    @Override
    public void clear() {
        mAdapter.clear();
    }

    @Override
    public void setPresenter(@Nullable DiagnosticContract.Presenter presenter) {
        this.presenter = presenter;
    }
}
