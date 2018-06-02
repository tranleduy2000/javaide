package com.duy.android.compiler.project;

import android.content.Context;
import android.content.res.AssetManager;

import com.android.io.StreamException;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.library.AndroidLibraryExtractor;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

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
    public AndroidApplicationProject createNewProject(Context context, File dir, String projectName,
                                                      String packageName, String activityName, String mainLayoutName,
                                                      String appName, boolean useCompatLibrary) throws Exception {

        String activityClass = String.format("%s.%s", packageName, activityName);
        AndroidApplicationProject project = new AndroidApplicationProject(dir, activityClass, packageName);
        //create directory
        project.mkdirs();

        AssetManager assets = context.getAssets();

        createRes(project, useCompatLibrary, appName);
        createManifest(project, activityClass, packageName, assets);
        createMainActivity(project, activityClass, packageName, activityName, appName, useCompatLibrary, assets);
        createMainLayoutXml(project, mainLayoutName);
        copyLibrary(project, useCompatLibrary, assets);

        return project;
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
        String content = IOUtils.toString(assets.open("templates/AndroidManifest.xml"));

        content = content.replace("PACKAGE", packageName);
        content = content.replace("MAIN_ACTIVITY", activityClass);
        saveFile(manifest, content);
    }


    private void createMainActivity(AndroidApplicationProject project, String activityClass,
                                    String packageName, String activityName, String appName,
                                    boolean useAppCompat, AssetManager assets) throws IOException {
        File activityFile = new File(project.getJavaSrcDir(),
                activityClass.replace(".", File.separator) + ".java");

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

    private void copyAssets(String assetsPath, File outFile) throws IOException {
        outFile.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(outFile);
        InputStream input = context.getAssets().open(assetsPath);
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

    private void copyLibrary(AndroidApplicationProject project, boolean useCompatLibrary, AssetManager assets) throws IOException, StreamException, SAXException, ParserConfigurationException {
        if (useCompatLibrary) {
            //v7
            addLib(project, "libs/27.1.1/appcompat-v7-27.1.1.aar", "appcompat-v7-27.1.1");
            addLib(project, "libs/27.1.1/animated-vector-drawable-27.1.1.aar", "animated-vector-drawable-27.1.1");
            addLib(project, "libs/27.1.1/livedata-core-1.1.0.aar", "livedata-core-1.1.0");
            addLib(project, "libs/27.1.1/runtime-1.1.0.aar", "runtime-1.1.0");
            addLib(project, "libs/27.1.1/common-1.1.0.jar", "common-1.1.0.jar");
            addLib(project, "libs/27.1.1/viewmodel-1.1.1.aar", "viewmodel-1.1.1");
            addLib(project, "libs/27.1.1/support-compat-27.1.1.aar", "support-compat-27.1.1");
            addLib(project, "libs/27.1.1/support-core-ui-27.1.1.aar", "support-core-ui-27.1.1");
            addLib(project, "libs/27.1.1/support-core-utils-27.1.1.aar", "support-core-utils-27.1.1");
            addLib(project, "libs/27.1.1/support-fragment-27.1.1.aar", "support-fragment-27.1.1");
            addLib(project, "libs/27.1.1/support-vector-drawable-27.1.1.aar", "support-vector-drawable-27.1.1");
            addLib(project, "libs/27.1.1/support-annotations-27.1.1.jar", "support-annotations-27.1.1.jar");
            addLib(project, "libs/27.1.1/support-media-compat-27.1.1.aar", "support-media-compat-27.1.1");
            addLib(project, "libs/27.1.1/support-v4-27.1.1.aar", "support-v4-27.1.1.aar");


            //test v4 lib
//            addLib(project, "libs/27.0.0/appcompat-v7-27.0.0.aar", "appcompat-v7-27.0.0");
//            addLib(project, "libs/27.0.0/support-vector-drawable-27.0.0.aar", "support-vector-drawable-27.0.0");
//            addLib(project, "libs/27.0.0/animated-vector-drawable-27.0.0.aar", "animated-vector-drawable-27.0.0");
//            addLib(project, "libs/27.0.0/common-1.1.1.jar", "common-1.1.1.jar");
//            addLib(project, "libs/27.0.0/runtime-1.1.1.aar", "runtime-1.1.1");
//            addLib(project, "libs/27.0.0/support-annotations-27.0.0.jar", "support-annotations-27.0.0.jar");
//            addLib(project, "libs/27.0.0/support-compat-27.0.0.aar", "support-compat-27.0.0");
//            addLib(project, "libs/27.0.0/support-core-ui-27.0.0.aar", "support-core-ui-27.0.0");
//            addLib(project, "libs/27.0.0/support-core-utils-27.0.0.aar", "support-core-utils-27.0.0");
//            addLib(project, "libs/27.0.0/support-fragment-27.0.0.aar", "support-fragment-27.0.0");
//            addLib(project, "libs/27.0.0/support-media-compat-27.0.0.aar", "support-media-compat-27.0.0");
//            addLib(project, "libs/27.0.0/support-v4-27.0.0.aar", "support-v4-27.0.0");

            addLib(project, "libs/27.1.1/support-fragment-27.1.1.aar", "support-fragment-27.1.1");
        }
    }

    private void addLib(AndroidApplicationProject project, String assetsPath, String libName)
            throws SAXException, StreamException, ParserConfigurationException, IOException {
        if (assetsPath.endsWith(".jar")) {
            File javaLib = new File(project.getDirLibs(), libName);
            FileOutputStream output = new FileOutputStream(javaLib);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();
        } else if (assetsPath.endsWith(".aar")) {
            File aarFile = new File(project.getDirBuildDexedLibs(), assetsPath);
//            if (!aarFile.exists()) {
            aarFile.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(aarFile);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();
//            }

            AndroidLibraryExtractor extractor = new AndroidLibraryExtractor(context);
            extractor.extract(aarFile, libName);

            File dirLib = new File(Environment.getSdCardLibraryCachedDir(context), libName);
            AndroidLibraryProject androidLib = new AndroidLibraryProject(dirLib, libName);
            project.addDependence(androidLib);
        }
    }

}
