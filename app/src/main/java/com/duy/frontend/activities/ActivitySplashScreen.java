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

package com.duy.frontend.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.duy.frontend.DLog;
import com.duy.frontend.R;
import com.duy.frontend.code.CompileManager;
import com.duy.frontend.editor.EditorActivity;
import com.duy.frontend.file.FileManager;
import com.duy.frontend.run.ExecuteActivity;
import com.duy.frontend.utils.DonateUtils;
import com.duy.frontend.utils.Installation;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ActivitySplashScreen extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private static final String TAG = "ActivitySplashScreen";
    private static final int REQUEST_CHECK_LICENSE = 1;

    @SuppressLint("ObsoleteSdkInt")
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
            if (isDonateInstalled(DonateUtils.DONATE_PACKAGE)) {
                new CheckTask().execute();
            } else {
                startMainActivity();
            }
        }
    }

    private boolean isDonateInstalled(String name) {
        PackageManager packageManager = getPackageManager();
        try {
            packageManager.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Exception e) {
            return false;
        }
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
                   if (isDonateInstalled(DonateUtils.DONATE_PACKAGE)) {
                        new CheckTask().execute();
                    } else {
                        startMainActivity();
                    }
                } else {
                    Toast.makeText(this, R.string.permission_denied_storage, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_LICENSE) {
            switch (resultCode) {
                case 0: //donate
                    DonateUtils.DONATED = true;
                    saveLicence();
                    startMainActivity();
                    break;
                case 1: //pirate
                    DonateUtils.DONATED = false;
                    //show dialog crack
                    showDialogPirate();
                    break;
                case 2: //not connect or ....
                    DonateUtils.DONATED = false;
                    showDialogCheckFailed();
                    break;

            }
        }
    }

    private void showDialogCheckFailed() {
        Bundle bundle = new Bundle();
        bundle.putString("device_id", Installation.id(this));
        FirebaseAnalytics.getInstance(this).logEvent("check_license_failed", bundle);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.license_invalid);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startMainActivity();
            }
        });
        builder.create().show();
    }

    private void saveLicence() {
        String path = getApplicationInfo().dataDir + "/license";
        String content = DonateUtils.encodeString(Installation.id(this));
        FileManager fileManager = new FileManager(this);
        fileManager.saveFile(path, content);
    }

    private void showDialogPirate() {
        Bundle bundle = new Bundle();
        bundle.putString("device_id", Installation.id(this));
        FirebaseAnalytics.getInstance(this).logEvent("cracked", bundle);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pirated);
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startMainActivity();
            }
        });
        builder.create().show();
    }

    /**
     * If receive data from other app (it could be file, text from clipboard),
     * You will be handle data and send to {@link EditorActivity}
     */
    private void startMainActivity() {
        Intent data = getIntent();
        String action = data.getAction();

        if (DLog.DEBUG) Log.d(TAG, "startMainActivity: action = " + action);

        String type = data.getType();
        final Intent intentEdit = new Intent(ActivitySplashScreen.this, EditorActivity.class);
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            FirebaseAnalytics.getInstance(this).logEvent("open_from_clipboard", new Bundle());
            if (type.equals("text/plain")) {
                handleActionSend(data, intentEdit);
            }

        } else if (Intent.ACTION_VIEW.equals(action) && type != null) {
            FirebaseAnalytics.getInstance(this).logEvent("open_from_another", new Bundle());
            handleActionView(data, intentEdit);
        } else if (action.equalsIgnoreCase("run_from_shortcut")) {
            FirebaseAnalytics.getInstance(this).logEvent("run_from_shortcut", new Bundle());
            handleRunProgram(data);
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                intentEdit.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                overridePendingTransition(0, 0);
                startActivity(intentEdit);
                finish();
            }
        }, 400);
    }

    private void handleRunProgram(Intent data) {
        Intent runIntent = new Intent(this, ExecuteActivity.class);
        runIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        runIntent.putExtra(CompileManager.FILE_PATH,
                data.getStringExtra(CompileManager.FILE_PATH));
        overridePendingTransition(0, 0);
        startActivity(runIntent);
        finish();
    }

    private void handleActionView(@NonNull Intent from,
                                  @NonNull Intent to) {
        Log.d(TAG, "handleActionView() called with: from = [" + from + "], to = [" + to + "]");
        if (from.getData().toString().endsWith(".pas")) {
            Uri uriPath = from.getData();
            Log.d(TAG, "handleActionView: " + uriPath.getPath());
            to.putExtra(CompileManager.FILE_PATH, uriPath.getPath());
        } else if (from.getType().equals("text/x-pascal")) {
            Uri uri = from.getData();
            try {
                //clone file
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileManager fileManager = new FileManager(this);
                String filePath = fileManager.createRandomFile();
                fileManager.copy(inputStream, new FileOutputStream(filePath));

                to.putExtra(CompileManager.FILE_PATH, filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionSend(Intent from, Intent to) {
        String text = from.getStringExtra(Intent.EXTRA_TEXT);

        FileManager fileManager = new FileManager(this);
        //create new temp file
        String filePath = fileManager.createNewFile(FileManager.getApplicationPath() +
                "new_" + Integer.toHexString((int) System.currentTimeMillis()) + ".pas");
        fileManager.saveFile(filePath, text);
        to.putExtra(CompileManager.FILE_PATH, filePath);
    }

    private class CheckTask extends AsyncTask<Object, Object, Boolean> {
        private ApplicationInfo mApplicationInfo;
        private boolean mLicensedCached;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.mApplicationInfo = getApplicationInfo();
        }

        @Override
        protected Boolean doInBackground(Object... voids) {
            if (mApplicationInfo != null) {
                if (DonateUtils.existFile(mApplicationInfo.dataDir + "/license")) {
                    String content = DonateUtils.readFile(mApplicationInfo.dataDir + "/license");
                    if (!content.isEmpty() && (content = DonateUtils.decodeString(content)) != null) {
                        if (content.equals(Installation.id(ActivitySplashScreen.this))) {
                            mLicensedCached = true;
                        }
                    }
                }
                return mLicensedCached;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean donationValid) {
            super.onPostExecute(donationValid);
            if (mLicensedCached) {
                DonateUtils.DONATED = true;
                startMainActivity();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName(DonateUtils.DONATE_PACKAGE,
                        DonateUtils.DONATE_PACKAGE + ".MainActivity"));
                intent.putExtra("requestCode", REQUEST_CHECK_LICENSE);
                startActivityForResult(intent, REQUEST_CHECK_LICENSE);
            }
        }
    }
}