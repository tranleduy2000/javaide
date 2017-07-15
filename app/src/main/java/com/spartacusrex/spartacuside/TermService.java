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

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.spartacusrex.spartacuside.session.TermSession;
import com.spartacusrex.spartacuside.util.ServiceForegroundCompat;
import com.spartacusrex.spartacuside.util.TermSettings;
import com.spartacusrex.spartacuside.util.hardkeymappings;
import com.spartacusrex.spartacuside.web.webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TermService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    /* Parallels the value of START_STICKY on API Level >= 5 */
    private static final int COMPAT_START_STICKY = 1;

    private static final int RUNNING_NOTIFICATION = 1;
    private static final String TAG = "TermService";
    /*
     * Key logger HACK to get the keycodes..
     */
    private static hardkeymappings mHardKeys = new hardkeymappings();
    private static boolean mLogKeys = false;
    //The Global Key logs File..
    private static File mKeyLogFile = null;
    private static FileOutputStream mKeyLogFileOutputStream = null;
    private static PrintWriter mKeyLogger = null;
    private final IBinder mTSBinder = new TSBinder();
    boolean mBacktoESC = false;
    private ServiceForegroundCompat compat;
    private ArrayList<TermSession> mTermSessions;
    private webserver mServer;
    private SharedPreferences mPrefs;
    private TermSettings mSettings;
    private boolean mSessionInit;
    private PowerManager.WakeLock mScreenLock;
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    //A link to the key logger
    public static void keyLoggerKey(int zKeyCode) {
        if (mLogKeys && mKeyLogger != null) {
            Log.v("Terminal IDE KEY_LOGGER", "Key Logged : " + zKeyCode);
            mKeyLogger.println("Key Logged : " + zKeyCode);
            mKeyLogger.flush();
        }
    }

    public static boolean isHardKeyEnabled() {
        return mHardKeys.isEnabled();
    }

    public static int isSpecialKeyCode(int zKeyCode) {
        return mHardKeys.checkKeyCode(zKeyCode);
    }

    public static void resetAllKeyCodes() {
        //Set them all to -1
        mHardKeys.resetAllMappings();
    }

    private void initKeyLog() {
        mKeyLogFile = new File(getFilesDir(), ".keylog");
        try {
            mKeyLogFileOutputStream = new FileOutputStream(mKeyLogFile, true);
            mKeyLogger = new PrintWriter(mKeyLogFileOutputStream);
            Log.v("KEY_LOGGER", "Keylog file opened..");
        } catch (FileNotFoundException fileNotFoundException) {
            Log.v("KEY_LOGGER", "Error - could not create ~/.keylog");
        }

        mLogKeys = true;
    }

    private void closeKeyLog() {
        try {
            if (mKeyLogger != null) {
                mKeyLogger.close();
            }
            if (mKeyLogFileOutputStream != null) {
                mKeyLogFileOutputStream.close();
            }
        } catch (IOException iOException) {
        }

        mKeyLogger = null;
        mKeyLogFileOutputStream = null;

        mLogKeys = false;
    }

    //Check for shared pref change
    public void onSharedPreferenceChanged(SharedPreferences zPrefs, String zKey) {
        if (zKey.contains("lock")) {
            setupWakeLocks();
        } else if (zKey.contains("hardmap_")) {
            //Check the key logger..
            Log.i("TermService", "Update Key Maps");
            mHardKeys.setKeyMappings(zPrefs);
        }
    }

    @Override
    public void onStart(Intent intent, int flags) {
    }

    /* This should be @Override if building with API Level >=5 */
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("TermService", "Activity called onBind()");
        return mTSBinder;
    }

    private Notification createNotification() {
        Intent notifyIntent = new Intent(this, Start.class);
        notifyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, 0);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.application_terminal));
        builder.setContentText(getString(R.string.service_notify_text));
        builder.setSmallIcon(R.drawable.app_terminal);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public void onCreate() {
        compat = new ServiceForegroundCompat(this);
        mTermSessions = new ArrayList<>();
        compat.startForeground(RUNNING_NOTIFICATION, createNotification());

        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        mHardKeys.setKeyMappings(mPrefs);

        //Setup the Hard Key Mappings..
        mSettings = new TermSettings(mPrefs);

        //Need to set the HOME Folder and Bash startup..
        //Sometime getfilesdir return NULL ?
        mSessionInit = false;
        File home = getFilesDir();
        if (home != null) {
            initSessions(home);
        }

        //Start a webserver for comms..
//        mServer = new webserver(this);
//        mServer.start();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        //Get a wake lock
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TermDebug.LOG_TAG);
        mScreenLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TermDebug.LOG_TAG);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, TermDebug.LOG_TAG);

        //Get the Initial Values
