package com.duy.project_files;

/**
 * Created by Duy on 17-Jul-17.
 */

public class FilePresenter implements ProjectFileContract.Presenter {
    private ProjectFileContract.View view;

    public FilePresenter(ProjectFileContract.View view) {
        view.setPresenter(this);
        this.view = view;
    }

    @Override
    public void show(ProjectFile projectFile) {
        view.display(projectFile);
    }

    @Override
    public void refresh(ProjectFile projectFile) {
        view.refresh();
    }
}
