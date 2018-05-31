package com.duy.compile.external.android.builder.task;

import com.duy.compile.external.android.builder.AndroidBuilder;
import com.duy.compile.external.android.builder.BuildType;

import java.io.IOException;
import java.security.GeneralSecurityException;

import kellinwood.security.zipsigner.ZipSigner;

public class SignApkTask extends BuildTask {
    private final BuildType type;

    public SignApkTask(AndroidBuilder builder, BuildType type) {
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

        String in = project.getApkUnsigned().getAbsolutePath();
        String out = project.getApkSigned().getAbsolutePath();
        signer.signZip(in, out);

        builder.stdout("Signed debug apk " + project.getApkUnsigned().getName() + " => " + project.getApkSigned().getName());

        return true;
    }
}
