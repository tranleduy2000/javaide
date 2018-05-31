package com.duy.compile.external.android;

import com.android.annotations.Nullable;
import com.duy.project.file.android.AndroidProject;
import com.sun.tools.javac.util.Context;

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
        return true;
    }

    public void setProject(AndroidProject project) {
        this.mAndroidProject = project;
    }
}
