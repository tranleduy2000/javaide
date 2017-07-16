/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spartacusrex.spartacuside.startup;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.editor.R;
import com.spartacusrex.spartacuside.TermService;
import com.spartacusrex.spartacuside.startup.setup.FileManager;
import com.spartacusrex.spartacuside.startup.tutorial.tutview;

import java.io.File;
import java.io.IOException;

/**
 * @author Spartacus Rex
 */
public class Installer extends Activity implements OnClickListener {


    private static final String TAG = "Installer";
    public static int CURRENT_INSTALL_SYSTEM_NUM = 20;
    public static String CURRENT_INSTALL_SYSTEM = "System v2.0";
    public static String CURRENT_INSTALL_ASSETFILE = "system3.tar.gz.mp3";
    boolean mOverwriteAll = false;
    private ProgressDialog mInstallProgress;
    public Handler mInstallHandler = new Handler() {
        @Override
        public void handleMessage(Message zMsg) {
            Bundle msg = zMsg.getData();

            if (msg.containsKey("close_install")) {

                mInstallProgress.dismiss();


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Installer.this);
                String current = prefs.getString("CURRENT_SYSTEM", "no system installed");
                TextView tv = findViewById(R.id.install_sys);
                tv.setText("Current   : " + current + "\n" + "Available : " + CURRENT_INSTALL_SYSTEM);


                Intent mTSIntent = new Intent(Installer.this, TermService.class);
                startService(mTSIntent);

            } else if (msg.containsKey("error")) {
                String info = msg.getString("error");
                mInstallProgress.setMessage(info);

                Toast.makeText(Installer.this, "ERROR : \n" + info, Toast.LENGTH_LONG).show();

            } else {
                String info = msg.getString("info");
                mInstallProgress.setMessage(info);
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);


        setContentView(R.layout.install);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String current = prefs.getString("CURRENT_SYSTEM", "no system installed");
        int currentnum = prefs.getInt("CURRENT_SYSTEM_NUM", 0);
        String avail = CURRENT_INSTALL_SYSTEM;

        TextView tv = findViewById(R.id.install_sys);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setText("Current   : " + current + "\n" + "Available : " + avail);

        Button but = findViewById(R.id.install_changelog);
        but.setOnClickListener(this);
        but = findViewById(R.id.install_start);
        but.setOnClickListener(this);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        mInstallProgress = new ProgressDialog(this);
        mInstallProgress.setTitle("System installing..");
        mInstallProgress.setMessage("Please wait..");
        mInstallProgress.setCancelable(false);

        return mInstallProgress;
    }

    public void onClick(View zButton) {
        if (zButton == findViewById(R.id.install_changelog)) {

            Intent res = new Intent(this, tutview.class);
            res.putExtra("com.spartacusrex.prodj.tutorial", R.layout.changelog);
            startActivity(res);

        } else if (zButton == findViewById(R.id.install_start)) {

            showDialog(0);


            Intent mTSIntent = new Intent(this, TermService.class);
            stopService(mTSIntent);


            CheckBox over = findViewById(R.id.install_overwrite);
            mOverwriteAll = over.isChecked();


            Thread tt = install1();
            tt.start();
        }
    }

