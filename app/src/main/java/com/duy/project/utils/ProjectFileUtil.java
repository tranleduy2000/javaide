package com.duy.project.utils;

import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by duy on 18/07/2017.
 */

public class ProjectFileUtil {


    public static boolean isRoot(File root, File current) {
        try {
            return root.getPath().equals(current.getPath());
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    public static String findPackage(File javaDir, File currentFolder) {
        try {
            String path = currentFolder.getPath();
            if (path.startsWith(javaDir.getPath())) {
                String pkg = path.substring(javaDir.getPath().length() + 1);
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
