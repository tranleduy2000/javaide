/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.ide.javaide.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.annotations.Nullable;
import com.duy.android.compiler.env.Environment;
import com.duy.ide.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class InstallActivity extends BaseActivity {
    private ProgressBar mProgressBar;
    private TextView mInfo;
    private boolean mIsInstalling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        setupToolbar();
        setTitle(R.string.install);

        mProgressBar = findViewById(R.id.progress_bar);
        mInfo = findViewById(R.id.txt_info);
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
            if ((Environment.isSdkInstalled(context))) {
                installSuccess();
            } else {
                showDialogFailed(error);
            }
            mProgressBar.setIndeterminate(false);
            mIsInstalling = false;
        }
    }

}
