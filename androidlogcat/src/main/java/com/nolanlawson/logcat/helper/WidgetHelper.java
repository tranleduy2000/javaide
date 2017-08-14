package com.nolanlawson.logcat.helper;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.nolanlawson.logcat.LogcatRecordingService;
import com.nolanlawson.logcat.R;
import com.nolanlawson.logcat.RecordingWidgetProvider;
import com.nolanlawson.logcat.util.UtilLogger;

public class WidgetHelper {
	
	private static UtilLogger log = new UtilLogger(WidgetHelper.class); 
	
	public static void updateWidgets(Context context) {

		int[] appWidgetIds = findAppWidgetIds(context);
		
		updateWidgets(context, appWidgetIds);
		
	}
	
	
	/**
	 * manually tell us if the service is running or not
	 * @param context
	 * @param serviceRunning
	 */
	public static void updateWidgets(Context context, boolean serviceRunning) {

		int[] appWidgetIds = findAppWidgetIds(context);
		
		updateWidgets(context, appWidgetIds, serviceRunning);
		
	}
	
	public static void updateWidgets(Context context, int[] appWidgetIds) {
		
		boolean serviceRunning = ServiceHelper.checkIfServiceIsRunning(context, LogcatRecordingService.class);
		
		updateWidgets(context, appWidgetIds, serviceRunning);
		
	}
	
	
	public static void updateWidgets(Context context, int[] appWidgetIds, boolean serviceRunning) {
		
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		
		for (int appWidgetId : appWidgetIds) {
			
			if (!PreferenceHelper.getWidgetExistsPreference(context, appWidgetId)) {
				// android has a bug that sometimes keeps stale app widget ids around
				log.d("Found stale app widget id %d; skipping...", appWidgetId);
				continue;
			}
			
			updateWidget(context, manager, appWidgetId, serviceRunning);
			
		}
		
	}
	private static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId, boolean serviceRunning) {
		

		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.recording_widget);
		
		// change the subtext depending on whether the service is running or not
		CharSequence subtext = context.getText(
				serviceRunning ? R.string.widget_recording_in_progress : R.string.widget_start_recording);
		updateViews.setTextViewText(R.id.widget_subtext, subtext);
		
		// if service not running, don't show the "recording" icon
		updateViews.setViewVisibility(R.id.record_badge_image_view, serviceRunning ? View.VISIBLE : View.INVISIBLE);
		
		PendingIntent pendingIntent = getPendingIntent(context, appWidgetId);
		
		updateViews.setOnClickPendingIntent(R.id.clickable_linear_layout, pendingIntent);
		
		manager.updateAppWidget(appWidgetId, updateViews);
		
	}
	
	private static PendingIntent getPendingIntent(Context context, int appWidgetId) {

		Intent intent = new Intent();
		intent.setAction(RecordingWidgetProvider.ACTION_RECORD_OR_STOP);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		// gotta make this unique for this appwidgetid - otherwise, the PendingIntents conflict
		// it seems to be a quasi-bug in Android
		Uri data = Uri.withAppendedPath(Uri.parse(RecordingWidgetProvider.URI_SCHEME + "://widget/id/#"), String.valueOf(appWidgetId));
        intent.setData(data);
        
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0 /* no requestCode */, intent, PendingIntent.FLAG_ONE_SHOT);

		return pendingIntent;
	}
	
	private static int[] findAppWidgetIds(Context context) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName widget = new ComponentName(context, RecordingWidgetProvider.class);
		int[] appWidgetIds = manager.getAppWidgetIds(widget);
		return appWidgetIds;
		
	}
	
}
