package com.duy.android.compiler.project;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.duy.android.compiler.env.Environment;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Duy on 16-Jul-17.
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class JavaProject {
    private static final String TAG = "ProjectFile";
    protected ArrayList<File> javaSrcDirs;
    protected File dirSrcMain;
    protected File dirGeneratedSource;
    /* Project */
    protected File dirRoot;
    protected File dirApp;
    @Nullable
    protected String packageName;
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
    File dirBuild;
    File dirBuildOutput;
    private File dirLibs;
    private File dirBuildClasses;
    private File dirBuildOutputJar;
    private File dirBuildDexedLibs;
    private File dirBuildDexedClass;
    private File dirBuildIntermediates;
    private File dexFile;
    private File outJarArchive;
    private File dirGenerated;
    public JavaProject(File root, @Nullable String packageName) {
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
        dirApp = new File(dirRoot, "app");
        dirLibs = new File(dirApp, "libs");
        dirSrcMain = new File(dirApp, "src/main");

        javaSrcDirs = new ArrayList<>();
        javaSrcDirs.add(new File(dirSrcMain, "java"));

        dirBuild = new File(dirApp, "build");
        dirBuildClasses = new File(dirBuild, "classes");
        dirGenerated = new File(dirBuild, "generated");
        dirGeneratedSource = new File(dirGenerated, "source");
        dirBuildOutput = new File(dirBuild, "output");
        dirBuildOutputJar = new File(dirBuildOutput, "jar");
        dirBuildDexedLibs = new File(dirBuild, "dexedLibs");
        dirBuildIntermediates = new File(dirBuild, "intermediates");
        dirBuildDexedClass = new File(dirBuild, "dexedClasses");

        dexFile = new File(dirBuildDexedClass, "classes.dex");
        outJarArchive = new File(dirBuildOutputJar, getProjectName() + ".jar");


        dirRoot.mkdirs();
        dirApp.mkdirs();
        dirLibs.mkdirs();
        dirSrcMain.mkdirs();
        mkdirs(javaSrcDirs);
        dirBuildClasses.mkdirs();
        dirGenerated.mkdirs();
        dirGeneratedSource.mkdirs();
    }

    protected void mkdirs(ArrayList<File> srcDirs) {
        for (File srcDir : srcDirs) {
            if (!srcDir.exists()) {
                srcDir.mkdirs();
            }
        }
    }

    public File getOutJarArchive() throws IOException {
        if (!outJarArchive.exists()) {
            outJarArchive.getParentFile().mkdirs();
            outJarArchive.createNewFile();
        }
        return outJarArchive;

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

    @CallSuper
    public void mkdirs() {
        if (!dirRoot.exists()) dirRoot.mkdirs();
        if (!dirApp.exists()) dirApp.mkdirs();
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

    /**
     * @param packageName - package name, can empty but not null
     * @param simpleName  - simple name
     */
    public void createMainClass(String packageName, String simpleName) throws IOException {
        if (packageName == null || simpleName == null) {
            return;
        }
        //create package file
        File pkgPath = new File(javaSrcDirs.get(0), packageName.replace(".", File.separator));
        pkgPath.mkdirs();

        File mainFile = new File(pkgPath, simpleName + ".java");
        if (!mainFile.exists()) {
            String content = Template.createClass(packageName, simpleName);
            FileOutputStream output = new FileOutputStream(mainFile);
            IOUtils.write(content, output);
            output.close();
        }
    }

    public File getAppDir() {
        return dirApp;
    }

    /**
     * @return the string contains all file *.jar in dirLibs
     */
    @NonNull
    @CallSuper
    public ArrayList<File> getJavaLibraries() {
        File[] files = getDirLibs().listFiles(new FileFilter() {
            @Override
            public boolean accept(File jarLib) {
                return (jarLib.isFile() && jarLib.getName().endsWith(".jar"));
            }
        });
        return new ArrayList<>(Arrays.asList(files));
    }

    @NonNull
    public String getClasspath() {
        ArrayList<File> javaLibraries = getJavaLibraries();
        StringBuilder classpath = new StringBuilder(".");
        for (File javaLibrary : javaLibraries) {
            if (classpath.length() != 0) {
                classpath.append(File.pathSeparator);
            }
            classpath.append(javaLibrary.getAbsolutePath());
        }
        return classpath.toString();
    }

    @NonNull
    public String getBootClassPath(Context context) {
        return Environment.getClasspathFile(context).getAbsolutePath();
    }

    @CallSuper
    public String getSourcePath() {
        StringBuilder srcPath = new StringBuilder();
        for (File javaSrcDir : javaSrcDirs) {
            if (srcPath.length() != 0) {
                srcPath.append(File.pathSeparator);
            }
            srcPath.append(javaSrcDir.getAbsolutePath());
        }
        srcPath.append(File.pathSeparator).append(dirGeneratedSource.getAbsolutePath());
        return srcPath.toString();
    }

    public File getRootDir() {
        return dirRoot;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getProjectName() {
        return dirRoot.getName();
    }

    public ArrayList<File> getJavaSrcDirs() {
        return javaSrcDirs;
    }

    public File getJavaSrcDir() {
        return javaSrcDirs.get(0);
    }

    public File getDirBuild() {
        return dirBuild;
    }

    public File getDirGeneratedSource() {
        return dirGeneratedSource;
    }

    public File getDirBuildIntermediates() {
        return dirBuildIntermediates;
    }


    @Override
    public String toString() {
        return "JavaProject{" +
                "javaSrcDirs=" + javaSrcDirs +
                ", dirSrcMain=" + dirSrcMain +
                ", dirGeneratedSource=" + dirGeneratedSource +
                ", dirRoot=" + dirRoot +
                ", dirApp=" + dirApp +
                ", packageName='" + packageName + '\'' +
                ", dirBuild=" + dirBuild +
                ", dirBuildOutput=" + dirBuildOutput +
                ", dirLibs=" + dirLibs +
                ", dirBuildClasses=" + dirBuildClasses +
                ", dirBuildOutputJar=" + dirBuildOutputJar +
                ", dirBuildDexedLibs=" + dirBuildDexedLibs +
                ", dirBuildDexedClass=" + dirBuildDexedClass +
                ", dirBuildIntermediates=" + dirBuildIntermediates +
                ", dexFile=" + dexFile +
                ", outJarArchive=" + outJarArchive +
                ", dirGenerated=" + dirGenerated +
                '}';
    }
}
