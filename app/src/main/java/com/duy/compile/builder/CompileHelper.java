package com.duy.compile.builder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.compile.builder.util.MD5Hash;
import com.duy.compile.dex.DexTool;
import com.duy.compile.java.Jar;
import com.duy.compile.java.Java;
import com.duy.compile.java.Javac;
import com.duy.dex.Dex;
import com.duy.dx.merge.CollisionPolicy;
import com.duy.dx.merge.DexMerger;
import com.duy.ide.DLog;
import com.duy.ide.file.FileManager;
import com.duy.project.file.android.AndroidProject;
import com.duy.project.file.java.JavaProject;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.io.FileFilter;
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
    public static File buildJarAchieve(Context context, JavaProject projectFile,
                                       DiagnosticListener listener) throws IOException {
        int status = compileJava(context, projectFile, listener);
        if (status != Main.EXIT_OK) {
            throw new RuntimeException("Compile time error... Exit code(" + status + ")");
        }
        //now create normal jar file
        Jar.createJarArchive(projectFile);
        return projectFile.getOutJarArchive();
    }

    public static int compileJava(Context context, JavaProject pf) {
        return compileJava(context, pf, null);
    }

    public static int compileJava(Context context, JavaProject projectFile, @Nullable DiagnosticListener listener) {
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

    public static void compileAndRun(Context context, InputStream in, File tempDir, JavaProject projectFile) throws Exception {
        compileJava(context, projectFile);
        convertToDexFormat(context, projectFile);
        executeDex(context, in, projectFile.getDexFile(), tempDir, projectFile.getMainClass().getName());
    }

    public static void dexLibs(@NonNull JavaProject projectFile) throws Exception {
        DLog.d(TAG, "dexLibs() called with: projectFile = [" + projectFile + "]");
        File dirLibs = projectFile.getDirLibs();
        File[] files = dirLibs.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".jar");
            }
        });
        for (File jarLib : files) {
            // compare hash of jar contents to name of dexed version
            String md5 = MD5Hash.getMD5Checksum(jarLib);

            String dexName = jarLib.getName().replace(".jar", "-" + md5 + ".dex");
            File dexLib = new File(projectFile.getDirBuildDexedLibs(), dexName);
            if (dexLib.exists()) {
                continue;
            }
            String[] args = {
                    "--dex",
                    "--verbose",
                    "--no-strict",
                    "--output=" + dexLib.getPath(), jarLib.getPath()};
            DexTool.main(args);
        }
    }

    public static File dexBuildClasses(@NonNull JavaProject projectFile) throws IOException {
        DLog.d(TAG, "dexBuildClasses() called with: projectFile = [" + projectFile + "]");
        String input = projectFile.getDirBuildClasses().getPath();
        FileManager.ensureFileExist(new File(input));
        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + projectFile.getDexFile().getPath(), //output dex file
                input}; //input file
        DexTool.main(args);
        return projectFile.getDexFile();
    }

    public static File dexMerge(@NonNull JavaProject projectFile) throws IOException {
        DLog.d(TAG, "dexMerge() called with: projectFile = [" + projectFile + "]");
        FileManager.ensureFileExist(projectFile.getDexFile());

        if (projectFile.getDirBuildDexedLibs().exists()) {
            File[] files = projectFile.getDirBuildDexedLibs().listFiles();
            if (files != null && files.length > 0) {
                for (File dexedLib : files) {
                    DexMerger dexMerger = new DexMerger(
                            new Dex[]{
                                    new Dex(projectFile.getDexFile()),
                                    new Dex(dexedLib)},
                            CollisionPolicy.FAIL);
                    Dex merged = dexMerger.merge();
                    merged.writeTo(projectFile.getDexFile());
                }
            }
        }
        return projectFile.getDexFile();
    }

    public static void executeDex(Context context, InputStream in, @NonNull File outDex, @NonNull File tempDir, String mainClass)
            throws FileNotFoundException {
        FileManager.ensureFileExist(outDex);

        String[] args = new String[]{"-jar", outDex.getPath(), mainClass};
        Java.run(args, tempDir.getPath(), in);
    }

    public static void convertToDexFormat(Context context, @NonNull JavaProject projectFile) throws Exception {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + projectFile + "]");
        dexLibs(projectFile);
        dexBuildClasses(projectFile);
        dexMerge(projectFile);
    }

    public static File buildApk(Context context, AndroidProject projectFile,
                                DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder builder = new AndroidBuilder(context, projectFile);
        builder.build(BuildType.DEBUG);
        return projectFile.getApkSigned();
    }

    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
    }
}
