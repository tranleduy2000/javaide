package com.duy.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.ide.common.xml.AndroidManifestParser;
import com.android.ide.common.xml.ManifestData;
import com.duy.project.file.android.AndroidProject;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFolder;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ProjectManager {

    private static final String CURRENT_PROJECT = "file_project.nide";
    private static final String TAG = "ProjectManager";

    private static final String ANDROID_PROJECT = "ANDROID_PROJECT";
    private static final String ROOT_DIR = "ROOT_DIR";
    private static final String MAIN_CLASS_NAME = "MAIN_CLASS_NAME";
    private static final String PACKAGE_NAME = "PACKAGE_NAME";
    private static final String PROJECT_NAME = "PROJECT_NAME";


    public static void saveProject(@NonNull Context context, @NonNull JavaProjectFolder folder) {
        SharedPreferences preferences = context.getSharedPreferences(CURRENT_PROJECT, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(ANDROID_PROJECT, folder instanceof AndroidProject);
        edit.putString(ROOT_DIR, folder.getRootDir().getPath());
        edit.putString(MAIN_CLASS_NAME, folder.getMainClass().getName());
        edit.putString(PACKAGE_NAME, folder.getPackageName());
        edit.putString(PROJECT_NAME, folder.getProjectName());
        edit.apply();
    }

    @Nullable
    public static JavaProjectFolder getLastProject(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences(CURRENT_PROJECT, Context.MODE_PRIVATE);
        boolean androidProject = preferences.getBoolean(ANDROID_PROJECT, false);
        String rootDir = preferences.getString(ROOT_DIR, null);
        if (rootDir == null || !(new File(rootDir).exists())) return null;
        String mainClassName = preferences.getString(MAIN_CLASS_NAME, null);
        String packageName = preferences.getString(PACKAGE_NAME, null);
        String projectName = preferences.getString(PROJECT_NAME, null);
        if (androidProject) {
            return new AndroidProject(new File(rootDir), mainClassName, packageName, projectName);
        } else {
            return new JavaProjectFolder(new File(rootDir), mainClassName, packageName, projectName);
        }
    }

    @Nullable
    public static JavaProjectFolder createProjectIfNeed(Context context, File file) {
        if (file.isFile() || !file.canWrite() || !file.canRead()) {
            return null;
        }
        // TODO: 05-Aug-17 dynamic change classpath
        JavaProjectFolder projectFile = new JavaProjectFolder(file.getParentFile(), null,
                null, file.getName());
        projectFile.setProjectName(file.getName());
        try {
            projectFile.createMainClass();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projectFile;
    }

    public static AndroidProject importAndroidProject(Context context, File file) {
        Log.d(TAG, "importAndroidProject() called with: context = [" + context + "], file = [" + file + "]");

        AndroidProject project = new AndroidProject(file.getParentFile(),
                null, null, file.getName());
        try {
            if (project.getXmlManifest().exists()) {
                ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(project.getXmlManifest()));
                ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
                if (launcherActivity != null) {
                    project.setMainClass(new ClassFile(launcherActivity.getName()));
                    project.setPackageName(manifestData.getPackage());
                }
                Log.d(TAG, "importAndroidProject launcherActivity = " + launcherActivity);
            } else {
                return null;
            }
            if (project.getKeyStore().getFile().exists()) {
                project.checkKeyStoreExits(context);
            }
            return project;
        } catch (Exception e) {

        }
        return null;
    }
}
