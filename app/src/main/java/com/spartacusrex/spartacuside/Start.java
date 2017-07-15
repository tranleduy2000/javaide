/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside;

import android.os.Build;
import android.os.Bundle;

import com.spartacusrex.spartacuside.startup.introscreen;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author Spartacus Rex
 */
public class Start extends introscreen {
    private static final String TAG = "Start";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, 0);
        }
    }


}
