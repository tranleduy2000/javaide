package com.duy.compile.diagnostic;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duy.editor.R;

import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;

/**
 * Created by duy on 19/07/2017.
 */

public class DiagnosticAdapter extends RecyclerView.Adapter<DiagnosticAdapter.ErrorHolder> {

    private List<Diagnostic> mDiagnostics = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private OnItemClickListener listener;

    public DiagnosticAdapter(Context context, @Nullable List<Diagnostic> diagnostics) {
        this.mContext = context;
        if (diagnostics != null) {
            this.mDiagnostics = diagnostics;
        }
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public ErrorHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_compile_msg, parent, false);
        return new ErrorHolder(view);
    }

    @Override
    public void onBindViewHolder(ErrorHolder holder, int position) {
        Diagnostic diagnostic = mDiagnostics.get(position);
        switch (diagnostic.getKind()) {
            case ERROR:
                break;
            case WARNING:
                break;
            case MANDATORY_WARNING:
                break;
            case NOTE:
                break;
            case OTHER:
                break;
        }
        holder.message.setText(diagnostic.getMessage(null));
    }

    @Override
    public int getItemCount() {
        return mDiagnostics.size();
    }

    public void clear() {
        mDiagnostics.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Diagnostic> diagnostics) {
        this.mDiagnostics.addAll(diagnostics);
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public interface OnItemClickListener {
        void onClick(Diagnostic diagnostic);
    }

    public static class ErrorHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private TextView message;

        public ErrorHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.img_icon);
            message = itemView.findViewById(R.id.txt_message);
        }
    }
}
