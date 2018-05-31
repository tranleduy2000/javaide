package com.duy.projectview.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.duy.ide.R;

import java.io.File;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogSelectType extends AppCompatDialogFragment {
    public static final String TAG = "DialogNewAndroidProject";
    private OnFileTypeSelectListener listener;

    public static DialogSelectType newInstance(File parent) {

        Bundle args = new Bundle();
        args.putSerializable("parent", parent);
        DialogSelectType fragment = new DialogSelectType();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnFileTypeSelectListener) getActivity();
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_file_type, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final File file = (File) getArguments().getSerializable("parent");
        ((TextView) view.findViewById(R.id.txt_path)).setText(file.getPath());
        ListView listView = view.findViewById(R.id.file_types);
        final String[] fileTypes = getResources().getStringArray(R.array.select_type);
        listView.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, fileTypes));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) listener.onFileTypeSelected(file, fileTypes[position]);
                dismiss();
            }
        });
    }

    public interface OnFileTypeSelectListener {
        void onFileTypeSelected(File parent, String ext);
    }
}
