package com.duy.project.file.java;

/**
 * Created by Duy on 17-Jul-17.
 */

public class ProjectFileContract {
    public interface View {
        void display(JavaProjectFolder projectFile, boolean expand);

        void refresh();

        void setPresenter(Presenter presenter);
    }

    public interface Presenter {
        void show(JavaProjectFolder projectFile, boolean expand);

        void refresh(JavaProjectFolder projectFile);
    }




}
