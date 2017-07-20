package com.duy.testapplication.dex;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Duy on 20-Jul-17.
 */

public class JavaClassReader {
    private String javaHome;

    public JavaClassReader(String javaHome) {
        this.javaHome = javaHome;
    }

    public static ArrayList<Class> readAllClassesFromJar(String path) {
        ArrayList<Class> classes = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(path);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + path + "!/")};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                try {
                    Class c = cl.loadClass(className);
                    classes.add(c);
                } catch (ClassNotFoundException e2) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    public void readAllClassesFromClasspath(String classPath, boolean skipLibs) {
        String[] split = classPath.split(":");
        ArrayList<Class> result = new ArrayList<>();
        for (String s : split) {
            result.addAll(readAllClassesFromPath(classPath, false));
        }
    }

    private ArrayList<Class> readAllClassesFromPath(String path, boolean skipLib) {
        ArrayList<Class> result = new ArrayList<>();
        if (skipLib && (path.endsWith(".jar") || path.endsWith("*"))) {
            return result;
        } else if (path.endsWith(".jar")) {
            result = readAllClassesFromJar(path);
        } else if (path.endsWith("*")) {
            path = path.replace("*", "");
            File file = new File(path);
            Collection<File> files = FileUtils.listFiles(file, new String[]{".jar"}, true);
            for (File f : files) {
                result.addAll(readAllClassesFromJar(f.getPath()));
            }
        }
        return result;
    }

    private Class readClasses(String path) {
        return null;
    }
}