//        boolean cpulock     = getStringPref("cpulock","1") == 1 ? true : false;
//        boolean wifilock    = getStringPref("wifilock","0") == 1 ? true : false;
//        boolean screenlock  = getStringPref("screenlock","0") == 1 ? true : false;
        setupWakeLocks();

        Log.d(TermDebug.LOG_TAG, "TermService started");

        return;
    }

    private int getStringPref(String zKey, String zDefault) {
        int ival = 0;
        try {
            String value = mPrefs.getString(zKey, zDefault);
            ival = Integer.parseInt(value);
        } catch (NumberFormatException numberFormatException) {
            return 0;
        }
        return ival;
    }

    public void setupWakeLocks() {
        //Get the Initial Values
        boolean cpulock = getStringPref("cpulock", "1") == 1 ? true : false;
        boolean wifilock = getStringPref("wifilock", "0") == 1 ? true : false;
        boolean screenlock = getStringPref("screenlock", "0") == 1 ? true : false;

        Log.d(TermDebug.LOG_TAG, "AQUIRING LOCKS " + cpulock + " " + screenlock + " " + wifilock);

        //Turn each Wake Lock On..
        try {
            if (cpulock) {
                if (!mWakeLock.isHeld()) {
                    mWakeLock.acquire();
                }
            } else {
                if (mWakeLock.isHeld()) {
                    mWakeLock.release();
                }
            }

            if (screenlock) {
                if (!mScreenLock.isHeld()) {
                    mScreenLock.acquire();
                }
            } else {
                if (mScreenLock.isHeld()) {
                    mScreenLock.release();
                }
            }

            if (wifilock) {
                if (!mWifiLock.isHeld()) {
                    mWifiLock.acquire();
                }
            } else {
                if (mWifiLock.isHeld()) {
                    mWifiLock.release();
                }
            }

        } catch (Exception e) {
            Log.d(TermDebug.LOG_TAG, "Error getting WAKELOCK " + e);
        }
    }

    private void initSessions(File zHome) {
        if (mSessionInit) {
            return;
        }

        //Create the initial BASH init-file
        if (!createBashInit(zHome)) {
            return;
        }

        //Create 4 initial Terminals
        mTermSessions.add(createTermSession(zHome));
        mTermSessions.add(createTermSession(zHome));
        mTermSessions.add(createTermSession(zHome));
        mTermSessions.add(createTermSession(zHome));

        mSessionInit = true;
    }

    private boolean createBashInit(File zHome) {
        Log.d(TAG, "createBashInit() called with: zHome = [" + zHome + "]");

        File init = new File(zHome, ".init");
        if (init.exists()) {
            init.delete();
        }

        try {
            //Create from scratch
            init.createNewFile();

            FileOutputStream fos = new FileOutputStream(init);
            PrintWriter pw = new PrintWriter(fos);
            pw.println("# BASH init-file - Called when BASH starts first time");
            pw.println("# AUTOMAGICALLY GENERATED - DO NOT TOUCH!");
            pw.println("export HOME=" + zHome.getPath());
            pw.println("export APK=" + getPackageResourcePath());
            pw.println("export HOSTNAME=" + getLocalIpAddress());
            pw.println("");
            pw.println("# This might work better as 'screen' ?");
            pw.println("export TERM=xterm");
            pw.println("");
            pw.println("# Set Special Paths");
            pw.println("export BOOTCLASSPATH=$HOME/system/classes/android.jar:$BOOTCLASSPATH");
            pw.println("export " +
                    "LD_LIBRARY_PATH=$HOME/local/lib" +
                    ":$HOME/system/lib" +
                    ":/system/lib64" +
                    ":/system/lib" +
                    ":$LD_LIBRARY_PATH");

            pw.println("export PATH=$HOME/bin:$HOME/local/bin" +
                    ":$HOME/android-gcc-4.4.0/bin" +
                    ":$HOME/system/bin" +
                    ":$HOME/system/bin/bbdir" +
                    ":$PATH");

            pw.println("");
            pw.println("#If ~/.bashrc exists - run it.");
            pw.println("if [ -f $HOME/.bashrc ]; then");
            pw.println("    . $HOME/.bashrc");
            pw.println("fi");
            pw.println("");
            pw.println("# And finally cd $HOME");
//            pw.println("cd $HOME/system/src/helloworld");
            //set default dir
            pw.println("cd /sdcard");
            pw.println("");
            pw.flush();
            pw.close();
            fos.close();

            //Make sure the /tmp folder ALWAYS exists
            File temp = new File(zHome, "tmp");
            if (!temp.exists()) {
                temp.mkdirs();
            }

        } catch (Exception ex) {
            Logger.getLogger(TermService.class.getName()).log(Level.SEVERE, null, ex);

            return false;
        }

        return true;
    }

    @Override
    public void onDestroy() {
        compat.stopForeground(true);
        for (TermSession session : mTermSessions) {
            session.finish();
        }
        mTermSessions.clear();

        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        if (mScreenLock.isHeld()) {
            mScreenLock.release();
        }

        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }

        //Close the key log file
        closeKeyLog();

