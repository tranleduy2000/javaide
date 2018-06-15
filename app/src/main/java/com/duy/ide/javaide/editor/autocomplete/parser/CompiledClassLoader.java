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

import android.util.Log;

import com.android.annotations.NonNull;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.JavaProject;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;

/**
 * Load classes from jar file or dex file
 */
class CompiledClassLoader {
    private static final String TAG = "CompiledClassLoader";
    private static final String DOT_DEX = ".dex";
    private File mBootClasspath;
    private File mTempDir;

    CompiledClassLoader(File bootClassPath, File tempDir) {
        mBootClasspath = bootClassPath;
        mTempDir = tempDir;
    }

    @NonNull
    public ArrayList<Class> getCompiledClassesFromProject(@NonNull JavaProject project) {
        ArrayList<Class> classes = new ArrayList<>();
        boolean android = project instanceof AndroidAppProject;

        //load all class from bootclasspath
        if (mBootClasspath != null) {
            classes.addAll(getAllClassesFromJar(android, mBootClasspath));
        }

        if (project.getDirBuildDexedLibs().listFiles() != null
                && project.getDirBuildDexedLibs().listFiles().length > 0) {
            File[] files = project.getDirBuildDexedLibs().listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().endsWith(DOT_DEX);
                }
            });
            for (File lib : files) {
                classes.addAll(getAllClassesFromDex(android, lib.getPath()));
            }
        }
        return classes;
    }

    private ArrayList<Class> getAllClassesFromDex(boolean android, String path) {
        Log.d(TAG, "getAllClassesFromDex() called with: android = [" + android + "], path = [" + path + "]");
        DexClassLoader dexClassLoader = new DexClassLoader(path,
                mTempDir.getAbsolutePath(),
                null, ClassLoader.getSystemClassLoader());
        ArrayList<Class> classes = new ArrayList<>();
        try {
            dalvik.system.DexFile dexFile = new dalvik.system.DexFile(path);
            for (Enumeration<String> iter = dexFile.entries(); iter.hasMoreElements(); ) {
                String className = iter.nextElement();
                try {
                    if (android) {
                        Class c = dexClassLoader.loadClass(className);
                        classes.add(c);
                    } else if (!className.startsWith("android")) {
                        Class c = dexClassLoader.loadClass(className);
                        classes.add(c);
                    }
                } catch (ClassNotFoundException e1) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classes;
    }

    private ArrayList<Class> getAllClassesFromJar(boolean android, File path) {
        DexClassLoader dexClassLoader = new DexClassLoader(path.getPath(),
                mTempDir.getAbsolutePath(),
                null, ClassLoader.getSystemClassLoader());
        ArrayList<Class> classes = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(path);
            Enumeration<JarEntry> e = jarFile.entries();

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try {
                    if (android) {
                        Class c = dexClassLoader.loadClass(className);
                        classes.add(c);
                    } else if (!className.startsWith("android")) {
                        Class c = dexClassLoader.loadClass(className);
                        classes.add(c);
                    }
                } catch (ClassNotFoundException e1) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

}
