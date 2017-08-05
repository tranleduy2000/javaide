package com.duy.project.file.android;

import com.duy.project.file.java.JavaProjectFile;
import com.google.common.base.MoreObjects;

import java.io.File;

/**
 * Created by Duy on 05-Aug-17.
 */
public class AndroidProjectFile extends JavaProjectFile {
    /* PROJECT */
    public final File dirRes;
    public final File dirAssets;
    public final File xmlManifest;
    /* ASSETS */
    public final File jksEmbedded;
    /* Output */
    public final File apkUnsigned;
    public final File apkUnaligned;
    public File ap_Resources;
    public File classR;
    private File dirOutApk;


    public AndroidProjectFile(File dirRoot, String mainClassName,
                              String packageName, String projectName,
                              String classpath) {
        super(dirRoot, mainClassName, packageName, projectName, classpath);

        dirRes = new File(dirSrcMain, "res");
        dirAssets = new File(dirSrcMain, "assets");
        xmlManifest = new File(dirSrcMain, "AndroidManifest.xml");

        dirOutApk = new File(dirOutput, "apk");

        apkUnsigned = new File(dirOutput, "app-unsigned-debug.apk");
        apkUnaligned = new File(dirOutput, "app-unaligned-debug.apk");
        classR = new File(dirBuildClasses, "R.java");

        dirDexedLibs = new File(dirBuild, "dexedLibs");
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

}