//        mServer.stop();

        return;
    }

    public ArrayList<TermSession> getSessions(File zHome) {
        if (zHome != null) {
            initSessions(zHome);
        }

        return mTermSessions;
    }

    public String getLocalIpAddress() {
        String addr = null;

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    String ip = inetAddress.getHostAddress().toString();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (addr == null || ip.length() < addr.length()) {
                            addr = ip;
                        }
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e("GET LOCAL IP : ", ex.toString());
        }

        if (addr != null) {
            return addr;
        }

        return "127.0.0.1";
    }

    private TermSession createTermSession(File zHome) {
        //String HOME = getApplicationContext().getFilesDir().getPath();
        //String APK  = getPackageResourcePath();
        //String IP   = getLocalIpAddress();
        //if(IP == null){
        //   IP = "127.0.0.1";
        //}

        String initialCommand = "";//export HOME="+HOME+";cd $HOME;~/system/init "+HOME+" "+APK+" "+IP;

//        return new TermSession(getApplicationContext(),mSettings, null, initialCommand);
        return new TermSession(zHome.getPath(), mSettings, null, initialCommand);
    }

    //Is BACK ESC
    public boolean isBackESC() {
        return mBacktoESC;
    }

    public void setBackToESC(boolean zBackToEsc) {
        mBacktoESC = zBackToEsc;
    }

    //Toggle Key Logger
    public boolean isKeyLoggerOn() {
        return mLogKeys;
    }

    public void setKeyLogger(boolean zOn) {
        if (zOn) {
            if (!mLogKeys) {
                initKeyLog();
            }
        } else {
            if (mLogKeys) {
                closeKeyLog();
            }
        }
    }

    public class TSBinder extends Binder {
        TermService getService() {
            Log.i("TermService", "Activity binding to service");
            return TermService.this;
        }
    }
}
