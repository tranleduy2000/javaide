package com.duy.compile.builder;

import android.content.Context;

import com.duy.compile.builder.model.BuildType;
import com.duy.compile.task.ABuildTask;
import com.duy.compile.task.java.BuildJarTask;
import com.duy.compile.task.java.CleanTask;
import com.duy.compile.task.java.CompileJavaTask;
import com.duy.compile.task.java.DxTask;
import com.duy.project.file.java.JavaProject;

import java.io.PrintStream;
import java.util.ArrayList;

import javax.tools.DiagnosticListener;

public class JavaProjectBuilder extends BuilderImpl<JavaProject> {


    private JavaProject mProject;

    public JavaProjectBuilder(Context context, JavaProject project, DiagnosticListener listener) {
        super(context, listener);
        mProject = project;

    }

    @Override
    public boolean build(BuildType buildType) {
        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ABuildTask> tasks = new ArrayList<>();
        tasks.add(new CleanTask(this));
        tasks.add(new CompileJavaTask(this, mDiagnosticListener));
        tasks.add(new DxTask(this));
        tasks.add(new BuildJarTask(this));

        for (ABuildTask task : tasks) {
            try {
                stdout("Run " + task.getTaskName() + " task");
                boolean result = task.run();
                if (!result) {
                    stdout(task.getTaskName() + " failed");
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                stdout(task.getTaskName() + " failed");
                return false;
            }
        }
        return true;
    }


    public void stdout(String message) {
        if (mVerbose) {
            mStdout.println(message);
        }
    }

    public void stderr(String stderr) {
        if (mVerbose) {
            mStderr.println(stderr);
        }
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public Context getContext() {
        return mContext;
    }

    public JavaProject getProject() {
        return mProject;
    }

    public PrintStream getStdout() {
        return mStdout;
    }
}
