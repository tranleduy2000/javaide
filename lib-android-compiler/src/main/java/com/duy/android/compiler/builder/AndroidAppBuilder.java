package com.duy.android.compiler.builder;

import android.content.Context;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.process.GradleProcessExecutor;
import com.android.builder.core.LibraryRequest;
import com.android.builder.sdk.SdkInfo;
import com.android.builder.sdk.TargetInfo;
import com.android.ide.common.process.ProcessExecutor;
import com.android.sdklib.repository.FullRevision;
import com.duy.android.compiler.builder.model.BuildType;
import com.duy.android.compiler.builder.task.ATask;
import com.duy.android.compiler.builder.task.CleanTask;
import com.duy.android.compiler.builder.task.android.GenerateConfigTask;
import com.duy.android.compiler.builder.task.android.PackageApplicationTask;
import com.duy.android.compiler.builder.task.android.ProcessAndroidResourceTask;
import com.duy.android.compiler.builder.task.android.SignApkTask;
import com.duy.android.compiler.builder.task.java.CompileJavaTask;
import com.duy.android.compiler.builder.task.java.DexTask;
import com.duy.android.compiler.project.AndroidAppProject;
import com.google.common.collect.ImmutableList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndroidAppBuilder extends BuilderImpl<AndroidAppProject> {
    private static final FullRevision MIN_BUILD_TOOLS_REV = new FullRevision(19, 1, 0);

    @NonNull
    private final String mProjectId;
    @NonNull
    private final ProcessExecutor mProcessExecutor;

    private AndroidAppProject mProject;
    private SdkInfo mSdkInfo;
    private TargetInfo mTargetInfo;
    @NonNull
    private List<LibraryRequest> mLibraryRequests = ImmutableList.of();

    public AndroidAppBuilder(@NonNull Context context,
                             @NonNull AndroidAppProject project) {
        super(context);
        mProject = project;
        mProjectId = mProject.getProjectName();

        mProcessExecutor =      new GradleProcessExecutor(project);
        setDefaultValues();
    }

    private void setDefaultValues() {

    }

    @Override
    public boolean build(BuildType buildType) {

        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build type " + buildType);
        }

        ArrayList<ATask> tasks = new ArrayList<>();

        tasks.add(new CleanTask(this));

        tasks.add(new GenerateConfigTask(this));

        tasks.add(new ProcessAndroidResourceTask(this));

        tasks.add(new CompileJavaTask(this));

        tasks.add(new DexTask(this));

        tasks.add(new PackageApplicationTask(this));

        tasks.add(new SignApkTask(this, buildType));

        return runTasks(tasks);
    }

    /**
     * Sets the SdkInfo and the targetInfo on the builder. This is required to actually
     * build (some of the steps).
     *
     * @param sdkInfo    the SdkInfo
     * @param targetInfo the TargetInfo
     * @see com.android.builder.sdk.SdkLoader
     */
    public void setTargetInfo(
            @NonNull SdkInfo sdkInfo,
            @NonNull TargetInfo targetInfo,
            @NonNull Collection<LibraryRequest> libraryRequests) {
        mSdkInfo = sdkInfo;
        mTargetInfo = targetInfo;

        if (mTargetInfo.getBuildTools().getRevision().compareTo(MIN_BUILD_TOOLS_REV) < 0) {
            throw new IllegalArgumentException(String.format(
                    "The SDK Build Tools revision (%1$s) is too low for project '%2$s'. Minimum required is %3$s",
                    mTargetInfo.getBuildTools().getRevision(), mProjectId, MIN_BUILD_TOOLS_REV));
        }

        mLibraryRequests = ImmutableList.copyOf(libraryRequests);
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

    public AndroidAppProject getProject() {
        return mProject;
    }

    public PrintStream getStdout() {
        return mStdout;
    }

    public TargetInfo getTargetInfo() {
        return mTargetInfo;
    }
}
