package com.duy.ide.javaide.autocomplete.autocomplete;

import android.os.FileObserver;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.ide.javaide.autocomplete.dex.JavaClassReader;
import com.duy.ide.javaide.autocomplete.model.PackageDescription;
import com.duy.android.compiler.file.java.JavaProject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class AutoCompletePackage {
    private static final String TAG = "AutoCompletePackage";
    private PackageDescription root;
    private FileObserver fileObserver;

    public AutoCompletePackage() {
        root = PackageDescription.root();
    }

    public void init(JavaProject projectFile, JavaClassReader classReader) {
        Log.d(TAG, "init() called with: classReader = [" + classReader + "]");

        ArrayList<Class> classes = classReader.getAllClasses();
        for (Class aClass : classes) {
            root.put(aClass.getName());
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
