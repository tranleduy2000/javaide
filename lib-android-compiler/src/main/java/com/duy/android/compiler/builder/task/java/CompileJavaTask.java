package com.duy.android.compiler.builder.task.java;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.task.ABuildTask;
import com.duy.android.compiler.builder.util.Argument;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.utils.DLog;

import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import java.io.PrintWriter;

import javax.tools.DiagnosticListener;

public class CompileJavaTask extends ABuildTask<JavaProject> {

    //    private boolean runOldJavaCompiler() {
//        ArrayList<File> javaLibraries = project.getJavaLibraries(context);
//        StringBuilder classpath = new StringBuilder(".");
//        for (File javaLibrary : javaLibraries) {
//            classpath.append(File.pathSeparator).append(javaLibrary.getParent());
//        }
//        String[] args = new String[]{
//                "-verbose",
//                "-cp", classpath.toString() + File.pathSeparator + project.getBootClassPath(context),
//                "-sourcepath", project.getSourcePath(), //sourcepath
//                "-d", project.getDirBuildClasses().getPath(), //output dir
//                project.getMainClass().getPath(project) //main class
//        };
//        System.out.println("args = " + Arrays.toString(args));
//        int resultCode = Javac.compile(args, listener);
//        return resultCode == 0;
//    }
    private static final String TAG = "CompileJavaTask";
    private DiagnosticListener listener;

    public CompileJavaTask(IBuilder<? extends JavaProject> builder, DiagnosticListener listener) {
        super(builder);
        this.listener = listener;
    }

    @Override
    public String getTaskName() {
        return "Compile java source";
    }

    public boolean run() {
//        return runOldJavaCompiler();
        return runEcj();
    }

    private boolean runEcj() {
        CompilationProgress compilationProgress = new CompilationProgress() {
            @Override
            public void begin(int remainingWork) {
                if (DLog.DEBUG)
                    DLog.d(TAG, "begin() called with: remainingWork = [" + remainingWork + "]");

            }

            @Override
            public void done() {
                if (DLog.DEBUG) DLog.d(TAG, "done() called");

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public void setTaskName(String name) {
                if (DLog.DEBUG) DLog.d(TAG, "setTaskName() called with: name = [" + name + "]");

            }

            @Override
            public void worked(int workIncrement, int remainingWork) {
                if (DLog.DEBUG)
                    DLog.d(TAG, "worked() called with: workIncrement = [" + workIncrement + "], remainingWork = [" + remainingWork + "]");

            }
        };
        PrintWriter outWriter = new PrintWriter(builder.getStdout());
        PrintWriter errWriter = new PrintWriter(builder.getStderr());
        org.eclipse.jdt.internal.compiler.batch.Main main =
                new org.eclipse.jdt.internal.compiler.batch.Main(outWriter, errWriter,
                        false, null, compilationProgress);

        Argument argument = new Argument();
        argument.add(builder.isVerbose() ? "-verbose" : "-warn:");
        argument.add("-bootclasspath", project.getBootClassPath(context));
        argument.add("-classpath", project.getClasspath());
        argument.add("-sourcepath", project.getSourcePath());
        argument.add("-" + CompilerOptions.VERSION_1_7); //host
        argument.add("-target", CompilerOptions.VERSION_1_7); //target
        //                "-proc:none", // Disable annotation processors...
        argument.add("-d", project.getDirBuildClasses().getAbsolutePath()); // The location of the output folder
        argument.add(project.getMainClass().getPath(project));// The location of the main activity
        System.err.println("argument = " + argument);
        return main.compile(argument.toArray());
    }
}
