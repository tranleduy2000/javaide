package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import kellinwood.security.zipsigner.ZipSigner;

public class SignApkTask extends Task<AndroidAppProject> {
    private final BuildType type;

    public SignApkTask(AndroidAppBuilder builder, BuildType type) {
        super(builder);
        this.type = type;
    }

    @Override
    public String getTaskName() {
        return "Sign apk";
    }

    @Override
    public boolean doFullTaskAction() throws Exception {
        if (type == BuildType.DEBUG) {
            return signDebug();
        } else {
            signRelease();
        }
        return true;
    }

    private void signRelease() {
        // TODO: 31-May-18  signRelease
    }

    private boolean signDebug() throws IOException, GeneralSecurityException, IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        mBuilder.stdout("Sign debug apk");

        String mode = "testkey";

        ZipSigner signer = new ZipSigner();
        signer.setKeymode(mode);

        File apkUnsigned = mProject.getApkUnsigned();
        String in = apkUnsigned.getAbsolutePath();
        File apkSigned = mProject.getApkSigned();
        apkSigned.delete();
        String out = apkSigned.getAbsolutePath();
        signer.signZip(in, out);

        mBuilder.stdout("Signed debug apk " + mProject.getApkUnsigned().getName() + " => " + mProject.getApkSigned().getName());


        return apkSigned.exists();
    }
}
