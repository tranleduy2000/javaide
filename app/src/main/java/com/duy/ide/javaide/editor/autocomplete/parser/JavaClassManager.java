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

import android.support.annotation.Nullable;
import android.util.Log;

import com.android.annotations.NonNull;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.JavaProject;
import com.duy.common.data.Pair;
import com.duy.common.interfaces.Filter;
import com.duy.ide.javaide.editor.autocomplete.model.ClassDescription;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassManager {

    private static final String TAG = "JavaClassReader";
    private static JavaClassManager INSTANCE;

    /**
     * All classes sorted by simple class name
     */
    private final ArrayList<Pair<String, Class>> mSimpleClasses = new ArrayList<>();
    private final HashMap<String, ClassDescription> mLoaded = new HashMap<>();

    private boolean loaded = false;
    private File mClasspath;
    private File mTempDir;
    /**
     * All classes sorted by full class name
     */
    private ArrayList<Class> mClasses = new ArrayList<>();


    private JavaClassManager(File classpath, File tempDir) {
        this.mClasspath = classpath;
        this.mTempDir = tempDir;
    }

    public static JavaClassManager getInstance(File classpath, File tempDir) {
        if (INSTANCE == null) {
            INSTANCE = new JavaClassManager(classpath, tempDir);
        }
        return INSTANCE;
    }

    public static JavaClassManager getInstance() {
        return INSTANCE;
    }

    public ArrayList<Class> getAllClasses() {
        return mClasses;
    }

    public ArrayList<Class> getAllClassesFromProject(@NonNull JavaProject projectFolder) {
        ArrayList<Class> classes = new ArrayList<>();
        boolean android = projectFolder instanceof AndroidAppProject;
        //load all class from classpath
        if (mClasspath != null) {
            classes.addAll(getAllClassesFromJar(android, mClasspath));
        }
        if (projectFolder.getDirBuildDexedLibs().listFiles() != null
                && projectFolder.getDirBuildDexedLibs().listFiles().length > 0) {
            for (File lib : projectFolder.getDirBuildDexedLibs().listFiles()) {
                if (lib.getPath().endsWith(".jar")) {
                    classes.addAll(getAllClassesFromJar(android, lib));
                } else if (lib.getPath().endsWith(".dex")) {
                    classes.addAll(getAllClassesFromDex(android, lib.getPath()));
                }
            }
        }
        return classes;
    }

    private Collection<? extends Class> getAllClassesFromDex(boolean android, String path) {
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
        } catch (IOException e) {
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

    public void load(JavaProject projectFolder) {
        mClasses.clear();
        mClasses.addAll(getAllClassesFromProject(projectFolder));
        long time = System.currentTimeMillis();
        Collections.sort(mClasses, new Comparator<Class>() {
            @Override
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Class mClass : mClasses) {
            mSimpleClasses.add(new Pair<>(mClass.getSimpleName(), mClass));
        }
        Collections.sort(mSimpleClasses, new Comparator<Pair<String, Class>>() {
            @Override
            public int compare(Pair<String, Class> o1, Pair<String, Class> o2) {
                return o1.first.compareTo(o2.first);
            }
        });

        System.out.println("load classes " + (System.currentTimeMillis() - time));
        loaded = true;
    }

    public void dispose() {
        mClasses.clear();
    }

    /**
     * @param fullClassName - full class name
     * @param instance      - instant of full class name, can use {@link Class#forName(String)}
     */
    @Nullable
    public ClassDescription getParsedClass(String fullClassName,
                                           @Nullable Class instance) {
        ClassDescription cache = mLoaded.get(fullClassName);
        if (cache != null) {
            return cache;
        }
        Class clazz = instance != null ? instance : binarySearch(fullClassName);
        if (clazz == null) {
            String javaLangClass = "java.lang." + fullClassName;
            clazz = binarySearch(javaLangClass);
        }
        if (clazz != null) {
            ClassDescription classDesc = new ClassDescription(clazz);
            mLoaded.put(fullClassName, classDesc);
            //careful, infinity loop if init member in cts
            classDesc.initMembers(clazz);
            return classDesc;
        }
        return null;
    }

    @NonNull
    public ClassDescription getClassWrapper(@NonNull Class clazz) {
        ClassDescription cache = mLoaded.get(clazz.getName());
        if (cache != null) {
            return cache;
        }
        ClassDescription classDesc = new ClassDescription(clazz);
        mLoaded.put(clazz.getName(), classDesc);
        classDesc.initMembers(clazz);
        return classDesc;
    }

    public void insertParsedClass(String fullName, ClassDescription classDescription) {
        mLoaded.put(fullName, classDescription);
    }

    /**
     * search class
     *
     * @param className - full class name
     */
    @Nullable
    private Class binarySearch(String className) {
        int left = 0;
        int right = mClasses.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            Class value = mClasses.get(mid);
            if (value.getName().equals(className)) {
                return value;
            }
            if (value.getName().compareTo(className) < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    /**
     * search class with binary search
     *
     * @param simpleName - simple class name
     */
    @Nullable
    private List<Pair<String, Class>> binarySearch(ArrayList<Pair<String, Class>> classes, String simpleName) {
        //find left index
        int start = -1, end = -1;
        int left = 0;
        int right = classes.size() - 1;
        //search left most
        while (left <= right) {
            int mid = (left + right) / 2;
            String midValue = classes.get(mid).first.substring(0, Math.min(classes.get(mid).first.length(),
                    simpleName.length()));
            if (midValue.compareTo(simpleName) < 0) { //mid < key
                left = mid + 1;
            } else if (midValue.compareTo(simpleName) > 0) {
                right = mid - 1;
            } else {
                start = mid;
                end = mid;
                //exit here
                while (start >= 0 && classes.get(start).first.substring(0, Math.min(classes.get(start).first.length(),
                        simpleName.length())).equals(simpleName)) {
                    start--;
                }
                //exit here
                while (end < classes.size() && classes.get(end).first.substring(0, Math.min(classes.get(end).first.length(),
                        simpleName.length())).equals(simpleName)) {
                    end++;
                }
                start++;
                end--;
                break;
            }
        }
        if (end >= 0 && start >= 0 && end - start + 1 >= 1) {
            return classes.subList(start, end + 1);
        }
        return null;
    }

    @NonNull
    public ArrayList<ClassDescription> find(@NonNull String simpleNamePrefix,
                                            @Nullable Filter<Class> filter) {
        ArrayList<ClassDescription> result = new ArrayList<>();

        //find with simple name
        List<Pair<String, Class>> simple = binarySearch(mSimpleClasses, simpleNamePrefix);
        if (simple != null) {
            for (Pair<String, Class> c : simple) {
                if (filter != null) {
                    if (!filter.accepts(c.second)) {
                        continue;
                    }
                }
                ClassDescription classDescription = getClassWrapper(c.second);
                result.add(classDescription);
            }
        }
        return result;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
