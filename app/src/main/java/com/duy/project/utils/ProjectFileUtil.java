package com.duy.project.utils;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by duy on 18/07/2017.
 */

public class ProjectFileUtil {
    public static boolean inSrcDir(File projectF, File current) {
        try {
            String path = projectF.getPath();
            path += "/src/main/java";
            return current.getPath().startsWith(path);
        } catch (Exception e) {
            return false;
        }
    }


    public static boolean isRoot(File root, File current) {
        try {
            return root.getPath().equals(current.getPath());
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    public static String findPackage(String rootDir, File currentFolder) {
        try {
            rootDir = rootDir + "/src/main/java";
            String path = currentFolder.getPath();
            if (path.startsWith(rootDir)) {
                String pkg = path.substring(rootDir.length() + 1);
                pkg = pkg.replace(File.separator, ".");
                return pkg;
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}
