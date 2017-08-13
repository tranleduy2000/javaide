/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.ide.activities;

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

import com.duy.ide.R;
import com.duy.ide.editor.code.MainActivity;

import java.io.File;


public class ActivitySplashScreen extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private static final String TAG = "ActivitySplashScreen";
    private static final int REQUEST_INSTALL_SYSTEM = 12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        // Here, this is the current activity
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST);
        } else {
            if (systemInstalled()) {
                startMainActivity();
            } else {
                installSystem();
            }
        }
    }

    private void installSystem() {
        Intent intent = new Intent(this, InstallActivity.class);
        startActivityForResult(intent, REQUEST_INSTALL_SYSTEM);
    }

    private boolean systemInstalled() {
//        JavaPreferences preferences = new JavaPreferences(this);
//        return preferences.hasSystemInstalled()
//                && preferences.getSystemVersion().equalsIgnoreCase(InstallActivity.SYSTEM_VERSION);
        File file = new File(getFilesDir(), "system" + File.separator + "classes"
                + File.separator + "android.jar");
        return file.exists() && file.isFile();
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
     * You will be handle data and send to {@link MainActivity}
     */
    private void startMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ActivitySplashScreen.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intent);
                finish();
            }
        }, 400);
    }


}