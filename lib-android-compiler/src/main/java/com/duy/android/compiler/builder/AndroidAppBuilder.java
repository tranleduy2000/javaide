package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.annotations.NonNull;
import com.android.ide.common.process.ProcessExecutor;
import com.duy.android.compiler.builder.internal.process.RuntimeProcessExecutor;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.builder.task.android.CompileAidlTask;
import com.duy.android.compiler.builder.task.android.GenerateBuildConfigTask;
import com.duy.android.compiler.builder.task.android.MergeManifestTask;
import com.duy.android.compiler.builder.task.android.PackageApkTask;
import com.duy.android.compiler.builder.task.android.ProcessAndroidResourceTask;
import com.duy.android.compiler.builder.task.android.SignApkTask;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.project.AndroidAppProject;

import java.util.ArrayList;

/**
 * Android build will execute all task
 * {@link GenerateBuildConfigTask}
 * {@link MergeManifestTask}
 * {@link ProcessAndroidResourceTask}
 * {@link CompileAidlTask}
 * {@link CompileJavaTask}
 * {@link PackageApkTask}
 */
public class AndroidAppBuilder extends BuilderImpl<AndroidAppProject> {

    @NonNull
    private final ProcessExecutor mProcessExecutor;
    private AndroidAppProject mProject;

    public AndroidAppBuilder(@NonNull Context context,
                             @NonNull AndroidAppProject project) {
        super(context);
        mProject = project;
        mProcessExecutor = new RuntimeProcessExecutor();
    }

    @Override
    public AndroidAppProject getProject() {
        return mProject;
    }

    @Override
    public boolean build(BuildType buildType) {
        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }
        ArrayList<Task> tasks = getTasks(buildType);
        return runTasks(tasks);
    }

    private ArrayList<Task> getTasks(BuildType buildType) {
        ArrayList<Task> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new GenerateBuildConfigTask(this));

        tasks.add(new ProcessAndroidResourceTask(this));
//        tasks.add(new ProcessAndroidResourceTask2(this));

        tasks.add(new CompileJavaTask(this));

        tasks.add(new DexTask(this));

        tasks.add(new PackageApkTask(this));

        tasks.add(new SignApkTask(this, buildType));
        return tasks;
    }


}
