package com.duy.run.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

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
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exec);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        bindView();

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                runProgram();
            }
        });
        runThread.start();
    }

    private void runProgram() {
        Intent intent = getIntent();
        if (intent != null) {
            final ProjectFile projectFile = (ProjectFile) intent.getSerializableExtra(CompileManager.PROJECT_FILE);
            if (projectFile == null) return;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setTitle(projectFile.getMainClass().getSimpleName());
                }
            });

            PrintStream out = new PrintStream(mConsoleEditText.getOutputStream());
            InputStream in = mConsoleEditText.getInputStream();
            PrintStream err = new PrintStream(mConsoleEditText.getErrorStream());

            int action = intent.getIntExtra(CompileManager.ACTION, -1);
            switch (action) {
                case CommandManager.Action.RUN: {
                    CommandManager.compileAndRun(out, in, err, getDir("dex", MODE_PRIVATE), projectFile);
                    break;
                }
                case CommandManager.Action.RUN_DEX: {
                    File dex = (File) intent.getSerializableExtra(CompileManager.DEX_FILE);
                    if (dex != null) {
                        CommandManager.executeDex(out, in, err, dex, getDir("dex", MODE_PRIVATE),
                                projectFile.getMainClass().getName());
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mConsoleEditText.destroy();
        super.onDestroy();
    }

    private void bindView() {
        mConsoleEditText = (ConsoleEditText) findViewById(R.id.console_view);
    }
}
