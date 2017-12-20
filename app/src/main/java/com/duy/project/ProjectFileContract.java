package com.duy.project;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.project.file.java.JavaProjectFolder;

import java.io.File;

/**
 * Created by Duy on 20-Dec-17.
 */

public class ProjectFileContract {
    public interface View {
        void display(JavaProjectFolder projectFile, boolean expand);

        void refresh();

        void setPresenter(ProjectFileContract.Presenter presenter);
    }

    public interface Presenter {
        void show(JavaProjectFolder projectFile, boolean expand);

        void refresh(JavaProjectFolder projectFile);
    }

    public interface FileActionListener {
        /**
         * This method will be call when user click file or folder
         */
        void onFileClick(@NonNull File file, @Nullable Callback callBack);

        void onFileLongClick(@NonNull File file, @Nullable Callback callBack);

        void onNewFileCreated(@NonNull File file);

        boolean clickRemoveFile(File file, Callback callBack);

        boolean clickCreateNewFile(File file, Callback callBack);

        void clickNewModule();
    }

    public static interface Callback {
        void onSuccess(File file);

        void onFailed(@Nullable Exception e);
    }
}
