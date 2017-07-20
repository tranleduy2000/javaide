package com.duy.testapplication.autocomplete;

import android.support.annotation.NonNull;

/**
 * Created by Duy on 20-Jul-17.
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

}
