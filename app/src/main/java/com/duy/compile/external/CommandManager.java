package com.duy.compile.external;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.dx.merge.CollisionPolicy;
import com.android.dx.merge.DexMerger;
import com.duy.compile.external.dex.Dex;
import com.duy.compile.external.java.Jar;
import com.duy.compile.external.java.Java;
import com.duy.compile.external.java.Javac;
import com.duy.project.file.java.JavaProjectFile;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.tools.DiagnosticListener;

/**
 * Created by duy on 18/07/2017.
 */

public class CommandManager {
    private static final String TAG = "CommandManager";
    private static final String LIBRARY_BUILDER_FILE = "builder/library_builder.sh";
    private static final String JAVA_BUILDER_FILE = "builder/java_builder.sh";

    @Nullable
    public static File buildJarAchieve(JavaProjectFile projectFile, PrintWriter out, DiagnosticListener listener) {
        try {
            int status = compileJava(projectFile, out, listener);
            if (status == Main.EXIT_ERROR) {
                return null; //can not compile
            }
            String projectName = projectFile.getProjectName();
            String rootPkg = projectFile.getMainClass().getRootPackage();

            File jarFolder = new File(projectFile.dirBuildClasses, rootPkg);
            File outJar = new File(projectFile.dirOutputJar, projectName + ".jar");
            if (outJar.exists()) outJar.delete();
            outJar.createNewFile();
            String[] args = new String[]{"-v", outJar.getPath(), jarFolder.getPath()};
            //now create normal jar file
            Jar.main(args);
            //ok, create dex file
            dexBuildClasses(projectFile);
            return outJar;
        } catch (Exception e) {
            e.printStackTrace();
            //compile time error
            e.printStackTrace(out);
        }
        return null;
    }

    public static int compileJava(JavaProjectFile pf, PrintWriter out) {
        return compileJava(pf, out, null);
    }

    public static int compileJava(JavaProjectFile projectFile, @Nullable PrintWriter out,
                                  @Nullable DiagnosticListener listener) {
        try {
            String[] args = new String[]{
                    "-verbose",
                    "-classpath", projectFile.getClassPath(),
                    "-sourcepath", projectFile.dirJava.getPath(), //sourcepath
                    "-d", projectFile.dirBuildClasses.getPath(), //output dir
                    projectFile.getMainClass().getPath(projectFile) //main class
            };
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
                                     File tempDir, JavaProjectFile projectFile) {
        try {
            compileJava(projectFile, new PrintWriter(out));
            convertToDexFormat(projectFile);
            executeDex(out, in, err, projectFile.dexedClassesFile, tempDir, projectFile.getMainClass().getName());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void dexLibs(@NonNull JavaProjectFile projectFile, boolean ignoreExist) {
        File dirLibs = projectFile.dirLibs;
        File[] files = dirLibs.listFiles();
        if (files != null) {
            for (File file : files) {
                File outLib = new File(projectFile.dirDexedLibs, file.getName().replace(".jar", ".dex"));
                if (outLib.exists() && !ignoreExist) {
                    continue;
                } else {
                    if (outLib.exists()) outLib.delete();
                }
                String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                        "--output=" + outLib.getPath(), file.getPath()};
                Dex.main(args);
            }
        }
    }

    public static File dexBuildClasses(@NonNull JavaProjectFile projectFile) throws FileNotFoundException {
        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + projectFile.dexedClassesFile.getPath(), //output dex file
                projectFile.dirBuildClasses.getPath()}; //input file
        Dex.main(args);
        return projectFile.dexedClassesFile;
    }

    public static File dexMerge(@NonNull JavaProjectFile projectFile) throws IOException {
        File[] files = projectFile.dirDexedLibs.listFiles();
        if (files != null) {
            for (File dexLib : files) {
                com.android.dex.Dex merged = new DexMerger(
                        new com.android.dex.Dex(projectFile.dexedClassesFile),
                        new com.android.dex.Dex(dexLib),
                        CollisionPolicy.FAIL).merge();
                merged.writeTo(projectFile.dexedClassesFile);
            }
        }
        return projectFile.dexedClassesFile;
    }

    public static void executeDex(@NonNull PrintStream out, InputStream in, PrintStream err,
                                  @NonNull File outDex, @NonNull File tempDir, String mainClass) {

        String[] args = new String[]{"-jar", outDex.getPath(),};
        Java.run(args, tempDir.getPath(), out, in, err);
    }

    public static void convertToDexFormat(@NonNull JavaProjectFile projectFile) throws IOException {
        dexBuildClasses(projectFile);
        dexLibs(projectFile, true);
        dexMerge(projectFile);
    }


    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
        public static final int BUILD_JAR = 2;
    }
}
