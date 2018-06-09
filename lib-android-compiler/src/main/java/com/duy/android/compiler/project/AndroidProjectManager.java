package com.duy.android.compiler.project;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.android.annotations.NonNull;
import com.android.builder.dependency.LibraryBundle;
import com.android.builder.dependency.LibraryDependency;
import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.android.io.StreamException;
import com.duy.android.compiler.builder.internal.dependency.LibraryDependencyImpl;
import com.duy.android.compiler.env.Environment;
import com.duy.android.compiler.library.LibraryCache;
import com.duy.android.compiler.utils.FileUtils;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    @NonNull
    @Override
    public AndroidAppProject loadProject(File rootDir, boolean tryToImport) throws Exception {
        AndroidAppProject project = new AndroidAppProject(rootDir, null, null);
        File file = new File(rootDir, AndroidGradleFileGenerator.DEFAULT_SETTING_FILE);
        if (!file.exists()) {
            //old version
            file = new File(rootDir, "setting.gradle");
            if (!file.exists()) {
                if (!tryToImport) {
                    throw new IOException("Can not find settings.gradle, try to create new project");
                } else {
                    AndroidGradleFileGenerator generator = new AndroidGradleFileGenerator(context, project);
                    generator.generate();
                }
            } else {
                file.renameTo(new File(rootDir, AndroidGradleFileGenerator.DEFAULT_SETTING_FILE));
                file = new File(rootDir, AndroidGradleFileGenerator.DEFAULT_SETTING_FILE);
            }
        }

        //compatible with old version
        if (tryToImport) {
            File oldLibs = new File(project.getRootDir(), "libs");
            if (oldLibs.exists()) {
                FileUtils.copyDirectory(oldLibs, project.getDirLibs());
                FileUtils.deleteDirectory(oldLibs);
            }

            File oldJavaDir = new File(project.getRootDir(), "src/main/java");
            if (oldJavaDir.exists()) {
                FileUtils.copyDirectory(oldJavaDir, project.getJavaSrcDir());
                FileUtils.deleteDirectory(oldJavaDir);
            }
            File oldResDir = new File(project.getRootDir(), "src/main/res");
            if (oldResDir.exists()) {
                FileUtils.copyDirectory(oldResDir, project.getResDir());
                FileUtils.deleteDirectory(oldResDir);
            }

            File oldAssetsDir = new File(project.getRootDir(), "src/main/assets");
            if (oldAssetsDir.exists()) {
                FileUtils.copyDirectory(oldAssetsDir, project.getAssetsDir());
                FileUtils.deleteDirectory(oldAssetsDir);
            }

            File oldManifest = new File(project.getRootDir(), "src/main/AndroidManifest.xml");
            if (oldManifest.exists()) {
                FileUtils.copyFile(oldManifest, project.getManifestFile());
                FileUtils.deleteQuietly(oldManifest);
            }
        }


        // TODO: 03-Jun-18 parse groovy file
        String content = IOUtils.toString(new FileInputStream(file));
        Pattern pattern = Pattern.compile("(include\\s+')(.*)'");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return project;
        }
        /// TODO: 03-Jun-18 dynamic change it
        String appDir = matcher.group(2);
        //find AndroidManifest
        if (project.getManifestFile().exists()) {
            ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(project.getManifestFile()));
            ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
            if (launcherActivity != null) {
                project.setPackageName(manifestData.getPackage());
            }
            Log.d(TAG, "importAndroidProject launcherActivity = " + launcherActivity);
        } else {
            throw new IOException("Can not find AndroidManifest.xml");
        }
        return project;
    }

    private void createGradleFile(AndroidAppProject project) throws IOException {
        AndroidGradleFileGenerator generator = new AndroidGradleFileGenerator(context, project);
        generator.generate();
    }


    private void createRes(AndroidAppProject project, boolean useAppCompat, String appName) throws IOException {
        File resDir = project.getResDir();

        //drawable
        copyAssets("templates/app/ic_launcher_hdpi.png",
                new File(resDir, "drawable-xhdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_ldpi.png",
                new File(resDir, "drawable-ldpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_mdpi.png",
                new File(resDir, "drawable-mdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_xhdpi.png",
                new File(resDir, "drawable-xhdpi/ic_launcher.png"));

        //styles
        File style = new File(resDir, "values/styles.xml");
        String content = IOUtils.toString(
                context.getAssets().open("templates/app/styles.xml"), "UTF-8");
        content = content.replace("APP_STYLE", useAppCompat
                ? "Theme.AppCompat.Light" : "@android:style/Theme.Light");
        saveFile(style, content);

        File string = new File(resDir, "values/strings.xml");
        content = IOUtils.toString(
                context.getAssets().open("templates/app/strings.xml"), "UTF-8");
        content = content.replace("APP_NAME", appName);
        content = content.replace("MAIN_ACTIVITY_NAME", appName);
        saveFile(string, content);
    }

    private void createManifest(AndroidAppProject project, String activityClass, String packageName,
                                AssetManager assets) throws IOException {
        File manifest = project.getManifestFile();
        String content = IOUtils.toString(assets.open("templates/app/AndroidManifest.xml"));

        content = content.replace("PACKAGE", packageName);
        content = content.replace("MAIN_ACTIVITY", activityClass);
        saveFile(manifest, content);
    }


    private void createMainActivity(AndroidAppProject project, String activityClass,
                                    String packageName, String activityName, String appName,
                                    boolean useAppCompat, AssetManager assets) throws IOException {
        File activityFile = new File(project.getJavaSrcDir(),
                activityClass.replace(".", File.separator) + ".java");

        String name = useAppCompat ? "templates/app/MainActivityAppCompat.java" : "templates/app/MainActivity.java";
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
        copyAssets("templates/app/activity_main.xml", layoutMain);
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
            addLib(project, "libs/27.1.1/support-v4-27.1.1.aar", "support-v4-27.1.1");
        }
    }

    private void addLib(AndroidAppProject project, String assetsPath, String bundleFolderName)
            throws SAXException, StreamException, ParserConfigurationException, IOException {
        if (assetsPath.endsWith(".jar")) {
            File javaLib = new File(project.getDirLibs(), bundleFolderName);
            FileOutputStream output = new FileOutputStream(javaLib);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();

        } else if (assetsPath.endsWith(".aar")) {
            File libraryExtractedFolder = Environment.getSdCardLibraryExtractedFolder();
            File libraryBundleFolder = Environment.getSdCardLibraryBundleFolder();

            String bundleName = assetsPath.substring(assetsPath.lastIndexOf("/"));
            File bundle = new File(libraryBundleFolder, bundleName);
            bundle.getParentFile().mkdirs();
            FileOutputStream output = new FileOutputStream(bundle);
            IOUtils.copy(context.getAssets().open(assetsPath), output);
            output.close();

            LibraryCache extractor = new LibraryCache(context);
            File folderOut = new File(libraryExtractedFolder, bundleFolderName);
            extractor.extractAar(bundle, folderOut);

            LibraryBundle androidLib = new LibraryDependencyImpl(bundle, folderOut, new ArrayList<LibraryDependency>(),
                    bundleFolderName, null, project.getRootDir().getAbsolutePath(), null, null, false);
            project.addLibrary(androidLib);
        }
    }

}
