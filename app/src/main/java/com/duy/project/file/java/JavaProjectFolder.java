package com.duy.project.file.java;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.ide.autocomplete.Template;
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
public class JavaProjectFolder implements Serializable, Cloneable {
    private static final String TAG = "ProjectFile";

    public File dirLibs;
    public File dirSrcMain;
    public File dirJava;
    public File dirBuildClasses;
    public File dirGenerated;
    public File dirGeneratedSource;

    /* Project */
    protected File dirRoot;
    protected File dirProject;

    /* Build */
    protected File dirBuild;
    protected File dirOutput;
    protected File dirOutputJar;
    protected File dirDexedLibs;
    protected File dirDexedClass;
    protected File dexedClassesFile;
    protected String packageName;

    /*Main class*/
    private ClassFile mainClass;
    private String projectName;
    private File jarArchive;

    public JavaProjectFolder(File root, String mainClassName, String packageName, String projectName) {
        this.mainClass = new ClassFile(mainClassName);
        this.projectName = projectName;
        this.packageName = packageName;
        this.dirRoot = root;
        initJavaProject();
    }

    @Nullable
    public static JavaProjectFolder restore(@Nullable JSONObject json) throws JSONException {
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
        return new JavaProjectFolder(dirRoot, mainClass.getName(), packageName, projectName);
    }

    public File createClass(String currentPackage, String className, String content) {
        File file = new File(dirJava, currentPackage.replace(".", File.separator));
        if (!file.exists()) file.mkdirs();
        File classf = new File(file, className + ".java");
        FileManager.saveFile(classf, content);

        Log.d(TAG, "createClass() returned: " + classf);
        return classf;
    }

    public void initJavaProject() {
        dirProject = new File(dirRoot, projectName);
        dirLibs = new File(dirProject, "libs");
        dirSrcMain = new File(dirProject, "src" + File.separator + "main");
        dirJava = new File(dirSrcMain, "java");

        dirBuild = new File(dirProject, "build");
        dirBuildClasses = new File(dirBuild, "classes");
        dirGenerated = new File(dirBuild, "generated");
        dirGeneratedSource = new File(dirGenerated, "source");
        dirOutput = new File(dirBuild, "output");
        dirOutputJar = new File(dirOutput, "jar");
        dirDexedLibs = new File(dirBuild, "dexedLibs");
        dirDexedClass = new File(dirBuild, "dexedClasses");

        dexedClassesFile = new File(dirDexedClass, projectName + ".dex");
        jarArchive = new File(dirOutputJar, projectName + ".jar");


        if (!dirRoot.exists()) {
            dirRoot.mkdirs();
            dirRoot.setReadable(true);
        }
        if (!dirProject.exists()) {
            dirProject.mkdirs();
            dirProject.setReadable(true);
        }
        if (!dirLibs.exists()) {
            dirLibs.mkdirs();
            dirLibs.setReadable(true);
        }
        if (!dirSrcMain.exists()) {
            dirSrcMain.mkdirs();
            dirSrcMain.setReadable(true);
        }
        if (!dirJava.exists()) {
            dirJava.mkdirs();
            dirJava.setReadable(true);
        }
        if (!dirBuildClasses.exists()) {
            dirBuildClasses.mkdirs();
            dirBuildClasses.setReadable(true);
        }
        if (!dirGenerated.exists()) {
            dirGenerated.mkdirs();
            dirGenerated.setReadable(true);
        }
        if (!dirGeneratedSource.exists()) {
            dirGeneratedSource.mkdirs();
            dirGeneratedSource.setReadable(true);
        }

    }

    public File getDirGenerated() {
        return dirGenerated;
    }

    public File getOutJarArchive() throws IOException {
        if (!jarArchive.exists()) {
            jarArchive.getParentFile().mkdirs();
            jarArchive.createNewFile();
        }
        return jarArchive;

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
        if (!dirDexedClass.exists()) {
            dirDexedClass.mkdirs();
        }
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
        if (!dirGenerated.exists()) dirGenerated.mkdirs();
        if (!dirGeneratedSource.exists()) dirGeneratedSource.mkdirs();
        if (!dirBuildClasses.exists()) dirBuildClasses.mkdirs();
        if (!dirOutput.exists()) dirOutput.mkdirs();
        if (!dirOutputJar.exists()) dirOutputJar.mkdirs();
    }

    @CallSuper
    public void mkdirs() {
        if (!dirRoot.exists()) dirRoot.mkdirs();
        if (!dirProject.exists()) dirProject.mkdirs();
        if (!dirLibs.exists()) dirLibs.mkdirs();
        if (!dirSrcMain.exists()) dirSrcMain.mkdirs();
        if (!dirJava.exists()) dirJava.mkdirs();
        if (!dirBuildClasses.exists()) dirBuildClasses.mkdirs();
        if (!dirGenerated.exists()) dirGenerated.mkdirs();
        if (!dirGeneratedSource.exists()) dirGeneratedSource.mkdirs();
    }

    @CallSuper
    public void clean() {
        try {
            com.android.utils.FileUtils.emptyFolder(dirBuildClasses);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public JavaProjectFolder createMainClass() throws IOException {
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
                    if (files == null || files.length == 0) {
                        f = null;
                    } else {
                        f = files[0];
                    }
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

    /**
     * @param context
     * @return the string contains all file *.jar in dirLibs
     */
    public String getJavaClassPath(Context context) {
        StringBuilder classpath = new StringBuilder(".");
        File[] files = getDirLibs().listFiles();
        if (files != null) {
            for (File jarLib : files) {
                if (jarLib.isFile() && jarLib.getName().endsWith(".jar")) {
                    classpath.append(File.pathSeparator).append(jarLib.getPath());
                }
            }
        }
        return classpath.append(File.pathSeparator) + getJavaBootClassPath(context);
    }

    public File getDirSrcJava() {
        if (!dirJava.exists()) dirJava.mkdirs();
        return dirJava;
    }

    private String getJavaBootClassPath(Context context) {
        return FileManager.getClasspathFile(context).getPath();
    }

    public String getSourcePath() {
        String sourcePath = getDirSrcJava().getPath();
        return sourcePath;
    }
}
