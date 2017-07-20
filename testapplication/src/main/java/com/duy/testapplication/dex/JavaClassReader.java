package com.duy.testapplication.dex;

import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.testapplication.model.ClassConstructor;
import com.duy.testapplication.model.ClassDescription;
import com.duy.testapplication.model.FieldDescription;
import com.duy.testapplication.model.MethodDescription;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassReader {
    private static final String TAG = "JavaClassReader";
    private String classpath;
    private String outDir;
    private HashMap<String, Class> mClasses = new HashMap<>();
    private DexClassLoader mDexClassLoader;
    private boolean loaded = false;

    public JavaClassReader(String classpath, String outDir) {
        this.classpath = classpath;
        this.outDir = outDir;

        mDexClassLoader = new DexClassLoader(classpath, outDir, null,
                ClassLoader.getSystemClassLoader());
    }

    public HashMap<String, Class> getAllClassesFromJar() {
        HashMap<String, Class> classes = new HashMap<>();
        try {
            JarFile jarFile = new JarFile(classpath);
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try {
                    Class c = mDexClassLoader.loadClass(className);
                    classes.put(c.getName(), c);
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void load() {
        Log.d(TAG, "load() called");
        if (loaded) {
            return;
        }
        this.mClasses.putAll(getAllClassesFromJar());
        Log.d(TAG, "load: " + mClasses.size());
    }

    public void dispose() {
        Log.d(TAG, "dispose() called");

        mClasses.clear();
    }

    @Nullable
    public ClassDescription readClassByName(String className, boolean b) {
        Class aClass = mClasses.get(className);
        if (aClass != null) {
            String superclass = aClass.getSuperclass() != null ? aClass.getSuperclass().getName() : "";
            ClassDescription desc = new ClassDescription(aClass.getSimpleName(), aClass.getName(), superclass, 0);
            for (Constructor constructor : aClass.getConstructors()) {
                desc.addConstructor(new ClassConstructor(constructor));
            }
            for (Field field : aClass.getDeclaredFields()) {
                desc.addField(new FieldDescription(field));
            }
            for (Method method : aClass.getDeclaredMethods()) {
                desc.addMethod(new MethodDescription(method));
            }

            return desc;
        }
        return null;
    }
}
