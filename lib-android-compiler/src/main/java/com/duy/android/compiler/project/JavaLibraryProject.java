package com.duy.android.compiler.project;

import java.io.File;

public class JavaLibraryProject extends JavaProject {
    public JavaLibraryProject(File rootFile) {
        super(rootFile, "", "");
    }

    public JavaLibraryProject(File root, String mainClassName, String packageName) {
        super(root, mainClassName, packageName);
    }
}
