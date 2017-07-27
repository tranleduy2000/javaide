package com.duy.project;

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
    public void show(ProjectFile projectFile, boolean expand) {
        view.display(projectFile, expand);
    }

    @Override
    public void refresh(ProjectFile projectFile) {
        view.refresh();
    }
}
