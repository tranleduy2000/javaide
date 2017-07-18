package com.duy.project_files;

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

    public String getSimpleName() {
        return name.substring(name.lastIndexOf(".") + 1, name.length());
    }

    public String getPackage() {
        return name.substring(0, name.lastIndexOf("."));
    }

    public String getRootPackage() {
        String aPackage = getPackage();
        if (aPackage.contains(".")) {
            return aPackage.substring(0, aPackage.indexOf(".") - 1);
        } else {
            return aPackage;
        }
    }

    public boolean exist(ProjectFile parent) {
        String path = getPath(parent);
        return !path.isEmpty() && new File(path).exists();
    }


    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    public String getPath(ProjectFile parent) {
        try {
            String projectDir = parent.getProjectDir();
            File src = new File(projectDir, "src/main/java");
            if (!src.exists()) return null;

            File file = new File(src, name.replace(".", File.separator) + ".java");
            return file.getPath();
        } catch (Exception e) {
            return "";
        }
    }
}
