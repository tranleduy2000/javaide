package com.duy.ide.javaide.autocomplete.dex;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.android.annotations.NonNull;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.JavaProject;
import com.duy.ide.javaide.autocomplete.model.ClassDescription;
import com.duy.ide.javaide.autocomplete.model.ConstructorDescription;
import com.duy.ide.javaide.autocomplete.model.FieldDescription;
import com.duy.ide.javaide.autocomplete.model.MethodDescription;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import dalvik.system.DexClassLoader;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassReader {
    private static final String TAG = "JavaClassReader";
    private String classpath;
    private String tempDir;
    /**
     * All classes sorted by full class name
     */
    private ArrayList<Class> mClasses = new ArrayList<>();

    /**
     * All classes sorted by simple class name
     */
    private ArrayList<Pair<String, Class>> mSimpleClasses = new ArrayList<>();

    private WeakHashMap<String, ClassDescription> mCache = new WeakHashMap<>();

    private boolean loaded = false;

    public JavaClassReader(String classpath, String tempDir) {
        this.classpath = classpath;
        this.tempDir = tempDir;
    }

    public ArrayList<Class> getAllClasses() {
        return mClasses;
    }

    public ArrayList<Class> getAllClassesFromProject(@NonNull JavaProject projectFolder) {
        ArrayList<Class> classes = new ArrayList<>();
        boolean android = projectFolder instanceof AndroidAppProject;
        //load all class from classpath
        if (classpath != null) {
            classes.addAll(getAllClassesFromJar(android, classpath));
        }
        if (projectFolder.getDirBuildDexedLibs().listFiles() != null
                && projectFolder.getDirBuildDexedLibs().listFiles().length > 0) {
            for (File lib : projectFolder.getDirBuildDexedLibs().listFiles()) {
                if (lib.getPath().endsWith(".jar")) {
                    classes.addAll(getAllClassesFromJar(android, lib.getPath()));
                } else if (lib.getPath().endsWith(".dex")) {
                    classes.addAll(getAllClassesFromDex(android, lib.getPath()));
                }
            }
        }
        return classes;
    }

    private Collection<? extends Class> getAllClassesFromDex(boolean android, String path) {
        Log.d(TAG, "getAllClassesFromDex() called with: android = [" + android + "], path = [" + path + "]");
        DexClassLoader dexClassLoader = new DexClassLoader(path, tempDir, null, ClassLoader.getSystemClassLoader());
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

    private ArrayList<Class> getAllClassesFromJar(boolean android, String path) {
        DexClassLoader dexClassLoader = new DexClassLoader(path, tempDir, null, ClassLoader.getSystemClassLoader());
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

    @Nullable
    public ClassDescription readClassByName(String className, @Nullable Class second) {
        ClassDescription cache = mCache.get(className);
        if (cache != null) {
            return cache;
        }
        Class aClass = second != null ? second : binarySearch(className);

        if (aClass != null) {
            String superclass = aClass.getSuperclass() != null ? aClass.getSuperclass().getName() : "";
            ClassDescription classDesc = new ClassDescription(aClass.getSimpleName(), aClass.getName(), superclass, 0);
            for (Constructor constructor : aClass.getConstructors()) {
                if (Modifier.isPublic(constructor.getModifiers())) {
                    classDesc.addConstructor(new ConstructorDescription(constructor));
                }
            }
            for (Field field : aClass.getDeclaredFields()) {
                if (Modifier.isPublic(field.getModifiers())) {
                    if (!field.getName().equals(field.getDeclaringClass().getName())) {
                        classDesc.addField(new FieldDescription(field));
                    }
                }
            }
            for (Method method : aClass.getMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    classDesc.addMethod(new MethodDescription(method));
                }
            }
            mCache.put(className, classDesc);
            return classDesc;
        }
        return null;
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
    public ArrayList<ClassDescription> findClass(String simpleNamePrefix) {
        Log.d(TAG, "findClass() called with: simpleNamePrefix = [" + simpleNamePrefix + "]");

        long start = System.currentTimeMillis();
        ArrayList<ClassDescription> classDescriptions = new ArrayList<>();
        List<Pair<String, Class>> classes = binarySearch(mSimpleClasses, simpleNamePrefix);
        if (classes != null) {
            for (Pair<String, Class> c : classes) {
                classDescriptions.add(readClassByName(c.second.getName(), c.second));
            }
        }
        Log.d(TAG, "findClass: time " + simpleNamePrefix + " - " + (System.currentTimeMillis() - start));
        return classDescriptions;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
