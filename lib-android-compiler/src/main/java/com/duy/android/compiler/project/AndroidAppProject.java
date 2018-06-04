package com.duy.android.compiler.project;

import com.android.annotations.Nullable;
import com.android.builder.dependency.LibraryBundle;
import com.android.builder.dependency.LibraryDependency;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.duy.android.compiler.builder.internal.dependency.LibraryDependencyImpl;
import com.duy.android.compiler.utils.IOUtils;
import com.google.common.base.MoreObjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private ArrayList<LibraryBundle> libraries;

    public AndroidAppProject(File dirRoot,
                             @Nullable String mainClassName,
                             @Nullable String packageName) {
        super(dirRoot, packageName);
        libraries = new ArrayList<>();
        try {
            readLibraries();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
            ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(getManifestFile()));
            ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
            this.launcherActivity = launcherActivity;
            return launcherActivity;
        } catch (Exception e) {
            return null;
        }
    }

    public File getManifestFile() {
        return xmlManifest;
    }

    public File getApkSigned() {
        apkSigned.getParentFile().mkdirs();
        return apkSigned;
    }

    public File getProcessResourcePackageOutputFile() {
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
        getAssetsDir();

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
        return sourcePath.toString();
    }

    @Override
    public ArrayList<File> getJavaLibraries() {
        ArrayList<File> libraries = (super.getJavaLibraries());
        for (LibraryDependency dependency : this.libraries) {
            if (dependency.getJarFile().exists()) {
                libraries.add(dependency.getJarFile());
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

    public File getAssetsDir() {
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


    public ArrayList<LibraryBundle> getLibraries() {
        return libraries;
    }

    public void addLibrary(LibraryBundle androidLibrary) {
        libraries.add(androidLibrary);
        try {
            writeToLibraryFile();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLibraries() throws IOException, JSONException {
        File file = new File(getAppDir(), "libraries.json");
        JSONObject jsonObject = new JSONObject(IOUtils.toStringAndClose(file));
        JSONArray array = jsonObject.getJSONArray("libraries");
        for (int i = 0; i < array.length(); i++) {
            JSONObject lib = array.getJSONObject(i);
            String bundle = lib.getString("bundle");
            String bundleFolder = lib.getString("bundleFolder");
            LibraryDependencyImpl androidLibrary
                    = new LibraryDependencyImpl(new File(bundle), new File(bundleFolder), new ArrayList<LibraryDependency>(),
                    null, null, getRootDir().getAbsolutePath(), null, null, false);
            addLibrary(androidLibrary);
        }
    }

    private void writeToLibraryFile() throws JSONException, IOException {
        File file = new File(getAppDir(), "libraries.json");
        JSONObject jsonObject = new JSONObject();
        JSONArray array = new JSONArray();
        jsonObject.put("libraries", array);
        for (LibraryBundle library : libraries) {
            JSONObject item = new JSONObject();
            item.put("bundle", library.getBundle().getAbsolutePath());
            item.put("bundleFolder", library.getBundleFolder().getAbsolutePath());
            array.put(item);
        }
        String str = jsonObject.toString(1);
        org.apache.commons.io.IOUtils.write(str, new FileOutputStream(file), "UTF-8");
    }

    public String getPackageForR() {
        return packageName;
    }

    public File getRClassSourceOutputDir() {
        return getDirGeneratedSource();
    }
}
