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

package com.spartacusrex.spartacuside;

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
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.duy.editor.BuildConfig;
import com.duy.editor.R;
import com.duy.editor.code.CompileManager;
import com.duy.editor.project_files.ProjectFile;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.util.TermSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

/**
 * A terminal emulator activity.
 */

public class TerminalActivity extends Activity {
    public static final int REQUEST_CHOOSE_WINDOW = 1;
    public static final String EXTRA_WINDOW_ID = "jackpal.androidterm.window_id";
    /**
     * The name of the ViewFlipper in the resources.
     */
    private static final int VIEW_FLIPPER = R.id.view_flipper;
    private static final String TAG = "TerminalActivity";
    /**
     * The ViewFlipper which holds the collection of EmulatorView widgets.
     */
    private TermViewFlipper mViewFlipper;
    private ArrayList<TermSession> mTermSessions;
    private SharedPreferences mPrefs;
    private TermSettings mSettings;
    private boolean mAlreadyStarted = false;
    private Intent TSIntent;
    private int onResumeSelectWindow = -1;
    private TermService mTermService;
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

    private AdView mAdView;

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
        mViewFlipper = findViewById(R.id.view_flipper);
        registerForContextMenu(mViewFlipper);

        updatePrefs();
        mAlreadyStarted = true;
        loadAdView();
    }

    private void loadAdView() {
//        mAdView = findViewById(R.id.adView);
        if (mAdView != null)
            mAdView.loadAd(new AdRequest.Builder().build());
    }


    private void populateViewFlipper() {
        Log.d(TAG, "populateViewFlipper() called");

        if (mTermService != null) {
            mTermSessions = mTermService.getSessions(getFilesDir());

            for (TermSession session : mTermSessions) {
                EmulatorView view = createEmulatorView(session);
                mViewFlipper.addView(view);
            }

            updatePrefs();

            Intent intent = getIntent();
            ProjectFile projectFile = (ProjectFile) intent.getSerializableExtra(CompileManager.PROJECT_FILE);
            if (projectFile != null) {
                compileAndRun(mTermSessions.get(0), projectFile);
            }
        }
    }

    private void compileAndRun(TermSession termSession, ProjectFile projectFile) {
        Log.d(TAG, "compileAndRun() called with: filePath = [" + projectFile + "]");
        File home = getFilesDir();
        try {
            FileOutputStream fos = termSession.getTermOut();
            PrintWriter pw = new PrintWriter(fos);
            pw.println("clear"); //clear screen
            pw.println("echo JAVA N-IDE. version " + BuildConfig.VERSION_CODE);
            pw.flush();

            //set value for variable
            pw.println("PROJECT_PATH=" + projectFile.getProjectDir());
            pw.println("PROJECT_NAME=" + projectFile.getProjectName());
            pw.println("MAIN_CLASS=" + projectFile.getMainClass().getName());
            pw.println("PATH_MAIN_CLASS=" + projectFile.getMainClass().getName().replace(".", "/"));
            pw.println("ROOT_PACKAGE=" + projectFile.getPackageName().substring(0, projectFile.getPackageName().indexOf(".")));

            pw.println("cd ~"); //go to home
            pw.println("cd ${PROJECT_PATH}");//move to root project
            pw.flush();

            //create build and bin dir
            File build = new File(projectFile.getProjectDir(), "build");
            if (!(build.exists())) build.mkdirs();
            File bin = new File(projectFile.getProjectDir(), "bin");
            if (!(bin.exists())) bin.mkdirs();

            //clean up
            pw.println("rm -rf build/*");
            pw.println("rm -rf bin/*");
            pw.flush();

            //cd to src dir
            pw.println("cd src/main/java");

            //now compile
            pw.println("echo Compile java file");
            pw.println("javac -verbose -d ../../../build/ ${PATH_MAIN_CLASS}.java");
            pw.flush();

            //go to build dir
            pw.println("cd ../../../build/");

            pw.println("echo Now convert to dex format");
            pw.println("dx --dex --verbose --no-strict --output=../bin/${PROJECT_NAME}.jar ${ROOT_PACKAGE}");
            pw.flush();

            pw.println("cd .."); //go to root dir
            pw.flush();

            //now run file
            pw.println("java -jar ./bin/${PROJECT_NAME}.jar ${MAIN_CLASS}");
            pw.flush();

            File temp = new File(home, "tmp");
            if (!temp.exists()) temp.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdView != null) mAdView.destroy();

        mViewFlipper.removeAllViews();
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
        EmulatorView emulatorView = new EmulatorView(this, session, mViewFlipper, metrics);

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
        return mTermSessions.get(mViewFlipper.getDisplayedChild());
    }

    private EmulatorView getCurrentEmulatorView() {
        return (EmulatorView) mViewFlipper.getCurrentView();
    }

    private void updatePrefs() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        for (View v : mViewFlipper) {
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
        if (mTermSessions != null && mTermSessions.size() < mViewFlipper.getChildCount()) {
            for (int i = 0; i < mViewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mViewFlipper.getChildAt(i);
                if (!mTermSessions.contains(v.getTermSession())) {
                    v.onPause();
                    mViewFlipper.removeView(v);
                    --i;
                }
            }
        }

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings.readPrefs(mPrefs);
        updatePrefs();

        if (onResumeSelectWindow >= 0) {
            mViewFlipper.setDisplayedChild(onResumeSelectWindow);
            onResumeSelectWindow = -1;
        } else {
            mViewFlipper.resumeCurrentView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdView != null) mAdView.pause();

        mViewFlipper.pauseCurrentView();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        EmulatorView v = (EmulatorView) mViewFlipper.getCurrentView();
        if (v != null) {
            v.updateSize(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_window_list) {
            //startActivityForResult(new Intent(this, WindowList.class), REQUEST_CHOOSE_WINDOW);
            //Show a list of windows..
            openContextMenu(mViewFlipper);
//        } else if (id == R.id.menu_reset) {
//            doResetTerminal();
        } else if (id == R.id.menu_toggle_soft_keyboard) {
            doToggleSoftKeyboard();

        } else if (id == R.id.menu_back_esc) {
            doBACKtoESC();

        } else if (id == R.id.menu_keylogger) {
            doToggelKeyLogger();

        } else if (id == R.id.menu_paste) {
            doPaste();

        } else if (id == R.id.menu_copyall) {
            doCopyAll();

        } else if (id == R.id.menu_copyemail) {
            doEmailTranscript();
        }

        return super.onOptionsItemSelected(item);
    }

    private void doCreateNewWindow() {
        if (mTermSessions == null) {
            Log.w(TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }

//        TermSession session = createTermSession();
//        mTermSessions.add(session);
//        EmulatorView view = createEmulatorView(session);
//        view.updatePrefs(mSettings);
//        mViewFlipper.addView(view);
//        mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount()-1);
    }

    private void doCloseWindow() {
        if (mTermSessions == null) {
            return;
        }

        EmulatorView view = getCurrentEmulatorView();
        if (view == null) {
            return;
        }
        TermSession session = mTermSessions.remove(mViewFlipper.getDisplayedChild());
        view.onPause();
        session.finish();
        mViewFlipper.removeView(view);
        if (mTermSessions.size() == 0) {
            finish();
        } else {
            mViewFlipper.showNext();
        }
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        switch (request) {
            case REQUEST_CHOOSE_WINDOW:
                if (result == RESULT_OK && data != null) {
                    int position = data.getIntExtra(EXTRA_WINDOW_ID, -2);
                    if (position >= 0) {
                        // Switch windows after session list is in sync, not here
                        onResumeSelectWindow = position;
                    } else if (position == -1) {
                        doCreateNewWindow();
                    }
                } else {
                    // Close the activity if user closed all sessions
                    if (mTermSessions.size() == 0) {
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //Show alist of windows
        menu.setHeaderTitle("Terminals");
        menu.add(0, 0, 0, "Terminal 1");
        menu.add(0, 1, 1, "Terminal 2");
        menu.add(0, 2, 2, "Terminal 3");
        menu.add(0, 3, 3, "Terminal 4");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        //Set the selected window..
        mViewFlipper.setDisplayedChild(item.getItemId());
        return super.onContextItemSelected(item);
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


    private void doToggleWakeLock() {
//        if (mWakeLock.isHeld()) {
//            mWakeLock.release();
//        } else {
//            mWakeLock.acquire();
//        }
    }

    private void doToggleWifiLock() {
//        if (mWifiLock.isHeld()) {
//            mWifiLock.release();
//        } else {
//            mWifiLock.acquire();
//        }
    }
}
