package com.duy.ide.autocomplete.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaUtil {
    public static String getSimpleName(String className) {
        if (className.contains(".")) {
            return className.substring(className.lastIndexOf(".") + 1);
        } else {
            return className;
        }
    }

    @NonNull
    public static String getPackageName(String classname) {
        if (classname.contains(".")) {
            return classname.substring(0, classname.lastIndexOf("."));
        } else {
            return "";
        }
    }

    public static String getInverseName(String className) {
        String[] split = className.split(".");
        String result = "";
        for (String s : split) {
            result = s + result;
        }
        return result;
    }


    public static boolean isValidClassName(@Nullable String name) {
        return name != null && name.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    @Nullable
    public static String getClassName(String rootDir, String filePath) {
        rootDir += "/src/main/java";
        if (filePath.startsWith(rootDir)) {
            //hello/src/main/java
            //hello ->
            String filename = filePath.substring(filePath.indexOf(rootDir) + rootDir.length() + 1);
            filename = filename.replace(File.separator, ".");
            if (filename.endsWith(".java")) {
                filename = filename.substring(0, filename.lastIndexOf(".java"));
            }
            return filename;
        } else {
            return null;
        }
    }
}
