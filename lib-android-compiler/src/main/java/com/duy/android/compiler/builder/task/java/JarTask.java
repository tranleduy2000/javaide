package com.duy.android.compiler.builder.task.java;

import com.android.annotations.Nullable;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.internal.jar.JarArchive;
import com.duy.android.compiler.builder.internal.jar.JarOptions;
import com.duy.android.compiler.builder.task.Task;
import com.duy.android.compiler.project.JavaProject;

public class JarTask extends Task<JavaProject> {
    @Nullable
    private JarOptions mJarOptions;

    public JarTask(IBuilder<JavaProject> builder) {
        super(builder);
    }

    public JarTask(IBuilder<JavaProject> builder, JarOptions jarOptions) {
        super(builder);
        mJarOptions = jarOptions;
    }

    @Override
    public String getTaskName() {
        return "Create jar archive";
    }

    @Override
    public boolean doFullTaskAction() throws Exception {
        //now create normal jar file
        JarArchive jarArchive = new JarArchive(mBuilder.isVerbose(), mJarOptions);
        jarArchive.createJarArchive(mProject);
        return true;
    }

    public void setJarOptions(JarOptions jarOptions) {
        this.mJarOptions = jarOptions;
    }
}
