package com.duy.compile.external.android.builder.task;

import com.android.sdklib.build.ApkBuilder;
import com.duy.compile.external.android.builder.AndroidBuilder;

import java.io.File;

public class BuildApkTask extends BuildTask {

    public BuildApkTask(AndroidBuilder builder) {
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
