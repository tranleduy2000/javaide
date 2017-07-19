/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.run.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.duy.editor.R;
import com.duy.editor.code.CompileManager;
import com.duy.external.CommandManager;
import com.duy.project_files.ProjectFile;
import com.duy.run.view.EmulatorView;
import com.duy.run.view.TermKeyListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.spartacusrex.spartacuside.TermService;
import com.spartacusrex.spartacuside.TerminalPreferences;
import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.util.TermSettings;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;


/**
 * A terminal emulator activity.
 */

public class TerminalActivity extends Activity {
    public static final int REQUEST_CHOOSE_WINDOW = 1;
    public static final String EXTRA_WINDOW_ID = "jackpal.androidterm.window_id";
    private static final String TAG = "TerminalActivity";
    private TermSession mTermSession;
    private SharedPreferences mPrefs;
    private TermSettings mSettings;
    private boolean mAlreadyStarted = false;
    private Intent TSIntent;
    private int onResumeSelectWindow = -1;
    private TermService mTermService;
    private AdView mAdView;
    private ViewGroup mContainer;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Bound to TermService");
            TermService.TSBinder binder = (TermService.TSBinder) service;
            mTermService = binder.getService();
            populateViewFlipper();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mTermService = null;
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(mPrefs);

        TSIntent = new Intent(this, TermService.class);

        if (!bindService(TSIntent, mTSConnection, BIND_AUTO_CREATE)) {
            Log.w(TAG, "bind to service failed!");
        }

        setContentView(R.layout.term_activity);
        mContainer = findViewById(R.id.container);

