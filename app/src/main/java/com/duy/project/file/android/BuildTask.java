package com.duy.project.file.android;

import android.util.Log;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.duy.Aapt;

import java.io.File;


public class BuildTask {
    private static final String TAG = "BuildTask";
    private AndroidProjectFile projectFile;

    public void build() {
        projectFile.clean();
        try {
            runAidl();
            runAapt();
            compileJava();
            dexLibs();
            dexClasses();
            dexMerge();
            buildApk();

//            zipSign();
//            zipAlign();
//            publishApk();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runAidl() throws Exception {
        Log.d(TAG, "runAidl() called");

        // TODO make aidl.so
    }

    private void runAapt() throws Exception {
        Log.d(TAG, "runAapt() called");

        Aapt aapt = new Aapt();
        int exitCode = aapt.fnExecute("aapt p -f -v" +
                " -M " + projectFile.xmlManifest.getPath() + //manifest file
                " -F " + projectFile.ap_Resources.getPath() + //
                " -I " + projectFile.jarAndroid.getPath() + //include
                " -A " + projectFile.dirAssets.getPath() + //assets dir
                " -S " + projectFile.dirRes.getPath() + //resource dir
                " -J " + projectFile.dirBuildClasses.getPath()); //out R.java dir

        if (exitCode != 0) {
            throw new Exception("AAPT exit(" + exitCode + ")");
        }

		/*
         * strStatus = "INDEXING RESOURCES"; exitCode = aapt.fnExecute(
		 * "aapt p -m -v -J " + dirGen.getPath() + " -M " + xmlMan.getPath() +
		 * " -S " + dirRes.getPath() + " -I " + jarAndroid.getPath());
		 *
		 * strStatus = "CRUNCH RESOURCES"; exitCode = aapt.fnExecute(
		 * "aapt c -v -S " + dirRes.getPath() + " -C " + dirCrunch.getPath());
		 *
		 * strStatus = "PACKAGE RESOURCES"; exitCode = aapt .fnExecute(
		 * "aapt p -v -S " + dirCrunch.getPath() + " -S " + dirRes.getPath() +
		 * " -f --no-crunch --auto-add-overlay --debug-mode -0 apk -M " +
		 * xmlBinMan.getPath() + " -A " + dirAssets.getPath() + " -I " +
		 * jarAndroid.getPath() + " -F " + ap_Resources.getPath());
		 */
    }

    private void compileJava() throws Exception {
        Log.d(TAG, "compileJava() called");
    }

    private void dexLibs() throws Exception {
//        Log.d(TAG, "dexLibs() called");
//
//        int percent = 20;
//
//        for (File jarLib : projectFile.dirLibs.listFiles()) {
//
//            // skip native libs in sub directories
//            if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
//                continue;
//            }
//
//            // compare hash of jar contents to name of dexed version
//            String md5 = Util.getMD5Checksum(jarLib);
//
//            // check if jar is pre-dexed
//            File dexLib = new File(projectFile.dirDexedLibs, jarLib.getName().replace(".jar", "-" + md5 + ".jar"));
//            System.out.println(dexLib.getName());
//            if (!dexLib.exists()) {
//                com.android.dx.command.dexer.Main
//                        .main(new String[]{"--verbose", "--output=" + dexLib.getPath(), jarLib.getPath()});
//            }
//        }
    }

    private boolean setProgress(int percent) {
        return false;
    }

    private void dexClasses() throws Exception {
        Log.d(TAG, "dexClasses() called");

        com.android.dx.command.dexer.Main
                .main(new String[]{"--verbose", "--output=" + projectFile.dexedClassesFile.getPath(), projectFile.dirBuildClasses.getPath()});
    }

    private void dexMerge() throws Exception {
        Log.d(TAG, "dexMerge() called");

        int percent = 40;

        for (File dexLib : projectFile.dirDexedLibs.listFiles()) {
            Dex merged = new DexMerger(new Dex(projectFile.dexedClassesFile), new Dex(dexLib), CollisionPolicy.FAIL).merge();
            merged.writeTo(projectFile.dexedClassesFile);

            if (setProgress(++percent)) {
                return;
            }
        }
    }

    private void buildApk() throws Exception {
    }

    private void zipSign() throws Exception {
//        if (!appContext.getString(R.string.keystore).contentEquals(projectFile.jksEmbedded.getName())) {
//            // TODO use user defined certificate
//        }
//
//        // use embedded private key
//        String keystorePath = projectFile.jksEmbedded.getPath();
//        char[] keystorePw = appContext.getString(R.string.keystorePw).toCharArray();
//        String certAlias = appContext.getString(R.string.certAlias);
//        char[] certPw = appContext.getString(R.string.certPw).toCharArray();
//        String signatureAlgorithm = appContext.getString(R.string.signatureAlgorithm);
//
//        boolean useKeyStore = false;
//        if (useKeyStore) {
//            kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();
//            zipsigner.addProgressListener(new SignProgress());
//            kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw, certAlias,
//                    certPw, signatureAlgorithm, projectFile.apkUnsigned.getPath(), projectFile.apkUnaligned.getPath());
//        }
    }

    private void zipAlign() throws Exception {
//         TODO make zipalign.so
    }

    private void publishApk() throws Exception {
//        if (projectFile.apkRedistributable.exists()) {
//            projectFile.apkRedistributable.delete();
//        }
//        Util.copy(projectFile.apkUnaligned, new FileOutputStream(projectFile.apkRedistributable));
//
//        projectFile.apkRedistributable.setReadable(true, false);
//
//        if (setProgress(100)) {
//            return;
//        }
    }

    public void run() {


    }

    class CompileProgress extends org.eclipse.jdt.core.compiler.CompilationProgress {

        @Override
        public void begin(int remainingWork) {
        }

        @Override
        public void done() {
        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setTaskName(String name) {
        }

        @Override
        public void worked(int workIncrement, int remainingWork) {
        }

    }

    class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

        public void onProgress(kellinwood.security.zipsigner.ProgressEvent event) {
        }

    }
}