package com.duy.testapplication.dex;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassReader {
    private static final String TAG = "JavaClassReader";
    private String classpath;
    private String outDir;
    private ArrayList<Class> mClasses = new ArrayList<>();

    public JavaClassReader(String classpath, String outDir) {
        this.classpath = classpath;
        this.outDir = outDir;
    }

    public static ArrayList<Class> readAllClassesFromJar(String path, String outDir) {
        Log.d(TAG, "readAllClassesFromJar() called with: path = [" + path + "]");

        ArrayList<Class> classes = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(path);
            Enumeration<JarEntry> e = jarFile.entries();

            JavaDexClassLoader cl = new JavaDexClassLoader(new File(path), new File(outDir));

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                Class c = cl.loadClass(className);
                if (c != null) {
                    classes.add(c);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void load() {
        Log.d(TAG, "load() called");

        this.mClasses.clear();
        this.mClasses.addAll(readAllClassesFromJar(classpath, outDir));
        Log.d(TAG, "load: " + mClasses.size());
    }

    public void dispose() {
        Log.d(TAG, "dispose() called");

        mClasses.clear();
    }
}
