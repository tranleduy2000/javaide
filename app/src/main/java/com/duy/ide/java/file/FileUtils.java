package com.duy.ide.java.file;

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

    public static String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();
        }
    }

    public static String ext(String path) {
        if (path.contains(".")) {
            return path.substring(path.lastIndexOf(".") + 1);
        } else {
            return "";
        }
    }

    public static boolean canEdit(File file) {
        String[] exts = {".java", ".xml", ".txt", ".gradle", ".json"};
        return file.canWrite() && hasExtension(file, exts);
    }

}
