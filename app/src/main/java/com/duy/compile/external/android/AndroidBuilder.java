package com.duy.compile.external.android;

import android.os.Environment;
import android.util.Log;

import com.android.annotations.NonNull;
import com.duy.compile.external.CompileHelper;
import com.duy.project.file.android.AndroidProjectFolder;
import com.duy.project.file.android.KeyStore;
import com.spartacusrex.spartacuside.external.apkbuilder;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;

import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.CustomKeySigner;

import static com.duy.compile.external.android.util.S.dirLibs;


public class AndroidBuilder {
    private static final String TAG = "BuildTask";

    private static void buildApk(AndroidProjectFolder projectFile) throws Exception {
        String[] args = {
                projectFile.getApkUnsigned().getPath(),
                "-v", "-u", "-z", projectFile.getResourceFile().getPath(),
                "-f", projectFile.getDexedClassesFile().getPath()
        };
        Log.d(TAG, "buildApk args = " + Arrays.toString(args));
        apkbuilder.main(args);
    }

    public static void build(AndroidProjectFolder projectFile,
                             @NonNull DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder.extractLibrary(projectFile);

        //create R.java
        System.out.println("Run aidl");
        AndroidBuilder.runAidl(projectFile);
        System.out.println("Run aapt");
        AndroidBuilder.runAapt(projectFile);

        //compile java
        System.out.println("Compile Java file");
        int status = CompileHelper.compileJava(projectFile, diagnosticCollector);
        System.gc();
        if (status != Main.EXIT_OK) {
            System.out.println("Compile error");
            throw new RuntimeException("Compile time error!");
        }

        //classes to dex
        System.out.println("Convert classes to dex");
        CompileHelper.convertToDexFormat(projectFile);

        //zip apk
        System.out.println("Build apk");
        AndroidBuilder.buildApk(projectFile);

        System.out.println("Zip sign");
        AndroidBuilder.zipSign(projectFile);

        System.out.println("Zip align");
        AndroidBuilder.zipAlign();

        System.out.println("Publish apk");
        AndroidBuilder.publishApk();
    }

    private static void extractLibrary(AndroidProjectFolder projectFolder) {
        File[] files = dirLibs.listFiles();
        if (files != null) {
            for (File lib : files) {
                if (lib.isFile() && lib.getPath().endsWith(".aar")) {
                }
            }
        }
    }

    private static void runAidl(AndroidProjectFolder projectFile) throws Exception {
        Log.d(TAG, "runAidl() called");

        // TODO make aidl.so
    }

    private static void runAapt(AndroidProjectFolder projectFile) throws Exception {
        Log.d(TAG, "runAapt() called");

        Aapt aapt = new Aapt();
        String command = "aapt p -f -v" +
                " --auto-add-overlay" +
                " -M " + projectFile.xmlManifest.getPath() + //manifest file
                " -F " + projectFile.getResourceFile().getPath() + //
                " -I " + projectFile.bootClasspath.getPath() + //include
                " -A " + projectFile.getDirAssets().getPath() + //assets dir
                " -S " + projectFile.getDirRes().getPath() + //resource dir
                " -J " + projectFile.getClassR().getParent();//out R.java dir

        File dirLibs = projectFile.getDirLibs();
        File[] files = dirLibs.listFiles();
        if (files != null) {
            for (File lib : files) {
                if (lib.isFile()) {
                    if (lib.getPath().endsWith(".jar")) {
                        command += " -I " + lib.getPath();
                    } else if (lib.getPath().endsWith(".aar")) {
                        command += " -I " + lib.getPath() + File.separator + "res";
                    }
                }
            }
        }
        Log.d(TAG, "runAapt command = " + command);
        int exitCode = aapt.fnExecute(command);
        if (exitCode != 0) {
            throw new Exception("AAPT exit(" + exitCode + ")");
        }

    }

    private static void zipSign(AndroidProjectFolder projectFile) throws Exception {
//        if (!appContext.getString(R.string.keystore).contentEquals(projectFile.jksEmbedded.getName())) {
//             TODO use user defined certificate
//        }

        // use embedded private key
        KeyStore keyStore = projectFile.getKeyStore();
        String keystorePath = keyStore.getFile().getPath();
        char[] keystorePw = keyStore.getPassword();
        String certAlias = keyStore.getCertAlias();
        char[] certPw = keyStore.getCertPassword();
        String signatureAlgorithm = "SHA1withRSA";

        ZipSigner zipsigner = new ZipSigner();
        zipsigner.addProgressListener(new SignProgress() {
            @Override
            public void onProgress(ProgressEvent event) {
                super.onProgress(event);
                System.out.println("Sign progress: " + event.getPercentDone());
            }
        });
        CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw, certAlias,
                certPw, signatureAlgorithm,
                projectFile.getApkUnsigned().getPath(),
                projectFile.getApkUnaligned().getPath());
    }


    private static void zipAlign() throws Exception {
//         TODO make zipalign.so
    }

    private static void publishApk() throws Exception {
//        if (projectFile.apkRedistributable.exists()) {
//            projectFile.apkRedistributable.delete();
//        }
//        Util.copy(projectFile.apkUnaligned, new FileOutputStream(projectFile.apkRedistributable));
//
//        projectFile.apkRedistributable.setReadable(true, false);
    }

    public void run() {


    }

    public static class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

        public void onProgress(kellinwood.security.zipsigner.ProgressEvent event) {
        }

    }

}