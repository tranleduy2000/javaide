package com.duy.compile.external;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.compile.external.android.AndroidBuilder;
import com.duy.compile.external.android.util.Util;
import com.duy.compile.external.dex.DexTool;
import com.duy.compile.external.java.Jar;
import com.duy.compile.external.java.Java;
import com.duy.compile.external.java.Javac;
import com.duy.dex.Dex;
import com.duy.dx.merge.CollisionPolicy;
import com.duy.dx.merge.DexMerger;
import com.duy.ide.DLog;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProjectFolder;
import com.duy.project.file.java.JavaProjectFolder;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;

/**
 * Created by duy on 18/07/2017.
 */

public class CompileHelper {
    private static final String TAG = "CommandManager";

    @Nullable
    public static File buildJarAchieve(Context context, JavaProjectFolder projectFile,
                                       DiagnosticListener listener) throws IOException {
        int status = compileJava(context, projectFile, listener);
        if (status != Main.EXIT_OK) {
            throw new RuntimeException("Compile time error... Exit code(" + status + ")");
        }
        //now create normal jar file
        Jar.createJarArchive(projectFile);
        return projectFile.getOutJarArchive();
    }

    public static int compileJava(Context context, JavaProjectFolder pf) {
        return compileJava(context, pf, null);
    }

    public static int compileJava(Context context, JavaProjectFolder projectFile, @Nullable DiagnosticListener listener) {
        try {

            String[] args = new String[]{
                    "-verbose",
                    "-cp", projectFile.getJavaClassPath(context),
                    "-sourcepath", projectFile.getSourcePath(), //sourcepath
                    "-d", projectFile.getDirBuildClasses().getPath(), //output dir
                    projectFile.getMainClass().getPath(projectFile) //main class
            };
            DLog.d(TAG, "compileJava args = " + Arrays.toString(args));
            int compileStatus;
            compileStatus = Javac.compile(args, listener);
            return compileStatus;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Main.EXIT_ERROR;
    }

    public static void compileAndRun(Context context, InputStream in, File tempDir, JavaProjectFolder projectFile) throws Exception {
        compileJava(context, projectFile);
        convertToDexFormat(context, projectFile);
        executeDex(context, in, projectFile.getDexedClassesFile(), tempDir, projectFile.getMainClass().getName());
    }

    public static void dexLibs(@NonNull JavaProjectFolder projectFile) throws Exception {
        DLog.d(TAG, "dexLibs() called with: projectFile = [" + projectFile + "]");
        File dirLibs = projectFile.getDirLibs();
        if (dirLibs.exists()) {
            File[] files = dirLibs.listFiles();
            if (files != null && files.length > 0) {
                for (File jarLib : files) {
                    // skip native libs in sub directories
                    if (!jarLib.isFile() || !jarLib.getName().endsWith(".jar")) {
                        continue;
                    }

                    // compare hash of jar contents to name of dexed version
                    String md5 = Util.getMD5Checksum(jarLib);

                    File dexLib = new File(projectFile.getDirDexedLibs(), jarLib.getName().replace(".jar", "-" + md5 + ".dex"));
                    if (dexLib.exists()) {
                        continue;
                    }
                    String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                            "--output=" + dexLib.getPath(), jarLib.getPath()};
                    DexTool.main(args);
                }
            }
        }
    }

    public static File dexBuildClasses(@NonNull JavaProjectFolder projectFile) throws IOException {
        DLog.d(TAG, "dexBuildClasses() called with: projectFile = [" + projectFile + "]");
        String input = projectFile.getDirBuildClasses().getPath();
        FileManager.ensureFileExist(new File(input));
        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + projectFile.getDexedClassesFile().getPath(), //output dex file
                input}; //input file
        DexTool.main(args);
        return projectFile.getDexedClassesFile();
    }

    public static File dexMerge(@NonNull JavaProjectFolder projectFile) throws IOException {
        DLog.d(TAG, "dexMerge() called with: projectFile = [" + projectFile + "]");
        FileManager.ensureFileExist(projectFile.getDexedClassesFile());

        if (projectFile.getDirDexedLibs().exists()) {
            File[] files = projectFile.getDirDexedLibs().listFiles();
            if (files != null && files.length > 0) {
                for (File dexedLib : files) {
                    DexMerger dexMerger = new DexMerger(
                            new Dex[]{
                                    new Dex(projectFile.getDexedClassesFile()),
                                    new Dex(dexedLib)},
                            CollisionPolicy.FAIL);
                    Dex merged = dexMerger.merge();
                    merged.writeTo(projectFile.getDexedClassesFile());
                }
            }
        }
        return projectFile.getDexedClassesFile();
    }

    public static void executeDex(Context context, InputStream in, @NonNull File outDex, @NonNull File tempDir, String mainClass)
            throws FileNotFoundException {
        FileManager.ensureFileExist(outDex);

        String[] args = new String[]{"-jar", outDex.getPath(), mainClass};
        Java.run(args, tempDir.getPath(), in);
    }

    public static void convertToDexFormat(Context context, @NonNull JavaProjectFolder projectFile) throws Exception {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + projectFile + "]");
        dexLibs(projectFile);
        dexBuildClasses(projectFile);
        dexMerge(projectFile);
    }

    public static File buildApk(Context context, AndroidProjectFolder projectFile,
                                DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder.build(context, projectFile, diagnosticCollector);
        return projectFile.getApkUnaligned();
    }

    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
        public static final int BUILD_JAR = 2;
    }
}
