package com.nolanlawson.logcat;

import java.util.Date;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.nolanlawson.logcat.util.UtilLogger;

/**
 * just writes a bunch of logs.  to be used during debugging and testing.
 * @author nolan
 *
 */
public class CrazyLoggerService extends IntentService {
	
	private static final long INTERVAL = 300;
	
	private static UtilLogger log = new UtilLogger(CrazyLoggerService.class);
	
	private boolean kill = false;

	public CrazyLoggerService() {
		super("CrazyLoggerService");
	}

	protected void onHandleIntent(Intent intent) {
	
		log.d("onHandleIntent()");
		
		while (!kill) {
		
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				log.e(e, "error");
			}
			Date date = new Date();
			log.i("Log message " + date + " " + (date.getTime() % 1000));
		
		}
	
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		kill = true;
	}
	
}
