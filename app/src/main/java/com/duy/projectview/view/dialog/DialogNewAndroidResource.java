package com.duy.projectview.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.utils.IOUtils;
import com.duy.ide.R;
import com.duy.projectview.ProjectFileContract;

import java.io.File;

import static android.view.ViewGroup.LayoutParams;

/**
 * Created by Duy on 16-Jul-17.
 */

public class DialogNewAndroidResource extends AppCompatDialogFragment implements View.OnClickListener {
    public static final String TAG = "DialogNewClass";
    private EditText mEditName;
    @Nullable
    private ProjectFileContract.FileActionListener listener;
    @Nullable
    private File currentFolder;

    @NonNull
    private JavaProject project;

    public static DialogNewAndroidResource newInstance(@NonNull JavaProject project, @Nullable File currentFolder) {
        DialogNewAndroidResource fragment = new DialogNewAndroidResource();
        fragment.setProject(project);
        fragment.setCurrentFolder(currentFolder);
        ;
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_xml, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ProjectFileContract.FileActionListener) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                this.dismiss();
                break;
            case R.id.btn_create:
                createNewFile();
                break;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditName = view.findViewById(R.id.edit_class_name);
        view.findViewById(R.id.btn_create).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
    }


    private void createNewFile() {
        String fileName = mEditName.getText().toString();
        if (fileName.isEmpty()) {
            mEditName.setError(getString(R.string.enter_name));
            return;
        }
        try {

            File xmlFile = new File(currentFolder, fileName);
            currentFolder.mkdirs();

            String header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
            IOUtils.writeAndClose(header, xmlFile);
            if (listener != null) {
                listener.onNewFileCreated(xmlFile);
            }
            dismiss();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Can not create new file", Toast.LENGTH_SHORT).show();
        }
    }

    public void setCurrentFolder(@Nullable File currentFolder) {
        this.currentFolder = currentFolder;
    }

    public void setProject(@NonNull JavaProject project) {
        this.project = project;
    }
}
