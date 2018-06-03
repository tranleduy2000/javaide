package com.duy.ide;

import com.android.annotations.NonNull;

import java.io.File;

/**
 * Created by duy on 19/07/2017.
 */

public class EditPageContract {
    public interface SourceView {
        void gotoLine(int line, int col);

        void display(String src);

        void display(File src);

        void setPresenter(Presenter presenter);

        void saveFile();

        void undo();

        void redo();

        void paste();

        void copyAll();

        void formatCode();

        File getCurrentFile();
    }

    public interface Presenter {
        int gotoPage(File path);

        int gotoPage(String path);

        void addPage(String path, boolean select);

        void addPage(@NonNull File path, boolean select);

        void invalidateTab();

        void removePage(String path);

        void removePage(int pos);

        boolean hasPage(String path);

        int getPagePosition(String path);

        SourceView getCurrentPage();

        void showError(SourceView sourceView, int line);

        void pause();
    }
}
