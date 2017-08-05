package com.duy.project.file.java;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.ide.file.FileManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class ProjectManager {

    private static final String CURRENT_PROJECT = "file_project.nide";

    public static boolean saveProject(@NonNull Context context, @NonNull JavaProjectFile projectFile) {
        JSONObject object = projectFile.exportJson();
        File file = new File(context.getFilesDir(), CURRENT_PROJECT);
        return FileManager.saveFile(file, object.toString());
    }

    @Nullable
    public static JavaProjectFile getLastProject(@NonNull Context context) {
        File file = new File(context.getFilesDir(), CURRENT_PROJECT);
        try {
            String s = FileManager.streamToString(new FileInputStream(file)).toString();
            return JavaProjectFile.restore(new JSONObject(s));
        } catch (FileNotFoundException | JSONException e) {
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
