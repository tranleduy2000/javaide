package com.duy.project.file.java;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
                new File(context.getFilesDir(), "system/classes/android.jar").getPath());
        projectFile.setProjectName(file.getName());
        try {
            projectFile.createMainClass();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return projectFile;
    }
}
