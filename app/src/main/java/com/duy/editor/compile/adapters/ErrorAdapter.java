package com.duy.editor.compile.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by duy on 19/07/2017.
 */

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ErrorHolder> {

    @Override
    public ErrorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ErrorHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ErrorHolder extends RecyclerView.ViewHolder {


        public ErrorHolder(View itemView) {
            super(itemView);
        }
    }
}
