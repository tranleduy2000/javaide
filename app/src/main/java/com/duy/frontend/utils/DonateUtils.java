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

package com.duy.frontend.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;

import com.duy.frontend.DLog;
import com.duy.frontend.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Duy on 12-Jul-17.
 */

public class DonateUtils {
    public static final String DONATE_PACKAGE = "com.duy.pascaldonate";
    public static final int REQUEST_DONATE = 2;
    private static final String TAG = "Utils";
    public static boolean DONATED = false;

    // MD5 code from
    // https://github.com/CyanogenMod/android_packages_apps_CMUpdater/blob/cm-12.1/src/com/cyanogenmod/updater/utils/MD5.java
    public static boolean checkMD5(String md5, File updateFile) {
        if (md5 == null || updateFile == null || md5.isEmpty()) {
            DLog.e(TAG, "MD5 string empty or updateFile null");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            DLog.e(TAG, "calculatedDigest null");
            return false;
        }

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    private static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            DLog.e(TAG, "Exception while getting digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (IOException e) {
            DLog.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                DLog.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }


    public static boolean existFile(String file) {
        return new File(file).exists();
    }

    public static String readFile(String file) {
        StringBuilder s = null;
        FileReader fileReader = null;
        BufferedReader buf = null;
        try {
            fileReader = new FileReader(file);
            buf = new BufferedReader(fileReader);

            String line;
            s = new StringBuilder();
            while ((line = buf.readLine()) != null) s.append(line).append("\n");
        } catch (FileNotFoundException ignored) {
            DLog.e(TAG, "File does not exist " + file);
        } catch (IOException e) {
            DLog.e(TAG, "Failed to read " + file);
        } finally {
            try {
                if (fileReader != null) fileReader.close();
                if (buf != null) buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return s == null ? null : s.toString().trim();
    }

    public static String decodeString(String text) {
        try {
            return new String(Base64.decode(text, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeString(String androidId) {
        try {
            return new String(Base64.encode(androidId.getBytes(), Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static void showDialogDonate(final Activity context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.donate);
        builder.setMessage(R.string.donate_summary);
        builder.setPositiveButton(R.string.donate_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAnalytics.getInstance(context).logEvent("click_donate", new Bundle());
                StoreUtil.gotoPlayStore(context, DonateUtils.DONATE_PACKAGE, DonateUtils.REQUEST_DONATE);
            }
        });
        builder.setNegativeButton(R.string.donate_nope, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

}
