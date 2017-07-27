package com.duy.project;

import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.ide.editor.completion.Template;
import com.duy.ide.file.FileManager;

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
    private static final String TAG = "ProjectFile";
    //main class name, don't include package, ex: Main
    private ClassFile mainClass;
    /**
     * root director
     */
    private String rootDir;
    private String projectName;
    private String packageName;

    public ProjectFile(String mainClassName, String packageName, String projectName) {
        this.mainClass = new ClassFile(mainClassName);
        this.projectName = projectName;
        this.packageName = packageName;
    }

    public ProjectFile() {


    }

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

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public ClassFile getMainClass() {
        return mainClass;
    }

    public void setMainClass(ClassFile classFile) {
        this.mainClass = classFile;
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

    /**
     * create new project as tree:
     * ├───bin
     * ├───build
     * ├───libs
     * └───src
     * └─main
     * └─ java
     * └──com
     * └──...
     * └──Main.class
     *
     * @param dir - parent dir for project
     * @return - path of parent of  main class
     * @throws IOException
     */
    public ProjectFile create(File dir) throws IOException {
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

        if (packageName != null) {
            //create package file
            File packageF = new File(javaF, packageName.replace(".", File.separator));
            if (!packageF.exists()) {
                packageF.getParentFile().mkdirs();
                packageF.mkdirs();
            }

            File mainFile = new File(packageF, mainClass.getSimpleName() + ".java");
            if (!mainFile.exists()) {
                mainFile.createNewFile();
                String content = Template.createClass(packageName, mainClass.getSimpleName());
                FileManager.saveFile(mainFile, content);
            }
        } else { //find package name
            File[] files = javaF.listFiles();
            packageName = "";
            if (files == null) {
            } else {
                File f = files[0];
                while (f != null && f.isDirectory()) {
                    packageName += f.getName() + ".";

                    files = f.listFiles();
                    if (files == null) f = null;
                    else f = files[0];
                }
                if (packageName.charAt(packageName.length() - 1) == '.') {
                    packageName = packageName.substring(0, packageName.length() - 1);
                }
            }

        }
        return this;
    }

    public String getProjectDir() {
        return rootDir;
    }

    public JSONObject exportJson() {
        JSONObject json = new JSONObject();
        try {
            if (mainClass != null) json.put("main_class_mame", mainClass.getName());

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
        if (json.has("main_class_mame")) {
            mainClass = new ClassFile(json.getString("main_class_mame"));
        }
        if (json.has("root_dir")) this.rootDir = json.getString("root_dir");
        if (json.has("package_name")) this.packageName = json.getString("package_name");
        if (json.has("project_name")) this.projectName = json.getString("project_name");
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

    public String getRootPackage() {
        if (packageName.contains(".")) {
            return packageName.substring(0, packageName.indexOf(".") - 1);
        } else {
            return packageName;
        }
    }
}
