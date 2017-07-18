package com.duy.editor.file;

import java.io.File;

/**
 * Created by duy on 18/07/2017.
 */

public class FileUtils {
    public static boolean hasExtension(File file, String... exts) {
        for (String ext : exts) {
            if (file.getPath().toLowerCase().endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }


    public static boolean canRead(File file) {
        String[] exts = new String[]{".java", ".txt"};
        return file.canRead() && hasExtension(file, exts);
    }
}
