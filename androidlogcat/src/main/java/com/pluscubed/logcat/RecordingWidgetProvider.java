package com.pluscubed.logcat;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.pluscubed.logcat.helper.DialogHelper;
import com.pluscubed.logcat.helper.PreferenceHelper;
import com.pluscubed.logcat.helper.ServiceHelper;
import com.pluscubed.logcat.helper.WidgetHelper;
import com.pluscubed.logcat.ui.RecordLogDialogActivity;
import com.pluscubed.logcat.util.UtilLogger;

import java.util.Arrays;

public class RecordingWidgetProvider extends AppWidgetProvider {

    public static final String ACTION_RECORD_OR_STOP = BuildConfig.APPLICATION_ID + ".action.RECORD_OR_STOP";

    public static final String URI_SCHEME = "catlog_widget";

    private static UtilLogger log = new UtilLogger(RecordingWidgetProvider.class);

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        log.d("onUpdate() for appWidgetIds %s", Arrays.toString(appWidgetIds));
        log.d("appWidgetIds are %s", Arrays.toString(appWidgetIds));

        // track which widgets were created, since there's a bug in the android system that lets
        // stale app widget ids stick around
        PreferenceHelper.setWidgetExistsPreference(context, appWidgetIds);

        WidgetHelper.updateWidgets(context, appWidgetIds);
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        log.d("onReceive(); intent is: %s", intent);

        if (ACTION_RECORD_OR_STOP.equals(intent.getAction())) {

            // start or stop recording as necessary
            synchronized (RecordingWidgetProvider.class) {

                boolean alreadyRunning = ServiceHelper.checkIfServiceIsRunning(context, LogcatRecordingService.class);

                if (alreadyRunning) {
                    // stop the current recording process
                    DialogHelper.stopRecordingLog(context);
                } else {
                    // start a new recording process
                    Intent targetIntent = new Intent();
                    targetIntent.setClass(context, RecordLogDialogActivity.class);
                    targetIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                    context.startActivity(targetIntent);
                }
            }
        }
    }
}
