package com.duy.run.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;

import com.duy.compile.CompileManager;
import com.duy.compile.external.CommandManager;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.project.file.java.JavaProjectFile;
import com.duy.run.view.ConsoleEditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ExecuteActivity extends AbstractAppCompatActivity {
    private static final int RUN_TIME_ERR = 1;
    private static final String TAG = "ExecuteActivity";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RUN_TIME_ERR:
                    Exception exception = (Exception) msg.obj;
                    showDialogError(exception.getMessage());
                    break;
            }
        }
    };
    private ConsoleEditText mConsoleEditText;

    private void showDialogError(String message) {
        if (isFinishing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exec);
        setupToolbar();
        bindView();
        final Intent intent = getIntent();
        if (intent != null) {
            final JavaProjectFile projectFile = (JavaProjectFile) intent.getSerializableExtra(CompileManager.PROJECT_FILE);
            if (projectFile == null) {
                finish();
                return;
            }
            final int action = intent.getIntExtra(CompileManager.ACTION, -1);
            setTitle(projectFile.getMainClass().getSimpleName());

            Thread runThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        runProgram(projectFile, action, intent);
                    } catch (Exception e) {
                        e.printStackTrace(mConsoleEditText.getErrorStream());
                        mHandler.sendMessage(mHandler.obtainMessage(RUN_TIME_ERR, e));
                    }
                }
            });
            runThread.start();
        } else {
            finish();
        }
    }

    @WorkerThread
    private void runProgram(JavaProjectFile projectFile, int action, Intent intent) throws IOException {
        PrintStream out = mConsoleEditText.getOutputStream();
        InputStream in = mConsoleEditText.getInputStream();
        PrintStream err = mConsoleEditText.getErrorStream();

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

    @Override
    protected void onStop() {
        super.onStop();
        mConsoleEditText.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void bindView() {
        mConsoleEditText = (ConsoleEditText) findViewById(R.id.console_view);
    }
}
