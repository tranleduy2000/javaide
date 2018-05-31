package com.duy.compile.task.java;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.task.ABuildTask;
import com.duy.project.file.java.JavaProject;

public class BuildJarTask extends ABuildTask<JavaProject> {
    public BuildJarTask(IBuilder<JavaProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return null;
    }

    @Override
    public boolean run() throws Exception {
        return false;
    }
}
