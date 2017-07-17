package com.duy.project_files;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ClassFile implements Serializable, Cloneable {
    private String simpleName;
    private String packageName;
    private String name;
    private String path;

    public ClassFile(String simpleName, String packageName) {
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.name = packageName + "." + simpleName;
    }

    public String getName() {
        return name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public boolean exist(ProjectFile parent) {
        String projectDir = parent.getProjectDir();
        File src = new File(projectDir, "src/main/java");
        if (!src.exists()) return false;

        File file = new File(src, name.replace(".", File.separator) + ".java");
        return file.exists();
    }
}
