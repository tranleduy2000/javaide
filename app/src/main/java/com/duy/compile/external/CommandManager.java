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
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;

/**
 * Created by duy on 18/07/2017.
 */

public class CommandManager {
    private static final String TAG = "CommandManager";

    @Nullable
    public static File buildJarAchieve(JavaProjectFolder projectFile, OutputStream out,
                                       DiagnosticListener listener) {
        File jarFile = null;

        PrintStream stdout = System.out, stderr = System.err;
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));

        try {
            int status = compileJava(projectFile, new PrintStream(out), listener);
            if (status != Main.EXIT_OK) {
                throw new RuntimeException("Compile time error... Exit code(" + status + ")");
            }
            //now create normal jar file
            Jar.createJarArchive(projectFile);
            jarFile = projectFile.getOutJarArchive();
        } catch (Exception e) {
            Log.e(TAG, "buildJarAchieve: ", e);
            //compile time error
            e.printStackTrace(new PrintStream(out));
        }
        System.setOut(stdout);
        System.setErr(stderr);
        return jarFile;
    }

    public static int compileJava(JavaProjectFolder pf, PrintStream out) {
        return compileJava(pf, out, null);
    }

    public static int compileJava(JavaProjectFolder projectFile, @Nullable PrintStream out,
                                  @Nullable DiagnosticListener listener) {
        try {
            String[] args = new String[]{
                    "-verbose",
//                    "-bootclasspath", projectFile.getJavaBootClassPath(),
                    "-cp", projectFile.getJavaClassPath(),
                    "-sourcepath", projectFile.getDirSrcJava().getPath(), //sourcepath
                    "-d", projectFile.getDirBuildClasses().getPath(), //output dir
                    projectFile.getMainClass().getPath(projectFile) //main class
            };
            Log.d(TAG, "compileJava args = " + Arrays.toString(args));
            int compileStatus;
            compileStatus = out != null ? Javac.compile(args, out, listener) : Javac.compile(args);
            return compileStatus;
        } catch (Throwable e) {
            e.printStackTrace();
            e.printStackTrace(out);
        }
        return Main.EXIT_ERROR;
    }

    public static void compileAndRun(PrintStream out, InputStream in, PrintStream err,
                                     File tempDir, JavaProjectFolder projectFile) throws Exception {
        compileJava(projectFile, out);
        convertToDexFormat(projectFile, out);
        executeDex(out, in, err, projectFile.getDexedClassesFile(), tempDir, projectFile.getMainClass().getName());
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

    public static void executeDex(@NonNull PrintStream out, InputStream in, PrintStream err,
                                  @NonNull File outDex, @NonNull File tempDir, String mainClass)
            throws FileNotFoundException {
        Log.d(TAG, "executeDex() called with: out = [" + out + "], in = [" + in + "], err " +
                "= [" + err + "], outDex = [" + outDex + "], tempDir = [" + tempDir + "], mainClass = [" + mainClass + "]");

        FileManager.ensureFileExist(outDex);

        String[] args = new String[]{"-jar", outDex.getPath(), mainClass};
        Java.run(args, tempDir.getPath(), out, in, err);
    }

    public static void convertToDexFormat(@NonNull JavaProjectFolder projectFile, PrintStream out) throws Exception {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + projectFile + "]");
        PrintStream stdout = System.out, stderr = System.err;
        System.setOut(out);
        System.setErr(out);

        dexLibs(projectFile);
        dexBuildClasses(projectFile);
        dexMerge(projectFile);

        System.setOut(stdout);
        System.setErr(stderr);
    }

    public static File buildApk(AndroidProjectFolder projectFile,
                                OutputStream out,
                                DiagnosticCollector diagnosticCollector) throws Exception {
        AndroidBuilder.build(projectFile, out, diagnosticCollector);
        return projectFile.getApkUnaligned();
    }

    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
        public static final int BUILD_JAR = 2;
    }
}
