package com.duy.projectview;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.AndroidProjectManager;
import com.duy.android.compiler.project.JavaProject;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ProjectManager {

    private static final String CURRENT_PROJECT = "file_project.nide";

    private static final String ANDROID_PROJECT = "ANDROID_PROJECT";
    private static final String ROOT_DIR = "ROOT_DIR";
    private static final String MAIN_CLASS_NAME = "MAIN_CLASS_NAME";
    private static final String PACKAGE_NAME = "PACKAGE_NAME";


    public static void saveProject(@NonNull Context context, @NonNull JavaProject folder) {
        SharedPreferences preferences = context.getSharedPreferences(CURRENT_PROJECT, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(ANDROID_PROJECT, folder instanceof AndroidAppProject);
        edit.putString(ROOT_DIR, folder.getRootDir().getPath());
        edit.apply();
    }

    @Nullable
    public static JavaProject getLastProject(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences(CURRENT_PROJECT, Context.MODE_PRIVATE);
        boolean androidProject = preferences.getBoolean(ANDROID_PROJECT, false);
        String rootDir = preferences.getString(ROOT_DIR, null);
        if (rootDir == null || !(new File(rootDir).exists())) {
            return null;
        }
        String mainClassName = preferences.getString(MAIN_CLASS_NAME, null);
        String packageName = preferences.getString(PACKAGE_NAME, null);
        if (androidProject) {
            AndroidProjectManager manager = new AndroidProjectManager(context);
            try {
                return manager.loadProject(new File(rootDir), true);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return new JavaProject(new File(rootDir), mainClassName, packageName);
        }
    }

    @Nullable
    public static JavaProject createProjectIfNeed(Context context, File file) {
        if (file.isFile() || !file.canWrite() || !file.canRead()) {
            return null;
        }
        // TODO: 05-Aug-17 dynamic change classpath
        JavaProject projectFile = new JavaProject(file.getParentFile(), null, null);
        try {
            projectFile.createMainClass();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projectFile;
    }

}
