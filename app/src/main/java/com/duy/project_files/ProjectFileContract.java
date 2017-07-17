package com.duy.project_files;

import java.io.File;

/**
 * Created by Duy on 17-Jul-17.
 */

public class ProjectFileContract {
    public interface View {
        void display(ProjectFile projectFile);

        void refresh();

        void setPresenter(Presenter presenter);
    }

    public interface Presenter {
    }

    public interface OnItemClickListener {
        void onClickDelete(File file);

        void onClickCreateNew(File parent);
    }
}
