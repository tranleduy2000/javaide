/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.editor.autocomplete.util;

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
    public static String getClassName(File javaSrc, String filePath) {
        if (filePath.startsWith(javaSrc.getPath())) {
            //hello/src/main/java
            //hello ->
            String filename = filePath.substring(filePath.indexOf(javaSrc.getPath()) + javaSrc.getPath().length() + 1);
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
