package com.duy.ide.autocomplete;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.duy.ide.autocomplete.autocomplete.AutoCompleteProvider;

/**
 * Created by Duy on 22-Jul-17.
 */

public class AutoCompleteService extends Service {
    private final IBinder mBinder = new ACBinder();
    private AutoCompleteProvider mAutoCompleteProvider;
    @Nullable
    private OnAutoCompleteServiceLoadListener callback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAutoCompleteProvider = new AutoCompleteProvider(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAutoCompleteProvider.load();
                if (callback != null) callback.onLoaded(mAutoCompleteProvider);
            }
        }).start();
    }

    public void setCallback(@Nullable OnAutoCompleteServiceLoadListener callback) {
        this.callback = callback;
        if (mAutoCompleteProvider.isLoaded()) {
            if (callback != null) callback.onLoaded(mAutoCompleteProvider);
        }
    }

    public interface OnAutoCompleteServiceLoadListener {
        void onLoaded(AutoCompleteProvider provider);
    }

    public class ACBinder extends Binder {
        public AutoCompleteService getService() {
            return AutoCompleteService.this;
        }
    }
}
