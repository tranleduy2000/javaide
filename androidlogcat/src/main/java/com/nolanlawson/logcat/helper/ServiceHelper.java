package com.nolanlawson.logcat.helper;

import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.nolanlawson.logcat.CrazyLoggerService;
import com.nolanlawson.logcat.LogcatRecordingService;
import com.nolanlawson.logcat.reader.LogcatReaderLoader;
import com.nolanlawson.logcat.util.UtilLogger;

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
	
	public static synchronized void stopBackgroundServiceIfRunning(Context context) {
		boolean alreadyRunning = ServiceHelper.checkIfServiceIsRunning(context, LogcatRecordingService.class);
		
		log.d("Is CatlogService running: %s", alreadyRunning);
		
		if (alreadyRunning) {
			Intent intent = new Intent(context, LogcatRecordingService.class);
			context.stopService(intent);
		}
		
	}
	
	public static synchronized void startBackgroundServiceIfNotAlreadyRunning(
			Context context, String filename, String queryFilter, String level) {
		
		boolean alreadyRunning = ServiceHelper.checkIfServiceIsRunning(context, LogcatRecordingService.class);
		
		log.d("Is CatlogService already running: %s", alreadyRunning);
		
		if (!alreadyRunning) {
			
			Intent intent = new Intent(context, LogcatRecordingService.class);
			intent.putExtra(LogcatRecordingService.EXTRA_FILENAME, filename);
			
			// load "lastLine" in the background
			LogcatReaderLoader loader = LogcatReaderLoader.create(context, true);
			intent.putExtra(LogcatRecordingService.EXTRA_LOADER, loader);
			
			// add query text and log level
			intent.putExtra(LogcatRecordingService.EXTRA_QUERY_FILTER, queryFilter);
			intent.putExtra(LogcatRecordingService.EXTRA_LEVEL, level);
			
			context.startService(intent);
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
