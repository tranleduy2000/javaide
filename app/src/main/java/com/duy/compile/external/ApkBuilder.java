package com.duy.compile.external;

import android.util.Log;

import com.duy.Aapt;
import com.duy.project.file.android.AndroidProjectFile;
import com.spartacusrex.spartacuside.external.apkbuilder;


public class ApkBuilder {
    private static final String TAG = "BuildTask";

    private static void buildApk(AndroidProjectFile projectFile) throws Exception {
//        String args = "./dist/demo_android.apk -v -u -z ./build/resources.res -f ./build/demo_android.dex";
        String[] args = {
                projectFile.getApkUnsigned().getPath(),
                "-v", "-u", "-z", projectFile.getResourceFile().getPath(),
                "-f", projectFile.dexedClassesFile.getPath()
        };
        apkbuilder.main(args);
    }

    public static void build(AndroidProjectFile projectFile) {
        projectFile.clean();
        try {
            ApkBuilder.runAidl(projectFile);
            ApkBuilder.runAapt(projectFile);
            CommandManager.compileJava(projectFile, null);
            System.gc();
            CommandManager.dexLibs(projectFile, true);
            CommandManager.dexBuildClasses(projectFile);
            CommandManager.dexMerge(projectFile);
            ApkBuilder.buildApk(projectFile);
            ApkBuilder.zipSign(projectFile);
//            zipAlign();
//            publishApk();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void runAidl(AndroidProjectFile projectFile) throws Exception {
        Log.d(TAG, "runAidl() called");

        // TODO make aidl.so
    }

    private static void runAapt(AndroidProjectFile projectFile) throws Exception {
        Log.d(TAG, "runAapt() called");

        Aapt aapt = new Aapt();
        int exitCode = aapt.fnExecute("aapt p -f -v" +
                " -M " + projectFile.xmlManifest.getPath() + //manifest file
                " -F " + projectFile.getResourceFile().getPath() + //
                " -I " + projectFile.classpathFile.getPath() + //include
                " -A " + projectFile.getDirAssets().getPath() + //assets dir
                " -S " + projectFile.getDirRes().getPath() + //resource dir
                " -J " + projectFile.getClassR().getParent()); //out R.java dir

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

    private static void zipSign(AndroidProjectFile projectFile) throws Exception {
//        if (!appContext.getString(R.string.keystore).contentEquals(projectFile.jksEmbedded.getName())) {
//             TODO use user defined certificate
//        }

        // use embedded private key
        String keystorePath = projectFile.getKeyStore().getPath();
        char[] keystorePw = "1234567".toCharArray();
        String certAlias = "android";
        char[] certPw = "1234567".toCharArray();
        String signatureAlgorithm = "SHA1withRSA";

        boolean useKeyStore = false;
        if (useKeyStore) {
            kellinwood.security.zipsigner.ZipSigner zipsigner = new kellinwood.security.zipsigner.ZipSigner();
            zipsigner.addProgressListener(new SignProgress());
            kellinwood.security.zipsigner.optional.CustomKeySigner.signZip(zipsigner, keystorePath, keystorePw, certAlias,
                    certPw, signatureAlgorithm,
                    projectFile.getApkUnsigned().getPath(),
                    projectFile.getApkUnaligned().getPath());
        }
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

    static class SignProgress implements kellinwood.security.zipsigner.ProgressListener {

        public void onProgress(kellinwood.security.zipsigner.ProgressEvent event) {
        }

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
}