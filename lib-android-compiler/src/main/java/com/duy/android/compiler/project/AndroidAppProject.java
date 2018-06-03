package com.duy.android.compiler.project;

import com.android.annotations.Nullable;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

/**
 * Created by Duy on 05-Aug-17.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class AndroidAppProject extends JavaProject {

    private File xmlManifest;
    /* Output */
    private File outResourceFile;
    private File apkUnsigned;
    private File apkSigned;

    /* PROJECT */
    private ArrayList<File> resDirs;
    private ArrayList<File> assetsDirs;

    private ManifestData.Activity launcherActivity;

    private ArrayList<AndroidLibraryProject> dependencies;

    public AndroidAppProject(File dirRoot,
                             @Nullable String mainClassName,
                             @Nullable String packageName) {
        super(dirRoot, packageName);
        dependencies = new ArrayList<>();
    }

    @Override
    public void init() {
        super.init();

        resDirs = new ArrayList<>();
        resDirs.add(new File(dirSrcMain, "res"));

        assetsDirs = new ArrayList<>();
        assetsDirs.add(new File(dirSrcMain, "assets"));
        xmlManifest = new File(dirSrcMain, "AndroidManifest.xml");

        apkUnsigned = new File(dirBuildOutput, "app-unsigned-debug.apk");
        apkSigned = new File(dirBuildOutput, "app-debug.apk");
        outResourceFile = new File(dirBuild, "resources.ap_");
    }

    @Nullable
    public ManifestData.Activity getLauncherActivity() {
        try {
            ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(getXmlManifest()));
            ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
            this.launcherActivity = launcherActivity;
            return launcherActivity;
        } catch (Exception e) {
            return null;
        }
    }

    public File getXmlManifest() {
        return xmlManifest;
    }

    public File getApkSigned() {
        apkSigned.getParentFile().mkdirs();
        return apkSigned;
    }

    public File getOutResourceFile() {
        outResourceFile.getParentFile().mkdirs();
        return outResourceFile;
    }

    @Override
    public void clean() {
        super.clean();
        apkUnsigned.delete();
        apkSigned.delete();
    }

    @Override
    public void mkdirs() {
        super.mkdirs();
        getResDirs();
        getAssetsDirs();

        File resDir = resDirs.get(0);
        new File(resDir, "menu").mkdirs();
        new File(resDir, "layout").mkdirs();
        new File(resDir, "drawable").mkdirs();
        new File(resDir, "drawable-hdpi").mkdirs();
        new File(resDir, "drawable-xhdpi").mkdirs();
        new File(resDir, "drawable-xxhdpi").mkdirs();
        new File(resDir, "drawable-xxxhdpi").mkdirs();
        new File(resDir, "values").mkdirs();
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public String getSourcePath() {
        StringBuilder sourcePath = new StringBuilder(super.getSourcePath());
//        for (AndroidLibraryProject library : getDependencies()) {
//            sourcePath.append(File.pathSeparator).append(library.getDirGeneratedSource());
//        }
        return sourcePath.toString();
    }

    @Override
    public ArrayList<File> getJavaLibraries() {
        ArrayList<File> libraries = (super.getJavaLibraries());
        for (AndroidLibraryProject dependency : dependencies) {
            if (dependency.getClassesJar() != null) {
                libraries.add(dependency.getClassesJar());
            }
        }
        return libraries;
    }

    public File getApkUnsigned() {
        apkUnsigned.getParentFile().mkdirs();
        return apkUnsigned;
    }

    public File getResDirs() {
        mkdirs(resDirs);
        return resDirs.get(0);
    }

    public File getResDir() {
        mkdirs(resDirs);
        return resDirs.get(0);
    }

    public File getAssetsDirs() {
        mkdirs(assetsDirs);
        return assetsDirs.get(0);
    }

    public File getDirLayout() {
        File file = new File(getResDirs(), "layout");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }


    public ArrayList<AndroidLibraryProject> getDependencies() {
        return dependencies;
    }

    public void addDependence(AndroidLibraryProject androidLibrary) {
        dependencies.add(androidLibrary);
    }
}
