package com.duy.project_files;

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

    private static final String FILE_PROJECT = "file_project.nide";

    public static boolean saveProject(@NonNull Context context, ProjectFile projectFile) {
        File file = new File(context.getFilesDir(), FILE_PROJECT);
        try {
            if (!file.exists()) file.createNewFile();
            FileOutputStream fo = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fo);
            out.writeObject(projectFile);
            out.flush();
            out.close();
            fo.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Nullable
    public static ProjectFile getLastProject(@NonNull Context context) {
        File file = new File(context.getFilesDir(), FILE_PROJECT);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
            return (ProjectFile) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
