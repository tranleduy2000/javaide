package com.duy.editor.project_files;

import com.duy.editor.editor.completion.Template;
import com.duy.editor.file.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ProjectFile {
    private String mainClassName;
    private String packageName;
    private String projectName;

    public ProjectFile(String mainClass, String packageName, String projectName) {
        this.mainClassName = mainClass;
        this.packageName = packageName;
        this.projectName = projectName;
    }

    /**
     * create new project as tree:
     * ├───bin
     * ├───build
     * ├───libs
     * └───src
     *      └─java
     *          └──com
     *              └──...
     *                  └──mainclass
     *
     * @param dir - parent dir for project
     * @return - path of parent of  main class
     * @throws IOException
     */
    public File create(File dir) throws IOException {
        //now create root director
        File root = new File(dir, projectName);
        if (!root.exists()) root.mkdirs();

        File build = new File(root, "build");
        if (!build.exists()) build.mkdirs();

        File bin = new File(root, "bin");
        if (!bin.exists()) bin.mkdirs();

        File src = new File(root, "src");
        if (!src.exists()) src.mkdirs();

        File javaF = new File(src, "java");
        if (!javaF.exists()) javaF.mkdirs();

        //create package file
        File packageF = new File(javaF, packageName.replace(".", "/"));
        if (!packageF.exists()) {
            packageF.getParentFile().mkdirs();
            packageF.mkdirs();
        }

        //create main class
        File mainFile = new File(packageF, mainClassName + ".java");
        mainFile.createNewFile();
        String content = Template.createClass(mainClassName);
        FileManager.saveFile(mainFile, content);

        return mainFile;
    }
}
