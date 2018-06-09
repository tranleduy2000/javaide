package com.duy.android.compiler.project;


import android.content.Context;

import com.duy.android.compiler.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class AndroidGradleFileGenerator {
    public static final String DEFAULT_BUILD_FILE = "build.gradle";
    public static final String DEFAULT_SETTING_FILE = "settings.gradle";
    private Context context;
    private AndroidAppProject project;

    public AndroidGradleFileGenerator(Context context, AndroidAppProject project) {
        this.context = context;
        this.project = project;
    }

    public void generate() throws IOException {
        generateSettingFile();
        File rootGradle = new File(project.getRootDir(), DEFAULT_BUILD_FILE);
        IOUtils.copyNotIfExistAndClose(context.getAssets().open("templates/app/build.gradle.root"), rootGradle);


        File appGradle = new File(project.getAppDir(), DEFAULT_BUILD_FILE);
        IOUtils.copyNotIfExistAndClose(context.getAssets().open("templates/app/build_gradle.template"), appGradle);
    }

    public void generateSettingFile() throws IOException {
        File settingGradle = new File(project.getRootDir(), DEFAULT_SETTING_FILE);
        IOUtils.copyNotIfExistAndClose(context.getAssets().open("templates/app/settings.gradle.root"), settingGradle);
    }

}
