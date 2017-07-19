package com.duy.external;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.duy.ide.file.FileManager;
import com.duy.external.dex.Dex;
import com.duy.external.java.Javac;
import com.duy.project_file.ClassFile;
import com.duy.project_file.ProjectFile;
import com.google.common.base.Verify;
import com.spartacusrex.spartacuside.session.TermSession;
import com.sun.tools.javac.main.Main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.tools.DiagnosticListener;

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

    @Nullable
    public static File compile(ProjectFile pf, PrintWriter out) {
        return compile(pf, out, null);
    }

    public static File compile(ProjectFile pf, @Nullable PrintWriter out,
                               @Nullable DiagnosticListener listener) {
        try {
            String projectPath = pf.getProjectDir();
            String projectName = pf.getProjectName();
            String rootPkg = pf.getMainClass().getRootPackage();


            //create build director, delete all file if exists
            File buildDir = new File(projectPath, "build/classes");
            FileManager.deleteFolder(buildDir);
            if (!buildDir.exists()) buildDir.mkdirs();

            File sourcepath = new File(projectPath, "src/main/java");
            if (!sourcepath.exists()) sourcepath.mkdirs();

            String[] args = new String[]{
                    "-verbose",
                    "-sourcepath", sourcepath.getPath(), //classpath
                    "-d", buildDir.getPath(), //output dir
                    pf.getMainClass().getPath(pf) //main class
            };
            int compileStatus;
            compileStatus = out != null ? Javac.compile(args, out, listener) : Javac.compile(args);
            switch (compileStatus) {
                case Main.EXIT_OK: {
                    return convertToDexFormat(projectPath, projectName, buildDir, rootPkg);
                }
            }
            return null;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void compileAndRun(TermSession termSession, ProjectFile pf) {
        try {
            String mainClass = pf.getMainClass().getName();
            //now compile
            File dex = compile(pf, null);
            if (dex != null) executeDex(termSession, dex, mainClass);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static File convertToDexFormat(@NonNull String projectPath,
                                           @NonNull String projectName,
                                           @NonNull File buildDir,
                                           @NonNull String rootPkg) throws FileNotFoundException {
        Verify.verifyNotNull(projectPath);
        Verify.verifyNotNull(projectName);
        Verify.verifyNotNull(buildDir);
        Verify.verifyNotNull(rootPkg);

        //convert to dex format
        File binDir = new File(projectPath, "bin");
        FileManager.deleteFolder(binDir);
        if (!binDir.exists()) binDir.mkdirs();

        File outDex = new File(binDir, projectName + ".dex.jar");
        System.out.println("outDex = " + outDex);

        File toDex = new File(buildDir, rootPkg);
        FileManager.ensureFileExist(toDex);

        String[] args = new String[]{"--dex", "--verbose", "--no-strict",
                "--output=" + outDex.getPath(), //output dir
                toDex.getPath()}; //input file
        Dex.main(args);
        return outDex;
    }

    public static void executeDex(@NonNull TermSession termSession, @NonNull File outDex,
                                  @NonNull String mainClass) {
        Verify.verifyNotNull(termSession);
        Verify.verifyNotNull(outDex);
        Verify.verifyNotNull(mainClass);

        //run via terminal
        FileOutputStream termOut = termSession.getTermOut();
        PrintWriter pw = new PrintWriter(termOut);
        pw.println("java -jar " + outDex.getPath() + " " + mainClass);
        pw.flush();
    }


    public class Action {
        public static final int RUN = 0;
        public static final int RUN_DEX = 1;
        public static final int BUILD_JAR = 2;
    }
}
