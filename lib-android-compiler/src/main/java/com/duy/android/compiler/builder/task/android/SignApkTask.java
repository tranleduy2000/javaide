package com.duy.android.compiler.builder.task.android;

import com.duy.android.compiler.builder.AndroidAppBuilder;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import kellinwood.security.zipsigner.ZipSigner;

public class SignApkTask extends ATask<AndroidAppProject> {
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
    public boolean run() throws Exception {
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
        builder.stdout("Sign debug apk");

        String mode = "testkey";

        ZipSigner signer = new ZipSigner();
        signer.setKeymode(mode);

        File apkUnsigned = project.getApkUnsigned();
        String in = apkUnsigned.getAbsolutePath();
        File apkSigned = project.getApkSigned();
        apkSigned.delete();
        String out = apkSigned.getAbsolutePath();
        signer.signZip(in, out);

        builder.stdout("Signed debug apk " + project.getApkUnsigned().getName() + " => " + project.getApkSigned().getName());


        return apkSigned.exists();
    }
}
