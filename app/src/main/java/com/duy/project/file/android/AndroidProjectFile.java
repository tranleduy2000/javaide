package com.duy.project.file.android;

import com.android.annotations.Nullable;
import com.duy.project.file.java.JavaProjectFile;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 05-Aug-17.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AndroidProjectFile extends JavaProjectFile {
    /* ASSETS */
    private final File jksEmbedded;
    /* Output */
    private final File apkUnsigned;
    private final File apkUnaligned;
    public File xmlManifest;
    public File ap_Resources;
    /* PROJECT */
    private File dirRes;
    private File dirAssets;
    private File classR;
    private File dirOutApk;

    public AndroidProjectFile(File dirRoot,
                              @Nullable String mainClassName,
                              @Nullable String packageName,
                              String projectName,
                              String classpath) {
        super(dirRoot, mainClassName, packageName, projectName, classpath);

        dirRes = new File(dirSrcMain, "res");
        dirAssets = new File(dirSrcMain, "assets");
        xmlManifest = new File(dirSrcMain, "AndroidManifest.xml");

        dirOutApk = new File(dirOutput, "apk");
        apkUnsigned = new File(dirOutput, "app-unsigned-debug.apk");
        apkUnaligned = new File(dirOutput, "app-unaligned-debug.apk");

        if (packageName != null) {
            classR = new File(dirJava,
                    packageName.replace(".", File.separator) + File.separator + "R.java");
        }

        ap_Resources = new File(dirBuild, "resources.ap_");
        dexedClassesFile = new File(dirBuild, "classes.dex");
        jksEmbedded = new File(dirAssets, "Embedded.jks");
    }

    public void clean() {
        super.clean();
    }


    public void mkdirs() {
        super.mkdirs();
        if (!dirRes.exists()) dirRes.mkdirs();
        if (!dirAssets.exists()) dirAssets.mkdirs();
        if (!dirDexedLibs.exists()) dirDexedLibs.mkdirs();
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    public File getApkUnsigned() throws IOException {
        if (apkUnsigned.exists()) {
            apkUnsigned.getParentFile().mkdirs();
            apkUnsigned.createNewFile();
        }
        return apkUnsigned;
    }

    public File getDirRes() {
        if (!dirRes.exists()) dirRes.mkdirs();
        return dirRes;
    }

    public File getDirAssets() {
        if (!dirAssets.exists()) dirAssets.mkdirs();
        return dirAssets;
    }

    public File getClassR() throws IOException {
        if (!classR.exists()) {
            classR.getParentFile().mkdirs();
            classR.createNewFile();
        }
        return classR;
    }
}
