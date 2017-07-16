package com.duy.editor.project_files;

import java.io.Serializable;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ClassFile implements Serializable, Cloneable {
    private String simpleName;
    private String packageName;

    public ClassFile(String simpleName, String packageName) {
        this.simpleName = simpleName;

        this.packageName = packageName;
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
}
