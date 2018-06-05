package com.duy.android.compiler.utils;

import com.duy.android.compiler.project.JavaProject;

import java.io.File;
import java.io.IOException;

public class ProjectUtils {
    public static boolean isFileBelongProject(JavaProject project, File file) {
        if (file == null || project == null) {
            return false;
        }
        try {
            File rootDir = project.getRootDir();
            return file.getCanonicalPath().startsWith(rootDir.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
