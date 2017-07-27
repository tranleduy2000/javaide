package com.duy.project;

import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Duy on 17-Jul-17.
 */

public class ProjectFileContract {
    public interface View {
        void display(ProjectFile projectFile, boolean expand);

        void refresh();

        void setPresenter(Presenter presenter);
    }

    public interface Presenter {
        void show(ProjectFile projectFile, boolean expand);

        void refresh(ProjectFile projectFile);
    }

    public interface ActionCallback {
        void onSuccess(File newf);

        void onFailed(@Nullable Exception e);
    }


    public interface FileActionListener {
        void onFileClick(File file, @Nullable ActionCallback callBack);

        void onFileLongClick(File file, @Nullable  ActionCallback callBack);

        boolean doRemoveFile(File file, ActionCallback callBack);

        boolean doCreateNewClass(File file, ActionCallback callBack);
    }
}
