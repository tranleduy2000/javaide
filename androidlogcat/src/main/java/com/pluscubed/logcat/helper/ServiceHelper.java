package com.pluscubed.logcat.helper;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.pluscubed.logcat.CrazyLoggerService;
import com.pluscubed.logcat.util.UtilLogger;

import java.util.List;

public class ServiceHelper {

    private static UtilLogger log = new UtilLogger(ServiceHelper.class);

    public static void startOrStopCrazyLogger(Context context) {

        boolean alreadyRunning = checkIfServiceIsRunning(context, CrazyLoggerService.class);
        Intent intent = new Intent(context, CrazyLoggerService.class);

        if (!alreadyRunning) {
            context.startService(intent);
        } else {
            context.stopService(intent);
        }

    }


    public static boolean checkIfServiceIsRunning(Context context, Class<?> service) {

        String serviceName = service.getName();

        ComponentName componentName = new ComponentName(context.getPackageName(), serviceName);

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> procList = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (procList != null) {

            for (ActivityManager.RunningServiceInfo appProcInfo : procList) {
                if (appProcInfo != null && componentName.equals(appProcInfo.service)) {
                    log.d("%s is already running", serviceName);
                    return true;
                }
            }
        }
        log.d("%s is not running", serviceName);
        return false;
    }
}
