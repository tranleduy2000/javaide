package com.duy.projectview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.android.compiler.file.java.JavaProject;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

/**
 * Created by Duy on 20-Dec-17.
 */

public class ProjectFileContract {
    public interface View {
        void display(JavaProject projectFile, boolean expand);

        TreeNode refresh();

        void setPresenter(ProjectFileContract.Presenter presenter);
    }

    public interface Presenter {
        void show(JavaProject projectFile, boolean expand);

        void refresh(JavaProject projectFile);
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
