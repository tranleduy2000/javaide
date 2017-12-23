package com.duy.ide.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
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
import java.io.InputStream;
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
    private boolean mIsInstalling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        setupToolbar();
        setTitle(R.string.install);

        mPreferences = new AppSetting(this);
        mProgressBar = findViewById(R.id.progress_bar);
        mInfo = findViewById(R.id.txt_info);
        mInstallButton = findViewById(R.id.btn_install);

//        mTxtVersion = (TextView) findViewById(R.id.txt_version);
//        String version = getString(R.string.system_version) + mPreferences.getSystemVersion();
//        mTxtVersion.setText(version);
        findViewById(R.id.btn_install).setOnClickListener(this);
        findViewById(R.id.btn_select_file).setOnClickListener(this);
        findViewById(R.id.down_load_from_github).setOnClickListener(this);

        extractFileFromAsset();
    }


    @Override
    public void onClick(View v) {
       /* if (v.getId() == R.id.btn_install) {
            if (isConnected()) {
                downloadFile();
            } else {
                showDialogNotConnect();
            }
        } else if (v.getId() == R.id.btn_select_file) {
            selectFile();
        } else if (v.getId() == R.id.down_load_from_github) {
            downloadFromGit();
        }*/
    }

    private void extractFileFromAsset() {
        new CopyFromAssetTask(this).execute();
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
        if (!mIsInstalling) {
            super.onBackPressed();
        }
    }

    private void showDialogDownload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Download system");
    }

    private void installFailed() {
        Toast.makeText(this, "Install failed", Toast.LENGTH_SHORT).show();
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
            mIsInstalling = true;
        }

        @Override
        protected Boolean doInBackground(File... params) {
            try {
                File zip = params[0];
                File outputDir = FileManager.getSdkDir(context);
                unzipArchive(zip, outputDir);
                zip.delete();

                if (!(FileManager.isSdkInstalled(context))) {
                    throw new RuntimeException("Install failed, Not a classes.zip file");
                }
            } catch (Exception e) {
                publishProgress("Error when install system");

                e.printStackTrace();
                error = e;
                return false;
            }


            publishProgress("System install complete!");
            return true;
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
            mIsInstalling = false;
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

    private class CopyFromAssetTask extends AsyncTask<File, String, File> {
        private Context context;

        public CopyFromAssetTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mInfo.setText(R.string.copy_from_asset);
            mProgressBar.setIndeterminate(true);
            mInstallButton.setEnabled(false);
            mIsInstalling = true;
        }

        @Override
        protected File doInBackground(File... params) {
            try {
                AssetManager assets = context.getAssets();
                InputStream open = assets.open("android-21/android-21.zip");
                File outFile = new File(getFilesDir(), "classes.zip");
                FileOutputStream fileOutputStream = new FileOutputStream(outFile);
                FileManager.copyStream(open, fileOutputStream);
                fileOutputStream.close();
                return outFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (file != null) {
                new InstallTask(context).execute(file);
            } else {
                installFailed();
            }
        }
    }


}
