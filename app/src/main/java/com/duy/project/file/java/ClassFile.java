package com.duy.project.file.java;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ClassFile implements Serializable, Cloneable {
    private String name;

    public ClassFile(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleName() {
        return name.substring(name.lastIndexOf(".") + 1, name.length());
    }

    public String getPackage() {
        return name.substring(0, name.lastIndexOf("."));
    }

    public String getRootPackage() {
        String aPackage = getPackage();
        if (aPackage.contains(".")) {
            return aPackage.substring(0, aPackage.indexOf("."));
        } else {
            return aPackage;
        }
    }

    public boolean exist(JavaProjectFolder parent) {
        String path = getPath(parent);
        return !path.isEmpty() && new File(path).exists();
    }

    @NonNull
    public String getPath(JavaProjectFolder parent) {
        try {
            File file = new File(parent.javaSrcDirs.get(0), name.replace(".", File.separator) + ".java");
            return file.getPath();
        } catch (Exception e) {
            return "";
        }
    }
}
