package com.duy.ide.autocomplete.dex;

import android.support.annotation.NonNull;

import com.duy.ide.autocomplete.datastructure.Dictionary;
import com.duy.ide.autocomplete.model.ClassDescription;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private static final String TAG = "JavaDexClassLoader";
    private JavaClassReader mClassReader;
    private Dictionary mDictionary;

    public JavaClassReader getClassReader() {
        return mClassReader;
    }

    public JavaDexClassLoader(File classpath, File outDir) {
        mDictionary = new Dictionary();
        mClassReader = new JavaClassReader(classpath.getPath(), outDir.getPath());
    }

    @NonNull
    public ArrayList<ClassDescription> findClass(String namePrefix) {
        return mDictionary.find(ClassDescription.class,  namePrefix);
    }


    public void touchClass(String className) {
        ArrayList<ClassDescription> classDescriptions = findClass(className);
        if (classDescriptions.size() > 0) {
            this.touch(classDescriptions.get(0));
        }
    }

    private void touch(ClassDescription classDescription) {
        mDictionary.touch(classDescription);
    }

    public ClassDescription loadClass(String className) {
        return mClassReader.readClassByName(className);
    }

    public void loadAllClasses(boolean fullRefresh) {
        mClassReader.load();
    }
}
