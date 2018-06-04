package com.duy.android.compiler.builder.task.java;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.internal.CompileOptions;
import com.duy.android.compiler.builder.internal.JavaVersion;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.builder.util.Argument;
import com.duy.android.compiler.project.JavaProject;
import com.duy.javacompiler.R;

import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class CompileJavaTask extends Task<JavaProject> {

    private static final String TAG = "CompileJavaTask";
    private CompileOptions mCompileOptions;

    public CompileJavaTask(IBuilder<? extends JavaProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Compile java source";
    }

    public boolean run() {
        loadCompilerOptions();
        return runEcj();
    }

    private void loadCompilerOptions() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        mCompileOptions = new CompileOptions();

        String sourceCompatibility = pref.getString(context.getString(R.string.key_pref_source_compatibility), null);
        if (sourceCompatibility == null || sourceCompatibility.isEmpty()) {
            sourceCompatibility = JavaVersion.VERSION_1_7.toString();
        }
        mCompileOptions.setSourceCompatibility(sourceCompatibility);


        String targetCompatibility = pref.getString(context.getString(R.string.key_pref_target_compatibility), null);
        if (targetCompatibility == null || targetCompatibility.isEmpty()) {
            targetCompatibility = JavaVersion.VERSION_1_7.toString();
        }
        mCompileOptions.setTargetCompatibility(targetCompatibility);

        String encoding = pref.getString(context.getString(R.string.key_pref_source_encoding), null);
        if (encoding == null || encoding.isEmpty()) {
            encoding = Charset.forName("UTF-8").toString();
        } else {
            try {
                Charset charset = Charset.forName(encoding);
                encoding = charset.toString();
            } catch (Exception e) {
                encoding = Charset.forName("UTF-8").toString();
            }
        }
        mCompileOptions.setEncoding(encoding);
    }


    private boolean runEcj() {
        mBuilder.stdout(TAG + ": Compile java with javac");
        PrintWriter outWriter = new PrintWriter(mBuilder.getStdout());
        PrintWriter errWriter = new PrintWriter(mBuilder.getStderr());
        org.eclipse.jdt.internal.compiler.batch.Main main =
                new org.eclipse.jdt.internal.compiler.batch.Main(outWriter, errWriter,
                        false, null, null);

        Argument argument = new Argument();
        argument.add(mBuilder.isVerbose() ? "-verbose" : "-warn:");
        argument.add("-bootclasspath", mBuilder.getBootClassPath());
        argument.add("-classpath", project.getClasspath());
        argument.add("-sourcepath", project.getSourcePath());
        argument.add("-" + mCompileOptions.getSourceCompatibility().toString()); //host
        argument.add("-target", mCompileOptions.getTargetCompatibility().toString()); //target
        argument.add("-proc:none"); // Disable annotation processors...
        argument.add("-d", project.getDirBuildClasses().getAbsolutePath()); // The location of the output folder

        String[] sourceFiles = getAllSourceFiles(project);
        argument.add(sourceFiles);

        Main.Logger logger = main.logger;
        //default output
        logger.setEmacs();

        System.out.println(TAG + ": Compiler arguments " + argument);
        return main.compile(argument.toArray());
    }

    private String[] getAllSourceFiles(JavaProject project) {
        ArrayList<String> javaFiles = new ArrayList<>();
        String[] sourcePaths = project.getSourcePath().split(File.pathSeparator);
        for (String sourcePath : sourcePaths) {
            getAllSourceFiles(javaFiles, new File(sourcePath));
        }

        System.out.println("source size: " + javaFiles.size());
        String[] sources = new String[javaFiles.size()];
        return javaFiles.toArray(sources);
    }

    private void getAllSourceFiles(ArrayList<String> toAdd, File parent) {
        if (!parent.exists()) {
            return;
        }
        for (File child : parent.listFiles()) {
            if (child.isDirectory()) {
                getAllSourceFiles(toAdd, child);
            } else if (child.exists() && child.isFile()) {
                if (child.getName().endsWith(".java")) {
                    toAdd.add(child.getAbsolutePath());
                }
            }
        }
    }

}
