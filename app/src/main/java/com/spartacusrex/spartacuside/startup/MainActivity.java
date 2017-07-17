/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.duy.editor.R;
import com.spartacusrex.spartacuside.TermService;
import com.duy.run.activities.TerminalActivity;

/**
 * @author Spartacus Rex
 */
public class MainActivity extends AppCompatActivity implements OnClickListener {

    Dialog mConfirmDialog;
    Dialog mInstallDialog;
    Intent mTSIntent;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.main);

        //Start the Service..
        mTSIntent = new Intent(this, TermService.class);
        startService(mTSIntent);

        Button but = (Button) findViewById(R.id.main_start);
        but.setOnClickListener(this);
        but = (Button) findViewById(R.id.main_stop);
        but.setOnClickListener(this);
        but = (Button) findViewById(R.id.main_keyboard);
        but.setOnClickListener(this);
        but = (Button) findViewById(R.id.main_install);
        but.setOnClickListener(this);
        but = (Button) findViewById(R.id.main_help);
        but.setOnClickListener(this);
        but = (Button) findViewById(R.id.main_options);
        but.setOnClickListener(this);

        AlertDialog.Builder build = new AlertDialog.Builder(this);
        build.setTitle("Confirm");
        build.setMessage("Shutdown all terminals ?");
        build.setCancelable(true);
        build.setPositiveButton("Shutdown", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                stopService(mTSIntent);

                finish();

                mConfirmDialog.dismiss();
            }
        });
        build.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                mConfirmDialog.dismiss();
            }
        });
        mConfirmDialog = build.create();

        build = new AlertDialog.Builder(this);
        build.setTitle("New System");
        build.setMessage("There is a newer system for you to install.");
        build.setCancelable(true);
        build.setPositiveButton("Show me", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //Install the system
                startActivity(new Intent(MainActivity.this, Installer.class));

                mConfirmDialog.dismiss();
            }
        });
        build.setNegativeButton("Later", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //Start the Terminal
                startActivity(new Intent(MainActivity.this, TerminalActivity.class));

                mConfirmDialog.dismiss();
            }
        });
        mInstallDialog = build.create();

        findViewById(R.id.btn_java_ide).setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_java_ide:
                startActivity(new Intent(this, com.duy.editor.editor.MainActivity.class));
                break;
            case (R.id.main_start):
                //Check system version
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String current = prefs.getString("CURRENT_SYSTEM", "no system installed");
                int currentnum = prefs.getInt("CURRENT_SYSTEM_NUM", -1);

                if (currentnum < Installer.CURRENT_INSTALL_SYSTEM_NUM) {
                    mInstallDialog.show();
                } else {
                    //Start the Terminal
                    startActivity(new Intent(MainActivity.this, TerminalActivity.class));
                }

                break;
            case (R.id.main_stop):
                mConfirmDialog.show();

                break;
            case (R.id.main_keyboard):
                //Show Keyboard Picker
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showInputMethodPicker();

                break;
            case (R.id.main_install):
                //Install the system
                startActivity(new Intent(this, Installer.class));

                break;

            case (R.id.main_options):
                //Show the Options..
                startActivity(new Intent(this, TerminalIDEPrefs.class));
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("SpartacusRex", "IntroScreen onConfigurationChanged!!!!");
    }
}
