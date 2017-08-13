package com.duy.ide.editor.uidesigner.inflate;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.duy.ide.R;

import java.io.File;

/**
 * Created by Duy on 13-Aug-17.
 */

public class DialogLayoutPreview extends AppCompatDialogFragment {
    public static final String TAG = "DialogLayoutPreview";
    private Inflater inflater;

    public static DialogLayoutPreview newInstance(File content) {

        Bundle args = new Bundle();
        args.putSerializable("file", content);
        DialogLayoutPreview fragment = new DialogLayoutPreview();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_layout_preview, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inflater = new Inflater((ViewGroup) view.findViewById(R.id.content),
                (TextView) view.findViewById(R.id.txt_error));
        inflater.inflate((File) getArguments().getSerializable("file"));
    }
}
