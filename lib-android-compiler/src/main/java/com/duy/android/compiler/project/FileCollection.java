package com.duy.android.compiler.project;


import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileCollection {
    private List<File> rootDirs;

    public FileCollection(List<File> rootDirs) {
        this.rootDirs = rootDirs;
    }

    public ArrayList<File> filter(FileFilter fileFilter) {
        ArrayList<File> files = new ArrayList<>();
        for (File rootDir : rootDirs) {
            filter(rootDir, files, fileFilter);
        }
        return files;
    }

    private void filter(File file, ArrayList<File> result, FileFilter fileFilter) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                filter(child, result, fileFilter);
            }
        } else {
            if (fileFilter.accept(file)) {
                result.add(file);
            }
        }
    }
}
