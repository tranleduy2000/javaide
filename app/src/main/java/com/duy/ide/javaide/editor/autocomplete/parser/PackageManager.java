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

package com.duy.ide.javaide.editor.autocomplete.parser;

import android.os.FileObserver;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.javaide.editor.autocomplete.model.PackageDescription;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class PackageManager {
    private static final String TAG = "AutoCompletePackage";
    private PackageDescription root;
    private FileObserver fileObserver;

    public PackageManager() {
        root = PackageDescription.root();
    }

    public void init(JavaProject projectFile, JavaClassManager classReader) {
        Log.d(TAG, "init() called with: classReader = [" + classReader + "]");

        ArrayList<IClass> classes = classReader.getAllClasses();
        for (IClass clazz : classes) {
            root.put(clazz.getFullClassName());
        }
        // TODO: 16-Aug-17 file watcher
//        final String parentPath = projectFile.getDirSrcJava().getPath();
//        fileObserver = new FileObserver(parentPath) {
//            @Override
//            public void onEvent(int event, String path) {
//                remove(path, parentPath);
//                add(path, parentPath);
//            }
//        };
//        fileObserver.startWatching();
    }

    private void add(String child, String parent) {
        if (child.startsWith(parent)) {
            child = child.substring(parent.length() + 1);
            child = child.replace(File.separator, ".");
            root.put(child);
        }
    }

    @Nullable
    private PackageDescription remove(String child, String parent) {
        if (child.startsWith(parent)) {
            child = child.substring(parent.length() + 1);
            child = child.replace(File.separator, ".");
            return root.remove(child);
        }
        return null;
    }

    public void destroy() {
        if (fileObserver != null) fileObserver.stopWatching();
    }

    @Nullable
    public PackageDescription trace(String child) {
        return this.root.get(child);
    }

}
