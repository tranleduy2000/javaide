package com.duy.android.compiler.builder.task.android;

import com.android.sdklib.build.ApkBuilder;
import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.File;

public class PackageApplicationTask extends ATask<AndroidAppProject> {

    public PackageApplicationTask(AndroidAppBuilder builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Package application";
    }

    @Override
    public boolean run() throws Exception {
        ApkBuilder apkBuilder = new ApkBuilder(
                project.getApkUnsigned(),
                project.getOutResourceFile(),
                project.getDexFile(),
                null,
                null);

        for (File file : project.getJavaSrcDirs()) {
            apkBuilder.addSourceFolder(file);
        }

        apkBuilder.sealApk();

        return true;
    }
}
