/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.projectview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.android.compiler.project.JavaProject;
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

        void onNewFileCreated(@NonNull File file);

        void clickRemoveFile(File file, Callback callback);

        void onClickNewButton(File file, Callback callback);

        void clickNewModule();
    }

    public interface Callback {
        void onSuccess(File file);

        void onFailed(@Nullable Exception e);
    }
}
