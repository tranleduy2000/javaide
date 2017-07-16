/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.editor.file;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.file.adapter.FileAdapterListener;
import com.duy.editor.file.adapter.FileDetail;
import com.duy.editor.file.adapter.FileListAdapter;
import com.duy.editor.utils.Build;
import com.github.clans.fab.FloatingActionMenu;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Locale;

//import butterknife.OnClick;


/**
 * Created by Duy on 15-Mar-17.
 */

public class FragmentFileManager extends Fragment implements
        View.OnClickListener, View.OnLongClickListener,
        SwipeRefreshLayout.OnRefreshListener, FileAdapterListener, SearchView.OnQueryTextListener {
    private static final int SORT_BY_NAME = 1;
    private static final int SORT_BY_SIZE = 2;
    private static final int SORT_BY_DATE = 3;
    private final Handler handler = new Handler();
    private FileActionListener listener;
    private FloatingActionMenu fabMenu;
    private RecyclerView listFiles;
    private Activity activity;
    private String currentFolder;
    private boolean wantAFile = true;
    private SearchView mSearchView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int sortMode = SORT_BY_NAME;
    @Nullable
    private FileListAdapter mAdapter;
    private String queryText = "";
    private final Runnable searchHandler = new Runnable() {
        @Override
        public void run() {
            if (mAdapter != null) {
                mAdapter.query(queryText);
            }
        }
    };
    private TextView txtPath;

    //    @OnClick(R.id.img_sort_size)
    public void doSortBySize(View view) {
        this.sortMode = SORT_BY_SIZE;
        swipeRefreshLayout.setRefreshing(true);
        Toast.makeText(activity, R.string.sort_size, Toast.LENGTH_SHORT).show();
        new UpdateList(currentFolder).execute();
    }

    //    @OnClick(R.id.img_sort_name)
    public void doSortByName(View view) {
        this.sortMode = SORT_BY_NAME;
        swipeRefreshLayout.setRefreshing(true);
        Toast.makeText(activity, R.string.sort_name, Toast.LENGTH_SHORT).show();
        new UpdateList(currentFolder).execute();
    }

    //    @OnClick(R.id.img_sort_date)
    public void doSortByDate(View view) {
        this.sortMode = SORT_BY_DATE;
        swipeRefreshLayout.setRefreshing(true);
        Toast.makeText(activity, R.string.sort_date, Toast.LENGTH_SHORT).show();
        new UpdateList(currentFolder).execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activity == null) activity = getActivity();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
        try {
            listener = (FileActionListener) activity;
        } catch (Exception ignored) {
            listener = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentFolder = FileManager.getApplicationPath();
        wantAFile = true; //action == Actions.SelectFile;

        bindView(view);

        //load file
        new UpdateList(currentFolder).execute();
    }

    private void bindView(View view) {
        mSearchView = view.findViewById(R.id.search_view);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);

        listFiles = view.findViewById(R.id.list_file);
        listFiles.setHasFixedSize(true);
        listFiles.setLayoutManager(new LinearLayoutManager(activity));
        listFiles.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));

        swipeRefreshLayout = view.findViewById(R.id.refresh_view);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_key_word_color));

        fabMenu = view.findViewById(R.id.fab_menu);
        fabMenu.findViewById(R.id.action_new_file).setOnClickListener(this);
        fabMenu.findViewById(R.id.action_new_folder).setOnClickListener(this);

        view.findViewById(R.id.img_sort_name).setOnClickListener(this);
        view.findViewById(R.id.img_sort_date).setOnClickListener(this);
        view.findViewById(R.id.img_sort_size).setOnClickListener(this);

        txtPath = view.findViewById(R.id.txt_path);
    }


    /**
     * show dialog create new file
     */
    private void createNewFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.new_file);
        builder.setView(R.layout.dialog_new_file);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        final EditText editText = (EditText) alertDialog.findViewById(R.id.edit_file_name);
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
                String fileName = editText.getText().toString();
                if (fileName.isEmpty()) {
                    editText.setError(getString(R.string.enter_new_file_name));
                    return;
                }

                RadioButton checkBoxPas = (RadioButton) alertDialog.findViewById(R.id.rad_class);
                RadioButton checkBoxInp = (RadioButton) alertDialog.findViewById(R.id.rad_enum);

                if (checkBoxInp != null && checkBoxInp.isChecked()) fileName += ".inp";
                else if (checkBoxPas.isChecked()) fileName += ".pas";

                //create new file
                File file = new File(currentFolder, fileName);
                try {
                    file.createNewFile();
                    new UpdateList(currentFolder).execute();
                } catch (IOException e) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
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
        if (i == R.id.action_new_file) {
            createNewFile();
            fabMenu.close(true);

        } else if (i == R.id.action_new_folder) {
            createNewFolder();
            fabMenu.close(true);

        } else if (i == R.id.img_sort_date) {
            doSortByDate(v);
        } else if (i == R.id.img_sort_name) {
            doSortByName(v);
        } else if (i == R.id.img_sort_size) {
            doSortBySize(v);
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

            final File selectedFile = new File(currentFolder, name);

            if (selectedFile.isFile() && wantAFile) {
                // TODO: 15-Mar-17
                if (listener != null) listener.onFileLongClick(selectedFile);
            } else if (selectedFile.isDirectory()) {
//            new UpdateList().execute(selectedFile.getAbsolutePath());
            }
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

            if (selectedFile.isFile() && wantAFile) {
                // TODO: 15-Mar-17
                if (listener != null) listener.onFileClick(selectedFile);
            } else if (selectedFile.isDirectory()) {
                new UpdateList(selectedFile.getAbsolutePath()).execute();
            }
        }
    }

    private void doRemoveFile(final File file) {
        listener.doRemoveFile(file);
    }

    public void refresh() {
        new UpdateList(currentFolder).execute();
    }

    @Override
    public void onRemoveClick(View view, String name, int action) {
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

        File selectedFile = new File(currentFolder, name);
        doRemoveFile(selectedFile);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.queryText = newText;
        handler.removeCallbacks(searchHandler);
        handler.postDelayed(searchHandler, 100);
        return true;
    }

    public void load(File parentFile) {
        new UpdateList(parentFile.getPath()).execute();
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
            if (mSearchView != null) {
                mSearchView.setIconified(true);
                mSearchView.setQuery("", false);
            }
        }

        /**
         * {@inheritDoc}
         */
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

                String[] canOpen = {"java"};

                final LinkedList<FileDetail> fileDetails = new LinkedList<>();
                final LinkedList<FileDetail> folderDetails = new LinkedList<>();
                currentFolder = tempFolder.getAbsolutePath();

                if (!tempFolder.canRead()) {

                } else {
                    File[] files = tempFolder.listFiles();
                    if (sortMode == SORT_BY_SIZE) {
                        Arrays.sort(files, getFileSizeComparator());
                    } else if (sortMode == SORT_BY_NAME) {
                        Arrays.sort(files, getFileNameComparator());
                    } else if (sortMode == SORT_BY_DATE) {
                        Arrays.sort(files, getFileDateComparator());
                    }

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

        private Comparator<? super File> getFileNameComparator() {
            return new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
        }

        private Comparator<? super File> getFileDateComparator() {
            return new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.lastModified() == o2.lastModified()) {
                        return 0;
                    }
                    if (o1.lastModified() > o2.lastModified()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            };
        }

        private Comparator<? super File> getFileSizeComparator() {
            return new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.length() == o2.length()) {
                        return 0;
                    }
                    if (o1.length() > o2.length()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            };
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(final LinkedList<FileDetail> names) {
            if (names != null) {
                boolean isRoot = currentFolder.equals("/");
                mAdapter = new FileListAdapter(activity, names, isRoot, FragmentFileManager.this);
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
