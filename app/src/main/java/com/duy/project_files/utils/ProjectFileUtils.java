package com.duy.project_files.utils;

import java.io.File;

/**
 * Created by duy on 18/07/2017.
 */

public class ProjectFileUtils {
    public static boolean inSrcDir(File projectF, File current) {
        try {
            String path = projectF.getPath();
            path += "/src/main/java";
            return current.getPath().startsWith(path);
        } catch (Exception e) {
            return false;
        }
    }
}
