package com.duy.project.file.java;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.sdklib.xml.AndroidManifestParser;
import com.android.sdklib.xml.ManifestData;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProjectFile;
import com.duy.project.file.android.Constants;
import com.duy.project.file.android.KeyStore;

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

    public static boolean saveProject(@NonNull Context context, @NonNull JavaProjectFile projectFile) {
        try {
            File file = new File(context.getFilesDir(), CURRENT_PROJECT);
            ObjectOutputStream inputStream = new ObjectOutputStream(new FileOutputStream(file));
            inputStream.writeObject(projectFile);
            inputStream.flush();
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nullable
    public static JavaProjectFile getLastProject(@NonNull Context context) {
        try {
            File file = new File(context.getFilesDir(), CURRENT_PROJECT);
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            Object o = objectInputStream.readObject();
            objectInputStream.close();
            return (JavaProjectFile) o;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static JavaProjectFile createProjectIfNeed(Context context, File file) {
        if (file.isFile() || !file.canWrite() || !file.canRead()) {
            return null;
        }
        // TODO: 05-Aug-17 dynamic change classpath
        JavaProjectFile projectFile = new JavaProjectFile(file.getParentFile(), null, null, file.getName(),
                FileManager.getClasspathFile(context).getPath());
        projectFile.setProjectName(file.getName());
        try {
            projectFile.createMainClass();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projectFile;
    }

    public static JavaProjectFile importAndroidProject(Context context, File file) {
        Log.d(TAG, "importAndroidProject() called with: context = [" + context + "], file = [" + file + "]");

        AndroidProjectFile project = new AndroidProjectFile(file.getParentFile(),
                null, null, file.getName(), FileManager.getClasspathFile(context).getPath());
        try {
            if (project.getXmlManifest().exists()) {
                ManifestData manifestData = AndroidManifestParser.parse(new FileInputStream(project.getXmlManifest()));
                ManifestData.Activity launcherActivity = manifestData.getLauncherActivity();
                if (launcherActivity != null) {
                    project.setMainClass(new ClassFile(launcherActivity.getName()));
                }
                Log.d(TAG, "importAndroidProject launcherActivity = " + launcherActivity);
            } else {
                return null;
            }
            if (project.getKeyStore().getFile().exists()) {
                File key = new File(project.dirProject, "keystore.jks");
                if (!key.getParentFile().exists()) {
                    key.getParentFile().mkdirs();
                }
                key.createNewFile();
                FileOutputStream out = new FileOutputStream(key);
                FileManager.copyFile(context.getAssets().open(Constants.KEY_STORE_ASSET_PATH), out);
                out.close();
                project.setKeystore(new KeyStore(key, Constants.KEY_STORE_PASSWORD,
                        Constants.KEY_STORE_ALIAS, Constants.KEY_STORE_ALIAS_PASS));
            }
        } catch (Exception e) {

        }
        return null;
    }
}
