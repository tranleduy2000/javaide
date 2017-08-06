package com.duy.project.file.java;

import android.support.annotation.CallSuper;
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

public class JavaProjectFile implements Serializable, Cloneable {
    private static final String TAG = "ProjectFile";
    public final File dirLibs;
    public final File dirSrcMain;
    public final File dirJava;
    public final File dirBuildClasses;
    /* Project */
    protected final File dirRoot;
    protected final File dirProject;
    /* Build */
    protected final File dirBuild;
    protected final File dirOutput;
    protected final File dirOutputJar;
    public File dirDexedLibs;
    public File classpathFile;
    protected File dirDexedClass;
    protected File dexedClassesFile;
    /*Main class*/
    private ClassFile mainClass;
    private String projectName;
    private String packageName;

    public JavaProjectFile(File root, String mainClassName, String packageName, String projectName,
                           String classpath) {
        Log.d(TAG, "JavaProjectFile() called with: root = [" + root + "], mainClassName = ["
                + mainClassName + "], packageName = [" + packageName + "], projectName = ["
                + projectName + "], classpath = [" + classpath + "]");

        this.mainClass = new ClassFile(mainClassName);
        this.projectName = projectName;
        this.packageName = packageName;
        this.dirRoot = root;

        dirProject = new File(dirRoot, projectName);
        dirLibs = new File(dirProject, "libs");
        dirSrcMain = new File(dirProject, "src" + File.separator + "main");
        dirJava = new File(dirSrcMain, "java");

        dirBuild = new File(dirProject, "build");
        dirBuildClasses = new File(dirBuild, "classes");
        dirOutput = new File(dirBuild, "output");
        dirOutputJar = new File(dirOutput, "jar");
        dirDexedLibs = new File(dirBuild, "dexedLibs");
        dirDexedClass = new File(dirBuild, "dexedClasses");

        dexedClassesFile = new File(dirDexedClass, projectName + ".dex");
        classpathFile = new File(classpath);

        if (!dirRoot.exists()) {
            dirRoot.mkdirs();
            dirRoot.setReadable(true, true);
        }
        if (!dirProject.exists()) {
            dirProject.mkdirs();
            dirProject.setReadable(true, true);
        }
        if (!dirLibs.exists()) {
            dirLibs.mkdirs();
            dirLibs.setReadable(true, true);
        }
        if (!dirSrcMain.exists()) {
            dirSrcMain.mkdirs();
            dirSrcMain.setReadable(true, true);
        }
        if (!dirJava.exists()) {
            dirJava.mkdirs();
            dirJava.setReadable(true, true);
        }
        if (!dirBuildClasses.exists()) {
            dirBuildClasses.mkdirs();
            dirBuildClasses.setReadable(true, true);
        }
    }

    public static File createClass(JavaProjectFile projectFile,
                                   String currentPackage, String className,
                                   String content) {
        File file = new File(projectFile.dirJava, currentPackage.replace(".", File.separator));
        if (!file.exists()) file.mkdirs();
        File classf = new File(file, className + ".java");
        FileManager.saveFile(classf, content);

        Log.d(TAG, "createClass() returned: " + classf);
        return classf;
    }

    @Nullable
    public static JavaProjectFile restore(@Nullable JSONObject json) throws JSONException {
        if (json == null) return null;
        ClassFile mainClass = new ClassFile("");
        if (json.has("main_class_mame")) {
            mainClass = new ClassFile(json.getString("main_class_mame"));
        }
        File dirRoot = null;
        String packageName = null;
        String projectName = null;
        String classpath = null;
        if (json.has("root_dir")) dirRoot = new File(json.getString("root_dir"));
        if (json.has("package_name")) packageName = json.getString("package_name");
        if (json.has("project_name")) projectName = json.getString("project_name");
        if (json.has("classpath")) classpath = json.getString("classpath");
        if (dirRoot == null || packageName == null || projectName == null || classpath == null) {
            return null;
        }
        return new JavaProjectFile(dirRoot, mainClass.getName(), packageName, projectName, classpath);
    }

