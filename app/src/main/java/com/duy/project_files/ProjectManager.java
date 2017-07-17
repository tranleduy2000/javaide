package com.duy.project_files;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.duy.editor.file.FileManager;

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

    public static boolean saveProject(@NonNull Context context, ProjectFile projectFile) {
        JSONObject object = projectFile.exportJson();
        File file = new File(context.getFilesDir(), CURRENT_PROJECT);
        return FileManager.saveFile(file, object.toString());
    }

    @Nullable
    public static ProjectFile getLastProject(@NonNull Context context) {
        File file = new File(context.getFilesDir(), CURRENT_PROJECT);
        try {
            String s = FileManager.streamToString(new FileInputStream(file)).toString();
            ProjectFile projectFile = new ProjectFile();
            projectFile.restore(new JSONObject(s));
            return projectFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
