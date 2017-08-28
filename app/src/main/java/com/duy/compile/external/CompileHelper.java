package com.duy.compile.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.dex.Dex;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.duy.compile.external.android.AndroidBuilder;
import com.duy.compile.external.android.util.Util;
import com.duy.compile.external.dex.DexTool;
import com.duy.compile.external.java.Jar;
import com.duy.compile.external.java.Java;
import com.duy.compile.external.java.Javac;
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
    public static File buildJarAchieve(JavaProjectFolder projectFile,
                                       DiagnosticListener listener) throws IOException {
        int status = compileJava(projectFile, listener);
        if (status != Main.EXIT_OK) {
            throw new RuntimeException("Compile time error... Exit code(" + status + ")");
        }
        //now create normal jar file
        Jar.createJarArchive(projectFile);
        return projectFile.getOutJarArchive();
    }

    public static int compileJava(JavaProjectFolder pf) {
        return compileJava(pf, null);
    }

    public static int compileJava(JavaProjectFolder projectFile, @Nullable DiagnosticListener listener) {
        try {
            String[] args = new String[]{
                    "-verbose",
                    "-cp", projectFile.getJavaClassPath(),
                    "-sourcepath", projectFile.getDirSrcJava().getPath(), //sourcepath
                    "-d", projectFile.getDirBuildClasses().getPath(), //output dir
                    projectFile.getMainClass().getPath(projectFile) //main class
            };
            Log.d(TAG, "compileJava args = " + Arrays.toString(args));
            int compileStatus;
            compileStatus = Javac.compile(args, listener);
            return compileStatus;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Main.EXIT_ERROR;
    }

    public static void compileAndRun(InputStream in, File tempDir, JavaProjectFolder projectFile) throws Exception {
        compileJava(projectFile);
        convertToDexFormat(projectFile);
        executeDex(in, projectFile.getDexedClassesFile(), tempDir, projectFile.getMainClass().getName());
    }

    public static void dexLibs(@NonNull JavaProjectFolder projectFile) throws Exception {
        File dirLibs = projectFile.dirLibs;
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
        Log.d(TAG, "dexBuildClasses() called with: projectFile = [" + projectFile + "]");
        String input = projectFile.dirBuildClasses.getPath();
        FileManager.ensureFileExist(new File(input));
        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + projectFile.getDexedClassesFile().getPath(), //output dex file
                input}; //input file
        DexTool.main(args);
        return projectFile.getDexedClassesFile();
    }

    public static File dexMerge(@NonNull JavaProjectFolder projectFile) throws IOException {
        Log.d(TAG, "dexMerge() called with: projectFile = [" + projectFile + "]");
        FileManager.ensureFileExist(projectFile.getDexedClassesFile());

        if (projectFile.getDirDexedLibs().exists()) {
            File[] files = projectFile.getDirDexedLibs().listFiles();
            if (files != null && files.length > 0) {
                for (File dexedLib : files) {
                    Dex merged = new DexMerger(
                            new Dex(projectFile.getDexedClassesFile()),
                            new Dex(dexedLib),
                            CollisionPolicy.FAIL).merge();
                    merged.writeTo(projectFile.getDexedClassesFile());
                }
            }
        }
        return projectFile.getDexedClassesFile();
    }

    public static void executeDex(InputStream in, @NonNull File outDex, @NonNull File tempDir, String mainClass)
            throws FileNotFoundException {
        FileManager.ensureFileExist(outDex);

        String[] args = new String[]{"-jar", outDex.getPath(), mainClass};
        Java.run(args, tempDir.getPath(), in);
    }

    public static void convertToDexFormat(@NonNull JavaProjectFolder projectFile) throws Exception {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + projectFile + "]");
        dexLibs(projectFile);
        dexBuildClasses(projectFile);
        dexMerge(projectFile);
    }

    public static File buildApk(AndroidProjectFolder projectFile,
                                DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder.build(projectFile, diagnosticCollector);
        return projectFile.getApkUnaligned();
    }

    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
        public static final int BUILD_JAR = 2;
    }
}
