package com.duy.compile.task.java;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.java.JarArchive;
import com.duy.compile.task.ABuildTask;
import com.duy.project.file.java.JavaProject;

public class BuildJarTask extends ABuildTask<JavaProject> {
    public BuildJarTask(IBuilder<JavaProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Create jar file";
    }

    @Override
    public boolean run() throws Exception {
        //now create normal jar file
        JarArchive jarArchive = new JarArchive(builder.isVerbose());
        jarArchive.createJarArchive(project);
        return true;
    }
}
