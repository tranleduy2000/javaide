package com.pluscubed.logcat;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.pluscubed.logcat.util.UtilLogger;

import java.util.Date;
import java.util.Random;

/**
 * just writes a bunch of logs.  to be used during debugging and testing.
 *
 * @author nolan
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

            if (new Random().nextInt(100) % 5 == 0) {
                log.i("email: emailme@hello.com");
                log.i("ftp: ftp://website.com:21/");
                log.i("http: https://website.com/");
            }

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
