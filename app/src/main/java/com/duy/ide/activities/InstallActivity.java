package com.duy.ide.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.duy.ide.R;
import com.duy.ide.file.FileManager;
import com.duy.ide.setting.AppSetting;
import com.duy.ide.utils.MemoryUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.jecelyin.android.file_explorer.FileExplorerActivity;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 * Created by Duy on 16-Jul-17.
 */

public class InstallActivity extends AbstractAppCompatActivity implements View.OnClickListener {
    public static final String SYSTEM_VERSION = "System v3.0";
    private static final int REQUEST_CODE_SELECT_FILE = 1101;
    private AppSetting mPreferences;
    private ProgressBar mProgressBar;
    private TextView mInfo;
    private Button mInstallButton;
    //    private TextView mTxtVersion;
    private ProgressDialog progressDialog;
    private boolean installing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        setupToolbar();
        setTitle(R.string.install);

        mPreferences = new AppSetting(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mInfo = (TextView) findViewById(R.id.txt_info);
        mInstallButton = (Button) findViewById(R.id.btn_install);

//        mTxtVersion = (TextView) findViewById(R.id.txt_version);
//        String version = getString(R.string.system_version) + mPreferences.getSystemVersion();
//        mTxtVersion.setText(version);
        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_select_file).setOnClickListener(this);
        findViewById(R.id.down_load_from_github).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_install) {
            if (isConnected()) {
                downloadFile();
            } else {
                showDialogNotConnect();
            }
        } else if (v.getId() == R.id.btn_select_file) {
            selectFile();
        } else if (v.getId() == R.id.down_load_from_github) {
            downloadFromGit();
        }
    }

    private void downloadFromGit() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.classes_link)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No browser installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        FileExplorerActivity.startPickFileActivity(this,
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath(), REQUEST_CODE_SELECT_FILE, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_FILE:
                if (resultCode == RESULT_OK) {
                    File file = new File(FileExplorerActivity.getFile(data));
                    new InstallTask(this).execute(file);
                }
                break;
        }
    }

    private void downloadFile() {
        progressDialog = new ProgressDialog(InstallActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("Downloading system, please wait...");
        progressDialog.setProgress(0);
        progressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        String systemURL = "gs://calculator-a283d.appspot.com/java_nide/system/classes.zip";
        StorageReference systemFile = storage.getReferenceFromUrl(systemURL);

        final File file = new File(getFilesDir(), "temp");
        systemFile.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                new InstallTask(InstallActivity.this).execute(file);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showDialogError(e);
            }
        }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                long totalByteCount = taskSnapshot.getTotalByteCount();
                long bytesTransferred = taskSnapshot.getBytesTransferred();
                if (bytesTransferred != 0 && totalByteCount != 0) {
                    String msg = String.format(Locale.ENGLISH,
                            "%.2f MB / %.2f MB",
                            MemoryUtils.toMB(bytesTransferred),
                            MemoryUtils.toMB(totalByteCount));
                    progressDialog.setMessage(msg);
                }
            }
        });
    }

    private void showDialogError(Exception e) {
        if (isFinishing()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.error);
        builder.setMessage(e == null ? " " : e.getMessage());
        builder.create().show();
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void showDialogNotConnect() {

    }

    private void showDialogSuccess() {
        if (isFinishing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.success);
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
        builder.setTitle(R.string.error);
        builder.setMessage(error.getMessage());
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed() {
        if (!installing) {
            super.onBackPressed();
        }
    }

    private void showDialogDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download system");
    }

    private class InstallTask extends AsyncTask<File, String, Boolean> {
        private Exception error = null;
        private Context context;

        public InstallTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInfo.setText(R.string.start_install_system);
            mProgressBar.setIndeterminate(true);
            mInstallButton.setEnabled(false);
            installing = true;
        }

        @Override
        protected Boolean doInBackground(File... params) {
            try {
                File download = params[0];
                File outputDir = new File(context.getFilesDir(), "system" + File.separator + "classes");
                unzipArchive(download, outputDir);
                download.delete();

                File classpath = FileManager.getClasspathFile(context);
                if (!(classpath.exists() && classpath.length() > 0)) {
                    throw new RuntimeException("Install failed, Not a classes.zip file");
                }

//                File tmp = new File(home, "tmp");
//                if (!tmp.exists()) tmp.mkdirs();
//
//                File worker = new File(tmp, "WORK_" + System.currentTimeMillis());
//                if (!worker.exists()) worker.mkdirs();
//
//                File busytar = new File(worker, "busybox");
//                if (busytar.exists()) busytar.delete();
//
//                publishProgress("Extract busybox");
//                FileManager.extractAsset(context, "busybox.mp3", busytar);
//
//                String[] env = new String[2];
//                env[0] = "PATH=/sbin" + ":/vendor/bin" + ":/system/sbin" + ":/system/bin" +
//                        ":/system/xbin";
//
//                env[1] = "LD_LIBRARY_PATH=" + "/vendor/lib" + ":/vendor/lib64" + ":/system/lib" +
//                        ":/system/lib64";
//
//                busytar.setReadable(true, true);
//                busytar.setWritable(true, true);
//                busytar.setExecutable(true, false);
//
//                publishProgress("Extract system file");
//                File systar = new File(worker, "system.tar.gz");
//                org.apache.commons.io.FileUtils.copyFile(params[0], systar);
//                publishProgress("Extracted system");
//
//                Process pp;
//                publishProgress("Removing Old System");
//
//                File systemFolder = new File(home, "system");
//                FileManager.deleteFolder(systemFolder);
//
//                publishProgress("Config system file");
//                String busyboxCmd_ = busytar.getPath() + " ";
//                pp = Runtime.getRuntime().exec(busyboxCmd_ + "tar -C " + home.getPath() + " -xzf " + systar.getPath(), env, home);
//                pp.waitFor();
//
//                File bindir = new File(systemFolder, "bin");
//                File bbindir = new File(bindir, "bbdir");
//                if (!bbindir.exists()) bbindir.mkdirs();
//
//                File busybox = new File(bindir, "busybox");
//                String command = busybox.getPath() + " --install -s " + bbindir.getPath();
//                pp = Runtime.getRuntime().exec(command, env, home);
//                pp.waitFor();
//
//                //Now delete the SU link.. too much confusion..
//                File su = new File(bbindir, "su");
//                su.delete();
//
//                publishProgress("Copy config file");
//                copyFileConfig(home, systemFolder, busyboxCmd_, env);
//
//                publishProgress("Create local file");
//                createLocalFile(home);
//
//                publishProgress("Cleaning up...");
//                FileManager.deleteFolder(worker);
//                FileManager.deleteFolder(params[0]);
//
//                mPreferences.put("system_installed", true);
//                mPreferences.put("system_version", SYSTEM_VERSION);

            } catch (Exception e) {
                publishProgress("Error when install system");

//                mPreferences.put("system_installed", false);
//                mPreferences.put("system_version", "");

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

            //config vim
            File vimrc = new File(systemFolder, "vimrc");
            File vimrcu = new File(home, ".vimrc");
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -f " + vimrc.getPath() + " " + vimrcu.getPath(), env, home);
            pp.waitFor();

            //Check the home vim folder
            File vimh = new File(systemFolder, "etc/default_vim");
            File vimhu = new File(home, ".vim");
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "cp -rf " + vimh.getPath() + " " + vimhu.getPath(), env, home);
            pp.waitFor();

            //Create a link to the sdcard
            File sdcard = Environment.getExternalStorageDirectory();
            File lnsdcard = new File(home, "sdcard");
            pp = Runtime.getRuntime().exec(busyboxCmd_ + "ln -s " + sdcard.getPath() + " " + lnsdcard.getPath(), env, home);
            pp.waitFor();

        }

        /**
         * create local dir
         *
         * @param home
         */
        private void createLocalFile(File home) {
            File local = new File(home, "local");
            if (!local.exists()) local.mkdirs();

            File bin = new File(local, "bin");
            if (!bin.exists()) bin.mkdirs();

            bin = new File(local, "lib");
            if (!bin.exists()) bin.mkdirs();

            bin = new File(local, "include");
            if (!bin.exists()) bin.mkdirs();

            bin = new File(home, "tmp");
            if (!bin.exists()) bin.mkdirs();

            bin = new File(home, "projects");
            if (!bin.exists()) bin.mkdirs();
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
            installing = false;
        }

        public void unzipArchive(File archive, File outputDir) {
            try {
                ZipFile zipfile = new ZipFile(archive);
                for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = (ZipEntry) e.nextElement();
                    unzipEntry(zipfile, entry, outputDir);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

            if (entry.isDirectory()) {
                createDir(new File(outputDir, entry.getName()));
                return;
            }

            File outputFile = new File(outputDir, entry.getName());
            if (!outputFile.getParentFile().exists()) {
                createDir(outputFile.getParentFile());
            }

            BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            try {
                IOUtils.copy(inputStream, outputStream);
            } finally {
                outputStream.close();
                inputStream.close();
            }
        }

        private void createDir(File dir) {
            if (!dir.mkdirs()) throw new RuntimeException("Can not create dir " + dir);
        }

    }


}
