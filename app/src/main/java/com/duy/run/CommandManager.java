package com.duy.run;

import android.content.Context;
import android.util.Log;

import com.duy.editor.file.FileManager;
import com.duy.project_files.ClassFile;
import com.duy.project_files.ProjectFile;
import com.spartacusrex.spartacuside.external.dx;
import com.spartacusrex.spartacuside.external.javac;
import com.spartacusrex.spartacuside.session.TermSession;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Created by duy on 18/07/2017.
 */

@SuppressWarnings("ResultOfMethodCallIgnored")
public class CommandManager {
    private static final String TAG = "CommandManager";
    private static final String LIBRARY_BUILDER_FILE = "builder/library_builder.sh";
    private static final String JAVA_BUILDER_FILE = "builder/java_builder.sh";

    public static void buildJarFile(Context context, TermSession termSession, ProjectFile pf) {
        Log.d(TAG, "compileAndRun() called with: filePath = [" + pf + "]");
        File home = context.getFilesDir();
        try {
            FileOutputStream fos = termSession.getTermOut();
            PrintWriter pw = new PrintWriter(fos);

            //set value for variable
            pw.println("PROJECT_PATH=" + pf.getProjectDir());
            pw.println("PROJECT_NAME=" + pf.getProjectName());
            ClassFile mainClass = pf.getMainClass();
            pw.println("MAIN_CLASS=" + mainClass.getName());
            pw.println("PATH_MAIN_CLASS=" + mainClass.getName().replace(".", "/"));
            pw.println("ROOT_PACKAGE=" + mainClass.getRootPackage());


            InputStream stream = context.getAssets().open(LIBRARY_BUILDER_FILE);
            String builder = FileManager.streamToString(stream).toString();
            pw.print(builder);
            pw.flush();

            File temp = new File(home, "tmp");
            if (!temp.exists()) temp.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void compile(Context context, ProjectFile pf, PrintWriter out) {
        try {
            String projectPath = pf.getProjectDir();
            String projectName = pf.getProjectName();
            String mainClass = pf.getMainClass().getName();
            String mainClassPath = pf.getMainClass().getName().replace(".", File.separator);
            String rootPkg = pf.getMainClass().getRootPackage();

            //create build director, delete all file if exists
            File buildDir = new File(projectPath, "build/classes");
            FileManager.deleteFolder(buildDir);
            if (!buildDir.exists()) buildDir.mkdirs();
            System.out.println("buildDir = " + buildDir);


            File sourcepath = new File(projectPath, "src/main/java");
            if (!sourcepath.exists()) sourcepath.mkdirs();
            System.out.println("cp = " + sourcepath);

            String[] args = new String[]{
                    "-sourcepath", sourcepath.getPath(), //classpath
                    "-d", buildDir.getPath(), //output dir
                    pf.getMainClass().getPath(pf) //main class
            };
            System.out.println("args = " + Arrays.toString(args));
            //exec javac command
            int compile = javac.compile(args, out);
            switch (compile) {
                case Main.EXIT_OK: {
                    break;
                }
                case Main.EXIT_CMDERR:
                    return;
                case Main.EXIT_ABNORMAL:
                    return;
                case Main.EXIT_ERROR:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void compileAndRun(Context context, TermSession termSession, ProjectFile pf) {
        Log.d(TAG, "compileAndRun() called with: filePath = [" + pf + "]");
//        File home = context.getFilesDir();
//        try {
//            FileOutputStream fos = termSession.getTermOut();
//            PrintWriter pw = new PrintWriter(fos);
//
//            //set value for variable
//            pw.println("PROJECT_PATH=" + pf.getProjectDir());
//            pw.println("PROJECT_NAME=" + pf.getProjectName());
//            pw.println("MAIN_CLASS=" + pf.getMainClass().getName());
//            pw.println("PATH_MAIN_CLASS=" + pf.getMainClass().getName().replace(".", "/"));
//            pw.println("ROOT_PACKAGE=" + pf.getMainClass().getRootPackage());
//
//            InputStream stream = context.getAssets().open(JAVA_BUILDER_FILE);
//            String builder = FileManager.streamToString(stream).toString();
//            pw.print(builder);
//            pw.flush();
//
//            File temp = new File(home, "tmp");
//            if (!temp.exists()) temp.mkdirs();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        try {
            String projectPath = pf.getProjectDir();
            String projectName = pf.getProjectName();
            String mainClass = pf.getMainClass().getName();
            String mainClassPath = pf.getMainClass().getName().replace(".", File.separator);
            String rootPkg = pf.getMainClass().getRootPackage();

            //create build director, delete all file if exists
            File buildDir = new File(projectPath, "build/classes");
            FileManager.deleteFolder(buildDir);
            if (!buildDir.exists()) buildDir.mkdirs();
            System.out.println("buildDir = " + buildDir);


            File sourcepath = new File(projectPath, "src/main/java");
            if (!sourcepath.exists()) sourcepath.mkdirs();
            System.out.println("cp = " + sourcepath);

            String[] args = new String[]{
                    "-sourcepath", sourcepath.getPath(), //classpath
                    "-d", buildDir.getPath(), //output dir
                    pf.getMainClass().getPath(pf) //main class
            };
            System.out.println("args = " + Arrays.toString(args));
            //exec javac command
            int compile = javac.compile(args);
            switch (compile) {
                case Main.EXIT_OK: {
                    //convert to dex format
                    File binDir = new File(projectPath, "bin");
                    FileManager.deleteFolder(binDir);
                    if (!binDir.exists()) binDir.mkdirs();

                    File outDex = new File(binDir, projectName + ".dex.jar");
                    System.out.println("outDex = " + outDex);

                    File toDex = new File(buildDir, rootPkg);
                    FileManager.ensureFileExist(toDex);

                    args = new String[]{"--dex", "--verbose", "--no-strict",
                            "--output=" + outDex.getPath(), //output dir
                            toDex.getPath()}; //input file
                    dx.main(args);

                    executeDex(termSession, outDex, mainClass);
                    break;
                }
                case Main.EXIT_CMDERR:
                    commandError();
                    return;
                case Main.EXIT_ABNORMAL:
                    return;
                case Main.EXIT_ERROR:
                    compileError();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static void executeDex(TermSession termSession, File outDex, String mainClass) {
        //run via terminal
        FileOutputStream termOut = termSession.getTermOut();
        PrintWriter pw = new PrintWriter(termOut);
        pw.println("java -jar " + outDex.getPath() + " " + mainClass);
        pw.flush();
    }

    private static void commandError() {
    }

    private static void compileError() {
        System.out.println("Compile error");
    }

}
