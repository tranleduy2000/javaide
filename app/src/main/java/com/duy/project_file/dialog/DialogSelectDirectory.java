package com.duy.project_file.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.FileSelectListener;
import com.duy.ide.file.PreferenceHelper;
import com.duy.ide.file.adapter.FileAdapterListener;
import com.duy.ide.file.adapter.FileDetail;
import com.duy.ide.file.adapter.FileListAdapter;
import com.duy.ide.utils.Build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by Duy on 17-Jul-17.
 */

public class DialogSelectDirectory extends AppCompatDialogFragment implements View.OnClickListener, View.OnLongClickListener,
        SwipeRefreshLayout.OnRefreshListener, FileAdapterListener {
    public static final String TAG = "DialogSelectDirectory";

    private FileSelectListener listener;
    private RecyclerView listFiles;
    private Activity activity;
    private String currentFolder;
    private boolean wantAFile = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    private FileListAdapter mAdapter;
    private TextView txtPath;
    private int request;

    public static DialogSelectDirectory newInstance(String path, int request) {

        Bundle args = new Bundle();
        args.putString("path", path);
        args.putInt("request", request);
        DialogSelectDirectory fragment = new DialogSelectDirectory();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FileSelectListener) getActivity();
        } catch (Exception ignored) {

        }
        activity = getActivity();
        request = getArguments().getInt("request");
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_choose_file, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentFolder = getArguments().getString("path", FileManager.EXTERNAL_DIR);
        wantAFile = false;

        bindView(view);

        //load file
        new UpdateList(currentFolder).execute();
    }

    private void bindView(View view) {
        listFiles = view.findViewById(R.id.list_file);
        listFiles.setHasFixedSize(true);
        listFiles.setLayoutManager(new LinearLayoutManager(activity));
        listFiles.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        swipeRefreshLayout = view.findViewById(R.id.refresh_view);
        swipeRefreshLayout.setOnRefreshListener(this);

        view.findViewById(R.id.action_new_folder).setOnClickListener(this);
        txtPath = view.findViewById(R.id.txt_path);
        view.findViewById(R.id.btn_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onFileSelected(new File(currentFolder), request);
                    dismiss();
                }
            }
        });
    }

    private void createNewFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.new_folder);
        builder.setView(R.layout.dialog_new_file);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText editText = (EditText) alertDialog.findViewById(R.id.edit_file_name);
        final TextInputLayout textInputLayout = (TextInputLayout) alertDialog.findViewById(R.id.hint);
        assert textInputLayout != null;
        textInputLayout.setHint(getString(R.string.enter_new_folder_name));
        Button btnOK = (Button) alertDialog.findViewById(R.id.btn_ok);
        Button btnCancel = (Button) alertDialog.findViewById(R.id.btn_cancel);
        assert btnCancel != null;
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });
        assert btnOK != null;
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get string path of in edit text
                String fileName = editText != null ? editText.getText().toString() : null;
                if (fileName.isEmpty()) {
                    editText.setError(getString(R.string.enter_new_file_name));
                    return;
                }
                //create new file
                File file = new File(currentFolder, fileName);
                file.mkdirs();
                new UpdateList(currentFolder).execute();
                alertDialog.cancel();
            }
        });

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.action_new_folder) {
            createNewFolder();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    @Override
    public void onRefresh() {
        new UpdateList(currentFolder).execute();
    }

    @Override
    public void onItemClick(View v, String name, int action) {
        if (action == ACTION_LONG_CLICK) {
            if (name.equals("..")) {
                if (currentFolder.equals("/")) {
                    new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();
                } else {
                    File tempFile = new File(currentFolder);
                    if (tempFile.isFile()) {
                        tempFile = tempFile.getParentFile()
                                .getParentFile();
                    } else {
                        tempFile = tempFile.getParentFile();
                    }
                    new UpdateList(tempFile.getAbsolutePath()).execute();
                }
            } else if (name.equals(getString(R.string.home))) {
                // TODO: 14-Mar-17
                new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();
            }

//            final File selectedFile = new File(currentFolder, name);

//            if (selectedFile.isFile() && wantAFile) {
//                // TODO: 15-Mar-17
//                if (listener != null) listener.onFileLongClick(selectedFile);
//            } else if (selectedFile.isDirectory()) {
//            }
        } else if (action == ACTION_CLICK) {
            if (name.equals("..")) {
                if (currentFolder.equals("/")) {
                    new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();
                } else {
                    File tempFile = new File(currentFolder);
                    if (tempFile.isFile()) {
                        tempFile = tempFile.getParentFile()
                                .getParentFile();
                    } else {
                        tempFile = tempFile.getParentFile();
                    }
                    new UpdateList(tempFile.getAbsolutePath()).execute();
                }
                return;
            } else if (name.equals(getString(R.string.home))) {
                // TODO: 14-Mar-17
                new UpdateList(PreferenceHelper.getWorkingFolder(activity)).execute();
                return;
            }

            final File selectedFile = new File(currentFolder, name);
//
//            if (selectedFile.isFile() && wantAFile) {
//                // TODO: 15-Mar-17
//                if (listener != null) listener.onFileSelected(selectedFile, request);
//            } else if (selectedFile.isDirectory()) {
            new UpdateList(selectedFile.getAbsolutePath()).execute();
//            }
        }
    }


    public void refresh() {
        new UpdateList(currentFolder).execute();
    }

    @Override
    public void onRemoveClick(View view, String name, int action) {
        Toast.makeText(activity, "Don't support this action", Toast.LENGTH_SHORT).show();
    }


    private class UpdateList extends AsyncTask<Void, Void, LinkedList<FileDetail>> {
        private String path;
        private String exceptionMessage;

        public UpdateList(@NonNull String path) {
            this.path = path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            txtPath.setText(path);
        }

        @Override
        protected LinkedList<FileDetail> doInBackground(final Void... params) {
            try {
                if (TextUtils.isEmpty(path)) {
                    return null;
                }

                File tempFolder = new File(path);
                if (tempFolder.isFile()) {
                    tempFolder = tempFolder.getParentFile();
                }

                String[] canOpen = {"java", "class", "jar"};

                final LinkedList<FileDetail> fileDetails = new LinkedList<>();
                final LinkedList<FileDetail> folderDetails = new LinkedList<>();
                currentFolder = tempFolder.getAbsolutePath();

                if (!tempFolder.canRead()) {

                } else {
                    File[] files = tempFolder.listFiles();
                    for (final File f : files) {
                        if (f.isDirectory() && !f.getName().equalsIgnoreCase("fonts")) {
                            folderDetails.add(new FileDetail(f.getName(), getString(R.string.folder), ""));
                        } else if (f.isFile()
                                && FilenameUtils.isExtension(f.getName().toLowerCase(), canOpen)
                                && FileUtils.sizeOf(f) <= Build.MAX_FILE_SIZE * FileUtils.ONE_KB) {
                            final long fileSize = f.length();
                            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a", Locale.getDefault());
                            String date = format.format(f.lastModified());
                            fileDetails.add(new FileDetail(f.getName(),
                                    FileUtils.byteCountToDisplaySize(fileSize), date));
                        }
                    }
                }

                folderDetails.addAll(fileDetails);
                return folderDetails;
            } catch (Exception e) {
                exceptionMessage = e.getMessage();
                return null;
            }
        }


        @Override
        protected void onPostExecute(final LinkedList<FileDetail> names) {
            if (names != null) {
                boolean isRoot = currentFolder.equals("/");
                mAdapter = new FileListAdapter(activity, names, isRoot, DialogSelectDirectory.this);
                listFiles.setAdapter(mAdapter);
            }
            if (exceptionMessage != null) {
                Toast.makeText(activity, exceptionMessage, Toast.LENGTH_SHORT).show();
            }
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
            super.onPostExecute(names);
        }
    }


}