package com.duy.android.compiler.task.android;

import com.android.sdklib.build.ApkBuilder;
import com.duy.android.compiler.builder.AndroidProjectBuilder;
import com.duy.android.compiler.task.ABuildTask;
import com.duy.android.compiler.file.AndroidProject;

import java.io.File;

public class BuildApkTask extends ABuildTask<AndroidProject> {

    public BuildApkTask(AndroidProjectBuilder builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Build apk";
    }

    @Override
    public boolean run() throws Exception {
        ApkBuilder apkBuilder = new ApkBuilder(
                project.getApkUnsigned(),
                project.getOutResourceFile(),
                project.getDexFile(),
                null,
                builder.getStdout());

        for (File file : project.getJavaSrcDirs()) {
            apkBuilder.addSourceFolder(file);
        }

        apkBuilder.sealApk();

        return true;
    }
}
