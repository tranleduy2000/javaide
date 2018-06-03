package com.duy.android.compiler.project;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class JavaProjectManager implements IProjectManager<JavaProject> {

    private static final String KEY_ANDROID_PROJECT = "is_android_project";
    private static final String KEY_ROOT_DIR = "last_project_dir";
    private Context context;

    public JavaProjectManager(Context context) {
        this.context = context;
    }


    public static void saveProject(@NonNull Context context, JavaProject project) {
        if (project == null) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean(KEY_ANDROID_PROJECT, project instanceof AndroidAppProject);
        edit.putString(KEY_ROOT_DIR, project.getRootDir().getPath());
        edit.apply();
    }

    @Nullable
    public static JavaProject getLastProject(@NonNull Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean androidProject = preferences.getBoolean(KEY_ANDROID_PROJECT, false);
        String rootDir = preferences.getString(KEY_ROOT_DIR, null);
        if (rootDir == null || !(new File(rootDir).exists())) {
            return null;
        }
        if (androidProject) {
            try {
                AndroidProjectManager manager = new AndroidProjectManager(context);
                return manager.loadProject(new File(rootDir), true);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                JavaProjectManager projectManager = new JavaProjectManager(context);
                return projectManager.loadProject(new File(rootDir), true);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @NonNull
    public JavaProject createNewProject(File dirToCreate, String projectName) {
        projectName = projectName.replaceAll("\\s+", "");
        File rootDir = new File(dirToCreate, projectName);
        JavaProject javaProject = new JavaProject(rootDir, null);
        javaProject.mkdirs();
        return javaProject;
    }

    @Override
    public JavaProject loadProject(File rootDir, boolean tryToImport) throws IOException {
        JavaProject project = new JavaProject(rootDir, null);

        //compatible with old version
        if (tryToImport) {
            File oldSrcDir = new File(project.getRootDir(), "src/main/java");
            if (oldSrcDir.exists()) {
                FileUtils.copyDirectory(oldSrcDir, project.getJavaSrcDir());
                FileUtils.deleteDirectory(oldSrcDir);
            }
            File oldLibs = new File(project.getRootDir(), "libs");
            if (oldLibs.exists()) {
                FileUtils.copyDirectory(oldLibs, project.getDirLibs());
                FileUtils.deleteDirectory(oldLibs);
            }
        }

        return project;
    }
}
