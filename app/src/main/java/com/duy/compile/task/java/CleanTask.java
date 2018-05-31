package com.duy.compile.task.java;

import com.duy.compile.builder.IBuilder;
import com.duy.compile.task.ABuildTask;
import com.duy.project.file.java.JavaProject;

public class CleanTask extends ABuildTask<JavaProject> {

    public CleanTask(IBuilder<? extends JavaProject> builder) {
        super(builder);
    }

    @Override
    public String getTaskName() {
        return "Clean";
    }

    @Override
    public boolean run() throws Exception {
        project.clean();
        return true;
    }
}
