package com.duy.ide.autocomplete.dex;

import android.support.annotation.NonNull;

import com.duy.ide.autocomplete.model.ClassDescription;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final String TAG = "JavaDexClassLoader";
    private JavaClassReader mClassReader;

    public JavaClassReader getClassReader() {
        return mClassReader;
    }

    public JavaDexClassLoader(File classpath, File outDir) {
        mClassReader = new JavaClassReader(classpath.getPath(), outDir.getPath());
    }

    @NonNull
    public ArrayList<ClassDescription> findClass(String simpleNamePrefix) {
        return mClassReader.findClass(simpleNamePrefix);
    }


    public void touchClass(String className) {
        ArrayList<ClassDescription> classDescriptions = findClass(className);
        if (classDescriptions.size() > 0) {
            classDescriptions.get(0).setLastUsed(System.currentTimeMillis());
        }
    }

    public ClassDescription loadClass(String className) {
        return mClassReader.readClassByName(className);
    }

    public void loadAllClasses(boolean fullRefresh) {
        mClassReader.load();
    }
}
