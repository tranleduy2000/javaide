package com.duy.projectview.dialog;

import android.app.Dialog;
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
    private OnFileTypeSelectListener mListener;
    private File mCurrentDir;

    public static DialogSelectType newInstance(File parent, OnFileTypeSelectListener onFileTypeSelectListener) {
        DialogSelectType fragment = new DialogSelectType();
        fragment.mCurrentDir = parent;
        fragment.mListener = onFileTypeSelectListener;
        return fragment;
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
        if (mCurrentDir == null) {
            dismiss();
            return;
        }

        ((TextView) view.findViewById(R.id.txt_path)).setText(mCurrentDir.getPath());
        ListView listView = view.findViewById(R.id.file_types);
        final String[] fileTypes = getResources().getStringArray(R.array.select_type);
        listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, fileTypes));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) mListener.onTypeSelected(mCurrentDir, fileTypes[position]);
                dismiss();
            }
        });
    }

    public interface OnFileTypeSelectListener {
        void onTypeSelected(File parent, String ext);
    }
}
