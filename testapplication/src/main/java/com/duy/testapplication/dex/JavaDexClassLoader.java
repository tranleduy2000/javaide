package com.duy.testapplication.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.testapplication.datastructure.Dictionary;
import com.duy.testapplication.model.Description;

import java.io.File;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private DexClassLoader mDexClassLoader;
    private Dictionary mDictionary;

    public JavaDexClassLoader(File classpath, File outDir) {
        mDexClassLoader = new DexClassLoader(classpath.getAbsolutePath(),
                outDir.getAbsolutePath(), null,
                ClassLoader.getSystemClassLoader());
        mDictionary = new Dictionary();
    }

    @Nullable
    public Class loadClass(String name) {
        try {
            return mDexClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    @NonNull
    public ArrayList<Description> findClass(String namePrefix) {
        return mDictionary.find("class", namePrefix);
    }

    @Nullable
    public void findSuperClassName(String className) {
        ArrayList<Description> classes = this.findClass(className);
        Description currentClass = null;
        for (Description aClass : classes) {
            if (aClass.getClassName().equals(className)) {
                currentClass = aClass;
                break;
            }
        }
        return currentClass == null ? null : currentClass.getSuperClass();
    }

    public ArrayList<Object> findClassMember(String className, String suffix) {
        return null;
    }

    public void touch(Description description) {

    }

    public void loadAll() {

    }
}
