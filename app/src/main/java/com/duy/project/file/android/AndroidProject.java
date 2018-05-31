package com.duy.project.file.android;

import android.content.Context;

import com.android.annotations.Nullable;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.duy.ide.file.FileManager;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFolder;
import com.google.common.base.MoreObjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Duy on 05-Aug-17.
 */
public class AndroidProject extends JavaProjectFolder {
    private KeyStore keystore;

    private File xmlManifest;
    /* Output */
    private File outResourceFile;
    private File apkUnsigned;
    private File apkUnaligned;

    /* PROJECT */
    private ArrayList<File> resDirs;
    private ArrayList<File> assetsDirs;
    private File classR;

    private ManifestData.Activity launcherActivity;

    public AndroidProject(File dirRoot,
                          @Nullable String mainClassName,
                          @Nullable String packageName,
                          String projectName) {
        super(dirRoot, mainClassName, packageName, projectName);
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
        apkUnaligned = new File(dirBuildOutput, "app-unaligned-debug.apk");

        createClassR();

        outResourceFile = new File(dirBuild, "resources.ap_");
        dexedClassesFile = new File(dirBuild, "classes.dex");
        keystore = new KeyStore(new File(dirProject, "keystore.jks"),
                "android".toCharArray(),
                "android",
                "android".toCharArray());
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


    private void createClassR() {
        if (packageName != null) {
            String path = packageName.replace(".", File.separator) + File.separator + "R.java";
            classR = new File(dirGeneratedSource, path);
        }
    }

    public void setKeystore(KeyStore keystore) {
        this.keystore = keystore;
    }

    public KeyStore getKeyStore() {
        return keystore;
    }

    public File getXmlManifest() {
        return xmlManifest;
    }

    public File getApkUnaligned() throws IOException {
        return apkUnaligned;
    }

    public File getOutResourceFile() {
        outResourceFile.getParentFile().mkdirs();
        return outResourceFile;
    }

    @Override
    public void clean() {
        super.clean();
        apkUnsigned.delete();
        apkUnaligned.delete();
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
        String sourcePath = super.getSourcePath();
        File generatedSource = new File(dirGenerated, "source");
        sourcePath += File.pathSeparator + generatedSource.getPath();
        return sourcePath;
    }

    public File getApkUnsigned() throws IOException {
        if (!apkUnsigned.exists()) {
            apkUnsigned.getParentFile().mkdirs();
        }
        return apkUnsigned;
    }

    public File getResDirs() {
        mkdirs(resDirs);
        return resDirs.get(0);
    }

    public File getAssetsDirs() {
        mkdirs(assetsDirs);
        return assetsDirs.get(0);
    }

    public File getClassR() {
        classR.getParentFile().mkdirs();
        return classR;
    }

    public File getDirLayout() {
        File file = new File(getResDirs(), "layout");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    /**
     * use for javac
     *
     * @return main class
     */
    @Override
    public ClassFile getMainClass() {
        if (launcherActivity == null) getLauncherActivity();
        if (launcherActivity != null) {
            return new ClassFile(this.launcherActivity.getName());
        } else return null;
    }

    public void checkKeyStoreExits(Context context) {
        if (!keystore.getFile().exists()) {
            File key = new File(dirProject, "keystore.jks");
            if (!key.getParentFile().exists()) {
                key.getParentFile().mkdirs();
            }
            try {
                key.createNewFile();
                FileOutputStream out = new FileOutputStream(key);
                FileManager.copyStream(context.getAssets().open(Constants.KEY_STORE_ASSET_PATH), out);
                out.close();
                setKeystore(new KeyStore(key, Constants.KEY_STORE_PASSWORD,
                        Constants.KEY_STORE_ALIAS, Constants.KEY_STORE_ALIAS_PASS));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
