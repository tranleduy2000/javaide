package com.duy.android.compiler.project;

import android.content.Context;

import java.io.File;

public interface IAndroidProjectManager {
    AndroidAppProject createNewProject(Context context, File dir, String projectName,
                                       String packageName, String activityName, String mainLayoutName,
                                       String appName, boolean useCompatLibrary) throws Exception;
}
