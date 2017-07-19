package com.duy.editor;

import com.duy.compile.diagnostic.DiagnosticContract;

import java.io.File;

/**
 * Created by duy on 19/07/2017.
 */

public class EditContract {
    public interface View {
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

    }

    public interface Presenter {
        void gotoPage(File path);

        void gotoPage(String path);

        void addPage(String path, boolean select);

        void addPage(File path, boolean select);

        void invalidateTab();

        void removePage(String path);

        void removePage(int pos);

        boolean hasPage(String path);

        int getPagePosition(String path);

        DiagnosticContract.View getCurrentPage();

        void showError(DiagnosticContract.View view, int line);
    }
}
