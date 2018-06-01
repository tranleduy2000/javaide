package com.duy.android.compiler.file;

import android.content.Context;
import android.content.res.AssetManager;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidProjectManager {
    private Context context;

    public AndroidProjectManager(Context context) {
        this.context = context;
    }

    /**
     * Create new android project
     *
     * @param context          - android context to get assets template
     * @param dir              - The directory will contain the project
     * @param projectName      - Name of project, it will be used for create root directory
     * @param useCompatLibrary - <code>true</code> if need copy android compat library
     */
    public void createNewProject(Context context, File dir, String projectName,
                                 String packageName, String activityName, String mainLayoutName,
                                 String appName, boolean useCompatLibrary) throws Exception {

        String activityClass = String.format("%s.%s", packageName, activityName);
        AndroidApplicationProject project = new AndroidApplicationProject(dir, activityClass, packageName, projectName);
        //create directory
        project.mkdirs();

        AssetManager assets = context.getAssets();

        createRes(project, useCompatLibrary, appName);
        createManifest(project, activityClass, packageName, assets);
        createMainActivity(project, activityClass, packageName, activityName, appName, useCompatLibrary, assets);
        createMainLayoutXml(project, mainLayoutName);
        copyLibrary(project, useCompatLibrary, assets);
    }


    private void createRes(AndroidApplicationProject project, boolean useAppCompat, String appName) throws IOException {
        File resDir = project.getResDir();

        //drawable
        copyAssets("templates/ic_launcher_hdpi.png", new File(resDir, "drawable-xhdpi/ic_launcher.png"));
        copyAssets("templates/ic_launcher_ldpi.png", new File(resDir, "drawable-ldpi/ic_launcher.png"));
        copyAssets("templates/ic_launcher_mdpi.png", new File(resDir, "drawable-mdpi/ic_launcher.png"));
        copyAssets("templates/ic_launcher_xhdpi.png", new File(resDir, "drawable-xhdpi/ic_launcher.png"));

        //styles
        File style = new File(resDir, "values/styles.xml");
        String content = IOUtils.toString(context.getAssets().open("templates/styles.xml"));
        content = content.replace("APP_STYLE", useAppCompat ? "Theme.AppCompat.Light" : "@android:style/Theme.Holo.Light");
        saveFile(style, content);

        File string = new File(resDir, "values/strings.xml");
        content = IOUtils.toString(context.getAssets().open("templates/strings.xml"));
        content = content.replace("APP_NAME", appName);
        content = content.replace("MAIN_ACTIVITY_NAME", appName);
        saveFile(string, content);
    }

    private void createManifest(AndroidApplicationProject project, String activityClass, String packageName,
                                AssetManager assets) throws IOException {
        File manifest = project.getXmlManifest();
        String content = IOUtils.toString(assets.open("templates/src/main/AndroidManifest.xml"));

        content = content.replace("PACKAGE", packageName);
        content = content.replace("MAIN_ACTIVITY", activityClass);
        saveFile(manifest, content);
    }


    private void createMainActivity(AndroidApplicationProject project, String activityClass,
                                    String packageName, String activityName, String appName,
                                    boolean useAppCompat, AssetManager assets) throws IOException {
        File activityFile = new File(project.getJavaSrcDir(), activityClass.replace(".", File.separator) + ".java");

        String name = useAppCompat ? "templates/MainActivityAppCompat.java" : "templates/MainActivity.java";
        String content = IOUtils.toString(assets.open(name));
        content = content.replace("PACKAGE", packageName);
        content = content.replace("ACTIVITY_NAME", activityName);
        saveFile(activityFile, content);
    }

    private void createMainLayoutXml(AndroidApplicationProject project, String layoutName) throws IOException {
        if (!layoutName.contains(".")) {
            layoutName += ".xml";
        }

        File layoutMain = new File(project.getResDir(), "layout/" + layoutName);
        copyAssets("templates/activity_main.xml", layoutMain);
    }

    private void copyAssets(String filePath, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();
        File in = new File(filePath);
        FileOutputStream output = new FileOutputStream(new File(outFile, in.getName()));
        FileInputStream input = new FileInputStream(in);
        org.apache.commons.io.IOUtils.copy(input, output);
        input.close();
        output.close();
    }

    private void saveFile(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        org.apache.commons.io.IOUtils.write(content, output);
        output.close();
    }

    private void copyLibrary(AndroidApplicationProject project, boolean useCompatLibrary, AssetManager assets) {
        if (useCompatLibrary) {

        }
    }

}
