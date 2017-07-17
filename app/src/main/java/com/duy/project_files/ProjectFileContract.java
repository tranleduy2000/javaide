package com.duy.project_files;

import java.io.File;

/**
 * Created by Duy on 17-Jul-17.
 */

public class ProjectFileContract {
    public interface View {

    }

    public interface Presenter {

    }

    public interface Listener {
        void onFileClick(File file);
    }
}