    public File getDexedClassesFile() throws IOException {
        if (!dexedClassesFile.exists()) {
            dexedClassesFile.getParentFile().mkdirs();
            dexedClassesFile.createNewFile();
        }
        return dexedClassesFile;
    }

    public File getDirLibs() {
        if (!dirLibs.exists()) dirLibs.mkdirs();
        return dirLibs;
    }

    public File getDirDexedLibs() {
        if (!dirDexedLibs.exists()) dirDexedLibs.mkdirs();
        return dirDexedLibs;
    }

    public File getDirDexedClass() {
        return dirDexedClass;
    }

    public File getDirOutputJar() {
        if (!dirOutputJar.exists()) dirOutputJar.mkdirs();
        return dirOutputJar;
    }

    public File getDirBuildClasses() {
        if (!dirBuildClasses.exists()) dirBuildClasses.mkdirs();
        return dirBuildClasses;
    }

    public void createBuildDir() {
        if (!dirBuild.exists()) dirBuild.mkdirs();
        if (!dirBuildClasses.exists()) dirBuildClasses.mkdirs();
        if (!dirOutput.exists()) dirOutput.mkdirs();
        if (!dirOutputJar.exists()) dirOutputJar.mkdirs();
    }

    public void mkdirs() {
        if (!dirRoot.exists()) dirRoot.mkdirs();
        if (!dirProject.exists()) dirProject.mkdirs();
        if (!dirLibs.exists()) dirLibs.mkdirs();
        if (!dirSrcMain.exists()) dirSrcMain.mkdirs();
        if (!dirJava.exists()) dirJava.mkdirs();
        if (!dirBuildClasses.exists()) dirBuildClasses.mkdirs();
    }

    @CallSuper
    public void clean() {
        FileManager.deleteFolder(dirBuildClasses);
        FileManager.deleteFolder(dirDexedLibs);
    }

    public File getRootDir() {
        return dirRoot;
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

    public JavaProjectFile createMainClass() throws IOException {
        this.mkdirs();
        if (packageName != null) {
            //create package file
            File packageF = new File(dirJava, packageName.replace(".", File.separator));
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
            File[] files = dirJava.listFiles();
            packageName = "";
            if (files == null) {
            } else if (files.length > 0) {
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

    public File getProjectDir() {
        return dirProject;
    }

    public JSONObject exportJson() {
        JSONObject json = new JSONObject();
        try {
            if (mainClass != null) json.put("main_class_mame", mainClass.getName());
            json.put("root_dir", dirRoot);
            json.put("package_name", packageName);
            json.put("project_name", projectName);
            json.put("classpath", classpathFile.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public String toString() {
        return "ProjectFile{" +
                "dirRoot='" + dirRoot + '\'' +
                ", packageName='" + packageName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", mainClass='" + mainClass + '\'' +
                '}';
    }

    public String getRootPackage() {
        if (packageName.contains(".")) {
            return packageName.substring(0, packageName.indexOf("."));
        } else {
            return packageName;
        }
    }

    public String getClassPath() {
        String classpath = "";
        File[] files = getDirLibs().listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File jarLib = files[i];
                if (jarLib.isFile() && jarLib.getName().endsWith(".jar")) {
                    if (i != 0) {
                        classpath += File.pathSeparator + jarLib.getPath();
                    } else {
                        classpath += jarLib.getPath();
                    }
                }
            }
        }
        classpath += (classpath.isEmpty() ? "" : File.pathSeparator) + classpathFile.getPath();
        Log.d(TAG, "getClassPath() returned: " + classpath);
        return classpath;
    }

    public File getDirJava() {
        if (!dirJava.exists()) dirJava.mkdirs();
        return dirJava;
    }
}
