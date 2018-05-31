package com.duy.compile.external.android;

import android.content.Context;

import com.android.annotations.Nullable;
import com.duy.project.file.android.AndroidProject;

import java.io.PrintStream;

public class AndroidBuilder2 implements IAndroidBuilder {
    private Context mContext;
    private boolean mVerbose;
    private PrintStream mStdout;
    private PrintStream mStderr;
    @Nullable
    private KeyStore mKeyStore;

    private AndroidProject mAndroidProject;

    public AndroidBuilder2(Context context) {
        mContext = context;
        mStdout = new PrintStream(System.out);
        mStderr = new PrintStream(System.err);
        mVerbose = true;
    }

    @Override
    public void build(AndroidProject projectFolder, BuildType buildType) {
        setProject(projectFolder);

        if (mVerbose) {
            mStdout.println("Starting build android project");
            mStdout.println("Build " + buildType);
        }

        if (cleanupBuild()) {
            mStdout.println("Aborted");
            return;
        }

        if (!runAAPT()) {
            mStdout.println("Aborted");
            return;
        }

        if (!compileJavaSource()) {
            mStdout.println("Aborted");
            return;
        }

    }

    private boolean compileJavaSource() {
        return true;
    }


    private boolean cleanupBuild() {
        if (mVerbose) {
            mStdout.print("Cleanup project");
        }
        return true;
    }

    private boolean runAAPT() {
        try {
            Aapt aapt = new Aapt(this, mAndroidProject);
            return aapt.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public void setProject(AndroidProject project) {
        this.mAndroidProject = project;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public Context getContext() {
        return mContext;
    }

}
