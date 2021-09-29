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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.annotations.VisibleForTesting;
import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.project.ProjectCreator;
import com.android.utils.StdLogger;
import com.duy.android.compiler.env.Environment;
import com.duy.ide.javaide.utils.DLog;
import com.duy.ide.R;
import com.duy.ide.javaide.JavaIdeActivity;

import java.io.File;
import java.util.Arrays;


public class SplashScreenActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private static final String TAG = "SplashScreenActivity";
    private static final int REQUEST_INSTALL_SYSTEM = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        // Here, this is the current activity
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        if (!permissionGranted()) {
            requestPermissions();
        } else {
            if (systemInstalled()) {
                startMainActivity();
            } else {
                installSystem();
            }
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST);
    }

    private boolean permissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void installSystem() {
        Intent intent = new Intent(this, InstallActivity.class);
        startActivityForResult(intent, REQUEST_INSTALL_SYSTEM);
    }

    private boolean systemInstalled() {
        return Environment.isSdkInstalled(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_INSTALL_SYSTEM:
                if (resultCode == RESULT_OK) {
                    startMainActivity();
                } else {
                    installFailed();
                }
                break;
        }
    }

    private void installFailed() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (systemInstalled()) {
                        startMainActivity();
                    } else {
                        installSystem();
                    }
                } else {
                    Toast.makeText(this, R.string.permission_denied_storage, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * If receive data from other app (it could be file, text from clipboard),
     * You will be handle data and send to {@link JavaIdeActivity}
     */
    private void startMainActivity() {
        //noinspection ConstantConditions
        if (true) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, JavaIdeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    finish();
                }
            }, 400);
            return;
        }
        try {
            testCreateProject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    private void testCreateProject() {
        StdLogger log = new StdLogger(StdLogger.Level.VERBOSE);
        String sdkFolder = Environment.getSdkDir(this).getAbsolutePath();
        SdkManager sdkManager = SdkManager.createManager(sdkFolder, log);
        ProjectCreator projectCreator = new ProjectCreator(sdkManager, sdkFolder,
                ProjectCreator.OutputLevel.VERBOSE, log);

        IAndroidTarget target = null;
        IAndroidTarget[] targets = sdkManager.getTargets();
        if (DLog.DEBUG) DLog.d(TAG, "targets = " + Arrays.toString(targets));
        for (IAndroidTarget tmp : targets) {
            if (tmp.getVersion().getApiLevel() == 27) {
                target = tmp;
            }
        }

        String projectName = "DemoAndroid";
        File rootProject = new File(Environment.getSdkAppDir(), projectName);
        com.duy.android.compiler.utils.FileUtils.deleteQuietly(rootProject);
        projectCreator.createGradleProject(rootProject.getAbsolutePath(), projectName,
                "com.duy.example",
                "MainActivity", target, false, "1.0");

        File[] files = rootProject.listFiles();
        if (DLog.DEBUG) DLog.d(TAG, "files = " + Arrays.toString(files));
    }

}


















