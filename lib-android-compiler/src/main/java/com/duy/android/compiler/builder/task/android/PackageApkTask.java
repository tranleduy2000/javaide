package com.duy.android.compiler.builder.task.android;

import com.android.sdklib.build.ApkBuilder;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.File;

public class PackageApkTask extends Task<AndroidAppProject> {

    public PackageApkTask(AndroidAppBuilder builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Package application";
    }

    @Override
    public boolean doFullTaskAction() throws Exception {
        ApkBuilder apkBuilder = new ApkBuilder(
                mProject.getApkUnsigned(),
                mProject.getProcessResourcePackageOutputFile(),
                mProject.getDexFile(),
                null,
                null);

        for (File file : mProject.getJavaSrcDirs()) {
            apkBuilder.addSourceFolder(file);
        }

        apkBuilder.sealApk();

        return true;
    }
}
