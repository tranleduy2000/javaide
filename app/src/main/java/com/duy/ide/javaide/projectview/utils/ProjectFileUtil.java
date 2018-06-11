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

package com.duy.ide.javaide.projectview.utils;

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
