package com.duy.ide.java.diagnostic;

import android.os.Bundle;
import android.os.Handler;
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

public class DiagnosticFragment extends Fragment implements com.duy.ide.java.diagnostic.DiagnosticContract.View {
    public static final String TAG = "DiagnosticFragment";
    public static final int INDEX = 1;
    private final Filter<Message> mFilter = new Filter<Message>() {
        @Override
        public boolean test(Message message) {
            switch (message.getKind()) {
                case ERROR:
                case WARNING:
                case INFO:
                    return true;
                default:
                    return false;
            }
        }
    };
    private final Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    private DiagnosticAdapter mAdapter;
    @Nullable
    private com.duy.ide.java.diagnostic.DiagnosticContract.Presenter presenter;

    public static DiagnosticFragment newInstance() {
        Bundle args = new Bundle();
        DiagnosticFragment fragment = new DiagnosticFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_diagnostic, container, false);
        return null;
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
    public void display(final List<Message> diagnostics) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                mAdapter.addAll(diagnostics);
            }
        });
    }

    @Override
    public void clear() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
            }
        });
    }

    @Override
    public void setPresenter(@Nullable com.duy.ide.java.diagnostic.DiagnosticContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void appendMessages(List<Message> messages) {
        final List<Message> finalMessages = applyFilter(messages);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.addAll(finalMessages);
            }
        });
    }

    private List<Message> applyFilter(List<Message> messages) {
        ArrayList<Message> filtered = new ArrayList<>();
        for (Message message : messages) {
            if (mFilter.test(message)) {
                filtered.add(message);
            }
        }
        return filtered;
    }

    interface Filter<T> {
        boolean test(T t);
    }
}
