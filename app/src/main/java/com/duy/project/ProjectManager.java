package com.duy.project;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.sdklib.xml.AndroidManifestParser;
import com.android.sdklib.xml.ManifestData;
import com.duy.project.file.android.AndroidProjectFolder;
import com.duy.project.file.java.ClassFile;
import com.duy.project.file.java.JavaProjectFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ProjectManager {

    private static final String CURRENT_PROJECT = "file_project.nide";
    private static final String TAG = "ProjectManager";

    public static void saveProject(@NonNull Context context, @NonNull JavaProjectFolder projectFile) {
        try {
            File file = new File(context.getFilesDir(), CURRENT_PROJECT);
            ObjectOutputStream inputStream = new ObjectOutputStream(new FileOutputStream(file));
            inputStream.writeObject(projectFile);
            inputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static JavaProjectFolder getLastProject(@NonNull Context context) {
        try {
            File file = new File(context.getFilesDir(), CURRENT_PROJECT);
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            Object o = objectInputStream.readObject();
            objectInputStream.close();
            JavaProjectFolder folder = (JavaProjectFolder) o;
            folder.initJavaProject();
            if (folder instanceof AndroidProjectFolder){
                ((AndroidProjectFolder) folder).initAndroidProject();
            }
            return folder;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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

    public static AndroidProjectFolder importAndroidProject(Context context, File file) {
        Log.d(TAG, "importAndroidProject() called with: context = [" + context + "], file = [" + file + "]");

        AndroidProjectFolder project = new AndroidProjectFolder(file.getParentFile(),
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
