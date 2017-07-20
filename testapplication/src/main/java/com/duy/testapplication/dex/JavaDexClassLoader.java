package com.duy.testapplication.dex;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.testapplication.model.Description;

import java.io.File;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaDexClassLoader {
    private DexClassLoader dexClassLoader;

    public JavaDexClassLoader(File classpath, File outDir) {
        dexClassLoader = new DexClassLoader(classpath.getAbsolutePath(),
                outDir.getAbsolutePath(), null,
                ClassLoader.getSystemClassLoader());
    }

    @Nullable
    public Class loadClass(String name) {
        try {
            return dexClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
        }
        return null;
    }

    @NonNull
    public ArrayList<Class> findClass(String text) {
        return null;
    }


    public ArrayList<Object> findClassMember(String className, String suffix) {
        return null;
    }

    @Nullable
    public String findSuperClassName(String className) {
        return null;
    }

    public void touch(Description description) {

    }
}
