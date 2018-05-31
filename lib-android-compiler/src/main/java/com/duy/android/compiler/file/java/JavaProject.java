package com.duy.android.compiler.file.java;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.android.annotations.Nullable;
import com.duy.android.compiler.env.Environment;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Duy on 16-Jul-17.
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class JavaProject implements Serializable, Cloneable {
    private static final String TAG = "ProjectFile";

    protected ArrayList<File> javaSrcDirs;
    protected File dirLibs;
    protected File dirBuildClasses;
    protected File dirSrcMain;
    protected File dirGenerated;
    protected File dirGeneratedSource;

    /* Project */
    protected File dirRoot;
    protected File dirProject;

    /**
     * Build folder
     * <p>
     * rootProject
     * ----module
     * --------build
     * ------------classes <- .class output
     * ------------dexedLibs <- contains dexed java library, it can be cached for build faster
     * ------------generated <- generate source, such as aapt generate class R
     * ------------output <- apk, jar file output
     * --------src
     * ------------main
     * ----------------java
     * ----------------res
     */
    protected File dirBuild;
    protected File dirBuildOutput;
    protected File dirBuildOutputJar;
    protected File dirBuildDexedLibs;
    protected File dirBuildDexedClass;
    protected File dexFile;


    @Nullable
    protected String packageName;

    /*Main class*/
    protected ClassFile mainClass;
    @Nullable
    protected String projectName;
    protected File jarArchive;

    public JavaProject(File root, @Nullable String mainClassName, @Nullable String packageName, String projectName) {
        this.mainClass = new ClassFile(mainClassName);
        this.projectName = projectName;
        this.packageName = packageName;
        this.dirRoot = root;
        init();
    }

    public File createClass(String currentPackage, String className, String content) {
        File file = new File(javaSrcDirs.get(0), currentPackage.replace(".", File.separator));
        if (!file.exists()) file.mkdirs();
        File classf = new File(file, className + ".java");
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(classf);
            IOUtils.write(content, output);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "createClass() returned: " + classf);
        return classf;
    }

    @CallSuper
    public void init() {
        dirProject = new File(dirRoot, projectName);
        dirLibs = new File(dirProject, "libs");
        dirSrcMain = new File(dirProject, "src" + File.separator + "main");

        javaSrcDirs = new ArrayList<>();
        javaSrcDirs.add(new File(dirSrcMain, "java"));

        dirBuild = new File(dirProject, "build");
        dirBuildClasses = new File(dirBuild, "classes");
        dirGenerated = new File(dirBuild, "generated");
        dirGeneratedSource = new File(dirGenerated, "source");
        dirBuildOutput = new File(dirBuild, "output");
        dirBuildOutputJar = new File(dirBuildOutput, "jar");
        dirBuildDexedLibs = new File(dirBuild, "dexedLibs");
        dirBuildDexedClass = new File(dirBuild, "dexedClasses");

        dexFile = new File(dirBuildDexedClass, "classes.dex");
        jarArchive = new File(dirBuildOutputJar, projectName + ".jar");


        if (!dirRoot.exists()) {
            dirRoot.mkdirs();
        }
        if (!dirProject.exists()) {
            dirProject.mkdirs();
        }
        if (!dirLibs.exists()) {
            dirLibs.mkdirs();
        }
        if (!dirSrcMain.exists()) {
            dirSrcMain.mkdirs();
        }
        mkdirs(javaSrcDirs);
        if (!dirBuildClasses.exists()) {
            dirBuildClasses.mkdirs();
        }
        if (!dirGenerated.exists()) {
            dirGenerated.mkdirs();
        }
        if (!dirGeneratedSource.exists()) {
            dirGeneratedSource.mkdirs();
        }

    }

    protected void mkdirs(ArrayList<File> srcDirs) {
        for (File srcDir : srcDirs) {
            if (!srcDir.exists()) {
                srcDir.mkdirs();
            }
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


    public File getDexFile() {
        dexFile.getParentFile().mkdirs();
        return dexFile;
    }

    public File getDirLibs() {
        if (!dirLibs.exists()) dirLibs.mkdirs();
        return dirLibs;
    }

    public File getDirBuildDexedLibs() {
        if (!dirBuildDexedLibs.exists()) dirBuildDexedLibs.mkdirs();
        return dirBuildDexedLibs;
    }

    public File getDirBuildOutputJar() {
        if (!dirBuildOutputJar.exists()) dirBuildOutputJar.mkdirs();
        return dirBuildOutputJar;
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
        if (!dirBuildOutput.exists()) dirBuildOutput.mkdirs();
        if (!dirBuildOutputJar.exists()) dirBuildOutputJar.mkdirs();
    }

    @CallSuper
    public void mkdirs() {
        if (!dirRoot.exists()) dirRoot.mkdirs();
        if (!dirProject.exists()) dirProject.mkdirs();
        if (!dirLibs.exists()) dirLibs.mkdirs();
        if (!dirSrcMain.exists()) dirSrcMain.mkdirs();
        mkdirs(javaSrcDirs);
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

    public JavaProject createMainClass() throws IOException {
        this.mkdirs();
        if (packageName != null) {
            //create package file
            File packageF = new File(javaSrcDirs.get(0), packageName.replace(".", File.separator));
            if (!packageF.exists()) {
                packageF.getParentFile().mkdirs();
                packageF.mkdirs();
            }

            File mainFile = new File(packageF, mainClass.getSimpleName() + ".java");
            if (!mainFile.exists()) {

                String content = Template.createClass(packageName, mainClass.getSimpleName());

                FileOutputStream output = new FileOutputStream(mainFile);
                IOUtils.write(content, output);
                output.close();
            }
        } else { //find package name
            File[] files = javaSrcDirs.get(0).listFiles();
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
        File[] files = getDirLibs().listFiles(new FileFilter() {
            @Override
            public boolean accept(File jarLib) {
                return (jarLib.isFile() && jarLib.getName().endsWith(".jar"));
            }
        });
        for (File jarLib : files) {
            if (jarLib.isFile() && jarLib.getName().endsWith(".jar")) {
                classpath.append(File.pathSeparator).append(jarLib.getAbsolutePath());
            }
        }
        return classpath.append(File.pathSeparator) + getJavaBootClassPath(context);
    }

    private String getJavaBootClassPath(Context context) {
        return Environment.getClasspathFile(context).getAbsolutePath();
    }

    public String getSourcePath() {
        StringBuilder srcPath = new StringBuilder();
        for (File javaSrcDir : javaSrcDirs) {
            if (srcPath.length() != 0) {
                srcPath.append(File.pathSeparator);
            }
            srcPath.append(javaSrcDir.getAbsolutePath());
        }
        return srcPath.toString();
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

    public ArrayList<File> getJavaSrcDirs() {
        return javaSrcDirs;
    }
}
