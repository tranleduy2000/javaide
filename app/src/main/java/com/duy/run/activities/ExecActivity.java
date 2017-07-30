package com.duy.run.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.duy.compile.CompileManager;
import com.duy.compile.external.CommandManager;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.project.ProjectFile;
import com.duy.run.view.ConsoleEditText;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ExecActivity extends AbstractAppCompatActivity {
    private ConsoleEditText mConsoleEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exec);
        bindView();
        runProgram();
    }

    private void runProgram() {
        Intent intent = getIntent();
        if (intent != null) {
            PrintStream out = new PrintStream(mConsoleEditText.getOutputStream());
            InputStream in = mConsoleEditText.getInputStream();
            PrintStream err = new PrintStream(mConsoleEditText.getErrorStream());

            ProjectFile projectFile = (ProjectFile) intent.getSerializableExtra(CompileManager.PROJECT_FILE);
            if (projectFile == null) return;
            int action = intent.getIntExtra(CompileManager.ACTION, -1);
            switch (action) {
                case CommandManager.Action.RUN:
                    CommandManager.compileAndRun(out, in, err, getDir("dex", MODE_PRIVATE), projectFile);
                    break;
                case CommandManager.Action.RUN_DEX:
                    File dex = (File) intent.getSerializableExtra(CompileManager.DEX_FILE);
                    if (dex != null) {
                        CommandManager.executeDex(out, in, err, dex, getDir("dex", MODE_PRIVATE),
                                projectFile.getMainClass().getName());
                    }
                    break;
            }
        }
    }

    private void bindView() {
        mConsoleEditText = (ConsoleEditText) findViewById(R.id.console_view);
    }
}