        updatePrefs();
        mAlreadyStarted = true;
        loadAdView();
    }

    private void loadAdView() {
        mAdView = findViewById(R.id.ad_view);
        if (mAdView != null)
            mAdView.loadAd(new AdRequest.Builder().build());
    }

    private void populateViewFlipper() {
        Log.d(TAG, "populateViewFlipper() called");

        if (mTermService != null) {
            mTermSession = mTermService.getSessions(getFilesDir());
            EmulatorView view = createEmulatorView(mTermSession);
            mContainer.removeAllViews();
            mContainer.addView(view);

            updatePrefs();

            Intent intent = getIntent();
            if (intent != null) {
                ProjectFile projectFile = (ProjectFile) intent.getSerializableExtra(CompileManager.PROJECT_FILE);
                if (projectFile == null) return;
                int action = intent.getIntExtra(CompileManager.ACTION, -1);
                switch (action) {
                    case CommandManager.Action.RUN:
                        CommandManager.compileAndRun(mTermSession, projectFile);
                        break;
                    case CommandManager.Action.RUN_DEX:
                        File dex = (File) intent.getSerializableExtra(CompileManager.DEX_FILE);
                        if (dex != null) {
                            CommandManager.executeDex(mTermSession, dex,
                                    projectFile.getMainClass().getName());
                        }
                        break;
                    case CommandManager.Action.BUILD_JAR:
                        CommandManager.buildJarFile(this, mTermSession, projectFile);
                        break;
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();
        mContainer.removeAllViews();
        unbindService(mTSConnection);
        mTermService = null;
        mTSConnection = null;

    }

    private void restart() {
        startActivity(getIntent());
        finish();
    }


    private EmulatorView createEmulatorView(TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        EmulatorView emulatorView = new EmulatorView(this, session, metrics);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.LEFT
        );
        emulatorView.setLayoutParams(params);

        session.setUpdateCallback(emulatorView.getUpdateCallback());


        return emulatorView;
    }


    private TermSession getCurrentTermSession() {
        return mTermSession;
    }

    @Nullable
    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) mContainer.getChildAt(0);
    }

    private void updatePrefs() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        View v = getCurrentEmulatorView();
        if (v != null) {
            ((EmulatorView) v).setDensity(metrics);
            ((EmulatorView) v).updatePrefs(mSettings);
        }
        {
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            final int FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            int desiredFlag = mSettings.showStatusBar() ? 0 : FULLSCREEN;
            if (desiredFlag != (params.flags & FULLSCREEN)) {
                if (mAlreadyStarted) {
                    // Can't switch to/from fullscreen after
                    // starting the activity.
                    restart();
                } else {
                    win.setFlags(desiredFlag, FULLSCREEN);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) mAdView.resume();

        if (mTermSession != null && 1 < mContainer.getChildCount()) {
            for (int i = 0; i < mContainer.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mContainer.getChildAt(i);
                if (!mTermSession.equals(v.getTermSession())) {
                    v.onPause();
                    mContainer.removeView(v);
                    --i;
                }
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings.readPrefs(mPrefs);
        updatePrefs();

        EmulatorView v = getCurrentEmulatorView();
        if (v != null) v.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();
        if (getCurrentEmulatorView() != null) {
            getCurrentEmulatorView().onPause();
            ;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        EmulatorView v = getCurrentEmulatorView();
        if (v != null) {
            v.updateSize(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    private void doCreateNewWindow() {
    }


    private boolean canPaste() {
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clip.hasText()) {
            return true;
        }
        return false;
    }

    private void doPreferences() {
        startActivity(new Intent(this, TerminalPreferences.class));
    }

    private void doResetTerminal() {
        restart();
    }

    private void doEmailTranscript() {
        // Don't really want to supply an address, but
        // currently it's required, otherwise we get an
        // exception.
        String addr = "user@example.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + addr));
        String intro = "Terminal Transcript @ " + new Date().toLocaleString() + "\n\n";
        intent.putExtra("body", intro + getCurrentTermSession().getTranscriptText().trim());
        startActivity(intent);
    }

    private void doCopyAll() {
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(getCurrentTermSession().getTranscriptText().trim());
    }

    private void doPaste() {
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (!clip.hasText()) {
            Toast tt = Toast.makeText(this, "No text to Paste..", Toast.LENGTH_SHORT);
            tt.show();
            return;
        }

        CharSequence paste = clip.getText();
        byte[] utf8;
        try {
            utf8 = paste.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 encoding not found.");
            return;
        }

        getCurrentTermSession().write(paste.toString());
    }

    private void doDocumentKeys() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        Resources r = getResources();
        dialog.setTitle(r.getString(R.string.control_key_dialog_title));
        dialog.setMessage(
                formatMessage(mSettings.getControlKeyId(), TermSettings.CONTROL_KEY_ID_NONE,
                        r, R.array.control_keys_short_names,
                        R.string.control_key_dialog_control_text,
                        R.string.control_key_dialog_control_disabled_text, "CTRLKEY")
                        + "\n\n" +
                        formatMessage(mSettings.getFnKeyId(), TermSettings.FN_KEY_ID_NONE,
                                r, R.array.fn_keys_short_names,
                                R.string.control_key_dialog_fn_text,
                                R.string.control_key_dialog_fn_disabled_text, "FNKEY"));
        dialog.show();
    }

    private String formatMessage(int keyId, int disabledKeyId,
                                 Resources r, int arrayId,
                                 int enabledId,
                                 int disabledId, String regex) {
        if (keyId == disabledKeyId) {
            return r.getString(disabledId);
        }
        String[] keyNames = r.getStringArray(arrayId);
        String keyName = keyNames[keyId];
        String template = r.getString(enabledId);
        String result = template.replaceAll(regex, keyName);
        return result;
    }

    private void doToggleSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void doToggelKeyLogger() {
        if (mTermService == null) {
            return;
        }

        boolean on = mTermService.isKeyLoggerOn();
        mTermService.setKeyLogger(!on);
        if (mTermService.isKeyLoggerOn()) {
            Toast tt = Toast.makeText(this, "KEY LOGGER NOW ON!\n\nCheck ~/.keylog \n\n# tail -f ~/.keylog", Toast.LENGTH_LONG);
            tt.show();
        } else {
            Toast tt = Toast.makeText(this, "Key Logger switched off..", Toast.LENGTH_SHORT);
            tt.show();
        }

    }

    private void doBACKtoESC() {
        if (mTermService == null) {
            return;
        }

        boolean on = mTermService.isBackESC();
        mTermService.setBackToESC(!on);
        if (mTermService.isBackESC()) {
            Toast tt = Toast.makeText(this, "BACK => ESC", Toast.LENGTH_SHORT);
            tt.show();
        } else {
            Toast tt = Toast.makeText(this, "BACK behaves NORMALLY", Toast.LENGTH_SHORT);
            tt.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Is BACK ESC
//        Log.v("Terminal IDE","TERM : onkeyDown code:"+keyCode+" flags:"+event.getFlags()+" meta:"+event.getMetaState());

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mTermService.isBackESC()) {
//                Log.v("SpartacusRex","TERM : ESC sent instead of back.!");
                //Send the ESC sequence..
                int ESC = TermKeyListener.KEYCODE_ESCAPE;
                getCurrentEmulatorView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, ESC));
                getCurrentEmulatorView().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, ESC));
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


}
