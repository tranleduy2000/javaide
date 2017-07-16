package com.duy.editor.system;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duy.editor.R;
import com.duy.editor.activities.AbstractAppCompatActivity;
import com.duy.editor.setting.JavaPreferences;
import com.spartacusrex.spartacuside.startup.setup.FileManager;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 16-Jul-17.
 */

public class InstallActivity extends AbstractAppCompatActivity implements View.OnClickListener {
    public static final String SYSTEM_ASSETFILE = "system3.tar.gz.mp3";
    public static final String SYSTEM_VERSION = "System v3.0";
    private JavaPreferences mPreferences;
    private ProgressBar mProgressBar;
    private TextView mInfo;
    private Button mInstallButton;
    private TextView mTxtVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        setupToolbar();
        setTitle(R.string.install);

        mPreferences = new JavaPreferences(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mInfo = (TextView) findViewById(R.id.txt_info);
        mInstallButton = (Button) findViewById(R.id.btn_install);

        mTxtVersion = (TextView) findViewById(R.id.txt_version);
        String version = getString(R.string.system_version) + mPreferences.getSystemVersion();
        mTxtVersion.setText(version);
        findViewById(R.id.btn_install).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_install) {
            new InstallTask(this).execute();
        }
    }

    private void showDialogSuccess() {
        if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Success!");
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(RESULT_OK);
                finish();
            }
        });
        builder.create().show();
    }

    private void showDialogFailed(Exception error) {
        if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(error.getMessage());
        builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private class InstallTask extends AsyncTask<Void, String, Boolean> {


        private Exception error = null;
        private Context context;

        public InstallTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInfo.setText("Starting install system");
            mProgressBar.setIndeterminate(true);
            findViewById(R.id.btn_install).setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            File home = getFilesDir();
            try {
                File tmp = new File(home, "tmp");
                if (!tmp.exists()) tmp.mkdirs();

                File worker = new File(tmp, "WORK_" + System.currentTimeMillis());
                if (!worker.exists()) worker.mkdirs();

                File busytar = new File(worker, "busybox");
                if (busytar.exists()) busytar.delete();

                publishProgress("Extract busybox");
                FileManager.extractAsset(context, "busybox.mp3", busytar);

                String[] env = new String[2];
                env[0] = "PATH=/sbin" + ":/vendor/bin" + ":/system/sbin" + ":/system/bin" +
                        ":/system/xbin";

                env[1] = "LD_LIBRARY_PATH=" + "/vendor/lib" + ":/vendor/lib64" + ":/system/lib" +
                        ":/system/lib64";


                busytar.setReadable(true, true);
                busytar.setWritable(true, true);
                busytar.setExecutable(true, false);

                publishProgress("Extract system file");
                File systar = new File(worker, "system.tar.gz");
                FileManager.extractAsset(context, SYSTEM_ASSETFILE, systar);
                publishProgress("Extracted system");

                Process pp;

                publishProgress("Removing Old System");

                File systemFolder = new File(home, "system");
                FileManager.deleteFolder(systemFolder);

                publishProgress("Config system file");
                String busyboxCmd_ = busytar.getPath() + " ";
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

                publishProgress("Copy config file");
                copyFileConfig(home, systemFolder, busyboxCmd_, env);

                publishProgress("Create local file");
                createLocalFile(home);

                publishProgress("Cleaning up...");
                FileManager.deleteFolder(worker);

                mPreferences.put("system_installed", true);
                mPreferences.put("system_version", SYSTEM_VERSION);
            } catch (Exception e) {
                publishProgress("Error when install system");

                mPreferences.put("system_installed", false);
                mPreferences.put("system_version", "");

                e.printStackTrace();
                error = e;
                return false;
            }


            publishProgress("System install complete!");
            return true;
        }

        private void copyFileConfig(File home, File systemFolder, String busyboxCmd_, String[] env)
                throws IOException, InterruptedException {
            boolean override = true;
            Process pp;
            File bashrc = new File(systemFolder, "bashrc");
            File bashrcu = new File(home, ".bashrc");
            if (!bashrcu.exists() || override) {
                pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + bashrc.getPath() + " " + bashrcu.getPath(), env, home);
                pp.waitFor();
            }

            File nanorc = new File(systemFolder, "nanorc");
            File nanorcu = new File(home, ".nanorc");
            if (!nanorcu.exists() || override) {
                pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + nanorc.getPath() + " " + nanorcu.getPath(), env, home);
                pp.waitFor();
            }

            File tmuxrc = new File(systemFolder, "tmux.conf");
            File tmuxrcu = new File(home, ".tmux.conf");
            if (!tmuxrcu.exists() || override) {
                pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + tmuxrc.getPath() + " " + tmuxrcu.getPath(), env, home);
                pp.waitFor();
            }


            File ini = new File(systemFolder, "mc.ini");
            File conf = new File(home, ".config");
            File confmc = new File(conf, "mc");
            if (!confmc.exists()) confmc.mkdirs();
            File mcini = new File(confmc, "ini");
            if (!mcini.exists() || override) {
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

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mInfo.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                showDialogSuccess();
            } else {
                showDialogFailed(error);
            }
            mInstallButton.setEnabled(true);
            mProgressBar.setIndeterminate(false);
            String version = getString(R.string.system_version) + mPreferences.getSystemVersion();
            mTxtVersion.setText(version);
        }
    }
}
