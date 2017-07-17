package com.duy.project_files;

/**
 * Created by Duy on 17-Jul-17.
 */

public class ProjectFilePresenter implements ProjectFileContract.Presenter {
    private ProjectFileContract.View view;

    public ProjectFilePresenter(ProjectFileContract.View view) {
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
