package com.android.tests.basic.buildscript;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.builder.testing.api.TestServer;

import java.io.File;

public class FakeServer extends TestServer {

    private boolean uploadCalled = false;

    @Override
    public String getName() {
        return "fake2";
    }

    @Override
    public void uploadApks(@NonNull String variantName,
                           @NonNull File testApk,
                           @Nullable File testedApk) {
        System.out.println("uploadApks CALLED");

        if (testApk == null) {
            throw new NullPointerException("Null testApk");
        }

        if (!testApk.isFile()) {
            throw new RuntimeException("Missing file: " + testApk.getAbsolutePath());
        }

        if (!testApk.getAbsolutePath().endsWith(".apk")) {
            throw new RuntimeException("Wrong extension: " + testApk.getAbsolutePath());
        }

        System.out.println("\ttestApk: " + testApk.getAbsolutePath());

        if (testedApk != null) {
            if (!testedApk.isFile()) {
                throw new RuntimeException("Missing file: " + testedApk.getAbsolutePath());
            }

            if (!testedApk.getAbsolutePath().endsWith(".apk")) {
                throw new RuntimeException("Wrong extension: " + testedApk.getAbsolutePath());
            }

            System.out.println("\ttestedApk: " + testedApk.getAbsolutePath());

            if (testApk.equals(testedApk)) {
                throw new RuntimeException("Both APKs are the same!");
            }
        }

        uploadCalled = true;
    }

    public String isValid() {
        if (!uploadCalled) {
            return "uploadApks not called";
        }

        return null;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

}
