package com.duy.ide.javaide;

import java.io.File;

public interface FileChangeListener {

    void onFileDeleted(File deleted);

    void onFileCreated(File newFile);

    void doOpenFile(File toEdit);
}