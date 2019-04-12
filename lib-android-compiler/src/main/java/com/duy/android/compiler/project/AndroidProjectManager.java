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
import com.duy.common.io.IOUtils;

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
        copyAssets("templates/app/ic_launcher_background.xml",
                new File(resDir, "drawable/ic_launcher_background.xml"));

        //drawable v24
        copyAssets("templates/app/ic_launcher_foreground_v24.xml",
                new File(resDir, "drawable-v24/ic_launcher_foreground.xml"));

        //mipmap
        copyAssets("templates/app/ic_launcher_hdpi.png",
                new File(resDir, "mipmap-hdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_mdpi.png",
                new File(resDir, "mipmap-mdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_xhdpi.png",
                new File(resDir, "mipmap-xhdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_xxhdpi.png",
                new File(resDir, "mipmap-xxhdpi/ic_launcher.png"));
        copyAssets("templates/app/ic_launcher_xxxhdpi.png",
                new File(resDir, "mipmap-xxxhdpi/ic_launcher.png"));

        //mipmap round
        copyAssets("templates/app/ic_launcher_round_hdpi.png",
                new File(resDir, "mipmap-hdpi/ic_launcher_round.png"));
        copyAssets("templates/app/ic_launcher_round_mdpi.png",
                new File(resDir, "mipmap-mdpi/ic_launcher_round.png"));
        copyAssets("templates/app/ic_launcher_round_xhdpi.png",
                new File(resDir, "mipmap-xhdpi/ic_launcher_round.png"));
        copyAssets("templates/app/ic_launcher_round_xxhdpi.png",
                new File(resDir, "mipmap-xxhdpi/ic_launcher_round.png"));
        copyAssets("templates/app/ic_launcher_round_xxxhdpi.png",
                new File(resDir, "mipmap-xxxhdpi/ic_launcher_round.png"));

        //mipmap-anydpi-v26
        copyAssets("templates/app/ic_launcher_anydpi_v26.xml",
                new File(resDir, "mipmap-anydpi-v26/ic_launcher.xml"));
        copyAssets("templates/app/ic_launcher_round_anydpi_v26.xml",
                new File(resDir, "mipmap-anydpi-v26/ic_launcher_round.xml"));

        //styles
        File style = new File(resDir, "values/styles.xml");
        String content = IOUtils.toString(
                context.getAssets().open("templates/app/styles.xml"), "UTF-8");
        content = content.replace("APP_STYLE", useAppCompat
                ? "Theme.AppCompat.Light.DarkActionBar" : "@android:style/Theme.Light");
        saveFile(style, content);

        File string = new File(resDir, "values/strings.xml");
        content = IOUtils.toString(
                context.getAssets().open("templates/app/strings.xml"), "UTF-8");
        content = content.replace("APP_NAME", appName);
        content = content.replace("MAIN_ACTIVITY_NAME", appName);
        saveFile(string, content);

        //colors
        File colors = new File(resDir, "values/colors.xml");
        content = IOUtils.toString(
                context.getAssets().open("templates/app/colors.xml"), "UTF-8");
        saveFile(colors, content);
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
        IOUtils.copy(input, output);
        input.close();
        output.close();
    }

    private void saveFile(File file, String content) throws IOException {
        file.getParentFile().mkdirs();
        FileOutputStream output = new FileOutputStream(file);
        IOUtils.write(content, output);
        output.close();
    }

    private void copyLibrary(AndroidAppProject project, boolean useCompatLibrary)
            throws IOException, StreamException, SAXException, ParserConfigurationException {
        if (useCompatLibrary) {

            //arch
            addLib(project, "sdk/extras/android.arch.core/common/common-1.1.1.jar", "android.arch.core-common-1.1.1.jar");
            addLib(project, "sdk/extras/android.arch.core/runtime/runtime-1.1.1.aar", "/android.arch.core/runtime-1.1.1");
            addLib(project, "sdk/extras/android.arch.lifecycle/common/common-1.1.1.jar", "android.arch.lifecycle-common-1.1.1.jar");
            addLib(project, "sdk/extras/android.arch.lifecycle/livedata/livedata-1.1.1.aar", "/android.arch.lifecycle/livedata-1.1.1");
            addLib(project, "sdk/extras/android.arch.lifecycle/livedata-core/livedata-core-1.1.1.aar", "/android.arch.lifecycle/livedata-core-1.1.1");
            addLib(project, "sdk/extras/android.arch.lifecycle/runtime/runtime-1.1.1.aar", "/android.arch.lifecycle/runtime-1.1.1");
            addLib(project, "sdk/extras/android.arch.lifecycle/viewmodel/viewmodel-1.1.1.aar", "/android.arch.lifecycle/viewmodel-1.1.1");

            //v7
            addLib(project, "sdk/extras/com.android.support/animated-vector-drawable/animated-vector-drawable-28.0.0.aar", "/com.android.support/28.0.0/animated-vector-drawable");
            addLib(project, "sdk/extras/com.android.support/appcompat-v7/appcompat-v7-28.0.0.aar", "/com.android.support/28.0.0/appcompat-v7");
            addLib(project, "sdk/extras/com.android.support/asynclayoutinflater/asynclayoutinflater-28.0.0.aar", "/com.android.support/28.0.0/asynclayoutinflater");
            addLib(project, "sdk/extras/com.android.support/collections/collections-28.0.0.jar", "collections-28.0.0.jar");
            addLib(project, "sdk/extras/com.android.support/coordinatorlayout/coordinatorlayout-28.0.0.aar", "/com.android.support/28.0.0/coordinatorlayout");
            addLib(project, "sdk/extras/com.android.support/cursoradapter/cursoradapter-28.0.0.aar", "/com.android.support/28.0.0/cursoradapter");
            addLib(project, "sdk/extras/com.android.support/customview/customview-28.0.0.aar", "/com.android.support/28.0.0/customview");
            addLib(project, "sdk/extras/com.android.support/documentfile/documentfile-28.0.0.aar", "/com.android.support/28.0.0/documentfile");
            addLib(project, "sdk/extras/com.android.support/drawerlayout/drawerlayout-28.0.0.aar", "/com.android.support/28.0.0/drawerlayout");
            addLib(project, "sdk/extras/com.android.support/interpolator/interpolator-28.0.0.aar", "/com.android.support/28.0.0/interpolator");
            addLib(project, "sdk/extras/com.android.support/loader/loader-28.0.0.aar", "/com.android.support/28.0.0/loader");
            addLib(project, "sdk/extras/com.android.support/localbroadcastmanager/localbroadcastmanager-28.0.0.aar", "/com.android.support/28.0.0/localbroadcastmanager");
            addLib(project, "sdk/extras/com.android.support/print/print-28.0.0.aar", "/com.android.support/28.0.0/print");
            addLib(project, "sdk/extras/com.android.support/slidingpanelayout/slidingpanelayout-28.0.0.aar", "/com.android.support/28.0.0/slidingpanelayout");
            addLib(project, "sdk/extras/com.android.support/support-annotations/support-annotations-28.0.0.jar", "support-annotations-28.0.0.jar");
            addLib(project, "sdk/extras/com.android.support/support-compat/support-compat-28.0.0.aar", "/com.android.support/28.0.0/support-compat");
            addLib(project, "sdk/extras/com.android.support/support-core-ui/support-core-ui-28.0.0.aar", "/com.android.support/28.0.0/support-core-ui");
            addLib(project, "sdk/extras/com.android.support/support-core-utils/support-core-utils-28.0.0.aar", "/com.android.support/28.0.0/support-core-utils");
            addLib(project, "sdk/extras/com.android.support/support-fragment/support-fragment-28.0.0.aar", "/com.android.support/28.0.0/support-fragment");
            addLib(project, "sdk/extras/com.android.support/support-vector-drawable/support-vector-drawable-28.0.0.aar", "/com.android.support/28.0.0/support-vector-drawable");
            addLib(project, "sdk/extras/com.android.support/swiperefreshlayout/swiperefreshlayout-28.0.0.aar", "/com.android.support/28.0.0/swiperefreshlayout");
            addLib(project, "sdk/extras/com.android.support/versionedparcelable/versionedparcelable-28.0.0.aar", "/com.android.support/28.0.0/versionedparcelable");
            addLib(project, "sdk/extras/com.android.support/viewpager/viewpager-28.0.0.aar", "/com.android.support/28.0.0/viewpager");
            addLib(project, "sdk/extras/com.android.support.constraint/constraint-layout/constraint-layout-1.1.3.aar", "/com.android.support.constraint/1.1.3/constraint-layout-1.1.3");
            addLib(project, "sdk/extras/com.android.support.constraint/constraint-layout-solver/constraint-layout-solver-1.1.3.jar", "constraint-layout-solver-1.1.3.jar");

            //AndroidX
            //addLib(project, "sdk/extras/androidx.arch.core/common/core-common-2.0.0.jar", "androidx.arch.core-common-2.0.0.jar");
            //addLib(project, "sdk/extras/androidx.arch.core/runtime/core-runtime-2.0.0.aar", "/androidx.arch.core/core-runtime-2.0.0");
            //addLib(project, "sdk/extras/androidx.lifecycle/common/lifecycle-common-2.0.0.jar", "androidx.lifecycle-common-2.0.0.jar");
            //addLib(project, "sdk/extras/androidx.lifecycle/livedata/lifecycle-livedata-2.0.0.aar", "/androidx.lifecycle/lifecycle-livedata-2.0.0");
            //addLib(project, "sdk/extras/androidx.lifecycle/livedata-core/lifecycle-livedata-core-2.0.0.aar", "/androidx.lifecycle/lifecycle-livedata-core-2.0.0");
            //addLib(project, "sdk/extras/androidx.lifecycle/runtime/lifecycle-runtime-2.0.0.aar", "/androidx.lifecycle/lifecycle-runtime-2.0.0");
            //addLib(project, "sdk/extras/androidx.lifecycle/viewmodel/lifecycle-viewmodel-2.0.0.aar", "/androidx.lifecycle/lifecycle-viewmodel-2.0.0");
            //
            //addLib(project, "sdk/extras/androidx.annotation/annotation/annotation-1.0.0.jar", "annotation-1.0.0.jar");
            //addLib(project, "sdk/extras/androidx.appcompat/appcompat/appcompat-1.0.0.aar", "appcompat-1.0.0");
            //addLib(project, "sdk/extras/androidx.asynclayoutinflater/asynclayoutinflater/asynclayoutinflater-1.0.0.aar", "asynclayoutinflater-1.0.0");
            //addLib(project, "sdk/extras/androidx.coordinatorlayout/coordinatorlayout/coordinatorlayout-1.0.0.aar", "coordinatorlayout-1.0.0");
            //addLib(project, "sdk/extras/androidx.core/core/core-1.0.0.aar", "core-1.0.0");
            //addLib(project, "sdk/extras/androidx.cursoradapter/cursoradapter/cursoradapter-1.0.0.aar", "cursoradapter-1.0.0");
            //addLib(project, "sdk/extras/androidx.customview/customview/customview-1.0.0.aar", "customview-1.0.0");
            //addLib(project, "sdk/extras/androidx.documentfile/documentfile/documentfile-1.0.0.aar", "documentfile-1.0.0");
            //addLib(project, "sdk/extras/androidx.drawerlayout/drawerlayout/drawerlayout-1.0.0.aar", "drawerlayout-1.0.0");
            //addLib(project, "sdk/extras/androidx.fragment/fragment/fragment-1.0.0.aar", "fragment-1.0.0");
            //addLib(project, "sdk/extras/androidx.interpolator/interpolator/interpolator-1.0.0.aar", "interpolator-1.0.0");
            //addLib(project, "sdk/extras/androidx.legacy-support-core-ui/legacy-support-core-ui/legacy-support-core-ui-1.0.0.aar", "legacy-support-core-ui-1.0.0");
            //addLib(project, "sdk/extras/androidx.legacy-support-core-utils/legacy-support-core-utils/legacy-support-core-utils-1.0.0.aar", "legacy-support-core-utils-1.0.0");
            //addLib(project, "sdk/extras/androidx.loader/loader/loader-1.0.0.aar", "loader-1.0.0");
            //addLib(project, "sdk/extras/androidx.localbroadcastmanager/localbroadcastmanager/localbroadcastmanager-1.0.0.aar", "localbroadcastmanager-1.0.0");
            //addLib(project, "sdk/extras/androidx.print/print/print-1.0.0.aar", "print-1.0.0");
            //addLib(project, "sdk/extras/androidx.slidingpanelayout/slidingpanelayout/slidingpanelayout-1.0.0.aar", "slidingpanelayout-1.0.0");
            //addLib(project, "sdk/extras/androidx.swiperefreshlayout/swiperefreshlayout/swiperefreshlayout-1.0.0.aar", "swiperefreshlayout-1.0.0");
            //addLib(project, "sdk/extras/androidx.vectordrawable/vectordrawable/vectordrawable-1.0.0.aar", "vectordrawable-1.0.0");
            //addLib(project, "sdk/extras/androidx.vectordrawable/vectordrawable-animated/vectordrawable-animated-1.0.0.aar", "vectordrawable-animated-1.0.0");
            //addLib(project, "sdk/extras/androidx.versionedparcelable/versionedparcelable/versionedparcelable-1.0.0.aar", "versionedparcelable-1.0.0");
            //addLib(project, "sdk/extras/androidx.viewpager/viewpager/viewpager-1.0.0.aar", "viewpager-1.0.0");
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
