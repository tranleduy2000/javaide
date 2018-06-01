package com.duy.ide.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.annotations.Nullable;
import com.duy.ide.R;
import com.duy.ide.file.FileManager;
import com.duy.ide.setting.AppSetting;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class InstallActivity extends AbstractAppCompatActivity implements View.OnClickListener {
    public static final String SYSTEM_VERSION = "System v3.0";
    private static final int REQUEST_CODE_SELECT_FILE = 1101;
    private AppSetting mPreferences;
    private ProgressBar mProgressBar;
    private TextView mInfo;
    private Button mInstallButton;
    //    private TextView mTxtVersion;
    private ProgressDialog progressDialog;
    private boolean mIsInstalling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        setupToolbar();
        setTitle(R.string.install);

        mPreferences = new AppSetting(this);
        mProgressBar = findViewById(R.id.progress_bar);
        mInfo = findViewById(R.id.txt_info);
        mInstallButton = findViewById(R.id.btn_install);

        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_select_file).setOnClickListener(this);
        findViewById(R.id.down_load_from_github).setOnClickListener(this);

        new InstallTask(this).execute();
    }


    private void installSuccess() {
        setResult(RESULT_OK);
        finish();
    }

    private void showDialogFailed(@Nullable Exception error) {
        if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error);
        if (error != null) {
            builder.setMessage(error.getMessage());
        }
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (!mIsInstalling) {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @SuppressLint("StaticFieldLeak")
    private class InstallTask extends AsyncTask<File, String, Void> {
        private Context context;
        private Exception error = null;

        public InstallTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInfo.setText(R.string.start_install_system);
            mProgressBar.setIndeterminate(true);
            mInstallButton.setEnabled(false);
            mIsInstalling = true;
        }

        @Override
        protected Void doInBackground(File... params) {
            try {
                com.duy.android.compiler.env.Environment.install(context);
            } catch (IOException e) {
                e.printStackTrace();
                error = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if ((FileManager.isSdkInstalled(context))) {
                installSuccess();
            } else {
                showDialogFailed(error);
            }
            mInstallButton.setEnabled(true);
            mProgressBar.setIndeterminate(false);
            mIsInstalling = false;
        }
    }

}
