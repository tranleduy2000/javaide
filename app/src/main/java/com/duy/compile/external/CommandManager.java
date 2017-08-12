package com.duy.compile.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.dx.io.DexBuffer;
import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.duy.compile.external.android.AndroidBuilder;
import com.duy.compile.external.dex.Dex;
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
import java.io.PrintWriter;
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
        Log.d(TAG, "buildJarAchieve() called with: projectFile = [" + projectFile + "], out = ["
                + out + "], listener = [" + listener + "]");
        File jarFile = null;
        PrintStream stdout, stderr;
        stdout = System.out;
        stderr = System.err;
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(out));
        try {
            int status = compileJava(projectFile, new PrintWriter(out), listener);
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

    public static int compileJava(JavaProjectFolder pf, PrintWriter out) {
        return compileJava(pf, out, null);
    }

    public static int compileJava(JavaProjectFolder projectFile, @Nullable PrintWriter out,
                                  @Nullable DiagnosticListener listener) {
        Log.d(TAG, "compileJava() called with: projectFile = [" + projectFile + "], out = [" + out + "], listener = [" + listener + "]");

        try {
            String[] args = new String[]{
                    "-verbose",
                    "-bootclasspath", projectFile.getJavaBootClassPath(),
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
                                     File tempDir, JavaProjectFolder projectFile) throws IOException {
        Log.d(TAG, "compileAndRun() called with: out = [" + out + "], in = [" + in + "], err = [" +
                err + "], tempDir = [" + tempDir + "], projectFile = [" + projectFile + "]");

        compileJava(projectFile, new PrintWriter(out));
        convertToDexFormat(projectFile);
        executeDex(out, in, err, projectFile.getDexedClassesFile(), tempDir, projectFile.getMainClass().getName());
    }

    public static void dexLibs(@NonNull JavaProjectFolder projectFile, boolean ignoreExist) throws IOException {
        Log.d(TAG, "dexLibs() called with: projectFile = [" + projectFile + "], ignoreExist = [" + ignoreExist + "]");

        File dirLibs = projectFile.dirLibs;
        if (dirLibs.exists()) {
            File[] files = dirLibs.listFiles();
            if (files != null && files.length > 0) {
                for (File lib : files) {
                    File outLib = new File(projectFile.getDirDexedLibs(), lib.getName().replace(".jar", ".dex"));
                    Log.d(TAG, "dexLibs outLib = " + lib);
                    if (lib.exists() && ignoreExist) {
                        continue;
                    }
                    if (!outLib.exists()) outLib.createNewFile();
                    String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                            "--output=" + outLib.getPath(), lib.getPath()};
                    Dex.main(args);
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
        Dex.main(args);
        return projectFile.getDexedClassesFile();
    }

    public static File dexMerge(@NonNull JavaProjectFolder projectFile) throws IOException {
        Log.d(TAG, "dexMerge() called with: projectFile = [" + projectFile + "]");
        FileManager.ensureFileExist(projectFile.getDexedClassesFile());

        if (projectFile.getDirDexedLibs().exists()) {
            File[] files = projectFile.getDirDexedLibs().listFiles();
            if (files != null && files.length > 0) {
                for (File dexedLib : files) {
                    DexBuffer merged = new DexMerger(
                            new DexBuffer(projectFile.getDexedClassesFile()),
                            new DexBuffer(dexedLib),
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

    public static void convertToDexFormat(@NonNull JavaProjectFolder projectFile) throws IOException {
        Log.d(TAG, "convertToDexFormat() called with: projectFile = [" + projectFile + "]");
        dexLibs(projectFile, false);
        dexBuildClasses(projectFile);
        dexMerge(projectFile);
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
