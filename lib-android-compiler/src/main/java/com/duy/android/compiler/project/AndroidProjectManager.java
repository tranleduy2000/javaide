package com.duy.android.compiler.project;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.io.StreamException;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.library.AndroidLibraryExtractor;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

public class AndroidProjectManager implements IAndroidProjectManager {
    private static final String TAG = "AndroidProjectManager";
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
    @Override
    public AndroidAppProject createNewProject(Context context, File dir, String projectName,
                                              String packageName, String activityName, String mainLayoutName,
                                              String appName, boolean useCompatLibrary) throws Exception {

        String activityClass = String.format("%s.%s", packageName, activityName);
        File projectDir = new File(dir, projectName);
        AndroidAppProject project = new AndroidAppProject(projectDir, activityClass, packageName);
        //create directory
        project.mkdirs();

        AssetManager assets = context.getAssets();
        createGradleFile(project);
        createRes(project, useCompatLibrary, appName);
        createManifest(project, activityClass, packageName, assets);
        createMainActivity(project, activityClass, packageName, activityName, appName, useCompatLibrary, assets);
        createMainLayoutXml(project, mainLayoutName);
        copyLibrary(project, useCompatLibrary);

        return project;
    }

    /**
     * Load previous project
     *
     * @param rootDir     - root dir
     * @param tryToImport -  if not found gradle file, try to create it instead of throw exception
     */
    public AndroidAppProject loadProject(File rootDir, boolean tryToImport) throws IOException {
        AndroidAppProject project = new AndroidAppProject(rootDir, null, null);
        File file = new File(rootDir, GradleFileGenerator.DEFAULT_SETTING_FILE);
        if (!file.exists()) {
            if (!tryToImport) {
                throw new IOException("Can not find setting.gradle, try to create new project");
            } else {
                GradleFileGenerator gradleFileGenerator = new GradleFileGenerator(context, project);
                gradleFileGenerator.generate();
            }
        }

        // TODO: 03-Jun-18 parse groovy file
        String content = IOUtils.toString(new FileInputStream(file));
        Pattern pattern = Pattern.compile("(include\\s+')(.*)'");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return project;
        }
        String module = matcher.group(2);


        //find AndroidManifest
        try {
            if (project.getXmlManifest().exists()) {
                ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(project.getXmlManifest()));
                ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
                if (launcherActivity != null) {
                    project.setMainClass(new ClassFile(launcherActivity.getName()));
                    project.setPackageName(manifestData.getPackage());
                    project.createClassR();
                }
                Log.d(TAG, "importAndroidProject launcherActivity = " + launcherActivity);
            } else {
                return null;
            }
            return project;
        } catch (Exception e) {

        }

        return project;
    }

    private void createGradleFile(AndroidAppProject project) throws IOException {
        GradleFileGenerator generator = new GradleFileGenerator(context, project);
        generator.generate();
    }


    private void createRes(AndroidAppProject project, boolean useAppCompat, String appName) throws IOException {
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

    private void createManifest(AndroidAppProject project, String activityClass, String packageName,
                                AssetManager assets) throws IOException {
        File manifest = project.getXmlManifest();
        String content = IOUtils.toString(assets.open("templates/AndroidManifest.xml"));

        content = content.replace("PACKAGE", packageName);
        content = content.replace("MAIN_ACTIVITY", activityClass);
        saveFile(manifest, content);
    }


    private void createMainActivity(AndroidAppProject project, String activityClass,
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

    private void createMainLayoutXml(AndroidAppProject project, String layoutName) throws IOException {
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

    private void copyLibrary(AndroidAppProject project, boolean useCompatLibrary)
            throws IOException, StreamException, SAXException, ParserConfigurationException {
        if (useCompatLibrary) {
            //v7
            addLib(project, "libs/27.1.1/android.arch.core-common-1.1.0.jar", "android.arch.core-common-1.1.0.jar");
            addLib(project, "libs/27.1.1/android.arch.core-runtime-1.1.0.aar", "android.arch.core-runtime-1.1.0");
            addLib(project, "libs/27.1.1/android.arch.lifecycle-common-1.1.0.jar", "android.arch.lifecycle-common-1.1.0.jar");
            addLib(project, "libs/27.1.1/android.arch.lifecycle-livedata-core-1.1.0.aar", "android.arch.lifecycle-livedata-core-1.1.0");
            addLib(project, "libs/27.1.1/android.arch.lifecycle-runtime-1.1.0.aar", "android.arch.lifecycle-runtime-1.1.0");
            addLib(project, "libs/27.1.1/android.arch.lifecycle-viewmodel-1.1.0.aar", "android.arch.lifecycle-viewmodel-1.1.0");
            addLib(project, "libs/27.1.1/appcompat-v7-27.1.1.aar", "appcompat-v7-27.1.1");
            addLib(project, "libs/27.1.1/animated-vector-drawable-27.1.1.aar", "animated-vector-drawable-27.1.1");
            addLib(project, "libs/27.1.1/support-compat-27.1.1.aar", "support-compat-27.1.1");
            addLib(project, "libs/27.1.1/support-core-ui-27.1.1.aar", "support-core-ui-27.1.1");
            addLib(project, "libs/27.1.1/support-core-utils-27.1.1.aar", "support-core-utils-27.1.1");
            addLib(project, "libs/27.1.1/support-fragment-27.1.1.aar", "support-fragment-27.1.1");
            addLib(project, "libs/27.1.1/support-vector-drawable-27.1.1.aar", "support-vector-drawable-27.1.1");
            addLib(project, "libs/27.1.1/support-annotations-27.1.1.jar", "support-annotations-27.1.1.jar");
            addLib(project, "libs/27.1.1/support-media-compat-27.1.1.aar", "support-media-compat-27.1.1");
            addLib(project, "libs/27.1.1/support-v4-27.1.1.aar", "support-v4-27.1.1.aar");
        }
    }

    private void addLib(AndroidAppProject project, String assetsPath, String libName)
            throws SAXException, StreamException, ParserConfigurationException, IOException {
        if (assetsPath.endsWith(".jar")) {
            File javaLib = new File(project.getDirLibs(), libName);
            FileOutputStream output = new FileOutputStream(javaLib);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();
        } else if (assetsPath.endsWith(".aar")) {
            File aarFile = new File(project.getRootDir(), libName + "/" + assetsPath);
            aarFile.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(aarFile);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();

            AndroidLibraryExtractor extractor = new AndroidLibraryExtractor(context);
            extractor.extract(aarFile, libName);

            File dirLib = new File(Environment.getSdCardLibraryCachedDir(context), libName);
            AndroidLibraryProject androidLib = new AndroidLibraryProject(dirLib, libName);
            project.addDependence(androidLib);
        }
    }

}
