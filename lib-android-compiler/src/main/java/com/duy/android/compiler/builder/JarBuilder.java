package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.annotations.NonNull;
import com.duy.android.compiler.builder.internal.jar.JarOptions;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.builder.task.java.JarTask;
import com.duy.android.compiler.project.JavaProject;

import java.util.ArrayList;

public class JarBuilder extends BuilderImpl<JavaProject> {

    private final JarOptions mJarOptions;
    private JavaProject mProject;

    public JarBuilder(@NonNull Context context,
                      @NonNull JavaProject project,
                      @NonNull JarOptions jarOptions) {
        super(context);
        mProject = project;
        mJarOptions = jarOptions;
    }

    @Override
    public JavaProject getProject() {
        return mProject;
    }

    @Override
    public boolean build(BuildType buildType) {
        if (mVerbose) {
            mStdout.println("Starting build jar archive");
        }
        ArrayList<Task> tasks = new ArrayList<>();
        tasks.add(new CleanTask(this));
        tasks.add(new CompileJavaTask(this));
        tasks.add(new JarTask(this, mJarOptions));
        return runTasks(tasks);
    }

}
