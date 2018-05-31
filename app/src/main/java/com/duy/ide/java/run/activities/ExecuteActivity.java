package com.duy.ide.java.run.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.duy.JavaApplication;
import com.duy.android.compiler.file.java.JavaProject;
import com.duy.android.compiler.java.Java;
import com.duy.ide.R;
import com.duy.ide.activities.AbstractAppCompatActivity;
import com.duy.ide.java.run.view.ConsoleEditText;

import java.io.File;
import java.io.InputStream;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ExecuteActivity extends AbstractAppCompatActivity {
    public static final String PROJECT_FILE = "project_file";
    private static final String TAG = "ExecuteActivity";

    private ConsoleEditText mConsoleEditText;
    private JavaProject mProjectFile;

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
        initInOut();
        final Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        mProjectFile = (JavaProject) intent.getSerializableExtra(PROJECT_FILE);
        if (mProjectFile == null) {
            finish();
            return;
        }
        setTitle(mProjectFile.getMainClass().getSimpleName());
        getSupportActionBar().setSubtitle(R.string.console_running);

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runProgram(mProjectFile, intent);
                } catch (Error error) {
                    error.printStackTrace(mConsoleEditText.getErrorStream());
                } catch (Exception e) {
                    e.printStackTrace(mConsoleEditText.getErrorStream());
                } catch (Throwable e) {
                    e.printStackTrace(mConsoleEditText.getErrorStream());
                }
                getSupportActionBar().setSubtitle(R.string.console_stopped);
            }
        });
        runThread.start();
    }

    private void initInOut() {
        JavaApplication application = (JavaApplication) getApplication();
        application.addStdErr(mConsoleEditText.getErrorStream());
        application.addStdOut(mConsoleEditText.getOutputStream());
    }

    @WorkerThread
    private void runProgram(JavaProject projectFile, Intent intent) throws Throwable {
        InputStream in = mConsoleEditText.getInputStream();
        File tempDir = getDir("dex", MODE_PRIVATE);
        File dex = mProjectFile.getDexFile();
        if (dex != null) {
            String mainClass = projectFile.getMainClass().getName();
            executeDex(in, dex, tempDir, mainClass);
        }
    }

    private void executeDex(InputStream in, File outDex, File tempDir, String mainClass) throws Throwable {
        String[] args = new String[]{"-jar", outDex.getPath(), mainClass};
        Java.run(args, tempDir.getPath(), in);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");

        mConsoleEditText.stop();
        JavaApplication application = (JavaApplication) getApplication();
        application.removeErrStream(mConsoleEditText.getErrorStream());
        application.removeOutStream(mConsoleEditText.getOutputStream());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void bindView() {
        mConsoleEditText = (ConsoleEditText) findViewById(R.id.console_view);
    }
}
