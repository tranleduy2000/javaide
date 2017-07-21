package com.duy.autocomplete.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 *Created by Duy on 20-Jul-17.
 */

public class JavaUtil {
    public static String getSimpleName(String className) {
        if (className.contains(".")) {
            return className.substring(className.lastIndexOf("."));
        } else {
            return className;
        }
    }

    @NonNull
    public static String getPackageName(String classname) {
        if (classname.contains(".")) {
            return classname.substring(0, classname.indexOf("."));
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

}
