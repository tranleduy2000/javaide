package com.duy.ide.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.duy.ide.setting.AppSetting;

import java.io.File;

/**
 * Created by Duy on 22-Aug-17.
 */

public class RootUtils {
    public static boolean installApk(Context context, File apk) {
        if (!apk.exists() || !apk.isFile() || !apk.canRead()) {
            return false;
        }
        AppSetting setting = new AppSetting(context);
        if (setting.installViaRootAccess()) {
            if (installWithoutPrompt(apk)) {
                return true;
            }
        } else {
            openApk(context, apk);
        }
        return false;
    }

    private static boolean openApk(Context context, File file) {
        try {
//            Uri uri;
//            if (Build.VERSION.SDK_INT >= 24) {
//                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
//            } else {
//                uri = Uri.fromFile(file);
//            }
//
//            //create intent open file
//            MimeTypeMap myMime = MimeTypeMap.getSingleton();
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            String ext = FileUtils.fileExt(file.getPath());
//            String mimeType = myMime.getMimeTypeFromExtension(ext != null ? ext : "");
//            intent.setDataAndType(uri, mimeType);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            context.startActivity(intent);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    //https://stackoverflow.com/questions/26926274/install-android-apk-without-prompt
    private static boolean installWithoutPrompt(File apk) {
        try {
            String filename = apk.getPath();
            String command;
            command = "adb install -r " + filename;
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            proc.waitFor();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
