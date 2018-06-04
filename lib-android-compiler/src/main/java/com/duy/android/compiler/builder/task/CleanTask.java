package com.duy.android.compiler.builder.task;

import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.project.JavaProject;

public class CleanTask extends Task<JavaProject> {

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
