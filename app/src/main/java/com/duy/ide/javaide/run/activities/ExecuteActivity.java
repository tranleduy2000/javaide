package com.duy.ide.javaide.run.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.duy.JavaApplication;
import com.duy.android.compiler.java.Java;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.utils.IOUtils;
import com.duy.ide.R;
import com.duy.ide.activities.BaseActivity;
import com.duy.ide.javaide.autocomplete.parser.JavaParser;
import com.duy.ide.javaide.run.view.ConsoleEditText;
import com.sun.tools.javac.tree.JCTree;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Duy on 30-Jul-17.
 */

public class ExecuteActivity extends BaseActivity {
    public static final String PROJECT_FILE = "PROJECT_FILE";
    public static final String MAIN_CLASS_FILE = "MAIN_CLASS_FILE";

    private static final String TAG = "ExecuteActivity";
    private final Handler mHandler = new Handler();
    private ConsoleEditText mConsoleEditText;
    private JavaProject mProjectFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exec);
        setupToolbar();

        bindView();
        initInOutStream();
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
        final File mainClassFile = (File) getIntent().getSerializableExtra(MAIN_CLASS_FILE);
        if (mainClassFile == null) {
            finish();
            return;
        }

        setTitle(mainClassFile.getName());
        getSupportActionBar().setSubtitle(R.string.console_running);

        Thread runThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exec(mainClassFile);
                } catch (Error error) {
                    error.printStackTrace(mConsoleEditText.getErrorStream());
                } catch (Exception e) {
                    e.printStackTrace(mConsoleEditText.getErrorStream());
                } catch (Throwable e) {
                    e.printStackTrace(mConsoleEditText.getErrorStream());
                }
                consoleStopped();
            }
        });
        runThread.start();
    }

    @WorkerThread
    private void consoleStopped() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setSubtitle(R.string.console_stopped);
            }
        });
    }

    private void initInOutStream() {
        JavaApplication application = (JavaApplication) getApplication();
        application.addStdErr(mConsoleEditText.getErrorStream());
        application.addStdOut(mConsoleEditText.getOutputStream());
    }

    @WorkerThread
    private void exec(File mainClassFile) throws Throwable {
        String mainClass = resolveMainClass(mainClassFile);
        InputStream stdin = mConsoleEditText.getInputStream();
        File tempDir = getDir("dex", MODE_PRIVATE);
        File dex = mProjectFile.getDexFile();
        executeDex(stdin, dex, tempDir, mainClass);
    }

    private String resolveMainClass(File mainClassFile) throws IOException {
        if (!mainClassFile.getName().endsWith(".java")){
            return null;
        }

        JavaParser parser = new JavaParser();
        JCTree.JCCompilationUnit unit = parser.parse(IOUtils.toStringAndClose(mainClassFile));
        JCTree.JCExpression packageName = unit.getPackageName();
        String simpleName = mainClassFile.getName().substring(0, mainClassFile.getName().indexOf("."));
        return packageName + "." + simpleName;
    }

    private void executeDex(InputStream in, File dex, File tempDir, String mainClass) throws Throwable {
        if (dex == null) {
            throw new RuntimeException("Dex file must be not null");
        }
        if (mainClass == null) {
            throw new RuntimeException("Main class must be not null");
        }
        String[] args = new String[]{"-jar", dex.getPath(), mainClass};
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
        mConsoleEditText = findViewById(R.id.console_view);
    }
}