    private Thread install1() {
        return new Thread() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public void run() {

                Message msg = new Message();
                msg.getData().putString("info", "Starting System install..");
                mInstallHandler.sendMessage(msg);

                File home = Installer.this.getFilesDir();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Installer.this);

                try {
                    File tmp = new File(home, "tmp");
                    if (!tmp.exists()) tmp.mkdirs();

                    File worker = new File(tmp, "WORK_" + System.currentTimeMillis());
                    if (!worker.exists()) worker.mkdirs();


                    //////////////////////////////////////////
                    File busytar = new File(worker, "busybox");
                    if (busytar.exists()) {
                        busytar.delete();
                    }

                    //Extract BusyBox, need it just for ln and cp
                    FileManager.extractAsset(Installer.this, "busybox.mp3", busytar);
                    //////////////////////////////////////////

                    String[] env = new String[2];
                    env[0] = "PATH=/sbin" +
                            ":/vendor/bin" +
                            ":/system/sbin" +
                            ":/system/bin" +
                            ":/system/xbin";

                    env[1] = "LD_LIBRARY_PATH=" +
                            "/vendor/lib" +
                            ":/vendor/lib64" +
                            ":/system/lib" +
                            ":/system/lib64";

                    Process pp;

                    //////////////
                    busytar.setReadable(true, true);
                    busytar.setWritable(true, true);
                    busytar.setExecutable(true, false);
                    /////////////////

                    File systar = new File(worker, "system.tar.gz");
                    FileManager.extractAsset(Installer.this, CURRENT_INSTALL_ASSETFILE, systar);

                    msg = new Message();
                    msg.getData().putString("info", "Removing Old System..");
                    mInstallHandler.sendMessage(msg);

                    File systemFolder = new File(home, "system");
                    FileManager.deleteFolder(systemFolder);

//                    String busyboxCmd_ = busytar.getPath() + " ";
                    String busyboxCmd_ = "busybox ";
                    pp = Runtime.getRuntime().exec(busyboxCmd_ + "tar -C " + home.getPath() + " -xzf " + systar.getPath(), env, home);
                    pp.waitFor();


                    File bindir = new File(systemFolder, "bin");
                    File bbindir = new File(bindir, "bbdir");
                    if (!bbindir.exists()) bbindir.mkdirs();

                    File busybox = new File(bindir, "busybox");
                    String command = busybox.getPath() + " --install -s " + bbindir.getPath();
                    pp = Runtime.getRuntime().exec(command, env, home);
                    pp.waitFor();

                    //Now delete the SU link.. too much confusion..
                    File su = new File(bbindir, "su");
                    su.delete();

                    copyFileConfig(home, systemFolder, busyboxCmd_, env);
                    createLocalFile(home);

                    msg = new Message();
                    msg.getData().putString("info", "Cleaning up..");
                    mInstallHandler.sendMessage(msg);
                    FileManager.deleteFolder(worker);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CURRENT_SYSTEM", CURRENT_INSTALL_SYSTEM);
                    editor.putInt("CURRENT_SYSTEM_NUM", CURRENT_INSTALL_SYSTEM_NUM);
                    editor.apply();

                } catch (Exception iOException) {
                    msg = new Message();
                    msg.getData().putString("error", iOException.toString());
                    mInstallHandler.sendMessage(msg);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CURRENT_SYSTEM", "ERROR : Last Install");
                    editor.putInt("CURRENT_SYSTEM_NUM", -1);
                    editor.apply();

                    iOException.printStackTrace();
                }


                msg = new Message();
                msg.getData().putString("info", "System install complete!");
                mInstallHandler.sendMessage(msg);

                msg = new Message();
                msg.getData().putString("close_install", "1");
                mInstallHandler.sendMessage(msg);

                Log.v("SpartacusRex", "Finished Binary Install");
            }
        };
    }

    private void copyFileConfig(File home, File systemFolder, String busyboxCmd_, String[] env) throws IOException, InterruptedException {
        Process pp;
        File bashrc = new File(systemFolder, "bashrc");
        File bashrcu = new File(home, ".bashrc");
        if (!bashrcu.exists() || mOverwriteAll) {
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + bashrc.getPath() + " " + bashrcu.getPath(), env, home);
            pp.waitFor();
        }

        File nanorc = new File(systemFolder, "nanorc");
        File nanorcu = new File(home, ".nanorc");
        if (!nanorcu.exists() || mOverwriteAll) {
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + nanorc.getPath() + " " + nanorcu.getPath(), env, home);
            pp.waitFor();
        }

        File tmuxrc = new File(systemFolder, "tmux.conf");
        File tmuxrcu = new File(home, ".tmux.conf");
        if (!tmuxrcu.exists() || mOverwriteAll) {
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + tmuxrc.getPath() + " " + tmuxrcu.getPath(), env, home);
            pp.waitFor();
        }


        File ini = new File(systemFolder, "mc.ini");
        File conf = new File(home, ".config");
        File confmc = new File(conf, "mc");
        if (!confmc.exists()) confmc.mkdirs();
        File mcini = new File(confmc, "ini");
        if (!mcini.exists() || mOverwriteAll) {
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + ini.getPath() + " " + mcini.getPath(), env, home);
            pp.waitFor();
        }

        File inputrc = new File(systemFolder, "inputrc");
        File inputrcu = new File(home, ".inputrc");
        pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + inputrc.getPath() + " " + inputrcu.getPath(), env, home);
        pp.waitFor();

    }

    private void createLocalFile(File home) {

        File local = new File(home, "local");
        if (!local.exists()) {
            local.mkdirs();
        }

        File bin = new File(local, "bin");
        if (!bin.exists()) {
            bin.mkdirs();
        }

        bin = new File(local, "lib");
        if (!bin.exists()) {
            bin.mkdirs();
        }

        bin = new File(local, "include");
        if (!bin.exists()) {
            bin.mkdirs();
        }

        bin = new File(home, "tmp");
        if (!bin.exists()) {
            bin.mkdirs();
        }

        bin = new File(home, "projects");
        if (!bin.exists()) {
            bin.mkdirs();
        }

    }

    private Thread install() {
        return new Thread() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            public void run() {

                Message msg = new Message();
                msg.getData().putString("info", "Starting System install..");
                mInstallHandler.sendMessage(msg);

                File home = Installer.this.getFilesDir();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Installer.this);

                try {

                    File tmp = new File(home, "tmp");
                    if (!tmp.exists()) {
                        tmp.mkdirs();
                    }


                    File worker = new File(tmp, "WORK_" + System.currentTimeMillis());
                    if (!worker.exists()) {
                        worker.mkdirs();
                    }


                    File busytar = new File(worker, "busybox");
                    if (busytar.exists()) {
                        busytar.delete();
                    }

                    //Extract BusyBox, need it just for ln and cp
                    FileManager.extractAsset(Installer.this, "busybox.mp3", busytar);


                    String[] env = new String[2];
                    env[0] = "PATH=/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin";
                    env[1] = "LD_LIBRARY_PATH=/vendor/lib:/vendor/lib64:/system/lib:/system/lib64";

                    Process pp;


                    pp = Runtime.getRuntime().exec("chmod 770 " + busytar.getPath(), env, home);
                    pp.waitFor();


                    msg = new Message();
                    msg.getData().putString("info", "Preparing " + CURRENT_INSTALL_SYSTEM + " ..");
                    mInstallHandler.sendMessage(msg);

                    File systar = new File(worker, "system.tar.gz");
                    FileManager.extractAsset(Installer.this, CURRENT_INSTALL_ASSETFILE, systar);


                    msg = new Message();
                    msg.getData().putString("info", "Removing Old System..");
                    mInstallHandler.sendMessage(msg);

                    File system = new File(home, "system");
                    FileManager.deleteFolder(system);

                    msg = new Message();
                    msg.getData().putString("info", "Installing new system.. can take a minute");
                    mInstallHandler.sendMessage(msg);


                    pp = Runtime.getRuntime().exec("busybox" + " tar -C " + home.getPath() + " -xzf " + systar.getPath(), env, home);
                    pp.waitFor();


                    msg = new Message();
                    msg.getData().putString("info", "Copying startup files..");
                    mInstallHandler.sendMessage(msg);


                    File bashrc = new File(system, "bashrc");
                    File bashrcu = new File(home, ".bashrc");
                    if (!bashrcu.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp " + bashrc.getPath() + " " + bashrcu.getPath(), env, home);
                        pp.waitFor();
                    }


                    File nanorc = new File(system, "nanorc");
                    File nanorcu = new File(home, ".nanorc");
                    if (!nanorcu.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp " + nanorc.getPath() + " " + nanorcu.getPath(), env, home);
                        pp.waitFor();
                    }


                    File tmuxrc = new File(system, "tmux.conf");
                    File tmuxrcu = new File(home, ".tmux.conf");
                    if (!tmuxrcu.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp " + tmuxrc.getPath() + " " + tmuxrcu.getPath(), env, home);
                        pp.waitFor();
                    }


                    File ini = new File(system, "mc.ini");
                    File conf = new File(home, ".config");
                    File confmc = new File(conf, "mc");
                    if (!confmc.exists()) {
                        confmc.mkdirs();
                    }
                    File mcini = new File(confmc, "ini");
                    if (!mcini.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp " + ini.getPath() + " " + mcini.getPath(), env, home);
                        pp.waitFor();
                    }


                    File inputrc = new File(system, "inputrc");
                    File inputrcu = new File(home, ".inputrc");

                    pp = Runtime.getRuntime().exec("busybox" + " cp " + inputrc.getPath() + " " + inputrcu.getPath(), env, home);
                    pp.waitFor();

                    File vimrc = new File(system, "vimrc");
                    File vimrcu = new File(home, ".vimrc");
                    if (!vimrcu.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp " + vimrc.getPath() + " " + vimrcu.getPath(), env, home);
                        pp.waitFor();
                    }


                    File vimh = new File(system, "etc/default_vim");
                    File vimhu = new File(home, ".vim");
                    if (!vimhu.exists() || mOverwriteAll) {

                        pp = Runtime.getRuntime().exec("busybox" + " cp -rf " + vimh.getPath() + " " + vimhu.getPath(), env, home);
                        pp.waitFor();
                    }


                    File sdcard = Environment.getExternalStorageDirectory();
                    File lnsdcard = new File(home, "sdcard");
                    String func = "busybox" + " ln -s " + sdcard.getPath() + " " + lnsdcard.getPath();
                    Log.v("SpartacusRex", "SDCARD ln : " + func);

                    pp = Runtime.getRuntime().exec(func, env, home);
                    pp.waitFor();


                    File local = new File(home, "local");
                    if (!local.exists()) {
                        local.mkdirs();
                    }

                    File bin = new File(local, "bin");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }

                    bin = new File(local, "lib");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }

                    bin = new File(local, "include");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }

                    bin = new File(home, "tmp");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }

                    bin = new File(home, "projects");
                    if (!bin.exists()) {
                        bin.mkdirs();
                    }

                    msg = new Message();
                    msg.getData().putString("info", "Cleaning up..");
                    mInstallHandler.sendMessage(msg);
                    FileManager.deleteFolder(worker);


                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CURRENT_SYSTEM", CURRENT_INSTALL_SYSTEM);
                    editor.putInt("CURRENT_SYSTEM_NUM", CURRENT_INSTALL_SYSTEM_NUM);
                    editor.apply();

                } catch (Exception iOException) {
                    Log.v("SpartacusRex", "INSTALL SYSTEM EXCEPTION : " + iOException);

                    msg = new Message();
                    msg.getData().putString("error", iOException.toString());
                    mInstallHandler.sendMessage(msg);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("CURRENT_SYSTEM", "ERROR : Last Install");
                    editor.putInt("CURRENT_SYSTEM_NUM", -1);
                    editor.apply();
                }


                msg = new Message();
                msg.getData().putString("info", "System install complete!");
                mInstallHandler.sendMessage(msg);

                msg = new Message();
                msg.getData().putString("close_install", "1");
                mInstallHandler.sendMessage(msg);

                Log.v("SpartacusRex", "Finished Binary Install");
            }
        };
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("SpartacusRex", "Installer onConfigurationChanged!!!!");
    }
}
