package com.duy.ide.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

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

    private static void openApk(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkUri);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } else {
            Uri apkUri = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
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
