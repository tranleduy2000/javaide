package com.duy.compile.external.android;

import android.content.Context;
import android.util.Log;

import com.android.annotations.NonNull;
import com.android.sdklib.build.ApkBuilderMain;
import com.duy.compile.external.CompileHelper;
import com.duy.ide.DLog;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProject;
import com.duy.project.file.android.KeyStore;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;

import kellinwood.security.zipsigner.ProgressEvent;
import kellinwood.security.zipsigner.ZipSigner;
import kellinwood.security.zipsigner.optional.CustomKeySigner;



public class AndroidBuilder {
    private static final String TAG = "BuildTask";

    private static void buildApk(AndroidProject projectFile) throws Exception {
        String[] args = {
                projectFile.getApkUnsigned().getPath(),
                "-v",
                "-u",
                "-z", projectFile.getOutResourceFile().getPath(),
                "-f", projectFile.getDexedClassesFile().getPath()
        };
        DLog.d(TAG, "buildApk args = " + Arrays.toString(args));
        ApkBuilderMain.main(args);
    }

    public static void build(Context context, AndroidProject projectFile,
                             @NonNull DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder.extractLibrary(projectFile);

        //create R.java
        System.out.println("Run aidl");
        AndroidBuilder.runAidl(projectFile);
        System.out.println("Run aapt");
        AndroidBuilder.runAapt(context, projectFile);

        //compile java
        System.out.println("Compile Java file");
        int status = CompileHelper.compileJava(context, projectFile, diagnosticCollector);
        System.gc();
        if (status != Main.EXIT_OK) {
            System.out.println("Compile error");
            throw new RuntimeException("Compile time error!");
        }

        //classes to dex
        System.out.println("Convert classes to dex");
        CompileHelper.convertToDexFormat(context, projectFile);

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

    private static void extractLibrary(AndroidProject projectFolder) {
//        File[] files = dirLibs.listFiles();
//        if (files != null) {
//            for (File lib : files) {
//                if (lib.isFile() && lib.getPath().endsWith(".aar")) {
//                }
//            }
//        }
    }

    private static void runAidl(AndroidProject projectFile) throws Exception {
        Log.d(TAG, "runAidl() called");

        // TODO make aidl.so
    }

    private static void runAapt(Context context, AndroidProject projectFile) throws Exception {
        Log.d(TAG, "runAapt() called");

        com.duy.aapt.Aapt aapt = new com.duy.aapt.Aapt();
        StringBuilder command = new StringBuilder("aapt p -f --auto-add-overlay"
                //"-v" + //print info
                + " -M " + projectFile.getXmlManifest().getPath()  //manifest file
                + " -F " + projectFile.getOutResourceFile().getPath()  //output resources.ap_
                + " -I " + FileManager.getClasspathFile(context).getPath()  //include
                + " -A " + projectFile.getAssetsDirs().getPath()  //input assets dir
                + " -S " + projectFile.getResDirs().getPath()  //input resource dir
                + " -J " + projectFile.getClassR().getParent());//parent file of R.java file

        //test
//        File appcompatDir = new File(Environment.getExternalStorageDirectory(), ".JavaNIDE/appcompat-v7-21.0.0");
//        File appcompatRes = new File(appcompatDir, "res");
//        File appcompatAsset = new File(appcompatDir, "assets");
//        command.append(" -S ").append(appcompatRes.getPath());
//        command.append(" -A ").append(appcompatAsset.getPath());

        File dirLibs = projectFile.getDirLibs();
        File[] files = dirLibs.listFiles();
        if (files != null) {
            for (File lib : files) {
                if (lib.isFile() && false) {
                    if (lib.getPath().endsWith(".jar")) {
                        command.append(" -I ").append(lib.getPath());
                    } else if (lib.getPath().endsWith(".aar")) {
                        command.append(" -I ").append(lib.getPath()).append(File.separator).append("res");
                    }
                }
            }
        }
        Log.d(TAG, "runAapt command = " + command);
        int exitCode = aapt.fnExecute(command.toString());
        if (exitCode != 0) {
            throw new Exception("AAPT exit(" + exitCode + ")");
        }

    }

    private static void zipSign(AndroidProject projectFile) throws Exception {
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