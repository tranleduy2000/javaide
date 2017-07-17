package com.duy.project_files;

import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.editor.editor.completion.Template;
import com.duy.editor.file.FileManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Duy on 16-Jul-17.
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ProjectFile implements Serializable, Cloneable {
    //main class name, don't include package, ex: Main
    private ClassFile mainClass;

    /**
     * root director
     */
    private String rootDir;

    /**
     * java package name: eg: com.duy.example
     */
    private String packageName;

    //project name
    private String projectName;


    public ProjectFile(String mainClass, String packageName, String projectName) {
        this.mainClass = new ClassFile(mainClass, packageName);
        this.packageName = packageName;
        this.projectName = projectName;
    }

    public ProjectFile() {


    }

    public String getRootDir() {
        return rootDir;
    }


    public ClassFile getMainClass() {
        return mainClass;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    private static final String TAG = "ProjectFile";

    public static File createClass(ProjectFile projectFile,
                                   String currentPackage, String className,
                                   String content) {
        File file = new File(projectFile.getRootDir(), "src/main/java/" + currentPackage.replace(".", File.separator));
        if (!file.exists()) file.mkdirs();
        File classf = new File(file, className + ".java");
        FileManager.saveFile(classf, content);

        Log.d(TAG, "createClass() returned: " + classf);
        return classf;
    }

    /**
     * create new project as tree:
     * ├───bin
     * ├───build
     * ├───libs
     * └───src
     *      └─main
     *          └─ java
     *             └──com
     *                 └──...
     *                     └──Main.class
     *
     * @param dir - parent dir for project
     * @return - path of parent of  main class
     * @throws IOException
     */
    public File create(File dir) throws IOException {
        this.rootDir = new File(dir, projectName).getPath();

        //now create root director
        File root = new File(dir, projectName);
        if (!root.exists()) root.mkdirs();

        File build = new File(root, "build");
        if (!build.exists()) build.mkdirs();

        File bin = new File(root, "bin");
        if (!bin.exists()) bin.mkdirs();

        File src = new File(root, "src");
        if (!src.exists()) src.mkdirs();

        File main = new File(src, "main");
        if (!main.exists()) main.mkdirs();

        File javaF = new File(main, "java");
        if (!javaF.exists()) javaF.mkdirs();

        //create package file
        File packageF = new File(javaF, packageName.replace(".", File.separator));
        if (!packageF.exists()) {
            packageF.getParentFile().mkdirs();
            packageF.mkdirs();
        }

        //create main class
        File mainFile = new File(packageF, mainClass.getSimpleName() + ".java");
        if (!mainFile.exists()) {
            mainFile.createNewFile();
            String content = Template.createClass(packageName, mainClass.getSimpleName());
            FileManager.saveFile(mainFile, content);
        }
        mainClass.setPath(mainFile.getPath());
        return mainFile;
    }

    public String getProjectDir() {
        return rootDir;
    }

    public JSONObject exportJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("main_class_mame", mainClass.getSimpleName());
            json.put("main_class_pkg", mainClass.getPackageName());
            json.put("main_class_path", mainClass.getPath());

            Log.d(TAG, "exportJson mainClass = " + mainClass);

            json.put("root_dir", rootDir);
            json.put("package_name", packageName);
            json.put("project_name", projectName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void restore(@Nullable JSONObject json) throws JSONException {
        if (json == null) return;
        mainClass = new ClassFile("", "");
        if (json.has("main_class_mame")) {
            mainClass.setSimpleName(json.getString("main_class_mame"));
        }
        if (json.has("main_class_pkg")) {
            mainClass.setPackageName(json.getString("main_class_pkg"));
        }
        if (json.has("main_class_path")) {
            mainClass.setPath(json.getString("main_class_path"));
        }
        mainClass.setName(mainClass.getPackageName() + "." + mainClass.getSimpleName());
        Log.d(TAG, "restore mainClass = " + mainClass);

        if (json.has("root_dir")) this.rootDir = json.getString("root_dir");
        if (json.has("package_name")) this.packageName = json.getString("package_name");
        if (json.has("project_name")) this.projectName = json.getString("project_name");
    }

    public void setMainClass(ClassFile classFile) {
        this.mainClass = classFile;
    }

    @Override
    public String toString() {
        return "ProjectFile{" +
                "rootDir='" + rootDir + '\'' +
                ", packageName='" + packageName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", mainClass='" + mainClass + '\'' +
                '}';
    }
}
