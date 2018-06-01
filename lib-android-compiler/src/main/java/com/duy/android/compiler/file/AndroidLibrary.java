package com.duy.android.compiler.file;

import java.io.File;

public class AndroidLibrary extends AndroidProject {
    public AndroidLibrary(File root, String mainClassName, String packageName, String projectName) {
        super(root, mainClassName, packageName, projectName);
    }
}
